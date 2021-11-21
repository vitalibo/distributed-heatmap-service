package com.github.vitalibo.heatmap.api.core.model.transform;

import com.github.vitalibo.heatmap.api.core.model.HeatmapJsonRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public final class HeatmapJsonRequestTranslator {

    private HeatmapJsonRequestTranslator() {
    }

    public static HeatmapJsonRequest from(HttpRequest request) {
        final Map<String, String> params = request.getQueryStringParameters();

        return new HeatmapJsonRequest()
            .withId(Long.parseLong(params.get("id")))
            .withFrom(LocalDateTime.parse(params.get("from"), DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .withUnit(LocalDateTime.parse(params.get("until"), DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

}
