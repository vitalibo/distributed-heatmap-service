package com.github.vitalibo.heatmap.api.core.model.transform;

import com.github.vitalibo.heatmap.api.core.model.HeatmapImageRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public final class HeatmapImageRequestTranslator {

    private HeatmapImageRequestTranslator() {
    }

    public static HeatmapImageRequest from(HttpRequest request) {
        final Map<String, String> params = request.getQueryStringParameters();

        return new HeatmapImageRequest()
            .withId(Long.parseLong(params.get("id")))
            .withFrom(LocalDateTime.parse(params.get("from"), DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .withUnit(LocalDateTime.parse(params.get("until"), DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .withOpacity(Optional.ofNullable(params.get("opacity"))
                .map(Double::parseDouble)
                .orElse(1.0))
            .withRadius(Optional.ofNullable(params.get("radius"))
                .map(Integer::parseInt)
                .orElse(64));
    }

}
