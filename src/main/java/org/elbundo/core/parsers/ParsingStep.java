package org.elbundo.core.parsers;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.elbundo.core.producers.ProducerIn;
import org.elbundo.core.producers.ProducerOut;
import org.elbundo.core.producers.examples.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParsingStep {
    private ProducerIn in;
    private ProducerIn authIn;
    private BiFunction<ProducerIn, ProducerOut, ProducerIn> auth;
    private List<Consumer<ProducerOut>> handlers = new ArrayList<>();
    private Function<ProducerOut, ? extends Result> end;

    public static ParsingStepBuilder builder() {
        return new ParsingStep().new ParsingStepBuilder();
    }

    public class ParsingStepBuilder {
        public ParsingStepBuilder request(ProducerIn in) {
            ParsingStep.this.in = in;
            return this;
        }

        public ParsingStepBuilder authRequest(ProducerIn authIn) {
            ParsingStep.this.authIn = authIn;
            return this;
        }

        public ParsingStepBuilder addHandler(Consumer<ProducerOut> handler) {
            ParsingStep.this.handlers.add(handler);
            return this;
        }

        public ParsingStepBuilder auth(BiFunction<ProducerIn, ProducerOut, ProducerIn> auth) {
            ParsingStep.this.auth = auth;
            return this;
        }

        public ParsingStepBuilder end(Function<ProducerOut, ? extends Result> end) {
            ParsingStep.this.end = end;
            return this;
        }
        public ParsingStep build() {
            return ParsingStep.this;
        }
    }
}
