package org.elbundo.core.parsers;

import org.elbundo.core.completers.Completer;
import org.elbundo.core.completers.example.CSVCompleter;
import org.elbundo.core.producers.AbstractFactory;
import org.elbundo.core.producers.ProducerIn;
import org.elbundo.core.producers.ProducerOut;
import org.elbundo.core.producers.examples.*;
import org.elbundo.core.producers.Producer;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractParser implements Parser {
    ThreadPoolExecutor pool;// = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    ThreadPoolExecutor pool2 = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    public AtomicLong atomicLong = new AtomicLong(0L);


    ThreadLocal<Producer> httpClientThreadLocal = ThreadLocal.withInitial(ApacheHttpClient::new);

    private final BlockingQueue<Producer> httpClientQueue;
    private final Supplier<Long> delay;
    private final AbstractFactory<Producer> factory;
    private Completer completer;

    public void setCompleter(Completer completer) {
        this.completer = completer;
    };

    protected AbstractParser(Supplier<Long> delayFunc, List<Proxy> proxies, AbstractFactory<Producer> abstractFactory) {
        factory = abstractFactory;
        delay = delayFunc;
        httpClientQueue = new ArrayBlockingQueue<>(proxies.size(), false);
        for (Proxy proxy : proxies) {
            httpClientQueue.add(factory.create(proxy.getHost(), proxy.getPort()));
        }
    }

    protected AbstractParser(Supplier<Long> delayFunc, AbstractFactory<Producer> abstractFactory) {
        factory = abstractFactory;
        delay = delayFunc;
        httpClientQueue = new ArrayBlockingQueue<>(1, false);
        httpClientQueue.add(factory.create());
    }
    private Producer getHttpClient() {
        try {
            return httpClientQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void returnBackHttpClient(Producer client) {
        try {
            long d = delay.get();
            if (d != 0)
                Thread.sleep(d);
            httpClientQueue.put(client);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Result get(ProducerOut response);

    @Override
    public void next(ParsingStep step) {
        Producer httpClient = getHttpClient();
        CompletableFuture<ProducerIn> start = new CompletableFuture<>();
        if (step.getAuth() != null && step.getAuthIn() != null) {
            start = start.thenCombine(CompletableFuture.supplyAsync(() -> httpClient.execute(step.getAuthIn()), pool), step.getAuth());
            atomicLong.incrementAndGet();
        }
        CompletableFuture<ProducerOut> fut = start.thenCompose((request) -> CompletableFuture.supplyAsync(() -> httpClient.execute(request), pool));
        atomicLong.incrementAndGet();
        List<Consumer<ProducerOut>> handlers = step.getHandlers();
        if (handlers.size() > 0)
            fut.thenAccept(handlers.get(0));
        for (int i = 1; i < handlers.size(); i++) {
            fut.thenAcceptAsync(handlers.get(i), pool);
        }
        if (step.getEnd() != null) {
            fut.thenApply(step.getEnd()).thenAccept(completer::complete);
        }
        start.completeAsync(step::getIn, pool);
        CompletableFuture.runAsync(() -> returnBackHttpClient(httpClient));
    }

    @Override
    public void join() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (!pool.getQueue().isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        pool.shutdown();
    }
}
