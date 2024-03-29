/*
 * Copyright 2007-2021, CIIC Guanaitong, Co., Ltd.
 * All rights reserved.
 */

package com.ciicgat.sdk.util.http.trace;

import com.ciicgat.grus.core.Module;
import com.ciicgat.grus.performance.SlowLogger;
import com.ciicgat.sdk.trace.SpanUtil;
import com.ciicgat.sdk.trace.Spans;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopSpan;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Created by August.Zhou on 2018/8/22 18:01.
 */
public class OkhttpTracingInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger("OkHttpClient");

    private OkHttpClientSpanDecorator decorator = OkHttpClientSpanDecorator.STANDARD_TAGS;


    @Override
    public Response intercept(Chain chain) throws IOException {

        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        Response response = null;

        Tracer tracer = GlobalTracer.get();

        Span rootSpan = Spans.getRootSpan();
        final Span span = tracer.buildSpan(originalRequest.method())
                .asChildOf(rootSpan == NoopSpan.INSTANCE ? null : rootSpan)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.COMPONENT.getKey(), "okhttp")
                .start();
        Spans.setSystemTags(span);

        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new RequestBuilderInjectAdapter(requestBuilder));

        Request newRequest = requestBuilder.build();
        decorator.onRequest(originalRequest, span);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Sending request {} on {} {}", originalRequest.url(), chain.connection(), originalRequest.headers());
        }
        try {
            response = chain.proceed(newRequest);
            decorator.onResponse(response, span);
        } catch (Throwable ex) {
            decorator.onError(ex, span);
            throw ex;
        } finally {
            span.finish();
            long durationMilliSeconds = SpanUtil.getDurationMilliSeconds(span);
            SlowLogger.logEvent(Module.HTTP_CLIENT, durationMilliSeconds, originalRequest.toString());
            if (LOGGER.isDebugEnabled()) {
                if (response != null) {
                    LOGGER.debug("Received response for {} on {}", response.request().url(), response.headers());
                }
            }
        }


        return response;
    }

}

