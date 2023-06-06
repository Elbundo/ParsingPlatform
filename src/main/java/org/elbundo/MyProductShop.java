package org.elbundo;


import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.elbundo.core.completers.example.CSVCompleter;
import org.elbundo.core.producers.AbstractFactory;
import org.elbundo.core.parsers.ParsingStep;
import org.elbundo.core.producers.ProducerOut;
import org.elbundo.core.producers.examples.Request;
import org.elbundo.core.producers.examples.Response;
import org.elbundo.core.producers.Producer;
import org.elbundo.core.parsers.AbstractParser;
import org.elbundo.core.producers.examples.Proxy;
import org.elbundo.model.Product;
import org.elbundo.model.Store;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements    ;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class MyProductShop extends AbstractParser {
    private final String URL = "http://localhost:8080";
    public final AtomicInteger count = new AtomicInteger(0);
    public MyProductShop(Supplier<Long> delay, List<Proxy> proxyList, AbstractFactory<Producer> factory) {
        super(delay, proxyList, factory);
        setCompleter(new CSVCompleter("data.txt"));
    }
    public MyProductShop(Supplier<Long> delay, AbstractFactory<Producer> factory) {
        super(delay, factory);
        setCompleter(new CSVCompleter("data.txt"));
    }

    public MyProductShop(int threadCount) {
        super(threadCount);
        setCompleter(new CSVCompleter("data.txt"));
    }

    @Override
    public void start() {
        next(ParsingStep.builder()
                .request(Request.builder()
                        .url(URL)
                        .build())
                .addHandler(this::getCategories)
                .build());
    }

    public void getCategories(ProducerOut response) {
        Document doc = Jsoup.parse(response.getBody());
        Elements items = doc.getElementsByClass("cat-card");
        for (Element item : items) {
            next(
                    ParsingStep.builder()
                            .request(Request.builder()
                                    .url(URL + item.attr("href") + "&page=1")
                                    .build())
                            .addHandler(this::getProducts)
                            .addHandler(this::nextPage)
                            .build()
            );
        }

    }

    public void getProducts(ProducerOut response) {
        Document doc = Jsoup.parse(response.getBody());
        Elements items = doc.getElementsByClass("product-card");
        for (Element item : items) {
            int id = Integer.parseInt(item
                    .getElementsByClass("bottom-panel").get(0)
                    .getElementsByTag("form").get(0)
                    .getElementsByTag("input").get(0)
                    .attr("value"));
            next(
                    ParsingStep.builder()
                            .request(Request.builder()
                                    .url(URL + "/product?id=" + id)
                                    .build())
                            .end(this::get)
                            .build()
            );
        }
    }

    public void nextPage(ProducerOut producerOut) {
        Response response = (Response) producerOut;
        URIBuilder builder = new URIBuilder();
        URI uri = URI.create(response.getRequest().getUrl());
        String url = null;
        try {
            url = builder.setScheme(uri.getScheme())
                    .setHost(uri.getHost())
                    .setPort(8080)
                    .appendPath(uri.getPath())
                    .addParameters(Arrays.stream(uri.getQuery().split("&")).map(pair -> {
                        String[] parts = pair.split("=");
                        if (parts[0].equals("page")) {
                            return new BasicNameValuePair(parts[0], Integer.toString(Integer.parseInt(parts[1]) + 1));
                        }
                        return (NameValuePair) new BasicNameValuePair(parts[0], parts[1]);
                    }).toList()).build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        next(ParsingStep.builder()
                .request(Request.builder()
                        .url(url)
                        .build())
                .addHandler(this::getProducts)
                .addHandler(this::nextPage)
                .build());
    }


    @Override
    public Product get(ProducerOut response) {
        Product.ProductBuilder builder = Product.builder();
        Document doc = Jsoup.parse(response.getBody());
        builder.title(doc.getElementsByClass("product-title").get(0).text());
        builder.desc(doc.getElementsByClass("product-desc").get(0).text());
        builder.brand(doc.getElementsByClass("product-brand").get(0).text());
        builder.price(Long.parseLong(doc.getElementsByClass("product-price").get(0).text()));
        builder.calories(Long.parseLong(doc.getElementsByClass("product-calories").get(0).text()));
        builder.fats(Long.parseLong(doc.getElementsByClass("product-fats").get(0).text()));
        builder.proteins(Long.parseLong(doc.getElementsByClass("product-proteins").get(0).text()));
        builder.carbohydrates(Long.parseLong(doc.getElementsByClass("product-carbohydrates").get(0).text()));
        Elements stores = doc.getElementsByClass("product-stores");
        List<Store> list = new ArrayList<>();
        for (Element store : stores) {
            list.add(
                    Store.builder()
                            .id(0)
                            .inNal(store.getElementsByClass("store-inNal").get(0).text())
                            .timeWork(store.getElementsByClass("store-timeWork").get(0).text())
                            .phone(store.getElementsByClass("store-phone").get(0).text())
                            .build()
            );
        }
        builder.stores(list);
        count.incrementAndGet();
        return builder.build();
    }
}
