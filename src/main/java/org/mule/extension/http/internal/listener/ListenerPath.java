/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import static org.mule.runtime.api.util.Preconditions.checkArgument;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

public class ListenerPath {

  private static final Logger LOGGER = getLogger(ListenerPath.class);

  private final String basePath;
  private final String resolvedPath;

  public ListenerPath(String basePath, String listenerPath) {
    this.basePath = basePath;
    this.resolvedPath = basePath == null ? listenerPath : pathWithoutEndSlash(basePath) + listenerPath;
  }

  public String getResolvedPath() {
    return resolvedPath;
  }

  public String getRelativePath(String requestPath) {
    checkArgument(requestPath.startsWith("/"), "requestPath must start with '/'");

    if (isEmptyBasePath()) {
      return requestPath;
    }

    String basePathWithoutEndSlash = pathWithoutEndSlash(basePath);
    if (!requestPath.startsWith(basePathWithoutEndSlash)) {
      LOGGER.warn("Request path '{}' doesn't start with base path '{}'", requestPath, basePath);
      return requestPath;
    }

    return requestPath.substring(basePathWithoutEndSlash.length());
  }

  private String pathWithoutEndSlash(String path) {
    if (path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    } else {
      return path;
    }
  }

  private boolean isEmptyBasePath() {
    return basePath == null || basePath.isEmpty() || basePath.equals("/");
  }
}
