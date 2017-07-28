/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors.runner;

import org.mule.modules.cors.attributes.CorsTestAttributesBuilder;
import org.mule.test.http.functional.listener.intercepting.cors.parameters.CorsParameters;

public class CorsHttpAttributesBuilder extends CorsTestAttributesBuilder<CorsHttpAttributesBuilder, CorsParameters> {

  @Override
  public CorsParameters build() {
    return new CorsParameters(method, headers);
  }
}
