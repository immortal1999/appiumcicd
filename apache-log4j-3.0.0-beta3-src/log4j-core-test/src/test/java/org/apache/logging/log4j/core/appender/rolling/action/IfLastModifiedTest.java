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
package org.apache.logging.log4j.core.appender.rolling.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.FileTime;
import java.time.Duration;
import org.apache.logging.log4j.core.test.appender.rolling.action.DummyFileAttributes;
import org.apache.logging.log4j.core.time.Clock;
import org.apache.logging.log4j.core.time.ClockFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests the IfLastModified class.
 */
public class IfLastModifiedTest {

    private static final Clock CLOCK = ClockFactory.getClock();

    @Test
    public void testGetDurationReturnsConstructorValue() {
        final IfLastModified filter = IfLastModified.newBuilder()
                .setAge(Duration.parse("P7D"))
                .setClock(CLOCK)
                .get();
        assertEquals(0, filter.getAge().compareTo(Duration.parse("P7D")));
    }

    @Test
    public void testAcceptsIfFileAgeEqualToDuration() {
        final IfLastModified filter = IfLastModified.newBuilder()
                .setAge(Duration.parse("PT33S"))
                .setClock(CLOCK)
                .get();
        final DummyFileAttributes attrs = new DummyFileAttributes();
        final long age = 33 * 1000;
        attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - age);
        assertTrue(filter.accept(null, null, attrs));
    }

    @Test
    public void testAcceptsIfFileAgeExceedsDuration() {
        final IfLastModified filter = IfLastModified.newBuilder()
                .setAge(Duration.parse("PT33S"))
                .setClock(CLOCK)
                .get();
        final DummyFileAttributes attrs = new DummyFileAttributes();
        final long age = 33 * 1000 + 5;
        attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - age);
        assertTrue(filter.accept(null, null, attrs));
    }

    @Test
    public void testDoesNotAcceptIfFileAgeLessThanDuration() {
        final IfLastModified filter = IfLastModified.newBuilder()
                .setAge(Duration.parse("PT33S"))
                .setClock(CLOCK)
                .get();
        final DummyFileAttributes attrs = new DummyFileAttributes();
        final long age = 33 * 1000 - 5;
        attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - age);
        assertFalse(filter.accept(null, null, attrs));
    }

    @Test
    public void testAcceptCallsNestedConditionsOnlyIfPathAccepted() {
        final CountingCondition counter = new CountingCondition(true);
        final IfLastModified filter = IfLastModified.newBuilder()
                .setAge(Duration.parse("PT33S"))
                .setNestedConditions(counter)
                .setClock(CLOCK)
                .get();
        final DummyFileAttributes attrs = new DummyFileAttributes();
        final long oldEnough = 33 * 1000 + 5;
        attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - oldEnough);

        assertTrue(filter.accept(null, null, attrs));
        assertEquals(1, counter.getAcceptCount());
        assertTrue(filter.accept(null, null, attrs));
        assertEquals(2, counter.getAcceptCount());
        assertTrue(filter.accept(null, null, attrs));
        assertEquals(3, counter.getAcceptCount());

        final long tooYoung = 33 * 1000 - 5;
        attrs.lastModified = FileTime.fromMillis(System.currentTimeMillis() - tooYoung);
        assertFalse(filter.accept(null, null, attrs));
        assertEquals(3, counter.getAcceptCount()); // no increase
        assertFalse(filter.accept(null, null, attrs));
        assertEquals(3, counter.getAcceptCount());
        assertFalse(filter.accept(null, null, attrs));
        assertEquals(3, counter.getAcceptCount());
    }

    @Test
    public void testBeforeTreeWalk() {
        final CountingCondition counter = new CountingCondition(true);
        final IfLastModified filter = IfLastModified.newBuilder()
                .setAge(Duration.parse("PT33S"))
                .setNestedConditions(counter, counter, counter)
                .setClock(CLOCK)
                .get();
        filter.beforeFileTreeWalk();
        assertEquals(3, counter.getBeforeFileTreeWalkCount());
    }
}
