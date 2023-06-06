package org.elbundo.core.producers.examples;


import lombok.*;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.elbundo.core.producers.ProducerIn;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Request implements ProducerIn {
    @NonNull
    private String url;
    @Builder.Default
    private Method method = Method.GET;
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    @Builder.Default
    private String Cookie = "";
    @Builder.Default
    private String body = "";

    public static ClassicHttpRequest toRequest(Request request) {
        ClassicRequestBuilder builder;
        switch (request.getMethod()) {
            case GET -> builder = ClassicRequestBuilder.get();
            case POST -> builder = ClassicRequestBuilder.post().setEntity(request.getBody());
            case PUT -> builder = ClassicRequestBuilder.put().setEntity(request.getBody());
            case PATCH -> builder = ClassicRequestBuilder.patch().setEntity(request.getBody());
            case DELETE -> builder = ClassicRequestBuilder.delete();
            default -> throw new RuntimeException("Very bad");
        }
        builder.setUri(request.getUrl());
        for(Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        builder.setHeader("Cookie", request.getCookie());
        return builder.build();
    }
}


