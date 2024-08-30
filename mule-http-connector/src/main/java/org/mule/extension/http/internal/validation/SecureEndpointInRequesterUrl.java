/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.validation;

import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsIdentifier;
import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsNamespace;
import static org.mule.runtime.ast.api.validation.Validation.Level.WARN;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.ast.api.ComponentParameterAst;
import org.mule.runtime.ast.api.validation.Validation;
import org.mule.runtime.ast.api.validation.ValidationResultItem;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SecureEndpointInRequesterUrl implements Validation {

  private final ComponentIdentifier HTTP_REQUEST_CONNECTION =
      ComponentIdentifier.builder().namespace("http").name("request").build();

  @Override
  public String getName() {
    return "HTTPS configured";
  }

  @Override
  public String getDescription() {
    return "HTTPS should be configured for http endpoints";
  }

  @Override
  public Level getLevel() {
    return WARN;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(equalsNamespace("http")
        .and(equalsIdentifier(HTTP_REQUEST_CONNECTION))
        .and(c -> {
          final ComponentParameterAst urlParameter = urlParam(c);
          return urlParameter != null && urlParameter.getRawValue() != null;
        }));
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    final String urlParameter = urlParam(component).getRawValue();

    // new UrlValidator().isValid(urlParameter);
    try {
      if (!"https".equals(new URI(urlParameter).getScheme())) {
        return of(ValidationResultItem.create(component, urlParam(component), this,
                                              "Connection is not secure. Should use `https`"));
      }
    } catch (URISyntaxException e) {
      // this is validated in RequesterUrlValid
      return empty();
    }

    return empty();
  }

  private ComponentParameterAst urlParam(ComponentAst component) {
    return component.getParameter("URI Settings", "url");
  }

}
