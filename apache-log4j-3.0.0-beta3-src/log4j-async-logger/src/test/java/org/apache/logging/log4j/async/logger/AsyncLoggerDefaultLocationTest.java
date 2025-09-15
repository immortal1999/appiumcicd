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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.apache.logging.log4j.core.test.junit.LoggerContextSource;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("async")
public class AsyncLoggerDefaultLocationTest {
    @Test
    @LoggerContextSource
    public void testAsyncLogWritesToLog(final LoggerContext context) throws Exception {
        final ListAppender app = context.getConfiguration().getAppender("List");
        assertNotNull(app);
        final Logger log = context.getLogger("com.foo.Bar");
        final String msg = "Async logger msg with no location by default";
        log.info(msg);
        context.stop();
        assertEquals(1, app.getEvents().size());
        final LogEvent event = app.getEvents().get(0);
        assertFalse(event.isIncludeLocation(), "includeLocation should be false");
        assertNull(event.getSource(), "Location data should not be present");
    }
}
