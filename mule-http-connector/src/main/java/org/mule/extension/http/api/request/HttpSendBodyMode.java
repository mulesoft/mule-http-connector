/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.request;

/**
 * Defines if the request should contain a body or not.
 *
 * @since 1.0
 */
public enum HttpSendBodyMode {
  /**
   * Will send a body, regardless of the HTTP method selected.
   */
  ALWAYS,
  /**
   * Will send a body depending on the HTTP method selected (GET, HEAD and OPTIONS will not send a body).
   */
  AUTO,
  /**
   * Will not send a body, regardless of the HTTP method selected.
   */
  NEVER
}
