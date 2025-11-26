package com.assessment.inventory.handler;


import com.assessment.inventory.dto.InventoryUpdateRequest;
import com.assessment.inventory.entity.InventoryBatch;

import java.util.List;

public interface InventoryHandler {
    List<InventoryBatch> handleUpdate(InventoryUpdateRequest request);
}