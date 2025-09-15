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
package org.apache.logging.log4j.core.layout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationProcessor;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.net.Facility;
import org.apache.logging.log4j.core.test.BasicConfigurationFactory;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.apache.logging.log4j.core.test.junit.ConfigurationFactoryType;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.util.ProcessIdUtil;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.StructuredDataCollectionMessage;
import org.apache.logging.log4j.message.StructuredDataMessage;
import org.apache.logging.log4j.plugins.Node;
import org.apache.logging.log4j.plugins.di.ConfigurableInstanceFactory;
import org.apache.logging.log4j.plugins.di.DI;
import org.apache.logging.log4j.plugins.model.PluginNamespace;
import org.apache.logging.log4j.plugins.model.PluginType;
import org.apache.logging.log4j.test.junit.UsingAnyThreadContext;
import org.apache.logging.log4j.util.Lazy;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@UsingAnyThreadContext
@ConfigurationFactoryType(BasicConfigurationFactory.class)
class Rfc5424LayoutTest {
    private final LoggerContext ctx = LoggerContext.getContext();
    private final Configuration CONFIGURATION = ctx.getConfiguration();
    private final Logger root = ctx.getRootLogger();

    private static final String PROCESSID = ProcessIdUtil.getProcessId();
    private static final String line1 =
            String.format("ATM %s - [RequestContext@3692 loginId=\"JohnDoe\"] starting mdc pattern test", PROCESSID);
    private static final String line2 =
            String.format("ATM %s - [RequestContext@3692 loginId=\"JohnDoe\"] empty mdc", PROCESSID);
    private static final String line3 =
            String.format("ATM %s - [RequestContext@3692 loginId=\"JohnDoe\"] filled mdc", PROCESSID);
    private static final String line4 = String.format(
            "ATM %s Audit [Transfer@18060 Amount=\"200.00\" FromAccount=\"123457\" ToAccount=\"123456\"]"
                    + "[RequestContext@3692 ipAddress=\"192.168.0.120\" loginId=\"JohnDoe\"] Transfer Complete",
            PROCESSID);
    private static final String lineEscaped3 = String.format(
            "ATM %s - [RequestContext@3692 escaped=\"Testing escaping #012 \\\" \\] \\\"\" loginId=\"JohnDoe\"] filled mdc",
            PROCESSID);
    private static final String lineEscaped4 = String.format(
            "ATM %s Audit [Transfer@18060 Amount=\"200.00\" FromAccount=\"123457\" ToAccount=\"123456\"]"
                    + "[RequestContext@3692 escaped=\"Testing escaping #012 \\\" \\] \\\"\" ipAddress=\"192.168.0.120\" loginId=\"JohnDoe\"] Transfer Complete",
            PROCESSID);
    private static final String collectionLine1 =
            "[Transfer@18060 Amount=\"200.00\" FromAccount=\"123457\" " + "ToAccount=\"123456\"]";
    private static final String collectionLine2 = "[Extra@18060 Item1=\"Hello\" Item2=\"World\"]";
    private static final String collectionLine3 =
            "[RequestContext@3692 ipAddress=\"192.168.0.120\" loginId=\"JohnDoe\"]";
    private static final String collectionEndOfLine = "Transfer Complete";

