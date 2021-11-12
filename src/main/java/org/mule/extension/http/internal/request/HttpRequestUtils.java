/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.internal.request;

import org.mule.extension.http.api.error.HttpErrorMessageGenerator;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.extension.http.internal.request.profiling.HttpProfilingServiceAdaptor;
import org.mule.extension.http.internal.request.profiling.HttpRequestResponseProfilingDataProducerAdaptor;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.Cursor;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.api.streaming.bytes.CursorStream;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.MuleContext;

public class HttpRequestUtils {

    /**
     * If the body is a {@link Cursor}, we need to change it for the {@link CursorProvider} to re-read the content in the case we
     * need to make a retry of a request.
     */
    public static void handleCursor(HttpRequesterRequestBuilder resolvedBuilder) {
        if (resolvedBuilder.getBody().getValue() instanceof CursorStream) {
            CursorStream cursor = (CursorStream) (resolvedBuilder.getBody().getValue());

            long position = cursor.getPosition();
            CursorStreamProvider provider = (CursorStreamProvider) cursor.getProvider();

            if (position == 0) {
                resolvedBuilder.setBody(new TypedValue<Object>(provider, resolvedBuilder.getBody().getDataType(),
                        resolvedBuilder.getBody().getByteLength()));
            } else {
                resolvedBuilder.setBody(new TypedValue<Object>(new OffsetCursorProviderWrapper(provider, position),
                        resolvedBuilder.getBody().getDataType(),
                        resolvedBuilder.getBody().getByteLength()));
            }
        }
    }

    public static HttpRequester createHttpRequester(boolean httpResponseProfilingEnabled, MuleContext muleContext) throws MuleException{
        return new HttpRequester(new HttpRequestFactory(), new HttpResponseToResult(), new HttpErrorMessageGenerator(), getProfilingDataProducer(httpResponseProfilingEnabled, muleContext));
    }

    private static HttpRequestResponseProfilingDataProducerAdaptor getProfilingDataProducer(boolean httpResponseProfilingEnabled, MuleContext muleContext) throws MuleException {
        if (!httpResponseProfilingEnabled) {
            return null;
        }

        HttpProfilingServiceAdaptor profilingServiceAdaptor = new HttpProfilingServiceAdaptor();

        // Manually inject the profiling service
        muleContext.getInjector().inject(profilingServiceAdaptor);

        return profilingServiceAdaptor.getProfilingHttpRequestDataProducer();
    }

}
