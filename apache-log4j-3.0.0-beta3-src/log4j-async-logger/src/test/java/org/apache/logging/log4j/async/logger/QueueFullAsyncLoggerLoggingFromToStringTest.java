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

import static org.apache.logging.log4j.core.test.TestConstants.ASYNC_LOGGER_RING_BUFFER_SIZE;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.test.async.BlockingAppender;
import org.apache.logging.log4j.core.test.junit.ContextSelectorType;
import org.apache.logging.log4j.core.test.junit.LoggerContextSource;
import org.apache.logging.log4j.core.test.junit.Named;
import org.apache.logging.log4j.test.junit.SetTestProperty;
import org.junit.jupiter.api.Test;

/**
 * Tests queue full scenarios with pure AsyncLoggers (all loggers async).
 */
@ContextSelectorType(AsyncLoggerContextSelector.class)
@SetTestProperty(key = ASYNC_LOGGER_RING_BUFFER_SIZE, value = "128")
public class QueueFullAsyncLoggerLoggingFromToStringTest extends QueueFullAsyncAbstractTest {

    @Override
    @Test
    @LoggerContextSource
    public void testLoggingFromToStringCausesOutOfOrderMessages(
            final LoggerContext ctx, final @Named(APPENDER_NAME) BlockingAppender blockingAppender) throws Exception {
        super.testLoggingFromToStringCausesOutOfOrderMessages(ctx, blockingAppender);
    }

    @Override
    protected void checkConfig(final LoggerContext ctx) {
        assertAsyncLogger(ctx, 128);
    }
}
