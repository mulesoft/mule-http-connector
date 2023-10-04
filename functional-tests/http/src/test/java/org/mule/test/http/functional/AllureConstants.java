/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

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
      String REQUEST_URL = "Request URL";
      String REQUEST_CONFIG = "Request Configuration";
      String STREAMING = "Streaming";
      String URL_ENCODED = "URL Encoded";
      String MULTIPART = "Multipart";
      String HTTPS = "HTTPS";
      String PROXY = "Proxy";
      String NTLM = "NTLM";
      String TIMEOUT = "Timeout";
      String STATIC_RESOURCE_LOADER = "Static Resource Loader";
      String RETRY_POLICY = "Retry Policy";
      String PROFILING = "Profiling";
      String POLLING_SOURCE = "Polling Source";
      String CERTIFICATE_REVOCATION = "Certificate Revocation Check";
      String HEADER_CASE_PRESERVATION = "Header Case Preservation";
      String SOURCE_OVERLOAD = "Source overload handling";
      String MULTI_VALUES_IN_POLICIES = "Multi-values preservation in policy";
    }

  }
}
