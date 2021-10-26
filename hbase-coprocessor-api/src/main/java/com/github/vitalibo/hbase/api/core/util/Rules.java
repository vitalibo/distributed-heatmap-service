package com.github.vitalibo.hbase.api.core.util;

import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Rules<T> {

    private final List<Rule<HttpRequest>> preRules;
    private final List<Rule<T>> postRules;

    public Rules() {
        this(Collections.emptyList(), Collections.emptyList());
    }

    public void verify(HttpRequest request) {
        verify(preRules, request);
    }

    public void verify(T request) {
        verify(postRules, request);
    }

    private static <T> void verify(List<Rule<T>> rules, T request) {
        final ErrorState errorState = new ErrorState();
        rules.forEach(rule -> rule.accept(request, errorState));
        if (errorState.hasErrors()) {
            throw new ValidationException(errorState);
        }
    }

    public static <T> Rules<T> lazy(List<Supplier<Rule<HttpRequest>>> preRules, List<Supplier<Rule<T>>> postRules) {
        return new Rules<>(
            preRules.stream()
                .map(Supplier::get)
                .collect(Collectors.toList()),
            postRules.stream()
                .map(Supplier::get)
                .collect(Collectors.toList()));
    }

}
