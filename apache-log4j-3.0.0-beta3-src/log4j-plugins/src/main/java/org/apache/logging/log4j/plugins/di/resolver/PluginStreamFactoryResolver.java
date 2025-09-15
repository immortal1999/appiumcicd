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
package org.apache.logging.log4j.plugins.di.resolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.logging.log4j.plugins.di.InstanceFactory;
import org.apache.logging.log4j.plugins.di.spi.ResolvableKey;

/**
 * Factory resolver for {@code Stream<T>} of plugin instances or factories within a namespace.
 * Value types can be {@code Supplier<T>} to inject plugin factories instead of plugin instances.
 */
public class PluginStreamFactoryResolver<T> extends AbstractPluginFactoryResolver<Stream<? extends T>> {
    @Override
    protected boolean supportsType(final Type rawType, final Type... typeArguments) {
        return rawType == Stream.class;
    }

    @Override
    public Supplier<Stream<? extends T>> getFactory(
            final ResolvableKey<Stream<? extends T>> resolvableKey, final InstanceFactory instanceFactory) {
        final String namespace = resolvableKey.namespace();
        final ParameterizedType containerType = resolvableKey.parameterizedType();
        final Type componentType = containerType.getActualTypeArguments()[0];
        return () -> Plugins.streamPluginInstancesMatching(instanceFactory, namespace, componentType);
    }
}
