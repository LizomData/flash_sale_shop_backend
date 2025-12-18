package com.flashsaleshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRequest {
    @NotBlank
    private String name;

    private String description;

    private String tag;

    @NotNull
    private BigDecimal price;

    @Min(0)
    private int stock = 0;
}

