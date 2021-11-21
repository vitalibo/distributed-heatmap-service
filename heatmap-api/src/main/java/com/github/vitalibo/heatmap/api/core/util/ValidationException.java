package com.github.vitalibo.heatmap.api.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class ValidationException extends RuntimeException {

    @Getter
    private final ErrorState errorState;

}
