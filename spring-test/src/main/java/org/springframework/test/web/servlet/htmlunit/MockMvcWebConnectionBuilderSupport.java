/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.test.web.servlet.htmlunit;

import java.util.ArrayList;
import java.util.List;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import com.gargoylesoftware.htmlunit.WebConnection;

/**
 * Support class that simplifies the creation of a {@link WebConnection} that
 * uses {@link MockMvc} and optionally delegates to a real {@link WebConnection}
 * for specific requests.
 *
 * <p>The default is to use {@link MockMvc} for requests to {@code localhost}
 * and otherwise use a real {@link WebConnection}.
 *
 * @author Rob Winch
 * @author Sam Brannen
 * @since 4.2
 */
public abstract class MockMvcWebConnectionBuilderSupport<T extends MockMvcWebConnectionBuilderSupport<T>> {

	private final MockMvc mockMvc;

	private final List<WebRequestMatcher> mockMvcRequestMatchers = new ArrayList<WebRequestMatcher>();

	private String contextPath = "";

	private boolean alwaysUseMockMvc;


	/**
	 * Create a new instance using the supplied {@link MockMvc} instance.
	 * @param mockMvc the {@code MockMvc} instance to use; never {@code null}
	 */
	protected MockMvcWebConnectionBuilderSupport(MockMvc mockMvc) {
		Assert.notNull(mockMvc, "mockMvc must not be null");
		this.mockMvc = mockMvc;
		this.mockMvcRequestMatchers.add(new HostRequestMatcher("localhost"));
	}

	/**
	 * Create a new instance using the supplied {@link WebApplicationContext}.
	 * @param context the {@code WebApplicationContext} to create a {@code MockMvc}
	 * instance from; never {@code null}
	 */
	protected MockMvcWebConnectionBuilderSupport(WebApplicationContext context) {
		this(MockMvcBuilders.webAppContextSetup(context).build());
	}

	/**
	 * Create a new instance using the supplied {@link WebApplicationContext}
	 * and {@link MockMvcConfigurer}.
	 * @param context the {@code WebApplicationContext} to create a {@code MockMvc}
	 * instance from; never {@code null}
	 * @param configurer the MockMvcConfigurer to apply; never {@code null}
	 */
	protected MockMvcWebConnectionBuilderSupport(WebApplicationContext context, MockMvcConfigurer configurer) {
		this(MockMvcBuilders.webAppContextSetup(context).apply(configurer).build());
	}

	/**
	 * Set the context path to use.
	 * <p>If the supplied value is {@code null} or empty, the first path
	 * segment of the request URL is assumed to be the context path.
	 * <p>Default is {@code ""}.
	 * @param contextPath the context path to use
	 * @return this builder for further customization
	 */
	@SuppressWarnings("unchecked")
	public T contextPath(String contextPath) {
		this.contextPath = contextPath;
		return (T) this;
	}

	/**
	 * Specify that {@link MockMvc} should always be used regardless of
	 * what the request looks like.
	 * @return this builder for further customization
	 */
	@SuppressWarnings("unchecked")
	public T alwaysUseMockMvc() {
		this.alwaysUseMockMvc = true;
		return (T) this;
	}

	/**
	 * Add additional {@link WebRequestMatcher} instances that will ensure
	 * that {@link MockMvc} is used to process the request, if such a matcher
	 * matches against the web request.
	 * @param matchers additional {@code WebRequestMatcher} instances
	 * @return this builder for further customization
	 */
	@SuppressWarnings("unchecked")
	public T useMockMvc(WebRequestMatcher... matchers) {
		for (WebRequestMatcher matcher : matchers) {
			this.mockMvcRequestMatchers.add(matcher);
		}
		return (T) this;
	}

	/**
	 * Add additional {@link WebRequestMatcher} instances that return {@code true}
	 * if a supplied host matches &mdash; for example, {@code "example.com"} or
	 * {@code "example.com:8080"}.
	 * @param hosts additional hosts that ensure {@code MockMvc} gets invoked
	 * @return this builder for further customization
	 */
	@SuppressWarnings("unchecked")
	public T useMockMvcForHosts(String... hosts) {
		this.mockMvcRequestMatchers.add(new HostRequestMatcher(hosts));
		return (T) this;
	}

	/**
	 * Create a new {@link WebConnection} that will use a {@link MockMvc}
	 * instance if one of the specified {@link WebRequestMatcher} instances
	 * matches.
	 * @param defaultConnection the default WebConnection to use if none of
	 * the specified {@code WebRequestMatcher} instances matches; never {@code null}
	 * @return a new {@code WebConnection} that will use a {@code MockMvc}
	 * instance if one of the specified {@code WebRequestMatcher} matches
	 * @see #alwaysUseMockMvc()
	 * @see #useMockMvc(WebRequestMatcher...)
	 * @see #useMockMvcForHosts(String...)
	 */
	protected final WebConnection createConnection(WebConnection defaultConnection) {
		Assert.notNull(defaultConnection, "defaultConnection must not be null");
		MockMvcWebConnection mockMvcWebConnection = new MockMvcWebConnection(this.mockMvc, this.contextPath);

		if (this.alwaysUseMockMvc) {
			return mockMvcWebConnection;
		}

		List<DelegatingWebConnection.DelegateWebConnection> delegates = new ArrayList<DelegatingWebConnection.DelegateWebConnection>(
			this.mockMvcRequestMatchers.size());
		for (WebRequestMatcher matcher : this.mockMvcRequestMatchers) {
			delegates.add(new DelegatingWebConnection.DelegateWebConnection(matcher, mockMvcWebConnection));
		}

		return new DelegatingWebConnection(defaultConnection, delegates);
	}

}