package com.github.vitalibo.hbase.api.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@FunctionalInterface
public interface Specification<T> extends BiConsumer<T, Consumer<String>> {

    default Specification<T> and(Specification<T> that) {
        final Specification<T> self = this;
        return (obj, consumer) -> {
            final boolean[] hasError = {false};
            self.accept(obj, error -> {
                hasError[0] = true;
                consumer.accept(error);
            });

            if (!hasError[0]) {
                that.accept(obj, consumer);
            }
        };
    }

    default Specification<T> or(Specification<T> that) {
        final Specification<T> self = this;
        return (obj, consumer) -> {
            final List<String> hiddenError = new ArrayList<>();
            self.accept(obj, hiddenError::add);

            if (hiddenError.isEmpty()) {
                return;
            }

            final boolean[] hasError = {false};
            that.accept(obj, error -> {
                hasError[0] = true;
                consumer.accept(error);
            });

            if (hasError[0]) {
                hiddenError.forEach(consumer);
            }
        };
    }

    default Rule<T> named(String field) {
        return (obj, errorState) -> this.accept(obj, error -> errorState.addError(field, error));
    }

}
