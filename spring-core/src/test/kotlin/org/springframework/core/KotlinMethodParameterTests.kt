/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Method

/**
 * Tests for Kotlin support in [MethodParameter].
 *
 * @author Raman Gupta
 * @author Sebastien Deleuze
 * @author Juergen Hoeller
 */
class KotlinMethodParameterTests {

	lateinit var nullableMethod: Method

	lateinit var nonNullableMethod: Method


	@Before
	@Throws(NoSuchMethodException::class)
	fun setup() {
		nullableMethod = javaClass.getMethod("nullable", String::class.java)
		nonNullableMethod = javaClass.getMethod("nonNullable", String::class.java)
	}


	@Test
	fun `Method parameter nullability`() {
		assertTrue(MethodParameter(nullableMethod, 0).isOptional())
		assertFalse(MethodParameter(nonNullableMethod, 0).isOptional())
	}

	@Test
	fun `Method return type nullability`() {
		assertTrue(MethodParameter(nullableMethod, -1).isOptional())
		assertFalse(MethodParameter(nonNullableMethod, -1).isOptional())
	}


	@Suppress("unused", "unused_parameter")
	fun nullable(p1: String?): Int? = 42

	@Suppress("unused", "unused_parameter")
	fun nonNullable(p1: String): Int = 42

}
