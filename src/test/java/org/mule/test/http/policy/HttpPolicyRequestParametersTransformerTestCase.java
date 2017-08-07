package org.mule.test.http.policy;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.metadata.DataType.ATOM_STRING;
import static org.mule.runtime.api.metadata.DataType.OBJECT;
import static org.mule.runtime.api.metadata.DataType.fromType;
import static org.mule.runtime.api.metadata.TypedValue.of;

import org.mule.extension.http.api.policy.HttpPolicyRequestAttributes;
import org.mule.extension.http.api.policy.HttpPolicyRequestParametersTransformer;
import org.mule.extension.http.api.policy.HttpPolicyResponseAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.util.MultiMap;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class HttpPolicyRequestParametersTransformerTestCase {

  private static final String BODY = "Body";
  private static final String PATH = "myPath";

  private HttpPolicyRequestParametersTransformer transformer = new HttpPolicyRequestParametersTransformer();

  @Test
  public void fromParametersToMessage() {
    TypedValue<Object> body = new TypedValue<>(BODY, ATOM_STRING);
    MultiMap<String, String> headers = new MultiMap<>(of("header", "headerValue"));
    MultiMap<String, String> queryParams = new MultiMap<>(of("queryParam", "queryParamValue"));
    MultiMap<String, String> uriParams = new MultiMap<>(of("uriParam", "uriParamValue"));

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("path", PATH);
    parameters.put("body", body);
    parameters.put("headers", headers);
    parameters.put("queryParams", queryParams);
    parameters.put("uriParams", uriParams);

    Message message = transformer.fromParametersToMessage(parameters);

    assertThat(message.getPayload().getValue(), is(BODY));
    assertThat(message.getPayload().getDataType(), is(ATOM_STRING));
    assertThat(message.getAttributes().getDataType(), is(fromType(HttpPolicyRequestAttributes.class)));

    HttpPolicyRequestAttributes requestAttributes = (HttpPolicyRequestAttributes) message.getAttributes().getValue();
    assertThat(requestAttributes.getRequestPath(), is(PATH));
    assertThat(requestAttributes.getHeaders(), is(headers));
    assertThat(requestAttributes.getQueryParams(), is(queryParams));
    assertThat(requestAttributes.getUriParams(), is(uriParams));
  }

  @Test
  public void fromParametersToMessageMissingParameter() {
    TypedValue<Object> body = new TypedValue<>(BODY, ATOM_STRING);
    MultiMap<String, String> headers = new MultiMap<>(of("header", "headerValue"));

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("path", PATH);
    parameters.put("body", body);
    parameters.put("headers", headers);

    Message message = transformer.fromParametersToMessage(parameters);

    assertThat(message.getPayload().getValue(), is(BODY));
    assertThat(message.getPayload().getDataType(), is(ATOM_STRING));
    assertThat(message.getAttributes().getDataType(), is(fromType(HttpPolicyRequestAttributes.class)));

    HttpPolicyRequestAttributes requestAttributes = (HttpPolicyRequestAttributes) message.getAttributes().getValue();
    assertThat(requestAttributes.getRequestPath(), is(PATH));
    assertThat(requestAttributes.getHeaders(), is(headers));
    assertThat(requestAttributes.getQueryParams(), is(emptyMultiMap()));
    assertThat(requestAttributes.getUriParams(), is(emptyMultiMap()));
  }

  @Test
  public void fromParametersToMessageEmptyParameters() {
    Map<String, Object> parameters = new HashMap<>();

    Message message = transformer.fromParametersToMessage(parameters);

    assertThat(message.getPayload().getValue(), nullValue());
    assertThat(message.getPayload().getDataType(), is(OBJECT));
    assertThat(message.getAttributes().getDataType(), is(fromType(HttpPolicyRequestAttributes.class)));

    HttpPolicyRequestAttributes requestAttributes = (HttpPolicyRequestAttributes) message.getAttributes().getValue();
    assertThat(requestAttributes.getRequestPath(), nullValue());
    assertThat(requestAttributes.getHeaders(), is(emptyMultiMap()));
    assertThat(requestAttributes.getQueryParams(), is(emptyMultiMap()));
    assertThat(requestAttributes.getUriParams(), is(emptyMultiMap()));
  }

  @Test
  public void fromMessageToParameters() {
    TypedValue<Object> payload = new TypedValue<>(BODY, ATOM_STRING);
    MultiMap<String, String> headers = new MultiMap<>(of("header", "headerValue"));
    MultiMap<String, String> queryParams = new MultiMap<>(of("queryParam", "queryParamValue"));
    MultiMap<String, String> uriParams = new MultiMap<>(of("uriParam", "uriParamValue"));
    TypedValue<HttpPolicyRequestAttributes> attributes =
        of(new HttpPolicyRequestAttributes(headers, queryParams, uriParams, PATH));
    Message message = Message.builder().payload(payload).attributes(attributes).build();

    Map<String, Object> parameters = transformer.fromMessageToParameters(message);

    assertThat(parameters.get("body"), is(payload));
    assertThat(parameters.get("path"), is(PATH));
    assertThat(parameters.get("headers"), is(headers));
    assertThat(parameters.get("uriParams"), is(uriParams));
    assertThat(parameters.get("queryParams"), is(queryParams));
  }

  @Test
  public void fromMessageToParametersMissingAttributesProperty() {
    TypedValue<Object> payload = new TypedValue<>(BODY, ATOM_STRING);
    MultiMap<String, String> headers = new MultiMap<>(of("header", "headerValue"));
    TypedValue<HttpPolicyRequestAttributes> attributes =
        of(new HttpPolicyRequestAttributes(emptyMultiMap(), null, null, null));
    Message message = Message.builder().payload(payload).attributes(attributes).build();

    Map<String, Object> parameters = transformer.fromMessageToParameters(message);

    assertThat(parameters.get("body"), is(payload));
    assertThat(parameters.get("path"), nullValue());
    assertThat(parameters.get("headers"), is(emptyMultiMap()));
    assertThat(parameters.get("uriParams"), nullValue());
    assertThat(parameters.get("queryParams"), nullValue());
  }

  @Test
  public void fromMessageToParametersMissingPayload() {
    TypedValue<Object> payload = null;
    MultiMap<String, String> headers = new MultiMap<>(of("header", "headerValue"));
    MultiMap<String, String> queryParams = new MultiMap<>(of("queryParam", "queryParamValue"));
    MultiMap<String, String> uriParams = new MultiMap<>(of("uriParam", "uriParamValue"));
    TypedValue<HttpPolicyRequestAttributes> attributes =
        of(new HttpPolicyRequestAttributes(headers, queryParams, uriParams, PATH));
    Message message = Message.builder().payload(payload).attributes(attributes).build();

    Map<String, Object> parameters = transformer.fromMessageToParameters(message);

    assertThat(parameters.get("body"), nullValue());
    assertThat(parameters.get("path"), is(PATH));
    assertThat(parameters.get("headers"), is(headers));
    assertThat(parameters.get("uriParams"), is(uriParams));
    assertThat(parameters.get("queryParams"), is(queryParams));
  }

  @Test
  public void fromMessageToParametersNotRequestAttributes() {
    TypedValue<Object> payload = null;
    TypedValue<HttpPolicyResponseAttributes> attributes = of(new HttpPolicyResponseAttributes());
    Message message = Message.builder().payload(payload).attributes(attributes).build();

    Map<String, Object> parameters = transformer.fromMessageToParameters(message);

    assertThat(parameters.get("body"), is(payload));
    assertThat(parameters.get("path"), nullValue());
    assertThat(parameters.get("headers"), nullValue());
    assertThat(parameters.get("uriParams"), nullValue());
    assertThat(parameters.get("queryParams"), nullValue());
  }

  private MultiMap<String, String> emptyMultiMap() {
    return new MultiMap<>();
  }

}
