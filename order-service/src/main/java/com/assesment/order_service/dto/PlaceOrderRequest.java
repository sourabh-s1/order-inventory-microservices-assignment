package com.assesment.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {
    private Integer productId;
    private Integer quantity;
}
