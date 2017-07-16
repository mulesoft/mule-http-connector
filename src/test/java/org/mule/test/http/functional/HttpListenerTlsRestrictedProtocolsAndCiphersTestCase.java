/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.mule.test.http.AllureConstants.HttpFeature.HTTP_EXTENSION;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.HTTPS;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature(HTTP_EXTENSION)
@Story(HTTPS)
public class HttpListenerTlsRestrictedProtocolsAndCiphersTestCase extends AbstractServerTlsRestrictedProtocolsAndCiphersTestCase {

  @Override
  protected String getConfigFile() {
    return "http-listener-restricted-protocols-ciphers-config.xml";
  }
}
