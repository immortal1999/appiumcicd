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
package org.apache.logging.log4j.plugins.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.plugins.model.PluginCache;
import org.apache.logging.log4j.plugins.model.PluginEntry;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.Issue;

public class PluginCacheTest {

    @Test
    @Issue("https://issues.apache.org/jira/browse/LOG4J2-2735")
    public void testOutputIsReproducibleWhenInputOrderingChanges() throws IOException {
        final PluginCache cacheA = new PluginCache();
        createCategory(cacheA, "one", Arrays.asList("bravo", "alpha", "charlie"));
        createCategory(cacheA, "two", Arrays.asList("alpha", "charlie", "bravo"));
        assertEquals(cacheA.getAllNamespaces().size(), 2);
        assertEquals(cacheA.getAllNamespaces().get("one").size(), 3);
        assertEquals(cacheA.getAllNamespaces().get("two").size(), 3);
        final PluginCache cacheB = new PluginCache();
        createCategory(cacheB, "two", Arrays.asList("bravo", "alpha", "charlie"));
        createCategory(cacheB, "one", Arrays.asList("alpha", "charlie", "bravo"));
        assertEquals(cacheB.getAllNamespaces().size(), 2);
        assertEquals(cacheB.getAllNamespaces().get("one").size(), 3);
        assertEquals(cacheB.getAllNamespaces().get("two").size(), 3);
        assertEquals(Objects.toString(cacheA.getAllNamespaces()), Objects.toString(cacheB.getAllNamespaces()));
    }

    private void createCategory(final PluginCache cache, final String categoryName, final List<String> entryNames) {
        final Map<String, PluginEntry> category = cache.getNamespace(categoryName);
        for (String entryName : entryNames) {
            final PluginEntry entry = PluginEntry.builder()
                    .setKey(entryName)
                    .setName(entryName)
                    .setClassName("com.example.Plugin")
                    .setNamespace(categoryName)
                    .get();
            category.put(entryName, entry);
        }
    }
}
