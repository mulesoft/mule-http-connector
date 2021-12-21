/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import static java.lang.Boolean.getBoolean;
import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.compile;
import static org.mule.extension.http.internal.HttpConnectorConstants.ENCODE_URI_PARAMS_PROPERTY;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.http.api.utils.HttpEncoderDecoderUtils.encodeSpaces;

import org.mule.extension.http.api.request.builder.UriParam;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.http.api.HttpConstants;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public final class UriUtils {

  private static final Pattern WRONGLY_ENCODED_SPACES = compile("\\+");
  public static final String HTTP_PATH_DELIMITER = "/";

  private static boolean encodeUriParams = getBoolean(ENCODE_URI_PARAMS_PROPERTY);

  private UriUtils() {
    // Empty private constructor to avoid instantiation.
  }

  public static String replaceUriParams(String path, Map<String, String> uriParams) {
    for (Entry<String, String> entry : uriParams.entrySet()) {
      String uriParamName = entry.getKey();
      String uriParamValue = entry.getValue();
      path = replaceUriParam(path, uriParamName, uriParamValue);
    }
    return path;
  }

  public static String replaceUriParams(String path, List<? extends UriParam> uriParams) {
    for (UriParam entry : uriParams) {
      String uriParamName = entry.getKey();
      String uriParamValue = entry.getValue();
      path = replaceUriParam(path, uriParamName, uriParamValue);
    }
    return path;
  }

  private static String replaceUriParam(String path, String uriParamName, String uriParamValue) {
    if (uriParamValue == null) {
      throw new NullPointerException(format("Expression {%s} evaluated to null.", uriParamName));
    }
    if (encodeUriParams) {
      try {
        uriParamValue = WRONGLY_ENCODED_SPACES.matcher(encode(uriParamValue, UTF_8.displayName()))
            // Spaces in path segments cannot be encoded as `+`
            .replaceAll("%20");
      } catch (UnsupportedEncodingException e) {
        throw new MuleRuntimeException(createStaticMessage("Could not encode URI parameter '%s'", uriParamValue), e);
      }
    }
    path = path.replaceAll("\\{" + uriParamName + "\\}", quoteReplacement(uriParamValue));
    return path;
  }

  public static String buildPath(String basePath, String path) {
    String resolvedBasePath = basePath;
    String resolvedRequestPath = path;

    if (!resolvedBasePath.startsWith(HTTP_PATH_DELIMITER)) {
      resolvedBasePath = HTTP_PATH_DELIMITER + resolvedBasePath;
    }

    if (resolvedBasePath.endsWith(HTTP_PATH_DELIMITER) && resolvedRequestPath.startsWith(HTTP_PATH_DELIMITER)) {
      resolvedBasePath = resolvedBasePath.substring(0, resolvedBasePath.length() - 1);
    }

    if (!resolvedBasePath.endsWith(HTTP_PATH_DELIMITER) && !resolvedRequestPath.startsWith(HTTP_PATH_DELIMITER) && !resolvedRequestPath.isEmpty()) {
      resolvedBasePath += HTTP_PATH_DELIMITER;
    }

    return resolvedBasePath + resolvedRequestPath;
  }

  public static String resolveUri(HttpConstants.Protocol scheme, String host, Integer port, String path) {
    // Encode spaces to generate a valid HTTP request.
    return scheme.getScheme() + "://" + host + ":" + port + encodeSpaces(path);
  }

  public static void refreshSystemProperties() {
    encodeUriParams = getBoolean(ENCODE_URI_PARAMS_PROPERTY);
  }
}
