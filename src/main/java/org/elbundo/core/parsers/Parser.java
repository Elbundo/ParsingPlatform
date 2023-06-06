package org.elbundo.core.parsers;

public interface Parser {
    void start();
    void next(ParsingStep step);
    void join();
}
