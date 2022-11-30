/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener;

import org.mule.runtime.core.api.util.StringUtils;

public class ListenerPath {

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
    if (isEmptyBasePath()) {
      return requestPath;
    }
    return requestPath.replaceFirst(pathWithoutEndSlash(basePath), StringUtils.EMPTY);
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