    /**
     * Test case for MDC conversion pattern.
     */
    @Test
    void testLayout() {
        for (final Appender appender : root.getAppenders().values()) {
            root.removeAppender(appender);
        }
        // set up appender
        final AbstractStringLayout layout = Rfc5424Layout.newBuilder()
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("3692")
                .setIncludeMDC(true)
                .setMdcId("RequestContext")
                .setIncludeNL(true)
                .setAppName("ATM")
                .setExcludes("key1, key2, locale")
                .setRequired("loginId")
                .setUseTLSMessageFormat(true)
                .setConfiguration(CONFIGURATION)
                .build();
        final ListAppender appender = new ListAppender("List", null, layout, true, false);

        appender.start();

        // set appender on root and set level to debug
        root.addAppender(appender);
        root.setLevel(Level.DEBUG);

        ThreadContext.put("loginId", "JohnDoe");

        // output starting message
        root.debug("starting mdc pattern test");

        root.debug("empty mdc");

        ThreadContext.put("key1", "value1");
        ThreadContext.put("key2", "value2");

        root.debug("filled mdc");

        ThreadContext.put("ipAddress", "192.168.0.120");
        ThreadContext.put("locale", Locale.US.getDisplayName());
        try {
            final StructuredDataMessage msg = new StructuredDataMessage("Transfer@18060", "Transfer Complete", "Audit");
            msg.put("ToAccount", "123456");
            msg.put("FromAccount", "123457");
            msg.put("Amount", "200.00");
            root.info(MarkerManager.getMarker("EVENT"), msg);

            List<String> list = appender.getMessages();

            assertTrue(list.get(0).endsWith(line1), "Expected line 1 to end with: " + line1 + " Actual " + list.get(0));
            assertTrue(list.get(1).endsWith(line2), "Expected line 2 to end with: " + line2 + " Actual " + list.get(1));
            assertTrue(list.get(2).endsWith(line3), "Expected line 3 to end with: " + line3 + " Actual " + list.get(2));
            assertTrue(list.get(3).endsWith(line4), "Expected line 4 to end with: " + line4 + " Actual " + list.get(3));

            for (final String frame : list) {
                int length;
                final int frameLength = frame.length();
                final int firstSpacePosition = frame.indexOf(' ');
                final String messageLength = frame.substring(0, firstSpacePosition);
                try {
                    length = Integers.parseInt(messageLength);
                    // the ListAppender removes the ending newline, so we expect one less size
                    assertEquals(frameLength, messageLength.length() + length);
                } catch (final NumberFormatException e) {
                    fail("Not a valid RFC 5425 frame");
                }
            }

            appender.clear();

            ThreadContext.remove("loginId");

            root.debug("This is a test");

            list = appender.getMessages();
            assertTrue(list.isEmpty(), "No messages expected, found " + list.size());
        } finally {
            root.removeAppender(appender);
            appender.stop();
        }
    }

    /**
     * Test case for MDC conversion pattern.
     */
    @Test
    void testCollection() {
        for (final Appender appender : root.getAppenders().values()) {
            root.removeAppender(appender);
        }
        // set up appender
        final AbstractStringLayout layout = Rfc5424Layout.newBuilder()
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("3692")
                .setIncludeMDC(true)
                .setMdcId("RequestContext")
                .setIncludeNL(true)
                .setAppName("ATM")
                .setExcludes("key1, key2, locale")
                .setRequired("loginId")
                .setUseTLSMessageFormat(true)
                .setConfiguration(CONFIGURATION)
                .build();
        final ListAppender appender = new ListAppender("List", null, layout, true, false);

        appender.start();

        // set appender on root and set level to debug
        root.addAppender(appender);
        root.setLevel(Level.DEBUG);

        ThreadContext.put("loginId", "JohnDoe");
        ThreadContext.put("ipAddress", "192.168.0.120");
        ThreadContext.put("locale", Locale.US.getDisplayName());
        try {
            final StructuredDataMessage msg = new StructuredDataMessage("Transfer@18060", "Transfer Complete", "Audit");
            msg.put("ToAccount", "123456");
            msg.put("FromAccount", "123457");
            msg.put("Amount", "200.00");
            final StructuredDataMessage msg2 = new StructuredDataMessage("Extra@18060", null, "Audit");
            msg2.put("Item1", "Hello");
            msg2.put("Item2", "World");
            final List<StructuredDataMessage> messages = new ArrayList<>();
            messages.add(msg);
            messages.add(msg2);
            final StructuredDataCollectionMessage collectionMessage = new StructuredDataCollectionMessage(messages);

            root.info(MarkerManager.getMarker("EVENT"), collectionMessage);

            final List<String> list = appender.getMessages();
            final String result = list.get(0);
            assertTrue(
                    result.contains(collectionLine1),
                    "Expected line to contain " + collectionLine1 + ", Actual " + result);
            assertTrue(
                    result.contains(collectionLine2),
                    "Expected line to contain " + collectionLine2 + ", Actual " + result);
            assertTrue(
                    result.contains(collectionLine3),
                    "Expected line to contain " + collectionLine3 + ", Actual " + result);
            assertTrue(
                    result.endsWith(collectionEndOfLine),
                    "Expected line to end with: " + collectionEndOfLine + " Actual " + result);

            for (final String frame : list) {
                int length;
                final int frameLength = frame.length();
                final int firstSpacePosition = frame.indexOf(' ');
                final String messageLength = frame.substring(0, firstSpacePosition);
                try {
                    length = Integers.parseInt(messageLength);
                    // the ListAppender removes the ending newline, so we expect one less size
                    assertEquals(frameLength, messageLength.length() + length);
                } catch (final NumberFormatException e) {
                    fail("Not a valid RFC 5425 frame");
                }
            }

            appender.clear();
        } finally {
            root.removeAppender(appender);
            appender.stop();
        }
    }

