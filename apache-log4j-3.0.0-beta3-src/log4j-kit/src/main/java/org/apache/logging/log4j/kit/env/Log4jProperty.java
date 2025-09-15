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
package org.apache.logging.log4j.kit.env;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a class or parameter that stores Log4j API configuration properties.
 * <p>
 *     This annotation is required for root property classes.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
public @interface Log4jProperty {

    /**
     * If used as default value, points to a JVM-dependent constant.
     */
    String SYSTEM = "system";

    /**
     * Provides a name for the configuration property.
     */
    String name() default "";

    /**
     * Provides the default value of the property.
     * <p>
     *     This only applies to scalar values.
     * </p>
     * <p>
     *     If the property is of type {@link java.nio.charset.Charset}, {@link java.util.Locale},
     *     {@link java.util.TimeZone} or {@link java.time.ZoneId}, the special constant {@value SYSTEM} uses the JVM's
     *     default value.
     * </p>
     */
    String defaultValue() default "";
}
