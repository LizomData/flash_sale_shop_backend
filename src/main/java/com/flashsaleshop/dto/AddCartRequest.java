package com.flashsaleshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartRequest {

    @NotBlank
    private String type; // normal | seckill

    private Long productId;

    private String eventId;

    @Min(1)
    private int qty = 1;
}

