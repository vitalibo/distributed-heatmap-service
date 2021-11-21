package com.github.vitalibo.heatmap.api.core;

import com.github.vitalibo.heatmap.api.core.model.HeatmapJsonRequest;
import com.github.vitalibo.heatmap.api.core.model.HeatmapRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import com.github.vitalibo.heatmap.api.core.util.Rule;
import com.github.vitalibo.heatmap.api.core.util.Specification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ValidationRules {

    private ValidationRules() {
    }

    public static Rule<HttpRequest> verifyQueryParameterId() {
        Rule<String> rule = ValidationRules.<String>requiredNotNull()
            .and(ValidationRules::requiredNotEmpty)
            .and(ValidationRules.requiredMatchRegex("[0-9]+"))
            .named("id");

        return (request, errorState) ->
            rule.accept(request.getQueryStringParameters().get("id"), errorState);
    }

    public static Rule<HttpRequest> verifyQueryParameterFrom() {
        Rule<String> rule = ValidationRules.<String>requiredNotNull()
            .and(ValidationRules::requiredNotEmpty)
            .and(ValidationRules.requiredStandardISO8601())
            .named("from");

        return (request, errorState) ->
            rule.accept(request.getQueryStringParameters().get("from"), errorState);
    }

    public static Rule<HttpRequest> verifyQueryParameterUntil() {
        Rule<String> rule = ValidationRules.<String>requiredNotNull()
            .and(ValidationRules::requiredNotEmpty)
            .and(ValidationRules.requiredStandardISO8601())
            .named("until");

        return (request, errorState) ->
            rule.accept(request.getQueryStringParameters().get("until"), errorState);
    }

    public static Rule<HttpRequest> verifyQueryParameterRadius() {
        Rule<String> rule = ValidationRules.<String>requiredNotNull()
            .and(ValidationRules::requiredNotEmpty)
            .and(ValidationRules.requiredMatchRegex("[0-9]+"))
            .or(ValidationRules::possibleNull)
            .named("radius");

        return (request, errorState) ->
            rule.accept(request.getQueryStringParameters().get("radius"), errorState);
    }

    public static Rule<HttpRequest> verifyQueryParameterOpacity() {
        Rule<String> rule = ValidationRules.<String>requiredNotNull()
            .and(ValidationRules::requiredNotEmpty)
            .and(ValidationRules.requiredMatchRegex("[0-1]\\.[0-9]*"))
            .or(ValidationRules::possibleNull)
            .named("opacity");

        return (request, errorState) ->
            rule.accept(request.getQueryStringParameters().get("opacity"), errorState);
    }

    public static Rule<HttpRequest> verifyHeatmapRequestSupportedQueryParameters() {
        Rule<Collection<String>> rule = verifySupportedParameters(
            "id", "from", "until", "radius", "opacity");

        return (request, errorState) ->
            rule.accept(request.getQueryStringParameters().keySet(), errorState);
    }

    public static Rule<HttpRequest> verifyHeatmapJsonRequestSupportedQueryParameters() {
        Rule<Collection<String>> rule = verifySupportedParameters(
            "id", "from", "until");

        return (request, errorState) ->
            rule.accept(request.getQueryStringParameters().keySet(), errorState);
    }

    public static Rule<HeatmapRequest> verifyHeatmapRequestFromIsBeforeUntil() {
        return ValidationRules.<HeatmapRequest>requiredNotNull()
            .and((request, consumer) -> {
                if (!request.getFrom().isBefore(request.getUnit())) {
                    consumer.accept("The field value should be before 'until' timestamp.");
                }
            })
            .named("from");
    }

    public static Rule<HeatmapJsonRequest> verifyHeatmapJsonRequestFromIsBeforeUntil() {
        return ValidationRules.<HeatmapJsonRequest>requiredNotNull()
            .and((request, consumer) -> {
                if (!request.getFrom().isBefore(request.getUnit())) {
                    consumer.accept("The field value should be before 'until' timestamp.");
                }
            })
            .named("from");
    }

    public static Rule<HeatmapRequest> verifyOpacity() {
        Rule<Double> rule = ValidationRules.<Double>requiredNotNull()
            .and(ValidationRules.requiredBetween(0.0, 1.0))
            .named("opacity");

        return (request, errorState) ->
            rule.accept(request.getOpacity(), errorState);
    }

    public static Rule<HeatmapRequest> verifyRadius() {
        Rule<Integer> rule = ValidationRules.<Integer>requiredNotNull()
            .and(ValidationRules.requiredBetween(8, 128))
            .named("radius");

        return (request, errorState) ->
            rule.accept(request.getRadius(), errorState);
    }

    public static Rule<Collection<String>> verifySupportedParameters(String... known) {
        Set<String> supported = new HashSet<>(Arrays.asList(known));

        return (params, errorState) -> params.stream()
            .map(String::trim)
            .filter(key -> !supported.contains(key))
            .forEach(key -> errorState.addError(key, "Unsupported parameter"));
    }

    public static <T> Specification<T> requiredNotNull() {
        return ValidationRules::requiredNotNull;
    }

    public static <T> void requiredNotNull(T obj, Consumer<String> consumer) {
        if (obj == null) {
            consumer.accept("The required field can't be null");
        }
    }

    public static <T> void possibleNull(T obj, Consumer<String> consumer) {
        if (obj != null) {
            consumer.accept("The optional field can be null");
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

    public static Specification<String> requiredStandardISO8601() {
        final Pattern pattern = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z");

        return (obj, consumer) -> {
            Matcher matcher = pattern.matcher(obj);
            if (!matcher.matches()) {
                consumer.accept("The timestamp field must meet to ISO-8601 standard, such as '2011-12-03T10:15:30Z'");
                return;
            }

            try {
                LocalDateTime.parse(obj, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (DateTimeParseException e) {
                consumer.accept(Optional.ofNullable(e.getCause())
                    .map(Throwable::getMessage)
                    .orElse(e.getMessage()));
            }
        };
    }

}
