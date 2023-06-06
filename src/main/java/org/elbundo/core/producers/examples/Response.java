package org.elbundo.core.producers.examples;

import lombok.Data;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.elbundo.core.producers.ProducerOut;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Data
public class Response implements ProducerOut {
    private Request request;
    private Integer code;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public static Response toResponse(ClassicHttpResponse res) throws IOException, ParseException {
        Response response = new Response();
        response.setCode(res.getCode());
        if (res.getCode() == 404) {
            throw new NoHttpResponseException("Not found");
        }
        for (Header header : res.getHeaders()) {
            response.addHeader(header.getName(), header.getValue());
        }
        HttpEntity entity = res.getEntity();
        response.setBody(EntityUtils.toString(entity, StandardCharsets.UTF_8));
        EntityUtils.consume(entity);
        return response;
    }
}