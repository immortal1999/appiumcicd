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
package org.apache.logging.log4j.layout.template.json;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout.EventTemplateAdditionalField;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class JsonTemplateLayoutBenchmarkState {

    private static final Configuration CONFIGURATION = new DefaultConfiguration();

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final int LOG_EVENT_COUNT = 1_000;

    private final ByteBufferDestination byteBufferDestination;

    private final Layout jtl4EcsLayout;

    private final List<LogEvent> fullLogEvents;

    private final List<LogEvent> liteLogEvents;

    private int logEventIndex = 0;

    public JsonTemplateLayoutBenchmarkState() {
        this.byteBufferDestination = new BlackHoleByteBufferDestination(1024 * 512);
        this.jtl4EcsLayout = createJtl4EcsLayout();
        this.fullLogEvents = LogEventFixture.createFullLogEvents(LOG_EVENT_COUNT);
        this.liteLogEvents = LogEventFixture.createLiteLogEvents(LOG_EVENT_COUNT);
    }

    private static JsonTemplateLayout createJtl4EcsLayout() {
        final EventTemplateAdditionalField[] additionalFields = new EventTemplateAdditionalField[] {
            EventTemplateAdditionalField.newBuilder()
                    .setKey("service.name")
                    .setValue("benchmark")
                    .build()
        };
        return JsonTemplateLayout.newBuilder()
                .setConfiguration(CONFIGURATION)
                .setCharset(CHARSET)
                .setEventTemplateUri("classpath:EcsLayout.json")
                .setEventTemplateAdditionalFields(additionalFields)
                .build();
    }

    ByteBufferDestination getByteBufferDestination() {
        return byteBufferDestination;
    }

    Layout getJtl4EcsLayout() {
        return jtl4EcsLayout;
    }

    List<LogEvent> getFullLogEvents() {
        return fullLogEvents;
    }

    List<LogEvent> getLiteLogEvents() {
        return liteLogEvents;
    }

    int nextLogEventIndex() {
        final int currentLogEventIndex = logEventIndex;
        logEventIndex = (logEventIndex + 1) % LOG_EVENT_COUNT;
        return currentLogEventIndex;
    }
}
