/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.security.Constraint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class HttpRequestAuthUtils {

  private HttpRequestAuthUtils() {
  }

  public static AbstractHandler createAuthHandler(Server server, AbstractHandler handler, String realmPath, Runnable callback) {
    LoginService loginService = new HashLoginService("TestRealm", realmPath);
    server.addBean(loginService);

    Constraint basicConstraint = new Constraint();
    basicConstraint.setName("auth");
    basicConstraint.setRoles(new String[] {"user"});
    basicConstraint.setAuthenticate(true);

    ConstraintMapping basicConstraintMapping = new ConstraintMapping();
    basicConstraintMapping.setConstraint(basicConstraint);
    basicConstraintMapping.setPathSpec("/*");

    ConstraintSecurityHandler basicSecurityHandler = new ExecutingCallbackConstraintSecurityHandler(callback);
    basicSecurityHandler.setAuthenticator(new BasicAuthenticator());
    basicSecurityHandler.setConstraintMappings(new ConstraintMapping[] {basicConstraintMapping});

    ContextHandler basicContext = new ContextHandler("/basic");
    basicContext.setHandler(basicSecurityHandler);


    Constraint digestConstraint = new Constraint();
    digestConstraint.setName("auth");
    digestConstraint.setRoles(new String[] {"user"});
    digestConstraint.setAuthenticate(true);

    ConstraintMapping digestConstraintMapping = new ConstraintMapping();
    digestConstraintMapping.setConstraint(digestConstraint);
    digestConstraintMapping.setPathSpec("/*");

    ConstraintSecurityHandler digestSecurityHandler = new ExecutingCallbackConstraintSecurityHandler(callback);
    digestSecurityHandler.setAuthenticator(new DigestAuthenticator());
    digestSecurityHandler.setConstraintMappings(new ConstraintMapping[] {digestConstraintMapping});

    ContextHandler digestContext = new ContextHandler("/digest");
    digestContext.setHandler(digestSecurityHandler);

    basicSecurityHandler.setHandler(handler);
    digestSecurityHandler.setHandler(handler);

    ContextHandlerCollection handlers = new ContextHandlerCollection();
    handlers.setHandlers(new Handler[] {basicContext, digestContext});

    return handlers;
  }

  private static class ExecutingCallbackConstraintSecurityHandler extends ConstraintSecurityHandler {

    private final Runnable callback;

    ExecutingCallbackConstraintSecurityHandler(Runnable callback) {
      this.callback = callback;
    }

    @Override
    public void handle(String pathInContext, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
      callback.run();
      super.handle(pathInContext, baseRequest, request, response);
    }
  }
}
