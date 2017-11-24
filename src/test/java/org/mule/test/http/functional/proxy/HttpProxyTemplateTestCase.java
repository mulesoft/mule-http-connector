/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.proxy;

import static java.lang.String.valueOf;
import static java.util.Optional.of;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.metadata.DataType.fromObject;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONNECTION;
import static org.mule.runtime.http.api.HttpHeaders.Names.CONTENT_LENGTH;
import static org.mule.runtime.http.api.HttpHeaders.Names.TRANSFER_ENCODING;
import static org.mule.runtime.http.api.HttpHeaders.Names.X_FORWARDED_FOR;
import static org.mule.runtime.http.api.HttpHeaders.Values.CHUNKED;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.PROXY;
import org.mule.extension.http.api.HttpAttributes;
import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.TestInputStream;
import org.mule.test.http.functional.requester.AbstractHttpRequestTestCase;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.generators.InputStreamBodyGenerator;
import com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.qameta.allure.Story;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@Story(PROXY)
public class HttpProxyTemplateTestCase extends AbstractHttpRequestTestCase {

  @Rule
  public DynamicPort proxyPort = new DynamicPort("proxyPort");

  private static final String HEADER = "multiple";
  private static final String MULTIPLE_KEY_QUERY = "/test?key=value&key=value%202";
  private RequestHandlerExtender handlerExtender;
  private boolean consumeAllRequest = true;
  private static String IO_THREAD_PREFIX = "[MuleRuntime].io";
  private static Function<Message.Builder, Message.Builder> policy;

  @Override
  protected String getConfigFile() {
    return "http-proxy-template-config.xml";
  }

  @Test
  public void proxySimpleRequests() throws Exception {
    handlerExtender = null;
    assertRequestOk(getProxyUrl(""), null);
    assertRequestOk(getProxyUrl("test"), null);
  }

