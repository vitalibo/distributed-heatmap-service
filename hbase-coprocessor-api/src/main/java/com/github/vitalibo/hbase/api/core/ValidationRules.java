package com.github.vitalibo.hbase.api.core;

import com.github.vitalibo.hbase.api.core.util.Specification;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ValidationRules {

    private ValidationRules() {
    }

    public static <T> Specification<T> requiredNotNull() {
        return ValidationRules::requiredNotNull;
    }

    public static <T> void requiredNotNull(T obj, Consumer<String> consumer) {
        if (obj == null) {
            consumer.accept("The required field can't be null");
        }
    }

    public static Specification<String> requiredNotEmpty() {
        return ValidationRules::requiredNotEmpty;
    }

    public static void requiredNotEmpty(String obj, Consumer<String> consumer) {
        if (obj.trim().isEmpty()) {
            consumer.accept("The required field can't be empty");
        }
    }

    public static <T> Specification<T> requiredEqualsTo(T that) {
        return (obj, consumer) -> {
            if (!obj.equals(that)) {
                consumer.accept(String.format("The value is not equal to '%s'", that));
            }
        };
    }

    public static Specification<String> requiredLength(Integer minLength, Integer maxLength) {
        return (obj, consumer) -> {
            if (obj.length() < minLength || obj.length() > maxLength) {
                consumer.accept(String.format(
                    "The field has length constraints: Minimum length of %d. Maximum length of %d",
                    minLength, maxLength));
            }
        };
    }

    public static Specification<String> requiredMatchRegex(String regex) {
        final Pattern pattern = Pattern.compile(regex);

        return (obj, consumer) -> {
            Matcher matcher = pattern.matcher(obj);
            if (!matcher.matches()) {
                consumer.accept(String.format("The field must match to regex '%s'", regex));
            }
        };
    }

    public static Specification<String> requiredAnyMatch(String... regexes) {
        final Set<Pattern> patterns = Arrays.stream(regexes)
            .map(Pattern::compile)
            .collect(Collectors.toSet());

        return (obj, consumer) -> {
            boolean noneMatch = patterns.stream()
                .map(o -> o.matcher(obj))
                .noneMatch(Matcher::matches);

            if (noneMatch) {
                consumer.accept(String.format(
                    "The value is not allowed for field. List allowed values: %s", Arrays.toString(regexes)));
            }
        };
    }

    public static <T extends Number> Specification<T> requiredBetween(T lower, T upper) {
        return (obj, consumer) -> {
            double value = obj.doubleValue();
            if (!(lower.doubleValue() <= value && value <= upper.doubleValue())) {
                consumer.accept(String.format("The value should be between [%s, %s]", lower, upper));
            }
        };
    }

}
