/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.validation;

import static org.mule.runtime.api.meta.model.parameter.ParameterGroupModel.CONNECTION;
import static org.mule.runtime.api.meta.model.parameter.ParameterGroupModel.DEFAULT_GROUP_NAME;
import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.currentElemement;
import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsComponentId;
import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsIdentifier;
import static org.mule.runtime.ast.api.util.ComponentAstPredicatesFactory.equalsNamespace;
import static org.mule.runtime.ast.api.validation.Validation.Level.ERROR;
import static org.mule.runtime.ast.api.validation.ValidationResultItem.create;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.meta.model.connection.ConnectionProviderModel;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.ast.api.ComponentParameterAst;
import org.mule.runtime.ast.api.validation.Validation;
import org.mule.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class ValidateHostUrlRequiredParam implements Validation {

  private final ComponentIdentifier HTTP_REQUEST_IDENTIFIER =
      ComponentIdentifier.builder().namespace("http").name("request").build();

  @Override
  public String getName() {
    return "Either 'host' or 'url' must be set in 'http:request'";
  }

  @Override
  public String getDescription() {
    return "Either 'host' or 'url' must be set in 'http:request'";
  }

  @Override
  public Level getLevel() {
    return ERROR;
  }

  @Override
  public Predicate<List<ComponentAst>> applicable() {
    return currentElemement(equalsNamespace("http")
        .and(equalsIdentifier(HTTP_REQUEST_IDENTIFIER))
        .and(req -> urlParam(req) == null
            || urlParam(req).getRawValue() == null));
  }

  @Override
  public Optional<ValidationResultItem> validate(ComponentAst httpRequest, ArtifactAst artifact) {
    final String configName = httpRequest.getParameter(DEFAULT_GROUP_NAME, "config-ref").getRawValue();
    final Optional<ComponentAst> reqConfigOptional = artifact.topLevelComponents()
        .stream()
        .filter(equalsComponentId(configName))
        .findAny();

    if (!reqConfigOptional.isPresent()) {
      return empty();
    }

    ComponentAst reqConfig = reqConfigOptional.get();
    final Optional<ComponentAst> connProviderOptional = reqConfig.directChildren().stream()
        .filter(ch -> ch.getModel(ConnectionProviderModel.class).isPresent())
        .findAny();

    if (!connProviderOptional.isPresent()) {
      return of(create(asList(httpRequest, reqConfig),
                       asList(urlParam(httpRequest)),
                       this,
                       "`http:request` does not have `url`, and its config `" + configName
                           + "` does not have a `http:request-connection`"));
    }

    ComponentAst connProvider = connProviderOptional.get();
    final ComponentParameterAst connHostParam = connProvider.getParameter(CONNECTION, "host");
    if (connHostParam == null || connHostParam.getRawValue() == null) {
      return of(create(asList(httpRequest, connProvider),
                       asList(urlParam(httpRequest), connHostParam),
                       this,
                       "`http:request` does not have `url`, and its config `" + configName
                           + "` does not have `host`"));
    }

    return empty();
  }

  private ComponentParameterAst urlParam(ComponentAst httpRequest) {
    return httpRequest.getParameter("URI Settings", "url");
  }

}
