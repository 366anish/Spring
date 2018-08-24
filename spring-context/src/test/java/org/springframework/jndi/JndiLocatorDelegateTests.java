/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.jndi;

import org.junit.Test;

import javax.naming.spi.NamingManager;
import java.lang.reflect.Field;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link JndiLocatorDelegate}.
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 */
public class JndiLocatorDelegateTests {

	@Test
	public void isDefaultJndiEnvironmentAvailableFalse() throws Exception {
		Field builderField = NamingManager.class.getDeclaredField("initctx_factory_builder");
		builderField.setAccessible(true);
		Object oldBuilder = builderField.get(null);
		builderField.set(null, null);

		try {
			assertThat(JndiLocatorDelegate.isDefaultJndiEnvironmentAvailable(), equalTo(false));
		}
		finally {
			builderField.set(null, oldBuilder);
		}
	}

}
