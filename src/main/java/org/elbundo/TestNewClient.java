package org.elbundo;

import java.net.URISyntaxException;

public class TestNewClient {
    public static void main(String[] args) {
//        List<Proxy> proxyList = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            proxyList.add(new Proxy());
//        }
//        MyProductShop myProductShop = new MyProductShop(() -> 0L, proxyList,  ApacheHttpClient.getFactory());
        MyProductShop myProductShop = new MyProductShop(15);
        long start = System.currentTimeMillis();
        myProductShop.start();
        myProductShop.join();
        long end = System.currentTimeMillis();
        System.out.println(myProductShop.count.get());
        System.out.println(myProductShop.atomicLong.get());
        System.out.println("Time: " + (end - start));
    }
}
