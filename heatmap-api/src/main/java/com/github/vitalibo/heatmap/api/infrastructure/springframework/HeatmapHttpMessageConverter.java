package com.github.vitalibo.heatmap.api.infrastructure.springframework;

import com.github.vitalibo.heatmap.api.core.model.HeatmapImageResponse;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import javax.imageio.ImageIO;
import java.io.IOException;

public class HeatmapHttpMessageConverter extends AbstractHttpMessageConverter<HeatmapImageResponse> {

    public HeatmapHttpMessageConverter() {
        super(MediaType.IMAGE_PNG);
    }

    @Override
    protected boolean supports(Class<?> cls) {
        return HeatmapImageResponse.class == cls;
    }

    @Override
    protected HeatmapImageResponse readInternal(Class<? extends HeatmapImageResponse> cls, HttpInputMessage message) {
        throw new IllegalArgumentException("method not supported");
    }

    @Override
    protected void writeInternal(HeatmapImageResponse response, HttpOutputMessage outputMessage) throws IOException {
        ImageIO.write(response.getCanvas(), "png", outputMessage.getBody());
    }

}
