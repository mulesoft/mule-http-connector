package org.mule.extension.http.internal.listener.intercepting.cors;

import org.mule.extension.http.internal.listener.intercepting.InterceptorException;
import org.mule.runtime.http.api.HttpConstants;

import java.util.Map;

public class RequestInterruptedException extends InterceptorException {

  public RequestInterruptedException(HttpConstants.HttpStatus status, Map<String, String> headers) {
    super(status, headers);
  }
}
