package com.github.vitalibo.heatmap.api.infrastructure.springframework;

import com.github.vitalibo.heatmap.api.core.model.Heatmap;
import com.github.vitalibo.heatmap.api.core.model.HeatmapResponse;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class HeatmapHttpMessageConverterTest {

    @Mock
    private HttpOutputMessage mockHttpOutputMessage;

    private HeatmapHttpMessageConverter converter;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        converter = new HeatmapHttpMessageConverter();
    }

    @Test
    public void testMediaType() {
        List<MediaType> actual = converter.getSupportedMediaTypes();

        Assert.assertNotNull(actual);
        Assert.assertEquals(actual, Collections.singletonList(MediaType.IMAGE_PNG));
    }

    @Test
    public void testSupports() {
        boolean actual = converter.supports(HeatmapResponse.class);

        Assert.assertTrue(actual);
    }

    @Test
    public void testUnSupports() {
        boolean actual = converter.supports(Heatmap.class);

        Assert.assertFalse(actual);
    }

    @Test
    public void testReadInternal() {
        IllegalArgumentException actual = Assert.expectThrows(IllegalArgumentException.class,
            () -> converter.readInternal(null, null));

        Assert.assertEquals(actual.getMessage(), "method not supported");
    }

    @Test
    public void testWriteInternal() throws IOException {
        BufferedImage img = new BufferedImage(2, 1, BufferedImage.TYPE_4BYTE_ABGR);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Mockito.when(mockHttpOutputMessage.getBody()).thenReturn(baos);

        converter.writeInternal(new HeatmapResponse(img), mockHttpOutputMessage);

        byte[] actual = baos.toByteArray();
        Assert.assertNotNull(actual);
        Assert.assertEquals(actual.length, 68);
        Mockito.verify(mockHttpOutputMessage).getBody();
    }

}
