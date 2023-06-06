package org.elbundo.core.completers;

import org.elbundo.core.parsers.Result;

@FunctionalInterface
public interface Completer {
    void complete(Result result);
}
