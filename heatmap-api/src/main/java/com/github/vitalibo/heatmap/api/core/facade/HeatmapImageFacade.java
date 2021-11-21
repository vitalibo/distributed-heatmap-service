package com.github.vitalibo.heatmap.api.core.facade;

import com.github.vitalibo.heatmap.api.core.Facade;
import com.github.vitalibo.heatmap.api.core.Renderer;
import com.github.vitalibo.heatmap.api.core.Repository;
import com.github.vitalibo.heatmap.api.core.ValidationRules;
import com.github.vitalibo.heatmap.api.core.model.HeatmapImageRequest;
import com.github.vitalibo.heatmap.api.core.model.HeatmapImageResponse;
import com.github.vitalibo.heatmap.api.core.model.HeatmapRangeQuery;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpResponse;
import com.github.vitalibo.heatmap.api.core.model.transform.HeatmapImageRequestTranslator;
import com.github.vitalibo.heatmap.api.core.util.Rules;
import lombok.RequiredArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Function;

@RequiredArgsConstructor
public class HeatmapImageFacade implements Facade<HeatmapImageRequest, HeatmapImageResponse> {

    private final Function<HttpRequest, HeatmapImageRequest> heatmapImageRequestTranslator;
    private final Rules<HeatmapImageRequest> rules;
    private final Repository repository;
    private final Renderer renderer;

    public HeatmapImageFacade(Repository repository, Renderer renderer) {
        this(HeatmapImageRequestTranslator::from,
            Rules.lazy(
                Arrays.asList(
                    ValidationRules::verifyQueryParameterId,
                    ValidationRules::verifyQueryParameterFrom,
                    ValidationRules::verifyQueryParameterUntil,
                    ValidationRules::verifyQueryParameterRadius,
                    ValidationRules::verifyQueryParameterOpacity,
                    ValidationRules::verifyHeatmapImageRequestSupportedQueryParameters),
                Arrays.asList(
                    ValidationRules::verifyHeatmapImageRequestFromIsBeforeUntil,
                    ValidationRules::verifyOpacity,
                    ValidationRules::verifyRadius)),
            repository,
            renderer);
    }

    @Override
    public HttpResponse<HeatmapImageResponse> process(HttpRequest request) {
        rules.verify(request);

        HeatmapImageResponse response = process(
            heatmapImageRequestTranslator.apply(request));

        return new HttpResponse<>(response);
    }

    @Override
    public HeatmapImageResponse process(HeatmapImageRequest request) {
        rules.verify(request);

        BufferedImage canvas = renderer.render(
            repository.queryByRange(
                new HeatmapRangeQuery()
                    .withId(request.getId())
                    .withFrom(request.getFrom())
                    .withUnit(request.getUnit())),
            request.getRadius(),
            request.getOpacity());

        return new HeatmapImageResponse()
            .withCanvas(canvas);
    }

}
