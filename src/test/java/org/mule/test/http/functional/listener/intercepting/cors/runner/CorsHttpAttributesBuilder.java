/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.listener.intercepting.cors.runner;

import org.mule.modules.cors.attributes.KernelTestAttributesBuilder;
import org.mule.test.http.functional.listener.intercepting.cors.parameters.CorsHttpParameters;

public class CorsHttpAttributesBuilder extends KernelTestAttributesBuilder<CorsHttpParameters> {

  @Override
  public CorsHttpParameters build() {
    return new CorsHttpParameters(method, headers);
  }
}
