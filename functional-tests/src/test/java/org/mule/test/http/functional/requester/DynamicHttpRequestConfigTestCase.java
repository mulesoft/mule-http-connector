/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;

import javax.inject.Inject;

import org.junit.Test;

public class DynamicHttpRequestConfigTestCase extends AbstractHttpRequestTestCase {

  @Inject
  private Registry registry;

  @Override
  protected String getConfigFile() {
    return "http-dynamic-requester-config.xml";
  }

  @Test
  public void sameInstanceForEquivalentValues() throws Exception {
    ConfigurationProvider configurationProvider = registry.<ConfigurationProvider>lookupByName("config").get();
    assertThat(configurationProvider, is(not(nullValue())));

    ConfigurationInstance config1 = configurationProvider.get(testEvent());
    ConfigurationInstance config2 = configurationProvider.get(testEvent());

    assertThat(config1.getValue(), is(sameInstance(config2.getValue())));
  }
}
