/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional.requester.crl;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCause;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mule.runtime.config.api.SpringXmlConfigurationBuilderFactory.createConfigurationBuilder;
import static org.mule.runtime.core.api.config.MuleProperties.OBJECT_POLICY_PROVIDER;
import static org.mule.runtime.core.api.config.bootstrap.ArtifactType.APP;
import static org.mule.runtime.core.api.context.notification.MuleContextNotification.CONTEXT_STARTED;
import static org.mule.runtime.core.privileged.security.tls.TlsConfiguration.formatInvalidCrlAlgorithm;
import static org.mule.runtime.module.extension.api.loader.AbstractJavaExtensionModelLoader.TYPE_PROPERTY_NAME;
import static org.mule.runtime.module.extension.api.loader.AbstractJavaExtensionModelLoader.VERSION;

import org.mule.extension.http.internal.temporary.HttpConnector;
import org.mule.extension.socket.api.SocketsExtension;
import org.mule.functional.services.NullPolicyProvider;
import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.CreateException;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.notification.IntegerAction;
import org.mule.runtime.api.notification.NotificationListenerRegistry;
import org.mule.runtime.api.util.concurrent.Latch;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationBuilder;
import org.mule.runtime.core.api.config.builders.AbstractConfigurationBuilder;
import org.mule.runtime.core.api.config.builders.SimpleConfigurationBuilder;
import org.mule.runtime.core.api.context.DefaultMuleContextFactory;
import org.mule.runtime.core.api.context.MuleContextBuilder;
import org.mule.runtime.core.api.context.MuleContextFactory;
import org.mule.runtime.core.api.context.notification.MuleContextNotification;
import org.mule.runtime.core.api.context.notification.MuleContextNotificationListener;
import org.mule.runtime.core.api.extension.ExtensionManager;
import org.mule.runtime.module.extension.api.loader.java.DefaultJavaExtensionModelLoader;
import org.mule.tck.config.TestServicesConfigurationBuilder;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class HttpInvalidCrlAlgorithmTestCase extends AbstractMuleTestCase {

  @Inject
  private NotificationListenerRegistry notificationListenerRegistry;

  @Test
  public void testInvalidCrlAlgorithm() throws MuleException, InterruptedException {
    try {
      loadConfiguration("http-requester-tls-crl-invalid-algorithm-config.xml");
    } catch (Exception e) {
      Throwable rootCause = getRootCause(e);
      assertThat(rootCause, instanceOf(CreateException.class));
      assertThat(rootCause.getMessage(), is(formatInvalidCrlAlgorithm("SunX509")));
    }
  }

  private void loadConfiguration(String configuration) throws MuleException, InterruptedException {
    MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
    List<ConfigurationBuilder> builders = new ArrayList<>();
    builders.add(new AbstractConfigurationBuilder() {

      @Override
      protected void doConfigure(MuleContext muleContext) throws Exception {
        ExtensionManager defaultExtensionManager = mock(ExtensionManager.class);
        getRequiredExtensions().forEach(defaultExtensionManager::registerExtension);
        muleContext.setExtensionManager(defaultExtensionManager);
      }
    });
    builders.add(createConfigurationBuilder(configuration));
    builders.add(new TestServicesConfigurationBuilder());
    builders.add(new SimpleConfigurationBuilder(singletonMap(OBJECT_POLICY_PROVIDER, new NullPolicyProvider())));
    MuleContextBuilder contextBuilder = MuleContextBuilder.builder(APP);
    MuleContext muleContext = muleContextFactory.createMuleContext(builders, contextBuilder);
    final AtomicReference<Latch> contextStartedLatch = new AtomicReference<>();
    contextStartedLatch.set(new Latch());
    notificationListenerRegistry.registerListener(new MuleContextNotificationListener<MuleContextNotification>() {

      @Override
      public boolean isBlocking() {
        return false;
      }

      @Override
      public void onNotification(MuleContextNotification notification) {
        if (new IntegerAction(CONTEXT_STARTED).equals(notification.getAction())) {
          contextStartedLatch.get().countDown();
        }
      }
    });
    muleContext.start();
    contextStartedLatch.get().await(20, SECONDS);
  }

  private List<ExtensionModel> getRequiredExtensions() {
    ExtensionModel sockets = loadExtension(SocketsExtension.class, emptySet());
    ExtensionModel http = loadExtension(HttpConnector.class, singleton(sockets));
    return asList(http, sockets);
  }

  private ExtensionModel loadExtension(Class extension, Set<ExtensionModel> deps) {
    DefaultJavaExtensionModelLoader loader = new DefaultJavaExtensionModelLoader();
    Map<String, Object> ctx = new HashMap<>();
    ctx.put(TYPE_PROPERTY_NAME, extension.getName());
    ctx.put(VERSION, "1.0.0-SNAPSHOT");
    return loader.loadExtensionModel(currentThread().getContextClassLoader(), DslResolvingContext.getDefault(deps), ctx);
  }
}
