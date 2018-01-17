/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static org.mule.extension.http.internal.HttpConnectorConstants.URL_CONFIGURATION;
import org.mule.extension.http.api.request.HttpSendBodyMode;
import org.mule.extension.http.api.request.builder.QueryParam;
import org.mule.extension.http.api.request.builder.RequestHeader;
import org.mule.extension.http.api.streaming.HttpStreamingType;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import java.net.CookieManager;
import java.util.List;

import javax.inject.Inject;

/**
 * Configuration element for a HTTP requests.
 *
 * @since 1.0
 */
@Configuration(name = "requestConfig")
@ConnectionProviders(HttpRequesterProvider.class)
@Operations({HttpRequestOperations.class})
public class HttpRequesterConfig implements Initialisable, HttpRequesterCookieConfig {

  @ParameterGroup(name = URL_CONFIGURATION)
  @Placement(order = 1)
  private RequestUrlConfiguration urlConfiguration;

  @ParameterGroup(name = "Request Settings")
  @Placement(order = 2)
  private RequestSettings requestSettings;

  @ParameterGroup(name = "Response Settings")
  @Placement(order = 3)
  private ResponseSettings responseSettings;

  @Inject
  private MuleContext muleContext;
  private CookieManager cookieManager;

  @Override
  public void initialise() throws InitialisationException {
    if (requestSettings.isEnableCookies()) {
      cookieManager = new CookieManager();
    }
  }

  public String getBasePath() {
    return urlConfiguration.getBasePath();
  }

  public List<RequestHeader> getDefaultHeaders() {
    return requestSettings.getDefaultHeaders();
  }

  public List<QueryParam> getDefaultQueryParams() {
    return requestSettings.getDefaultQueryParams();
  }

  public boolean getFollowRedirects() {
    return requestSettings.getFollowRedirects();
  }

  public HttpStreamingType getRequestStreamingMode() {
    return requestSettings.getRequestStreamingMode();
  }

  public HttpSendBodyMode getSendBodyMode() {
    return requestSettings.getSendBodyMode();
  }

  public Integer getResponseTimeout() {
    return responseSettings.getResponseTimeout();
  }

  @Override
  public boolean isEnableCookies() {
    return requestSettings.isEnableCookies();
  }

  @Override
  public CookieManager getCookieManager() {
    return cookieManager;
  }

  public MuleContext getMuleContext() {
    return muleContext;
  }

}
