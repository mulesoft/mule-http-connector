/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.extensions.http.mock.internal;

import static org.mule.sdk.api.meta.JavaVersion.JAVA_11;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_17;
import static org.mule.sdk.api.meta.JavaVersion.JAVA_8;

import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.sdk.api.annotation.JavaVersionSupport;
import org.mule.test.extensions.http.mock.internal.client.HTTPMockClientConfiguration;
import org.mule.test.extensions.http.mock.internal.client.HTTPMockClientOperations;
import org.mule.test.extensions.http.mock.internal.server.HTTPMockServerConfiguration;


@Xml(prefix = "http-mock")
@Extension(name = "HTTP Mock")
@Configurations({HTTPMockServerConfiguration.class, HTTPMockClientConfiguration.class})
@Operations(HTTPMockClientOperations.class)
@JavaVersionSupport({JAVA_8, JAVA_11, JAVA_17})
public class HTTPMockExtension {

}
