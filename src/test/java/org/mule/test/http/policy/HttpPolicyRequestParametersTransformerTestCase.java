package org.mule.test.http.policy;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.api.metadata.DataType.ATOM_STRING;
import static org.mule.runtime.api.metadata.DataType.fromType;

import org.mule.extension.http.api.policy.HttpPolicyRequestAttributes;
import org.mule.extension.http.api.policy.HttpPolicyRequestParametersTransformer;
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

    Map<String, Object> map = new HashMap<>();
    map.put("path", PATH);
    map.put("body", body);
    map.put("headers", headers);
    map.put("queryParams", queryParams);
    map.put("uriParams", uriParams);

    Message message = transformer.fromParametersToMessage(map);

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

    Map<String, Object> map = new HashMap<>();
    map.put("path", PATH);
    map.put("body", body);
    map.put("headers", headers);

    Message message = transformer.fromParametersToMessage(map);

    assertThat(message.getPayload().getValue(), is(BODY));
    assertThat(message.getPayload().getDataType(), is(ATOM_STRING));
    assertThat(message.getAttributes().getDataType(), is(fromType(HttpPolicyRequestAttributes.class)));

    HttpPolicyRequestAttributes requestAttributes = (HttpPolicyRequestAttributes) message.getAttributes().getValue();
    assertThat(requestAttributes.getRequestPath(), is(PATH));
    assertThat(requestAttributes.getHeaders(), is(headers));
    assertThat(requestAttributes.getQueryParams(), nullValue());
    assertThat(requestAttributes.getUriParams(), nullValue());
  }

}
