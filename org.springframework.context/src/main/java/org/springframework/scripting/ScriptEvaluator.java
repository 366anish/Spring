/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.scripting;

import java.util.Map;

/**
 * Script evaluator.
 *
 * @author Costin Leau
 * @since 3.1.1
 */
public interface ScriptEvaluator {

	/**
	 * Evaluate the given script (without any arguments) and returns the result (if any).
	 *
	 * @param script script to evaluate
	 * @return script result (may be {@code null})
	 */
	Object evaluate(ScriptSource script);

	/**
	 * Evaluates the given script and returns the result (if any).
	 *
	 * @param script script to evaluate
	 * @param arguments script arguments
	 * @return script result (may be {@code null})
	 */
	Object evaluate(ScriptSource script, Map<String, Object> arguments);

}
