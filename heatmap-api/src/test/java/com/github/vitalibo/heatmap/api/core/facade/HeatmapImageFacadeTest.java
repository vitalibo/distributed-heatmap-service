package com.github.vitalibo.heatmap.api.core.facade;

import com.github.vitalibo.heatmap.api.core.Renderer;
import com.github.vitalibo.heatmap.api.core.Repository;
import com.github.vitalibo.heatmap.api.core.model.Heatmap;
import com.github.vitalibo.heatmap.api.core.model.HeatmapImageRequest;
import com.github.vitalibo.heatmap.api.core.model.HeatmapImageResponse;
import com.github.vitalibo.heatmap.api.core.model.HeatmapRangeQuery;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpResponse;
import com.github.vitalibo.heatmap.api.core.util.ErrorState;
import com.github.vitalibo.heatmap.api.core.util.Rules;
import com.github.vitalibo.heatmap.api.core.util.ValidationException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

public class HeatmapImageFacadeTest {

    @Mock
    private Repository mockRepository;
    @Mock
    private Renderer mockRenderer;
    @Mock
    private Function<HttpRequest, HeatmapImageRequest> mockTranslator;
    @Mock
    private Rules<HeatmapImageRequest> mockRules;
    @Mock
    private BufferedImage mockBufferedImage;
    @Mock
    private Heatmap mockHeatmap;
    @Captor
    private ArgumentCaptor<HeatmapRangeQuery> captorHeatmapRangeQuery;

    private HeatmapImageFacade spyFacade;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        spyFacade = Mockito.spy(new HeatmapImageFacade(mockTranslator, mockRules, mockRepository, mockRenderer));
    }

    @Test
    public void testHttpProcess() {
        HeatmapImageResponse heatmapResponse = new HeatmapImageResponse();
        HeatmapImageRequest heatmapRequest = new HeatmapImageRequest();
        heatmapRequest.setId(123L);
        Mockito.doReturn(heatmapResponse)
            .when(spyFacade).process(Mockito.any(HeatmapImageRequest.class));
        Mockito.when(mockTranslator.apply(Mockito.any())).thenReturn(heatmapRequest);
        HttpRequest httpRequest = new HttpRequest();
        Map<String, String> params = new HashMap<>();
        params.put("id", "123");
        httpRequest.setQueryStringParameters(params);

        HttpResponse<HeatmapImageResponse> actual = spyFacade.process(httpRequest);

        Assert.assertNotNull(actual);
        Assert.assertSame(actual.getBody(), heatmapResponse);
        Mockito.verify(mockRules).verify(httpRequest);
        Mockito.verify(mockTranslator).apply(httpRequest);
    }

    @Test
    public void testHttpProcessVerify() {
        spyFacade = Mockito.spy(new HeatmapImageFacade(mockRepository, mockRenderer));
        HeatmapImageResponse heatmapResponse = new HeatmapImageResponse();
        Mockito.doReturn(heatmapResponse)
            .when(spyFacade).process(Mockito.any(HeatmapImageRequest.class));
        HttpRequest httpRequest = new HttpRequest();
        Map<String, String> params = new HashMap<>();
        params.put("radius", "null");
        params.put("opacity", "null");
        params.put("foo", "null");
        httpRequest.setQueryStringParameters(params);

        ValidationException exception = Assert.expectThrows(ValidationException.class,
            () -> spyFacade.process(httpRequest));
        ErrorState actual = exception.getErrorState();

        Assert.assertEquals(actual.keySet(), new HashSet<>(
            Arrays.asList("id", "from", "until", "opacity", "radius", "foo")));
    }

    @Test
    public void testProcess() {
        Mockito.when(mockRenderer.render(Mockito.any(), Mockito.anyInt(), Mockito.anyDouble()))
            .thenReturn(mockBufferedImage);
        Mockito.when(mockRepository.queryByRange(Mockito.any())).thenReturn(mockHeatmap);
        HeatmapImageRequest request = new HeatmapImageRequest();
        request.setId(123L);
        request.setFrom(LocalDateTime.parse("2021-10-31T19:34:15"));
        request.setUnit(LocalDateTime.parse("2021-11-01T01:24:08"));
        request.setRadius(64);
        request.setOpacity(0.5);

        HeatmapImageResponse actual = spyFacade.process(request);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getCanvas(), mockBufferedImage);
        Mockito.verify(mockRepository).queryByRange(captorHeatmapRangeQuery.capture());
        HeatmapRangeQuery query = captorHeatmapRangeQuery.getValue();
        Assert.assertEquals(query.getId(), (Long) 123L);
        Assert.assertEquals(query.getFrom(), LocalDateTime.parse("2021-10-31T19:34:15"));
        Assert.assertEquals(query.getUnit(), LocalDateTime.parse("2021-11-01T01:24:08"));
        Mockito.verify(mockRenderer).render(mockHeatmap, 64, 0.5);
    }

}