    /**
     * Test case for escaping newlines and other SD PARAM-NAME special characters.
     */
    @Test
    void testEscape() {
        for (final Appender appender : root.getAppenders().values()) {
            root.removeAppender(appender);
        }
        // set up layout/appender
        final AbstractStringLayout layout = Rfc5424Layout.newBuilder()
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("3692")
                .setIncludeMDC(true)
                .setMdcId("RequestContext")
                .setIncludeNL(true)
                .setEscapeNL("#012")
                .setAppName("ATM")
                .setExcludes("key1, key2, locale")
                .setRequired("loginId")
                .setUseTLSMessageFormat(true)
                .setConfiguration(CONFIGURATION)
                .build();
        final ListAppender appender = new ListAppender("List", null, layout, true, false);

        appender.start();

        // set appender on root and set level to debug
        root.addAppender(appender);
        root.setLevel(Level.DEBUG);

        ThreadContext.put("loginId", "JohnDoe");

        // output starting message
        root.debug("starting mdc pattern test");

        root.debug("empty mdc");

        ThreadContext.put("escaped", "Testing escaping \n \" ] \"");

        root.debug("filled mdc");

        ThreadContext.put("ipAddress", "192.168.0.120");
        ThreadContext.put("locale", Locale.US.getDisplayName());
        try {
            final StructuredDataMessage msg = new StructuredDataMessage("Transfer@18060", "Transfer Complete", "Audit");
            msg.put("ToAccount", "123456");
            msg.put("FromAccount", "123457");
            msg.put("Amount", "200.00");
            root.info(MarkerManager.getMarker("EVENT"), msg);

            List<String> list = appender.getMessages();

            assertTrue(list.get(0).endsWith(line1), "Expected line 1 to end with: " + line1 + " Actual " + list.get(0));
            assertTrue(list.get(1).endsWith(line2), "Expected line 2 to end with: " + line2 + " Actual " + list.get(1));
            assertTrue(
                    list.get(2).endsWith(lineEscaped3),
                    "Expected line 3 to end with: " + lineEscaped3 + " Actual " + list.get(2));
            assertTrue(
                    list.get(3).endsWith(lineEscaped4),
                    "Expected line 4 to end with: " + lineEscaped4 + " Actual " + list.get(3));

            appender.clear();

            ThreadContext.remove("loginId");

            root.debug("This is a test");

            list = appender.getMessages();
            assertTrue(list.isEmpty(), "No messages expected, found " + list.size());
        } finally {
            root.removeAppender(appender);
            appender.stop();
        }
    }

    /**
     * Test case for MDC exception conversion pattern.
     */
    @Test
    void testException() {
        for (final Appender appender : root.getAppenders().values()) {
            root.removeAppender(appender);
        }
        // set up layout/appender
        final AbstractStringLayout layout = Rfc5424Layout.newBuilder()
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("3692")
                .setIncludeMDC(true)
                .setMdcId("RequestContext")
                .setIncludeNL(true)
                .setAppName("ATM")
                .setExcludes("key1, key2, locale")
                .setRequired("loginId")
                .setExceptionPattern("%xEx")
                .setUseTLSMessageFormat(true)
                .setConfiguration(CONFIGURATION)
                .build();
        final ListAppender appender = new ListAppender("List", null, layout, true, false);
        appender.start();

        // set appender on root and set level to debug
        root.addAppender(appender);
        root.setLevel(Level.DEBUG);

        ThreadContext.put("loginId", "JohnDoe");

        // output starting message
        root.debug("starting mdc pattern test", new IllegalArgumentException("Test"));

        try {

            final List<String> list = appender.getMessages();

            assertTrue(list.size() > 1, "Not enough list entries");
            final String string = list.get(1);
            assertTrue(string.contains("IllegalArgumentException"), "No Exception in " + string);

            appender.clear();
        } finally {
            root.removeAppender(appender);
            appender.stop();
        }
    }

