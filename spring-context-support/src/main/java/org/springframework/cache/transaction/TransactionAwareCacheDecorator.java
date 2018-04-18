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

package org.springframework.cache.transaction;

import org.springframework.cache.Cache;
import org.springframework.cache.support.CacheDecorator;

/**
 * An implementation of {@code CacheDecorator} that decorates the given cache with {@code TransactionAwareCache} and also
 * keeps track of the {@link #transactionAware} flag to denote whether or not to decorate the given cache.
 *
 * @author Petar Tahchiev
 * @since 5.1.0
 */
public class TransactionAwareCacheDecorator implements CacheDecorator {

	/**
	 * Whether this CacheDecorator is transaction aware and should expose transaction-aware Cache objects.
	 * <p>Default is "false". Set this to "true" to synchronize cache put/evict
	 * operations with ongoing Spring-managed transactions, performing the actual cache
	 * put/evict operation only in the after-commit phase of a successful transaction.
	 */
	private boolean transactionAware;

	public TransactionAwareCacheDecorator(boolean transactionAware) {
		this.transactionAware = transactionAware;
	}

	@Override
	public Cache decorateCache(Cache cache) {
		if (this.transactionAware) {
			return new TransactionAwareCache(cache);
		}
		return cache;
	}
}
