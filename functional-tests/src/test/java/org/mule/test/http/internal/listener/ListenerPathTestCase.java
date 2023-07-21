/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.http.internal.listener;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.mule.extension.http.internal.listener.ListenerPath;
import org.mule.tck.junit4.AbstractMuleTestCase;

import org.junit.Test;

public class ListenerPathTestCase extends AbstractMuleTestCase {

  @Test
  public void relativePathGivenNullBasePath() {
    ListenerPath listenerPath = new ListenerPath(null, "/ignored");
    String relativePath = listenerPath.getRelativePath("/relative/path");
    assertThat(relativePath, is("/relative/path"));
  }

  @Test
  public void relativePathGivenEmptyBasePath() {
    ListenerPath listenerPath = new ListenerPath("", "/ignored");
    String relativePath = listenerPath.getRelativePath("/relative/path");
    assertThat(relativePath, is("/relative/path"));
  }

  @Test
  public void relativePathGivenSlashBasePath() {
    ListenerPath listenerPath = new ListenerPath("/", "/ignored");
    String relativePath = listenerPath.getRelativePath("/relative/path");
    assertThat(relativePath, is("/relative/path"));
  }

  @Test
  public void relativePathGivenSomeBasePath() {
    ListenerPath listenerPath = new ListenerPath("/some", "/ignored");
    String relativePath = listenerPath.getRelativePath("/some/relative/path");
    assertThat(relativePath, is("/relative/path"));
  }

  @Test
  public void relativePathGivenABasePathRepeatedInTheRelativePath() {
    ListenerPath listenerPath = new ListenerPath("/path", "/ignored");
    String relativePath = listenerPath.getRelativePath("/path/relative/path");
    assertThat(relativePath, is("/relative/path"));
  }

  @Test
  public void basePathInTheMiddle() {
    ListenerPath listenerPath = new ListenerPath("/middle", "/ignored");
    String relativePath = listenerPath.getRelativePath("/path/middle/path");
    assertThat(relativePath, is("/path/middle/path"));
  }
}
