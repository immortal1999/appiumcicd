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
package org.apache.logging.log4j.plugins.condition;

import java.lang.reflect.AnnotatedElement;

/**
 * Checks an annotated element to see if it matches some condition.
 * <p>
 * <strong>Implementation note:</strong> must have a default constructor. The class should be used in a
 * {@link Conditional} annotation on an annotation class to provide parameters to the condition.
 * </p>
 */
@FunctionalInterface
public interface Condition {
    boolean matches(final ConditionContext context, final AnnotatedElement element);
}
