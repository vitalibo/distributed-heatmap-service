package com.github.vitalibo.heatmap.api.core.facade;

import com.github.vitalibo.heatmap.api.core.Facade;
import com.github.vitalibo.heatmap.api.core.Repository;
import com.github.vitalibo.heatmap.api.core.ValidationRules;
import com.github.vitalibo.heatmap.api.core.model.Heatmap;
import com.github.vitalibo.heatmap.api.core.model.HeatmapJsonRequest;
import com.github.vitalibo.heatmap.api.core.model.HeatmapJsonResponse;
import com.github.vitalibo.heatmap.api.core.model.HeatmapRangeQuery;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpResponse;
import com.github.vitalibo.heatmap.api.core.model.transform.HeatmapJsonRequestTranslator;
import com.github.vitalibo.heatmap.api.core.model.transform.HeatmapJsonResponseTranslator;
import com.github.vitalibo.heatmap.api.core.util.Rules;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;

@RequiredArgsConstructor
public class HeatmapJsonFacade implements Facade<HeatmapJsonRequest, HeatmapJsonResponse> {

    private final Function<HttpRequest, HeatmapJsonRequest> heatmapJsonRequestTranslator;
    private final Function<Heatmap, HeatmapJsonResponse> heatmapJsonResponseTranslator;
    private final Rules<HeatmapJsonRequest> rules;
    private final Repository repository;

    public HeatmapJsonFacade(Repository repository, double sparseHeatmapThreshold) {
        this(HeatmapJsonRequestTranslator::from,
            (o) -> HeatmapJsonResponseTranslator.from(o, sparseHeatmapThreshold),
            Rules.lazy(
                Arrays.asList(
                    ValidationRules::verifyQueryParameterId,
                    ValidationRules::verifyQueryParameterFrom,
                    ValidationRules::verifyQueryParameterUntil,
                    ValidationRules::verifyHeatmapJsonRequestSupportedQueryParameters),
                Collections.singletonList(
                    ValidationRules::verifyHeatmapJsonRequestFromIsBeforeUntil)),
            repository);
    }

    @Override
    public HttpResponse<HeatmapJsonResponse> process(HttpRequest request) {
        rules.verify(request);

        HeatmapJsonResponse response = process(
            heatmapJsonRequestTranslator.apply(request));

        return new HttpResponse<>(response);
    }

    @Override
    public HeatmapJsonResponse process(HeatmapJsonRequest request) {
        rules.verify(request);

        Heatmap heatmap = repository.queryByRange(
            new HeatmapRangeQuery()
                .withId(request.getId())
                .withFrom(request.getFrom())
                .withUnit(request.getUnit()));

        return heatmapJsonResponseTranslator.apply(heatmap);
    }

}