    /**
     * Test case for MDC logger field inclusion.
     */
    @Test
    void testMDCLoggerFields() {
        final LoggerFields[] loggerFields = new LoggerFields[] {
            LoggerFields.createLoggerFields(
                    new KeyValuePair[] {new KeyValuePair("source", "%C.%M")}, null, null, false),
            LoggerFields.createLoggerFields(
                    new KeyValuePair[] {new KeyValuePair("source2", "%C.%M")}, null, null, false)
        };

        // set up layout/appender
        final AbstractStringLayout layout = Rfc5424Layout.newBuilder()
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("3692")
                .setIncludeMDC(true)
                .setMdcId("RequestContext")
                .setIncludeNL(true)
                .setAppName("ATM")
                .setExcludes("key1, key2, locale")
                .setUseTLSMessageFormat(true)
                .setLoggerFields(loggerFields)
                .setConfiguration(CONFIGURATION)
                .build();
        final LogEvent event = Log4jLogEvent.newBuilder()
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("starting logger fields test"))
                .setSource(new RuntimeException().getStackTrace()[0])
                .build();
        final String serializedEvent = layout.toSerializable(event);
        assertTrue(serializedEvent.contains("Rfc5424LayoutTest.testMDCLoggerFields"), "No class/method");
    }

    @Test
    void testLoggerFields() {
        final String[] expectedToContain = new String[] {
            "[BAZ@32473 baz=\"org.apache.logging.log4j.core.layout.Rfc5424LayoutTest.testLoggerFields\"]",
            "[RequestContext@3692 bar=\"org.apache.logging.log4j.core.layout.Rfc5424LayoutTest.testLoggerFields\"]",
            "[SD-ID@32473 source=\"org.apache.logging.log4j.core.layout.Rfc5424LayoutTest.testLoggerFields\"]"
        };

        final LoggerFields[] loggerFields = new LoggerFields[] {
            LoggerFields.createLoggerFields(
                    new KeyValuePair[] {new KeyValuePair("source", "%C.%M")}, "SD-ID", "32473", false),
            LoggerFields.createLoggerFields(
                    new KeyValuePair[] {new KeyValuePair("baz", "%C.%M"), new KeyValuePair("baz", "%C.%M")},
                    "BAZ",
                    "32473",
                    false),
            LoggerFields.createLoggerFields(new KeyValuePair[] {new KeyValuePair("bar", "%C.%M")}, null, null, false)
        };

        final AbstractStringLayout layout = Rfc5424Layout.newBuilder()
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("3692")
                .setIncludeMDC(true)
                .setMdcId("RequestContext")
                .setIncludeNL(true)
                .setAppName("ATM")
                .setExcludes("key1, key2, locale")
                .setUseTLSMessageFormat(false)
                .setLoggerFields(loggerFields)
                .setConfiguration(CONFIGURATION)
                .build();
        final LogEvent event = Log4jLogEvent.newBuilder()
                .setLevel(Level.INFO)
                .setMessage(new SimpleMessage("starting logger fields test"))
                .setSource(new RuntimeException().getStackTrace()[0])
                .build();
        final String serializedEvent = layout.toSerializable(event);
        assertTrue(serializedEvent.contains("Rfc5424LayoutTest.testLoggerFields"), "No class/method");
        for (final String value : expectedToContain) {
            assertTrue(serializedEvent.contains(value), "Message expected to contain " + value + " but did not");
        }
    }

    @Test
    void testDiscardEmptyLoggerFields() {
        final String mdcId = "RequestContext";

        for (final Appender appender : root.getAppenders().values()) {
            root.removeAppender(appender);
        }

        final LoggerFields[] loggerFields = new LoggerFields[] {
            LoggerFields.createLoggerFields(
                    new KeyValuePair[] {
                        new KeyValuePair("dummy", Strings.EMPTY), new KeyValuePair("empty", Strings.EMPTY)
                    },
                    "SD-ID",
                    "32473",
                    true),
            LoggerFields.createLoggerFields(
                    new KeyValuePair[] {new KeyValuePair("baz", "%C.%M"), new KeyValuePair("baz", "%C.%M")},
                    "BAZ",
                    "32473",
                    false),
            LoggerFields.createLoggerFields(new KeyValuePair[] {new KeyValuePair("bar", "%C.%M")}, null, null, false)
        };

        final AbstractStringLayout layout = Rfc5424Layout.newBuilder()
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("3692")
                .setIncludeMDC(true)
                .setMdcId(mdcId)
                .setIncludeNL(true)
                .setAppName("ATM")
                .setExcludes("key1, key2, locale")
                .setUseTLSMessageFormat(false)
                .setLoggerFields(loggerFields)
                .setConfiguration(CONFIGURATION)
                .build();
        final ListAppender appender = new ListAppender("List", null, layout, true, false);
        appender.start();

        root.addAppender(appender);
        root.setLevel(Level.DEBUG);

        root.info("starting logger fields test");

        try {

            final List<String> list = appender.getMessages();
            assertFalse(list.isEmpty(), "Not enough list entries");
            final String message = list.get(0);
            assertFalse(message.contains("SD-ID"), "SD-ID should have been discarded");
            assertTrue(message.contains("BAZ"), "BAZ should have been included");
            assertTrue(message.contains(mdcId), mdcId + "should have been included");
            appender.clear();
        } finally {
            root.removeAppender(appender);
            appender.stop();
        }
    }

    @Test
    void testSubstituteStructuredData() {
        final String mdcId = "RequestContext";

        final String expectedToContain = String.format("ATM %s MSG-ID - Message", PROCESSID);

        for (final Appender appender : root.getAppenders().values()) {
            root.removeAppender(appender);
        }

        final AbstractStringLayout layout = Rfc5424Layout.newBuilder()
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("3692")
                .setIncludeMDC(false)
                .setMdcId(mdcId)
                .setIncludeNL(true)
                .setAppName("ATM")
                .setMessageId("MSG-ID")
                .setExcludes("key1, key2, locale")
                .setUseTLSMessageFormat(false)
                .setConfiguration(CONFIGURATION)
                .build();
        final ListAppender appender = new ListAppender("List", null, layout, true, false);
        appender.start();

        root.addAppender(appender);
        root.setLevel(Level.DEBUG);

        root.info("Message");

        try {
            final List<String> list = appender.getMessages();
            assertFalse(list.isEmpty(), "Not enough list entries");
            final String message = list.get(0);
            assertTrue(message.contains(expectedToContain), "Not the expected message received");
            appender.clear();
        } finally {
            root.removeAppender(appender);
            appender.stop();
        }
    }

    @Test
    public void testParameterizedMessage() {
        for (final Appender appender : root.getAppenders().values()) {
            root.removeAppender(appender);
        }
        // set up appender
        final AbstractStringLayout layout = Rfc5424Layout.newBuilder()
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("3692")
                .setIncludeMDC(true)
                .setMdcId("RequestContext")
                .setIncludeNL(true)
                .setAppName("ATM")
                .setExcludes("key1, key2, locale")
                .setUseTLSMessageFormat(true)
                .setConfiguration(CONFIGURATION)
                .build();

        final ListAppender appender = new ListAppender("List", null, layout, true, false);
        appender.start();

        // set appender on root and set level to debug
        root.addAppender(appender);
        root.setLevel(Level.DEBUG);
        root.info("Hello {}", "World");
        try {
            final List<String> list = appender.getMessages();
            assertFalse(list.isEmpty(), "Not enough list entries");
            final String message = list.get(0);
            assertTrue(
                    message.contains("Hello World"), "Incorrect message. Expected - Hello World, Actual - " + message);
        } finally {
            root.removeAppender(appender);
            appender.stop();
        }
    }

    @Test
    void testLayoutBuilder() {
        for (final Appender appender : root.getAppenders().values()) {
            root.removeAppender(appender);
        }

        final AbstractStringLayout layout = new Rfc5424Layout.Rfc5424LayoutBuilder()
                .setConfiguration(CONFIGURATION)
                .setFacility(Facility.LOCAL0)
                .setId("Event")
                .setEin("1234.56.7")
                .setIncludeMDC(true)
                .setMdcId("RequestContext")
                .setIncludeNL(true)
                .setAppName("ATM")
                .setExcludes("key1, key2, locale")
                .setUseTLSMessageFormat(true)
                .build();

        final ListAppender appender = new ListAppender("List", null, layout, true, false);
        appender.start();

        root.addAppender(appender);
        root.setLevel(Level.DEBUG);
        root.info("Hello {}", "World");
        try {
            final List<String> list = appender.getMessages();
            assertFalse(list.isEmpty(), "Not enough list entries");
            final String message = list.get(0);
            assertTrue(
                    message.contains("Hello World"), "Incorrect message. Expected - Hello World, Actual - " + message);
        } finally {
            root.removeAppender(appender);
            appender.stop();
        }
    }

    @Test
    public void testLayoutBuilderDefaultValues() {
        final Configuration configuration = ctx.getConfiguration();
        final Rfc5424Layout layout = new Rfc5424Layout.Rfc5424LayoutBuilder()
                .setConfiguration(configuration)
                .build();
        checkDefaultValues(layout);

        final PluginNamespace corePlugins = ctx.getInstanceFactory().getInstance(Core.PLUGIN_NAMESPACE_KEY);
        final PluginType<?> pluginType = corePlugins.get("Rfc5424Layout");
        assertNotNull(pluginType);
        final Node node = new Node(null, "Rfc5424Layout", pluginType);
        node.getAttributes().put("name", "Rfc5242Layout");

        final ConfigurableInstanceFactory factory = DI.createInitializedFactory();
        factory.registerBinding(Configuration.KEY, Lazy.value(configuration));
        final ConfigurationProcessor processor = new ConfigurationProcessor(factory);
        final Rfc5424Layout object = processor.processNodeTree(node);
        assertNotNull(object);
        checkDefaultValues(object);
    }

    private void checkDefaultValues(final Rfc5424Layout layout) {
        assertNotNull(layout);
        assertEquals(Facility.LOCAL0, layout.getFacility());
        assertEquals(String.valueOf(Rfc5424Layout.DEFAULT_ENTERPRISE_NUMBER), layout.getEnterpriseNumber());
        assertTrue(layout.isIncludeMdc());
        assertEquals(Rfc5424Layout.DEFAULT_MDCID, layout.getMdcId());
        assertEquals(Rfc5424Layout.DEFAULT_ID, layout.getDefaultId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "0", "2147483647", "123.45.6.78.9", "0.0.0.0.0.0.0.0.0.0.0.0.0.0"})
    void testLayoutBuilderValidEids(final String eid) {
        final AbstractStringLayout layout = new Rfc5424Layout.Rfc5424LayoutBuilder()
                .setConfiguration(CONFIGURATION)
                .setEin(eid)
                .build();

        assertNotNull(layout);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "someEid", "-1"})
    void testLayoutBuilderInvalidEids(final String eid) {
        final AbstractStringLayout layout = new Rfc5424Layout.Rfc5424LayoutBuilder()
                .setConfiguration(CONFIGURATION)
                .setEin(eid)
                .build();

        assertNull(layout);
    }

    @Test
    void testFQDN() throws UnknownHostException {
        final String fqdn = InetAddress.getLocalHost().getCanonicalHostName();
        final Rfc5424Layout layout =
                Rfc5424Layout.newBuilder().setConfiguration(CONFIGURATION).build();
        assertThat(layout.getLocalHostName()).isEqualTo(fqdn);
    }
}
