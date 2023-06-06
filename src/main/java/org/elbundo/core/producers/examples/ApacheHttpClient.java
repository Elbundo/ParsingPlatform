package org.elbundo.core.producers.examples;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.elbundo.core.producers.AbstractFactory;
import org.elbundo.core.producers.Producer;
import org.elbundo.core.producers.ProducerIn;
import org.elbundo.core.producers.ProducerOut;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ApacheHttpClient implements Producer, AutoCloseable {
    private final CloseableHttpClient client;

    public ApacheHttpClient() {
        client = HttpClients.custom()
                .setUserAgent("")
                .build();
    }

    public ApacheHttpClient(String host, int port) {
        client = HttpClients.custom()
                .setUserAgent("")
                //.setProxy(new HttpHost(host, port))
                .build();
    }

    public static AbstractFactory<Producer> getFactory() {
        return null;
    }

    @Override
    public ProducerOut execute(ProducerIn in) {
        try {
            Response response = client.execute(Request.toRequest((Request) in), Response::toResponse);
            response.setRequest((Request) in);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        client.close();
    }
}
