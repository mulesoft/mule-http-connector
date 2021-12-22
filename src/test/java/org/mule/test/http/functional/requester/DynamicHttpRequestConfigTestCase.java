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
import static org.mule.runtime.api.message.Message.of;
import static org.mule.runtime.core.api.event.CoreEvent.builder;

import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.event.CoreEvent;
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

  @Test
  public void sameInstancesForSamePayload() throws Exception {
    ConfigurationProvider configurationProvider = registry.<ConfigurationProvider>lookupByName("configDependingOnPayload").get();
    assertThat(configurationProvider, is(not(nullValue())));

    CoreEvent ev1 = builder(testEvent()).message(of("Same Payload")).build();
    CoreEvent ev2 = builder(testEvent()).message(of("Same Payload")).build();

    ConfigurationInstance config1 = configurationProvider.get(ev1);
    ConfigurationInstance config2 = configurationProvider.get(ev2);

    assertThat(config1.getValue(), is(sameInstance(config2.getValue())));
  }

  @Test
  public void differentInstancesForNonEquivalentValues() throws Exception {
    ConfigurationProvider configurationProvider = registry.<ConfigurationProvider>lookupByName("configDependingOnPayload").get();
    assertThat(configurationProvider, is(not(nullValue())));

    CoreEvent ev1 = builder(testEvent()).message(of("First Payload")).build();
    CoreEvent ev2 = builder(testEvent()).message(of("Second Payload")).build();

    ConfigurationInstance config1 = configurationProvider.get(ev1);
    ConfigurationInstance config2 = configurationProvider.get(ev2);

    assertThat(config1.getValue(), is(not(sameInstance(config2.getValue()))));
  }
}
