/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.test.web.client.response;

import java.io.IOException;

import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

/**
 * A {@code ResponseCreator} which delegates to a {@link ClientHttpRequestFactory}
 * to perform the request and return the associated response.
 * <p>Note that the input request is asserted to be a {@code MockClientHttpRequest} and
 * the URI, method, headers and body are copied.
 * <p>The {@link MockRestResponseCreators#byExecutingRequestUsing(RestTemplate)} method
 * can be used as a pseudo-DSL for creating an instance of this class.
 *
 * @since 6.0.4
 */
public class ExecutingResponseCreator implements ResponseCreator {

	private final ClientHttpRequestFactory requestFactory;


	/**
	 * Create a {@code ExecutingResponseCreator} from a {@code ClientHttpRequestFactory}.
	 * <p>See also {@link MockRestResponseCreators#byExecutingRequestUsing(RestTemplate)}
	 * for a factory method alternative to this constructor which accepts a
	 * {@code RestTemplate} instead.
	 * @param requestFactory the request factory to delegate to
	 * @see MockRestResponseCreators#byExecutingRequestUsing(RestTemplate)
	 */
	public ExecutingResponseCreator(ClientHttpRequestFactory requestFactory) {
		this.requestFactory = requestFactory;
	}


	@Override
	public ClientHttpResponse createResponse(ClientHttpRequest request) throws IOException {
		Assert.state(request instanceof MockClientHttpRequest, "Request should be an instance of MockClientHttpRequest");
		MockClientHttpRequest mockRequest = (MockClientHttpRequest) request;
		ClientHttpRequest newRequest = this.requestFactory.createRequest(mockRequest.getURI(), mockRequest.getMethod());
		newRequest.getHeaders().putAll(mockRequest.getHeaders());
		StreamUtils.copy(mockRequest.getBodyAsBytes(), newRequest.getBody());
		return newRequest.execute();
	}
}