  @Test
  public void failIfTargetServiceIsDown() throws Exception {
    handlerExtender = null;
    stopServer();
    Response response = Request.Get(getProxyUrl("")).connectTimeout(RECEIVE_TIMEOUT).execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(500));
  }

  @Test
  public void proxyMethod() throws Exception {
    handlerExtender = new EchoRequestHandlerExtender() {

      @Override
      protected String selectRequestPartToReturn(org.eclipse.jetty.server.Request baseRequest) {
        return baseRequest.getMethod();
      }
    };
    assertRequestOk(getProxyUrl("test?parameterName=parameterValue"), "GET");

    Response response = Request.Post(getProxyUrl("test?parameterName=parameterValue"))
        .bodyString("Some Text", ContentType.DEFAULT_TEXT).connectTimeout(RECEIVE_TIMEOUT).execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(200));
    assertThat(IOUtils.toString(httpResponse.getEntity().getContent()), is("POST"));
  }

  @Ignore
  @Test
  public void proxyProtocolHttp1_0() throws Exception {
    handlerExtender = new EchoRequestHandlerExtender() {

      @Override
      protected String selectRequestPartToReturn(org.eclipse.jetty.server.Request baseRequest) {
        return baseRequest.getProtocol();
      }
    };

    Response response = Request.Get(getProxyUrl("test?parameterName=parameterValue")).version(HttpVersion.HTTP_1_0)
        .connectTimeout(RECEIVE_TIMEOUT).execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(200));
    assertThat(IOUtils.toString(httpResponse.getEntity().getContent()), is("HTTP/1.0"));
  }

  @Test
  public void proxyProtocolHttp1_1() throws Exception {
    handlerExtender = new EchoRequestHandlerExtender() {

      @Override
      protected String selectRequestPartToReturn(org.eclipse.jetty.server.Request baseRequest) {
        return baseRequest.getProtocol();
      }
    };
    assertRequestOk(getProxyUrl("test?parameterName=parameterValue"), "HTTP/1.1");
  }

  @Test
  public void proxyStreaming() throws Exception {
    final Latch latch = new Latch();
    consumeAllRequest = false;
    handlerExtender = (baseRequest, request, response) -> {
      extractHeadersFromBaseRequest(baseRequest);

      latch.release();
      IOUtils.toString(baseRequest.getInputStream());

      response.setContentType(request.getContentType());
      response.setStatus(SC_OK);
      response.getWriter().print("OK");
    };

    AsyncHttpClientConfig.Builder configBuilder = new AsyncHttpClientConfig.Builder();
    AsyncHttpClientConfig config = configBuilder.build();
    AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new GrizzlyAsyncHttpProvider(config), config);

    AsyncHttpClient.BoundRequestBuilder boundRequestBuilder =
        asyncHttpClient.preparePost(getProxyUrl("test?parameterName=parameterValue"));
    boundRequestBuilder.setBody(new InputStreamBodyGenerator(new TestInputStream(latch)));
    ListenableFuture<com.ning.http.client.Response> future = boundRequestBuilder.execute();

    com.ning.http.client.Response response = future.get();
    assertThat(response.getStatusCode(), is(SC_OK));
    response.getHeaders();

    assertThat(getFirstReceivedHeader(TRANSFER_ENCODING), is(CHUNKED));
    assertThat(response.getResponseBody(), is("OK"));

    asyncHttpClient.close();
  }

  @Test
  public void proxyContentLength() throws Exception {
    Response response = Request.Post(getProxyUrl(""))
        .body(new StringEntity(TEST_MESSAGE))
        .connectTimeout(RECEIVE_TIMEOUT)
        .execute();
    HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse.getStatusLine().getStatusCode(), is(SC_OK));
    assertThat(httpResponse.getFirstHeader(CONTENT_LENGTH).getValue(), is(valueOf(DEFAULT_RESPONSE.length())));
    assertThat(getFirstReceivedHeader(CONTENT_LENGTH), is(valueOf(TEST_MESSAGE.length())));
  }

  @Test
  public void doesNotProxyChunkedWhenModifiedWithString() throws Exception {
    policy = builder -> builder.value(TEST_PAYLOAD);

    Response response = Request.Post(getProxyUrl("policy"))
        .body(new InputStreamEntity(new ByteArrayInputStream(TEST_MESSAGE.getBytes())))
        .connectTimeout(RECEIVE_TIMEOUT)
        .execute();
    HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse.getStatusLine().getStatusCode(), is(SC_OK));
    String length = valueOf(TEST_PAYLOAD.length());
    assertThat(httpResponse.getFirstHeader(CONTENT_LENGTH).getValue(), is(length));
    assertThat(getFirstReceivedHeader(CONTENT_LENGTH), is(length));
  }

  @Test
  public void doesNotProxyContentLengthWhenModifiedWithStream() throws Exception {
    policy = builder -> builder.value(new ByteArrayInputStream(TEST_PAYLOAD.getBytes()));

    Response response = Request.Post(getProxyUrl("policy"))
        .body(new StringEntity(TEST_MESSAGE))
        .connectTimeout(RECEIVE_TIMEOUT)
        .execute();
    HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse.getStatusLine().getStatusCode(), is(SC_OK));
    assertThat(httpResponse.getFirstHeader(TRANSFER_ENCODING).getValue(), is(CHUNKED));
    assertThat(getFirstReceivedHeader(TRANSFER_ENCODING), is(CHUNKED));
  }

  @Test
  public void usesContentLengthWhenModifiedWithStreamAndLength() throws Exception {
    long length = (long) TEST_PAYLOAD.length();

    policy = builder -> {
      ByteArrayInputStream stream = new ByteArrayInputStream(TEST_PAYLOAD.getBytes());
      return builder.payload(new TypedValue<Object>(stream, fromObject(stream), of(length)));
    };

    Response response = Request.Post(getProxyUrl("policy"))
        .body(new StringEntity(TEST_MESSAGE))
        .connectTimeout(RECEIVE_TIMEOUT)
        .execute();
    HttpResponse httpResponse = response.returnResponse();

    assertThat(httpResponse.getStatusLine().getStatusCode(), is(SC_OK));
    assertThat(httpResponse.getFirstHeader(CONTENT_LENGTH).getValue(), is(valueOf(length)));
    assertThat(getFirstReceivedHeader(CONTENT_LENGTH), is(valueOf(length)));
  }

  @Test
  public void proxyPath() throws Exception {
    handlerExtender = new EchoRequestHandlerExtender() {

      @Override
      protected String selectRequestPartToReturn(org.eclipse.jetty.server.Request baseRequest) {
        return baseRequest.getPathInfo();
      }
    };
    assertRequestOk(getProxyUrl("test?parameterName=parameterValue"), "/test");
  }

  @Test
  public void proxyQueryString() throws Exception {
    handlerExtender = new EchoRequestHandlerExtender() {

      @Override
      protected String selectRequestPartToReturn(org.eclipse.jetty.server.Request baseRequest) {
        return baseRequest.getQueryString();
      }
    };
    assertRequestOk(getProxyUrl("test?parameterName=parameterValue"), "parameterName=parameterValue");
  }

  @Test
  public void proxyBody() throws Exception {
    handlerExtender = new EchoRequestHandlerExtender() {

      @Override
      protected String selectRequestPartToReturn(org.eclipse.jetty.server.Request baseRequest) {
        return body;
      }
    };

    Response response = Request.Post(getProxyUrl("test")).bodyString("Some Text", ContentType.DEFAULT_TEXT)
        .connectTimeout(RECEIVE_TIMEOUT).execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(200));
    assertThat(IOUtils.toString(httpResponse.getEntity().getContent()), is("Some Text"));
  }

  @Test
  public void proxyHeaders() throws Exception {
    handlerExtender = null;

    Response response = Request.Get(getProxyUrl("/test?name=value")).addHeader("MyCustomHeaderName", "MyCustomHeaderValue")
        .connectTimeout(RECEIVE_TIMEOUT).execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(200));

    assertThat(getFirstReceivedHeader("MyCustomHeaderName"), is("MyCustomHeaderValue"));

    Set<String> lowerCaseHeaderNames = new HashSet<>();
    for (Header header : httpResponse.getAllHeaders()) {
      lowerCaseHeaderNames.add(header.getName().toLowerCase());
      // Ensure no synthetic properties in headers
      assertThat(header.getName(), not(startsWith("http.")));
    }

    // Ensure not repeated headers
    assertThat(lowerCaseHeaderNames.size(), is(httpResponse.getAllHeaders().length));
  }

  @Test
  public void setXForwardedForHeader() throws Exception {
    handlerExtender = null;

    Response response = Request.Get(getProxyUrl("")).connectTimeout(RECEIVE_TIMEOUT).execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(200));

    assertThat(getFirstReceivedHeader(X_FORWARDED_FOR), startsWith("/127.0.0.1:"));
  }

  @Test
  public void forwardsMultipleValuedHeadersAndQueryParams() throws Exception {
    Response response = Request.Get(getProxyUrl(MULTIPLE_KEY_QUERY))
        .addHeader(HEADER, "value1")
        .addHeader(HEADER, "value2")
        .connectTimeout(RECEIVE_TIMEOUT)
        .execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(200));

    Collection<String> headerValues = headers.get(HEADER);
    assertThat(headerValues, hasSize(2));
    assertThat(headerValues, containsInAnyOrder("value1", "value2"));

    assertThat(uri, endsWith(MULTIPLE_KEY_QUERY));
  }

  private void assertRequestOk(String url, String expectedResponse) throws IOException {
    Response response = Request.Get(url).connectTimeout(RECEIVE_TIMEOUT).execute();
    HttpResponse httpResponse = response.returnResponse();
    assertThat(httpResponse.getStatusLine().getStatusCode(), is(200));
    if (expectedResponse != null) {
      assertThat(IOUtils.toString(httpResponse.getEntity().getContent()), is(expectedResponse));
    }
  }

  private String getProxyUrl(String path) {
    return String.format("http://localhost:%s/%s", proxyPort.getNumber(), path);
  }

  @Override
  protected void handleRequest(org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                               HttpServletResponse response)
      throws IOException {
    if (consumeAllRequest) {
      extractBaseRequestParts(baseRequest);
    }

    if (handlerExtender == null) {
      writeResponse(response);
    } else {
      handlerExtender.handleRequest(baseRequest, request, response);
    }
  }

  private static interface RequestHandlerExtender {

    void handleRequest(org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response)
        throws IOException;
  }

  private static abstract class EchoRequestHandlerExtender implements RequestHandlerExtender {

    @Override
    public void handleRequest(org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request,
                              HttpServletResponse response)
        throws IOException {
      response.setContentType(request.getContentType());
      response.setStatus(SC_OK);
      response.getWriter().print(selectRequestPartToReturn(baseRequest));
    }

    protected abstract String selectRequestPartToReturn(org.eclipse.jetty.server.Request baseRequest);
  }


  /**
   * Simulates the header clean up of proxies.
   */
  private static class ProxyProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      HttpAttributes attributes = (HttpAttributes) event.getMessage().getAttributes().getValue();
      MultiMap<String, String> headers = new MultiMap<>(attributes.getHeaders());

      headers.remove(CONTENT_LENGTH.toLowerCase());
      headers.remove(TRANSFER_ENCODING.toLowerCase());
      headers.remove(CONNECTION.toLowerCase());

      HttpAttributes attributesToSend;
      if (attributes instanceof HttpResponseAttributes) {
        HttpResponseAttributes attributesFromResponse = (HttpResponseAttributes) attributes;
        attributesToSend = new HttpResponseAttributes(attributesFromResponse.getStatusCode(),
                                                      attributesFromResponse.getReasonPhrase(),
                                                      headers);
      } else {
        HttpRequestAttributes attributesFromRequest = (HttpRequestAttributes) attributes;
        attributesToSend =
            new HttpRequestAttributes(headers, attributesFromRequest.getListenerPath(), attributesFromRequest.getRelativePath(),
                                      attributesFromRequest.getVersion(), attributesFromRequest.getScheme(),
                                      attributesFromRequest.getMethod(),
                                      attributesFromRequest.getRequestPath(), attributesFromRequest.getRequestUri(),
                                      attributesFromRequest.getQueryString(),
                                      attributesFromRequest.getQueryParams(), attributesFromRequest.getUriParams(),
                                      attributesFromRequest.getLocalAddress(), attributesFromRequest.getRemoteAddress(),
                                      attributesFromRequest.getClientCertificate());
      }

      return CoreEvent.builder(event).message(getBuilder(event).attributesValue(attributesToSend).build()).build();
    }

    protected Message.Builder getBuilder(CoreEvent event) {
      return Message.builder(event.getMessage());
    }
  }


  /**
   * Simulates the modification of the payload when policies are applied to a proxy.
   */
  private static class ProxyPolicyProcessor extends ProxyProcessor {

    @Override
    protected Message.Builder getBuilder(CoreEvent event) {
      return policy.apply(super.getBuilder(event));
    }
  }

}
