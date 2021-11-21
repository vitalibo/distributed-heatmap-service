package com.github.vitalibo.heatmap.api.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.LocalDateTime;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapRequest {

    private Long id;
    private Double opacity;
    private LocalDateTime from;
    private LocalDateTime unit;
    private Integer radius;

}
