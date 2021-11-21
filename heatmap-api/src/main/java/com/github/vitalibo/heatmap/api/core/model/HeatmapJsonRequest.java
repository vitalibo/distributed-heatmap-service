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
public class HeatmapJsonRequest {

    private Long id;
    private LocalDateTime from;
    private LocalDateTime unit;

}
