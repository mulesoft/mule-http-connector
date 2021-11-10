/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request;

import static org.mule.runtime.extension.api.annotation.param.display.Placement.DEFAULT_TAB;
import static org.mule.runtime.http.api.utils.HttpEncoderDecoderUtils.encodeSpaces;

import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.api.request.client.HttpExtensionClient;
import org.mule.extension.http.api.request.client.UriParameters;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.http.api.HttpConstants;

@ExclusiveOptionals(isOneRequired = true)
public class UriSettings {

  /**
   * Path where the request will be sent.
   */
  @Parameter
  @Optional
  @Placement(tab = DEFAULT_TAB, order = 2)
  private String path = "/";

  /**
   * URL where to send the request.
   */
  @Parameter
  @Optional
  @DisplayName("URL")
  @Example("http://www.mulesoft.com")
  @Placement(tab = DEFAULT_TAB, order = 1)
  private String url;

  public String getPath() {
    return path;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return "UriSettings{" +
        "path='" + path + '\'' +
        ", url='" + url + '\'' +
        '}';
  }

  public String getResolvedUri(HttpExtensionClient client, String basePath,
                               HttpRequesterRequestBuilder requestBuilder) {
    if (url == null) {
      UriParameters uriParameters = client.getDefaultUriParameters();
      String resolvedPath = requestBuilder.replaceUriParams(buildPath(basePath, path));
      return resolveUri(uriParameters.getScheme(), uriParameters.getHost().trim(), uriParameters.getPort(), resolvedPath);
    } else {
      return requestBuilder.replaceUriParams(url);
    }
  }

  private String resolveUri(HttpConstants.Protocol scheme, String host, Integer port, String path) {
    // Encode spaces to generate a valid HTTP request.
    return scheme.getScheme() + "://" + host + ":" + port + encodeSpaces(path);
  }

  protected String buildPath(String basePath, String path) {
    String resolvedBasePath = basePath;
    String resolvedRequestPath = path;

    if (!resolvedBasePath.startsWith("/")) {
      resolvedBasePath = "/" + resolvedBasePath;
    }

    if (resolvedBasePath.endsWith("/") && resolvedRequestPath.startsWith("/")) {
      resolvedBasePath = resolvedBasePath.substring(0, resolvedBasePath.length() - 1);
    }

    if (!resolvedBasePath.endsWith("/") && !resolvedRequestPath.startsWith("/") && !resolvedRequestPath.isEmpty()) {
      resolvedBasePath += "/";
    }

    return resolvedBasePath + resolvedRequestPath;
  }
}
