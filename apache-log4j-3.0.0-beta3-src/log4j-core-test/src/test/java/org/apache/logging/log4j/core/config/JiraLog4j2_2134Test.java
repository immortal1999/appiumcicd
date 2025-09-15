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
package org.apache.logging.log4j.core.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.Test;

public class JiraLog4j2_2134Test {

    @Test
    public void testRefresh() {
        final Logger log = LogManager.getLogger(this.getClass());
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final PatternLayout layout = PatternLayout.newBuilder()
                // @formatter:off
                .setPattern(PatternLayout.SIMPLE_CONVERSION_PATTERN)
                .setConfiguration(config)
                .build();
        // @formatter:on
        final Appender appender = FileAppender.newBuilder()
                .setFileName("target/test.log")
                .setLayout(layout)
                .setConfiguration(config)
                .setBufferSize(4000)
                .setName("File")
                .build();
        // appender.start();
        config.addAppender(appender);
        final AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        final AppenderRef[] refs = new AppenderRef[] {ref};
        final LoggerConfig loggerConfig = LoggerConfig.newBuilder()
                .setAdditivity(false)
                .setLevel(Level.INFO)
                .setLoggerName("testlog4j2refresh")
                .setIncludeLocation(true)
                .setRefs(refs)
                .setConfig(config)
                .build();
        loggerConfig.addAppender(appender, null, null);
        config.addLogger("testlog4j2refresh", loggerConfig);
        ctx.stop();
        ctx.start(config);

        assertDoesNotThrow(() -> log.error("Info message"));
    }

    @Test
    public void testRefreshMinimalCodeStart() {
        final Logger log = LogManager.getLogger(this.getClass());
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        ctx.start(config);

        assertDoesNotThrow(() -> log.error("Info message"));
    }

    @Test
    public void testRefreshMinimalCodeStopStart() {
        final Logger log = LogManager.getLogger(this.getClass());
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.stop();
        ctx.start();

        assertDoesNotThrow(() -> log.error("Info message"));
    }

    @Test
    public void testRefreshMinimalCodeStopStartConfig() {
        final Logger log = LogManager.getLogger(this.getClass());
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        ctx.stop();
        ctx.start(config);

        assertDoesNotThrow(() -> log.error("Info message"));
    }

    @Test
    public void testRefreshDeprecatedApis() {
        final Logger log = LogManager.getLogger(this.getClass());
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final PatternLayout layout = PatternLayout.newBuilder()
                .setPattern(PatternLayout.SIMPLE_CONVERSION_PATTERN)
                .setPatternSelector(null)
                .setConfiguration(config)
                .setRegexReplacement(null)
                .setCharset(null)
                .setAlwaysWriteExceptions(false)
                .setNoConsoleNoAnsi(false)
                .setHeader(null)
                .setFooter(null)
                .build();
        // @formatter:off
        final Appender appender = FileAppender.newBuilder()
                .setFileName("target/test.log")
                .setAppend(false)
                .setLocking(false)
                .setName("File")
                .setImmediateFlush(true)
                .setIgnoreExceptions(false)
                .setBufferedIo(false)
                .setBufferSize(4000)
                .setLayout(layout)
                .setAdvertise(false)
                .setConfiguration(config)
                .build();
        // @formatter:on
        appender.start();
        config.addAppender(appender);
        final AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        final AppenderRef[] refs = new AppenderRef[] {ref};
        final LoggerConfig loggerConfig = LoggerConfig.newBuilder()
                .setAdditivity(false)
                .setLevel(Level.INFO)
                .setLoggerName("testlog4j2refresh")
                .setIncludeLocation(true)
                .setRefs(refs)
                .setConfig(config)
                .build();
        loggerConfig.addAppender(appender, null, null);
        config.addLogger("testlog4j2refresh", loggerConfig);
        ctx.stop();
        ctx.start(config);

        assertDoesNotThrow(() -> log.error("Info message"));
    }
}
