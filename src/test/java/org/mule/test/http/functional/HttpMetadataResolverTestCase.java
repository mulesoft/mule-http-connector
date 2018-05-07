/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.mule.runtime.api.component.location.Location.builder;
import static org.mule.test.http.AllureConstants.HttpFeature.HttpStory.METADATA;

import org.mule.metadata.api.model.AnyType;
import org.mule.metadata.api.model.BinaryType;
import org.mule.runtime.api.meta.model.OutputModel;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.meta.model.source.SourceModel;
import org.mule.runtime.api.metadata.MetadataService;
import org.mule.runtime.api.metadata.descriptor.ComponentMetadataDescriptor;
import org.mule.runtime.api.metadata.resolving.MetadataResult;
import org.mule.tck.junit4.rule.DynamicPort;

import javax.inject.Inject;

import io.qameta.allure.Story;
import org.junit.Rule;
import org.junit.Test;

@Story(METADATA)
public class HttpMetadataResolverTestCase extends AbstractHttpTestCase {

  @Inject
  private MetadataService service;

  @Rule
  public DynamicPort serverPort = new DynamicPort("serverPort");

  @Override
  protected String getConfigFile() {
    return "http-metadata-config.xml";
  }

  @Test
  public void resolvesRequestMetadata() {
    MetadataResult<ComponentMetadataDescriptor<OperationModel>> result =
        service.getOperationMetadata(builder().globalName("client").addProcessorsPart().addIndexPart(0).build());
    assertThat(result.isSuccess(), is(true));
    OutputModel request = result.get().getModel().getOutput();
    assertThat(request.getType(), is(instanceOf(AnyType.class)));
  }

  @Test
  public void resolverListenerMetadata() {
    MetadataResult<ComponentMetadataDescriptor<SourceModel>> server =
        service.getSourceMetadata(builder().globalName("server").addSourcePart().build());
    assertThat(server.isSuccess(), is(true));
    assertThat(server.get().getModel().getOutput().getType(), is(instanceOf(AnyType.class)));
  }

}
