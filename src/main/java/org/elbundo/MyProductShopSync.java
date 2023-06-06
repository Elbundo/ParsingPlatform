package org.elbundo;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.elbundo.model.Product;
import org.elbundo.model.Store;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MyProductShopSync {
    private static final String URL = "http://localhost:8080";
    private static List<Product> list = new ArrayList<>();
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        start();
        long end = System.currentTimeMillis();
        System.out.println(list.size());
        System.out.println("Time: " + (end - start));
    }

    public static void start() {
        String response = get(URL);
        Document doc = Jsoup.parse(response);
        Elements items = doc.getElementsByClass("cat-card");
        for (Element item : items) {
            getProducts(item);
        }
    }

    public static void getProducts(Element item) {
        int page = 1;
        while (true) {
            try{
                String response = get(URL + item.attr("href") + "&page=" + (page++));
                Document doc = Jsoup.parse(response);
                Elements items = doc.getElementsByClass("product-card");
                for (Element item1 : items) {
                    int id = Integer.parseInt(item1.getElementsByClass("bottom-panel").get(0).getElementsByTag("form").get(0).getElementsByTag("input").get(0).attr("value"));
                    getById(id);
                }
            }catch (Exception e) {
                break;
            }
        }
    }

    public static void getById(int id) {
        String response = get(URL + "/product?id=" + id);
        Product product = getProduct(response);
        list.add(product);
    }

    public static Product getProduct(String response) {
        Product.ProductBuilder builder = Product.builder();
        Document doc = Jsoup.parse(response);
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
        return builder.build();
    }

    public static String get(String url) {
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36")
                .build()) {
            ClassicHttpRequest httpGet = ClassicRequestBuilder.get(url)
                    .build();
            return httpclient.execute(httpGet, response -> {
                if (response.getCode() != 200)
                    throw new RuntimeException("");
                final HttpEntity entity1 = response.getEntity();
                String res = EntityUtils.toString(entity1, StandardCharsets.UTF_16);
                EntityUtils.consume(entity1);
                return res;
            });
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}


