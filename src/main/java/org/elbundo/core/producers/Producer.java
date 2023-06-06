package org.elbundo.core.producers;

import java.io.IOException;

@FunctionalInterface
public interface Producer{
    ProducerOut execute(ProducerIn in);
}
