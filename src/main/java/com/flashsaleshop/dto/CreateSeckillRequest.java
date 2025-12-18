package com.flashsaleshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSeckillRequest {
    @NotBlank
    private String title;

    @NotNull
    private Long productId;

    @NotNull
    private BigDecimal seckillPrice;

    @Min(1)
    private int limitPerUser = 1;

    @Min(1)
    private int stock = 1;

    @NotNull
    private Long startAt;

    @NotNull
    private Long endAt;
}

