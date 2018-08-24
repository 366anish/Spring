/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.mock.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Mock implementation of {@code javax.servlet.http.Part}.
 *
 * @author Rossen Stoyanchev
 * @since 5.0
 */
public class MockPart implements Part {

	private final String name;

	@Nullable
	private final String filename;

	private final byte[] content;

	private final HttpHeaders headers = new HttpHeaders();


	/**
	 * Constructor for a part with byte[] content only.
	 */
	public MockPart(String name, byte[] content) {
		this(name, null, content);
	}

	/**
	 * Constructor for a part with a filename.
	 */
	public MockPart(String name, @Nullable String filename, InputStream content) throws IOException {
		this(name, filename, FileCopyUtils.copyToByteArray(content));
	}

	/**
	 * Constructor for a part with byte[] content only.
	 * @see #getHeaders()
	 */
	private MockPart(String name, @Nullable String filename, @Nullable byte[] content) {
		Assert.hasLength(name, "Name must not be null");
		this.name = name;
		this.filename = filename;
		this.content = (content != null ? content : new byte[0]);
		this.headers.setContentDispositionFormData(name, filename);
	}


	@Override
	public String getName() {
		return this.name;
	}

	@Override
	@Nullable
	public String getSubmittedFileName() {
		return this.filename;
	}

	@Override
	@Nullable
	public String getContentType() {
		MediaType contentType = this.headers.getContentType();
		return (contentType != null ? contentType.toString() : null);
	}

	@Override
	public long getSize() {
		return this.content.length;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(this.content);
	}

	@Override
	@Nullable
	public String getHeader(String name) {
		return this.headers.getFirst(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		Collection<String> headerValues = this.headers.get(name);
		return (headerValues != null ? headerValues : Collections.emptyList());
	}

	@Override
	public Collection<String> getHeaderNames() {
		return this.headers.keySet();
	}

	/**
	 * Return the {@link HttpHeaders} backing header related accessor methods.
	 */
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	@Override
	public void write(String fileName) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete() throws IOException {
		throw new UnsupportedOperationException();
	}

}
