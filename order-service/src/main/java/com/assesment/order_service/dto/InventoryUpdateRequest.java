package com.assesment.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryUpdateRequest {
    private Integer productId;
    private Map<String, Integer> batchQuantityToDeduct;
}
