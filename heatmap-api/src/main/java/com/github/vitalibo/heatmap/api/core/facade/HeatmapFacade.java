package com.github.vitalibo.heatmap.api.core.facade;

import com.github.vitalibo.heatmap.api.core.Facade;
import com.github.vitalibo.heatmap.api.core.Renderer;
import com.github.vitalibo.heatmap.api.core.Repository;
import com.github.vitalibo.heatmap.api.core.ValidationRules;
import com.github.vitalibo.heatmap.api.core.model.HeatmapRangeQuery;
import com.github.vitalibo.heatmap.api.core.model.HeatmapRequest;
import com.github.vitalibo.heatmap.api.core.model.HeatmapResponse;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpResponse;
import com.github.vitalibo.heatmap.api.core.model.transform.HeatmapRequestTranslator;
import com.github.vitalibo.heatmap.api.core.util.Rules;
import lombok.RequiredArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Function;

@RequiredArgsConstructor
public class HeatmapFacade implements Facade<HeatmapRequest, HeatmapResponse> {

    private final Function<HttpRequest, HeatmapRequest> heatmapRequestTranslator;
    private final Rules<HeatmapRequest> rules;
    private final Repository repository;
    private final Renderer renderer;

    public HeatmapFacade(Repository repository, Renderer renderer) {
        this(HeatmapRequestTranslator::from,
            Rules.lazy(
                Arrays.asList(
                    ValidationRules::verifyQueryParameterId,
                    ValidationRules::verifyQueryParameterFrom,
                    ValidationRules::verifyQueryParameterUntil,
                    ValidationRules::verifyQueryParameterRadius,
                    ValidationRules::verifyQueryParameterOpacity,
                    ValidationRules::verifyHeatmapRequestSupportedQueryParameters),
                Arrays.asList(
                    ValidationRules::verifyHeatmapRequestFromIsBeforeUntil,
                    ValidationRules::verifyOpacity,
                    ValidationRules::verifyRadius)),
            repository,
            renderer);
    }

    @Override
    public HttpResponse<HeatmapResponse> process(HttpRequest request) {
        rules.verify(request);

        HeatmapResponse response = process(
            heatmapRequestTranslator.apply(request));

        return new HttpResponse<>(response);
    }

    @Override
    public HeatmapResponse process(HeatmapRequest request) {
        rules.verify(request);

        BufferedImage canvas = renderer.render(
            repository.queryByRange(
                new HeatmapRangeQuery()
                    .withId(request.getId())
                    .withFrom(request.getFrom())
                    .withUnit(request.getUnit())),
            request.getRadius(),
            request.getOpacity());

        return new HeatmapResponse()
            .withCanvas(canvas);
    }

}
