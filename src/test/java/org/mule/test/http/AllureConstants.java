/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http;

public interface AllureConstants {

  interface HttpFeature {

    String HTTP_EXTENSION = "HTTP Extension";

    interface HttpStory {

      String CONTENT = "Content Type";
      String ERRORS = "Errors";
      String ERROR_HANDLING = "Error Handling";
      String ERROR_MAPPINGS = "Error Mappings";
      String METADATA = "Metadata";
      String MULTI_MAP = "Multi Map";
      String PROXY_CONFIG_BUILDER = "Proxy Config Builder";
      String REQUEST_BUILDER = "Request Builder";
      String REQUEST_URL = "Request URL";
      String REQUEST_CONFIG = "Request Configuration";
      String RESPONSE_BUILDER = "Response Builder";
      String STREAMING = "Streaming";
      String TCP_BUILDER = "TCP Builders";
      String URL_ENCODED = "URL Encoded";
      String MULTIPART = "Multipart";
      String HTTPS = "HTTPS";
      String PROXY = "Proxy";
      String NTLM = "NTLM";
      String CORS = "Cross Origin Resource Sharing";
      String TIMEOUT = "Timeout";
      String STATIC_RESOURCE_LOADER = "Static Resource Loader";
      String POLICY_SUPPORT = "Policy Support";
      String RETRY_POLICY = "Retry Policy";
    }

  }
}
