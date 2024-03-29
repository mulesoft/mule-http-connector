/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.server;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.http.api.domain.request.HttpRequestContext;
import org.mule.runtime.http.api.server.RequestHandler;

import java.io.InputStream;

public interface ModuleRequestHandler extends RequestHandler {

  Result<InputStream, HttpRequestAttributes> createResult(HttpRequestContext requestContext);

}
