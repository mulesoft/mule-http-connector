/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.temporary;

import static org.mule.extension.http.internal.listener.HttpListener.HTTP_NAMESPACE;
import org.mule.extension.http.api.error.HttpError;
import org.mule.extension.http.api.listener.server.HttpListenerConfig;
import org.mule.extension.http.api.notification.HttpNotificationAction;
import org.mule.extension.http.api.notification.HttpNotificationData;
import org.mule.extension.http.api.policy.HttpPolicyRequestAttributes;
import org.mule.extension.http.api.request.authentication.BasicAuthentication;
import org.mule.extension.http.api.request.authentication.DigestAuthentication;
import org.mule.extension.http.api.request.authentication.HttpRequestAuthentication;
import org.mule.extension.http.api.request.authentication.NtlmAuthentication;
import org.mule.extension.http.api.request.proxy.DefaultNtlmProxyConfig;
import org.mule.extension.http.api.request.proxy.DefaultProxyConfig;
import org.mule.extension.http.api.request.proxy.HttpProxyConfig;
import org.mule.extension.http.api.request.validator.ExpressionResponseValidator;
import org.mule.extension.http.api.request.validator.FailureStatusCodeValidator;
import org.mule.extension.http.api.request.validator.ResponseValidator;
import org.mule.extension.http.api.request.validator.SuccessStatusCodeValidator;
import org.mule.extension.http.internal.HttpOperations;
import org.mule.extension.http.internal.request.HttpRequesterConfig;
import org.mule.extension.socket.api.socket.tcp.TcpClientSocketProperties;
import org.mule.extension.socket.api.socket.tcp.TcpServerSocketProperties;
import org.mule.modules.cors.api.configuration.origin.EveryOrigin;
import org.mule.modules.cors.api.configuration.origin.Origin;
import org.mule.modules.cors.api.configuration.origin.SingleOrigin;
import org.mule.runtime.extension.api.annotation.*;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.notification.NotificationActions;

/**
 * HTTP connector used to handle and perform HTTP requests.
 * <p>
 * This class only serves as an extension definition. It's configurations are divided on server ({@code <http:listener-config>})
 * and client ({@code <http:requester-config>}) capabilities.
 *
 * @since 1.0
 */
@Extension(name = "HTTP")
@Configurations({HttpListenerConfig.class, HttpRequesterConfig.class})
@Operations(HttpOperations.class)
@SubTypeMapping(baseType = HttpRequestAuthentication.class,
    subTypes = {BasicAuthentication.class, DigestAuthentication.class, NtlmAuthentication.class})
@SubTypeMapping(baseType = HttpProxyConfig.class, subTypes = {DefaultProxyConfig.class, DefaultNtlmProxyConfig.class})
@SubTypeMapping(baseType = ResponseValidator.class,
    subTypes = {SuccessStatusCodeValidator.class, FailureStatusCodeValidator.class, ExpressionResponseValidator.class})
@SubTypeMapping(baseType = Origin.class, subTypes = {EveryOrigin.class, SingleOrigin.class})
@Import(type = TcpClientSocketProperties.class)
@Import(type = TcpServerSocketProperties.class)
@ErrorTypes(HttpError.class)
@NotificationActions(HttpNotificationAction.class)
@Xml(namespace = "http://www.mulesoft.org/schema/mule/http", prefix = HTTP_NAMESPACE)
// TODO move back to package org.mule.extension.http.internal as part of MULE-10651. Now we are using this package
// because it doesn't work in the former package since the classloader mechanism will try to load the class from another bundle.
@Export(classes = {HttpPolicyRequestAttributes.class, HttpProxyConfig.class, HttpNotificationData.class})
@PrivilegedExport(packages = {
    "org.mule.extension.http.api.request.client",
    "org.mule.extension.http.internal.request",
    "org.mule.extension.http.api.request.builder",
    "org.mule.extension.http.internal.request.client"},
    artifacts = {"com.mulesoft.composer.connectors:mule4-rest-consumer-composer-connector"})
public class HttpConnector {

}
