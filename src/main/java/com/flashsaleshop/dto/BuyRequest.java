package com.flashsaleshop.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BuyRequest {
    @Min(1)
    private int qty = 1;
}

