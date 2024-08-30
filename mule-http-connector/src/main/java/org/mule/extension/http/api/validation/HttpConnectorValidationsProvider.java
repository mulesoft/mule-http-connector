/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.validation;

import org.mule.extension.http.internal.validation.ValidateHostUrlRequiredParam;
import org.mule.runtime.ast.api.validation.Validation;
import org.mule.runtime.ast.api.validation.ValidationsProvider;

import java.util.ArrayList;
import java.util.List;

public class HttpConnectorValidationsProvider implements ValidationsProvider {

  @Override
  public List<Validation> get() {
    final List<Validation> validations = new ArrayList<>();

    validations.add(new ValidateHostUrlRequiredParam());

    return validations;
  }

}
