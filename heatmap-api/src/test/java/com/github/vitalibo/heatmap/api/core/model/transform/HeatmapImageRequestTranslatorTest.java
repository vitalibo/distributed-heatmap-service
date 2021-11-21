package com.github.vitalibo.heatmap.api.core.model.transform;

import com.github.vitalibo.heatmap.api.core.model.HeatmapImageRequest;
import com.github.vitalibo.heatmap.api.core.model.HttpRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class HeatmapImageRequestTranslatorTest {

    @Test
    public void testFrom() {
        HttpRequest request = new HttpRequest();
        Map<String, String> params = new HashMap<>();
        params.put("id", "123");
        params.put("from", "2021-10-30T12:23:56Z");
        params.put("until", "2021-10-31T19:39:21Z");
        params.put("opacity", "0.5");
        params.put("radius", "32");
        request.setQueryStringParameters(params);

        HeatmapImageRequest actual = HeatmapImageRequestTranslator.from(request);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getId(), (Long) 123L);
        Assert.assertEquals(actual.getFrom(), LocalDateTime.of(2021, 10, 30, 12, 23, 56));
        Assert.assertEquals(actual.getUnit(), LocalDateTime.of(2021, 10, 31, 19, 39, 21));
        Assert.assertEquals(actual.getOpacity(), (Double) 0.5);
        Assert.assertEquals(actual.getRadius(), (Integer) 32);
    }

    @Test
    public void testFromDefault() {
        HttpRequest request = new HttpRequest();
        Map<String, String> params = new HashMap<>();
        params.put("id", "123");
        params.put("from", "2021-10-30T12:23:56Z");
        params.put("until", "2021-10-31T19:39:21Z");
        request.setQueryStringParameters(params);

        HeatmapImageRequest actual = HeatmapImageRequestTranslator.from(request);

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.getId(), (Long) 123L);
        Assert.assertEquals(actual.getFrom(), LocalDateTime.of(2021, 10, 30, 12, 23, 56));
        Assert.assertEquals(actual.getUnit(), LocalDateTime.of(2021, 10, 31, 19, 39, 21));
        Assert.assertEquals(actual.getOpacity(), (Double) 1.0);
        Assert.assertEquals(actual.getRadius(), (Integer) 64);
    }

}
