/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.client.reactive;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import static org.springframework.http.MediaType.ALL_VALUE;

/**
 * {@link ClientHttpRequest} implementation for the Apache HttpComponents HttpClient 5.x.
 *
 * @author Martin Tarjányi
 * @since 5.3
 * @see <a href="https://hc.apache.org/index.html">Apache HttpComponents</a>
 */
class ApacheClientHttpRequest extends AbstractClientHttpRequest {
	private final HttpRequest httpRequest;

	private final DataBufferFactory dataBufferFactory;

	private Flux<ByteBuffer> byteBufferFlux;

	private long contentLength = -1;

	public ApacheClientHttpRequest(HttpMethod method, URI uri, DataBufferFactory dataBufferFactory) {
		this.httpRequest = new BasicHttpRequest(method.name(), uri);
		this.dataBufferFactory = dataBufferFactory;
	}

	@Override
	public HttpMethod getMethod() {
		return HttpMethod.resolve(this.httpRequest.getMethod());
	}

	@Override
	public URI getURI() {
		try {
			return this.httpRequest.getUri();
		}
		catch (URISyntaxException ex) {
			throw new IllegalArgumentException("Invalid URI syntax.", ex);
		}
	}

	@Override
	public DataBufferFactory bufferFactory() {
		return this.dataBufferFactory;
	}

	@Override
	public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
		return doCommit(() -> {
			this.byteBufferFlux = Flux.from(body).map(DataBuffer::asByteBuffer);
			return Mono.empty();
		});
	}

	@Override
	public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
		return writeWith(Flux.from(body).flatMap(p -> p));
	}

	@Override
	public Mono<Void> setComplete() {
		return doCommit();
	}

	@Override
	protected void applyHeaders() {
		HttpHeaders headers = getHeaders();
		this.contentLength = headers.getContentLength();

		headers.entrySet()
				.stream()
				.filter(entry -> !HttpHeaders.CONTENT_LENGTH.equals(entry.getKey()))
				.forEach(entry -> entry.getValue().forEach(v -> this.httpRequest.addHeader(entry.getKey(), v)));

		if (!this.httpRequest.containsHeader(HttpHeaders.ACCEPT)) {
			this.httpRequest.addHeader(HttpHeaders.ACCEPT, ALL_VALUE);
		}
	}

	@Override
	protected void applyCookies() {
		if (getCookies().isEmpty()) {
			return;
		}

		String cookiesString = getCookies().values()
				.stream()
				.flatMap(Collection::stream)
				.map(HttpCookie::toString)
				.collect(Collectors.joining("; "));

		this.httpRequest.addHeader(HttpHeaders.COOKIE, cookiesString);
	}

	public HttpRequest getHttpRequest() {
		return this.httpRequest;
	}

	public Flux<ByteBuffer> getByteBufferFlux() {
		return this.byteBufferFlux;
	}

	public long getContentLength() {
		return this.contentLength;
	}
}
