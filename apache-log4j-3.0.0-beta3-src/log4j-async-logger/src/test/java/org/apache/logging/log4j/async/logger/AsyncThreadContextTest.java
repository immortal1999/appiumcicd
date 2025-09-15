/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.logging.log4j.async.logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.ThreadContextTestAccess;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.core.selector.ClassLoaderContextSelector;
import org.apache.logging.log4j.core.selector.ContextSelector;
import org.apache.logging.log4j.core.test.CoreLoggerContexts;
import org.apache.logging.log4j.core.test.TestConstants;
import org.apache.logging.log4j.plugins.di.DI;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.test.TestProperties;
import org.apache.logging.log4j.test.junit.InitializesThreadContext;
import org.apache.logging.log4j.test.junit.SetTestProperty;
import org.apache.logging.log4j.test.junit.TempLoggingDir;
import org.apache.logging.log4j.util.Unbox;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Tag("sleepy")
@SetTestProperty(key = TestConstants.ASYNC_LOGGER_RING_BUFFER_SIZE, value = "128") // minimum ringbuffer size
@SetTestProperty(key = TestConstants.ASYNC_LOGGER_CONFIG_RING_BUFFER_SIZE, value = "128") // minimum ringbuffer size
@InitializesThreadContext
public class AsyncThreadContextTest {

    private static final int LINE_COUNT = 130;

    private static TestProperties props;

    @TempLoggingDir
    private static Path loggingPath;

    enum Mode {
        ALL_ASYNC(AsyncLoggerContextSelector.class),
        MIXED(ClassLoaderContextSelector.class),
        BOTH_ALL_ASYNC_AND_MIXED(AsyncLoggerContextSelector.class);

        final Class<? extends ContextSelector> contextSelectorType;
        final URI configUri;

