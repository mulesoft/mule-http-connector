/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.validation;

import static org.mule.runtime.api.meta.model.parameter.ParameterGroupModel.CONNECTION;
import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsIdentifier;
import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsNamespace;
import static org.mule.runtime.ast.api.validation.Validation.Level.WARN;
import static org.mule.runtime.http.api.HttpConstants.Protocol.HTTPS;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.ast.api.ComponentParameterAst;
import org.mule.runtime.ast.api.validation.Validation;
import org.mule.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SecureEndpointInListener implements Validation {

  private final ComponentIdentifier HTTP_LISTENER_CONNECTION =
      ComponentIdentifier.builder().namespace("http").name("listener-connection").build();

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
        .and(equalsIdentifier(HTTP_LISTENER_CONNECTION)));
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst component, ArtifactAst artifact) {
    final ComponentParameterAst protocolParameter = component.getParameter(CONNECTION, "protocol");
    if (!HTTPS.name().equals(protocolParameter.getRawValue())) {
      return of(ValidationResultItem.create(component, protocolParameter, this, "Connection is not secure. Should use `HTTPS`"));
    }

    return empty();
  }

}
