package org.elbundo.core.producers;

public interface AbstractFactory<T> {
    T create();
    T create(String host, int port);
}