        Mode(final Class<? extends ContextSelector> contextSelectorType) {
            this.contextSelectorType = contextSelectorType;
            try {
                configUri = AsyncThreadContextTest.class
                        .getResource(AsyncThreadContextTest.class.getSimpleName() + "/" + name() + ".xml")
                        .toURI();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    enum ContextImpl {
        WEBAPP("WebApp", "org.apache.logging.log4j.spi.DefaultThreadContextMap"),
        GARBAGE_FREE(
                "GarbageFree", "org.apache.logging.log4j.core.context.internal.GarbageFreeSortedArrayThreadContextMap");

        private final String threadContextMap;
        private final String implClass;

        ContextImpl(final String threadContextMap, final String implClass) {
            this.threadContextMap = threadContextMap;
            this.implClass = implClass;
        }

        void init() {
            props.setProperty("log4j.threadContext.map.type", threadContextMap);
            ThreadContextTestAccess.init();
        }

        public String getImplClassSimpleName() {
            return StringUtils.substringAfterLast(implClass, '.');
        }

        public String getImplClass() {
            return implClass;
        }
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
        "WEBAPP, MIXED",
        "WEBAPP, ALL_ASYNC",
        "WEBAPP, BOTH_ALL_ASYNC_AND_MIXED",
        "GARBAGE_FREE, MIXED",
        "GARBAGE_FREE, ALL_ASYNC",
        "GARBAGE_FREE, BOTH_ALL_ASYNC_AND_MIXED",
    })
    public void testAsyncLogWritesToLog(final ContextImpl contextImpl, final Mode asyncMode) throws Exception {
        doTestAsyncLogWritesToLog(contextImpl, asyncMode, getClass(), loggingPath, props);
    }

    static void doTestAsyncLogWritesToLog(
            final ContextImpl contextImpl,
            final Mode asyncMode,
            final Class<?> testClass,
            final Path loggingPath,
            final TestProperties props)
            throws Exception {
        final Path testLoggingPath = loggingPath.resolve(contextImpl.toString()).resolve(asyncMode.toString());
        props.setProperty("logging.path", testLoggingPath.toString());
        final Log4jContextFactory factory = DI.builder()
                .addInitialBindingFrom(ContextSelector.KEY)
                .toFunction(instanceFactory -> instanceFactory.getFactory(asyncMode.contextSelectorType))
                .build()
                .getInstance(Log4jContextFactory.class);
        final String fqcn = testClass.getName();
        final ClassLoader classLoader = testClass.getClassLoader();
        final String name = contextImpl.toString() + ' ' + asyncMode;
        contextImpl.init();
        final LoggerContext context = factory.getContext(fqcn, classLoader, null, false, asyncMode.configUri, name);
        runTest(context, contextImpl, asyncMode, testLoggingPath);
    }

    private static LongSupplier remainingCapacity(final LoggerContext loggerContext, final LoggerConfig loggerConfig) {
        final LongSupplier contextSupplier = loggerContext instanceof final AsyncLoggerContext asyncLoggerContext
                ? asyncLoggerContext.getAsyncLoggerDisruptor().getRingBuffer()::remainingCapacity
                : null;
        if (loggerConfig instanceof final AsyncLoggerConfig asyncLoggerConfig) {
            final LongSupplier configSupplier =
                    asyncLoggerConfig.getAsyncLoggerConfigDisruptor().getRingBuffer()::remainingCapacity;
            return contextSupplier == null
                    ? configSupplier
                    : () -> Math.min(contextSupplier.getAsLong(), configSupplier.getAsLong());
        }
        return contextSupplier != null ? contextSupplier : () -> Long.MAX_VALUE;
    }

    private static void runTest(
            final LoggerContext context, final ContextImpl contextImpl, final Mode asyncMode, final Path loggingPath)
            throws Exception {
        final Path[] files = new Path[] {
            loggingPath.resolve("AsyncLoggerTest.log"),
            loggingPath.resolve("SynchronousContextTest.log"),
            loggingPath.resolve("AsyncLoggerAndAsyncAppenderTest.log"),
            loggingPath.resolve("AsyncAppenderContextTest.log"),
        };
        ThreadContext.push("stackvalue");
        ThreadContext.put("KEY", "mapvalue");

        final Logger log = context.getLogger("com.foo.Bar");
        final LoggerConfig loggerConfig = log.get();
        final String loggerContextName = context.getClass().getSimpleName();
        final LongSupplier remainingCapacity = remainingCapacity(context, loggerConfig);

        for (int i = 0; i < LINE_COUNT; i++) {
            // buffer may be full
            if (i >= 128) {
                waitAtMost(500, TimeUnit.MILLISECONDS)
                        .pollDelay(10, TimeUnit.MILLISECONDS)
                        .until(() -> remainingCapacity.getAsLong() > 0);
            }
            if ((i & 1) == 1) {
                ThreadContext.put("count", String.valueOf(i));
            } else {
                ThreadContext.remove("count");
            }
            log.info("{} {} {} i={}", contextImpl, contextMap(), loggerContextName, Unbox.box(i));
        }
        ThreadContext.pop();
        context.stop();
        CoreLoggerContexts.stopLoggerContext(files[0].toFile()); // stop async thread

        checkResult(files[0], loggerContextName, contextImpl);
        if (asyncMode == Mode.MIXED || asyncMode == Mode.BOTH_ALL_ASYNC_AND_MIXED) {
            for (int i = 1; i < files.length; i++) {
                checkResult(files[i], loggerContextName, contextImpl);
            }
        }
    }

    private static String contextMap() {
        final ReadOnlyThreadContextMap impl = ThreadContext.getThreadContextMap();
        return impl == null
                ? ContextImpl.WEBAPP.getImplClassSimpleName()
                : impl.getClass().getSimpleName();
    }

    private static void checkResult(final Path file, final String loggerContextName, final ContextImpl contextImpl)
            throws IOException {
        final String contextDesc = contextImpl + " " + contextImpl.getImplClassSimpleName() + " " + loggerContextName;
        try (final BufferedReader reader = Files.newBufferedReader(file)) {
            String expect;
            for (int i = 0; i < LINE_COUNT; i++) {
                final String line = reader.readLine();
                if ((i & 1) == 1) {
                    expect = "INFO c.f.Bar mapvalue [stackvalue] {KEY=mapvalue, configProp=configValue,"
                            + " configProp2=configValue2, count="
                            + i + "} " + contextDesc + " i=" + i;
                } else {
                    expect = "INFO c.f.Bar mapvalue [stackvalue] {KEY=mapvalue, configProp=configValue,"
                            + " configProp2=configValue2} "
                            + contextDesc + " i=" + i;
                }
                assertThat(line).as("Log file '%s'", file.getFileName()).isEqualTo(expect);
            }
            assertThat(reader.readLine()).as("Last line").isNull();
        }
    }
}
