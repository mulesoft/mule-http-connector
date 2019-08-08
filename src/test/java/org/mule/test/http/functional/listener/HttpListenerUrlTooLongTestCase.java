package org.mule.test.http.functional.listener;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Rule;
import org.junit.Test;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.http.functional.AbstractHttpTestCase;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.mule.runtime.http.api.HttpConstants.HttpStatus.REQUEST_URI_TOO_LONG;

public class HttpListenerUrlTooLongTestCase extends AbstractHttpTestCase {

    @Rule
    public DynamicPort listenPort = new DynamicPort("port");

    @Override
    protected String getConfigFile() {
        return "http-listener-url-too-long-config.xml";
    }


    @Test
    public void returnsRequestUriTooLong() throws Exception {
        final Response response = Request.Get(getListenerUrl(repeat("path", 3000)))
                .execute();

        assertThat(response.returnResponse().getStatusLine().getStatusCode(), is(REQUEST_URI_TOO_LONG.getStatusCode()));
        assertThat(response.returnResponse().getStatusLine().getReasonPhrase(), is(REQUEST_URI_TOO_LONG.getReasonPhrase()));
    }


    private String getListenerUrl(String path) {
        return String.format("http://localhost:%s/%s", listenPort.getNumber(), path);
    }
}
