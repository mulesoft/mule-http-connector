/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.listener.intercepting.cors;

import static org.mule.runtime.http.api.HttpConstants.HttpStatus.ACCEPTED;
import static org.mule.runtime.http.api.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;

import org.mule.extension.http.internal.listener.intercepting.Interception;
import org.mule.extension.http.internal.listener.intercepting.NoInterception;
import org.mule.extension.http.internal.listener.intercepting.RequestInterruptedException;
import org.mule.modules.cors.response.AddCorsHeaders;
import org.mule.modules.cors.response.BlockRequest;
import org.mule.modules.cors.response.CorsAction;
import org.mule.modules.cors.response.NoCorsHeaders;
import org.mule.modules.cors.response.PreflightAction;
import org.mule.modules.cors.response.visitor.CorsResponseVisitor;
import org.mule.runtime.api.util.MultiMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates an {@link Interception} from a {@link CorsAction}
 */
public class CorsInterceptionFactory implements CorsResponseVisitor<Interception> {

  public Interception from(CorsAction action) {
    return action.accept(this);
  }

  @Override
  public Interception visit(BlockRequest blockRequest) {
    throw new RequestInterruptedException(ACCEPTED, new MultiMap<>());
  }

  @Override
  public Interception visit(NoCorsHeaders noCorsHeaders) {
    return new NoInterception();
  }

  @Override
  public Interception visit(AddCorsHeaders addCorsHeaders) {
    return new AddHeadersInterception(addCorsHeaders);
  }

  @Override
  public Interception visit(PreflightAction preflightAction) {
    Map<String, String> headers = preflightAction.headers();
    headers.put(ACCESS_CONTROL_ALLOW_ORIGIN, preflightAction.origin());
    throw new RequestInterruptedException(ACCEPTED, new MultiMap<>(headers));
  }
}
