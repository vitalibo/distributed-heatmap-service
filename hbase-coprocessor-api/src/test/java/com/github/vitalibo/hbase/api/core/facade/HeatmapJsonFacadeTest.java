package com.github.vitalibo.hbase.api.core.facade;

import com.github.vitalibo.hbase.api.core.Repository;
import com.github.vitalibo.hbase.api.core.model.Heatmap;
import com.github.vitalibo.hbase.api.core.model.HeatmapJsonRequest;
import com.github.vitalibo.hbase.api.core.model.HeatmapJsonResponse;
import com.github.vitalibo.hbase.api.core.model.HeatmapRangeQuery;
import com.github.vitalibo.hbase.api.core.model.HttpRequest;
import com.github.vitalibo.hbase.api.core.model.HttpResponse;
import com.github.vitalibo.hbase.api.core.util.ErrorState;
import com.github.vitalibo.hbase.api.core.util.Rules;
import com.github.vitalibo.hbase.api.core.util.ValidationException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

public class HeatmapJsonFacadeTest {

    @Mock
    private Repository mockRepository;
    @Mock
    private Function<HttpRequest, HeatmapJsonRequest> mockRequestTranslator;
    @Mock
    private Function<Heatmap, HeatmapJsonResponse> mockResponseTranslator;
    @Mock
    private Rules<HeatmapJsonRequest> mockRules;
    @Mock
    private Heatmap mockHeatmap;
    @Captor
    private ArgumentCaptor<HeatmapRangeQuery> captorHeatmapRangeQuery;

    private HeatmapJsonFacade spyFacade;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        spyFacade = Mockito.spy(new HeatmapJsonFacade(
            mockRequestTranslator, mockResponseTranslator, mockRules, mockRepository));
    }

    @Test
    public void testHttpProcess() {
        HeatmapJsonResponse heatmapResponse = new HeatmapJsonResponse(640, 480, null, null);
        HeatmapJsonRequest heatmapRequest = new HeatmapJsonRequest();
        heatmapRequest.setId(123L);
        Mockito.doReturn(heatmapResponse)
            .when(spyFacade).process(Mockito.any(HeatmapJsonRequest.class));
        Mockito.when(mockRequestTranslator.apply(Mockito.any())).thenReturn(heatmapRequest);
        HttpRequest httpRequest = new HttpRequest();
        Map<String, String> params = new HashMap<>();
        params.put("id", "123");
        httpRequest.setQueryStringParameters(params);

        HttpResponse<HeatmapJsonResponse> actual = spyFacade.process(httpRequest);

        Assert.assertNotNull(actual);
        Assert.assertSame(actual.getBody(), heatmapResponse);
        Mockito.verify(mockRules).verify(httpRequest);
        Mockito.verify(mockRequestTranslator).apply(httpRequest);
    }

    @Test
    public void testHttpProcessVerify() {
        spyFacade = Mockito.spy(new HeatmapJsonFacade(mockRepository, 0.5));
        HeatmapJsonResponse heatmapResponse = new HeatmapJsonResponse();
        Mockito.doReturn(heatmapResponse)
            .when(spyFacade).process(Mockito.any(HeatmapJsonRequest.class));
        HttpRequest httpRequest = new HttpRequest();
        Map<String, String> params = new HashMap<>();
        params.put("id", "null");
        params.put("foo", "null");
        httpRequest.setQueryStringParameters(params);

        ValidationException exception = Assert.expectThrows(ValidationException.class,
            () -> spyFacade.process(httpRequest));
        ErrorState actual = exception.getErrorState();

        Assert.assertEquals(actual.keySet(), new HashSet<>(
            Arrays.asList("id", "from", "until", "foo")));
    }

    @Test
    public void testProcess() {
        Mockito.when(mockRepository.queryByRange(Mockito.any())).thenReturn(mockHeatmap);
        HeatmapJsonRequest request = new HeatmapJsonRequest();
        request.setId(123L);
        request.setFrom(LocalDateTime.parse("2021-10-31T19:34:15"));
        request.setUnit(LocalDateTime.parse("2021-11-01T01:24:08"));
        HeatmapJsonResponse response = new HeatmapJsonResponse(640, 480, null, null);
        Mockito.when(mockResponseTranslator.apply(Mockito.any())).thenReturn(response);

        HeatmapJsonResponse actual = spyFacade.process(request);

        Assert.assertNotNull(actual);
        Assert.assertSame(actual, response);
        Mockito.verify(mockRepository).queryByRange(captorHeatmapRangeQuery.capture());
        HeatmapRangeQuery query = captorHeatmapRangeQuery.getValue();
        Assert.assertEquals(query.getId(), (Long) 123L);
        Assert.assertEquals(query.getFrom(), LocalDateTime.parse("2021-10-31T19:34:15"));
        Assert.assertEquals(query.getUnit(), LocalDateTime.parse("2021-11-01T01:24:08"));
    }

}
