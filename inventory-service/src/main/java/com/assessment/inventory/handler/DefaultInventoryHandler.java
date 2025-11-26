package com.assessment.inventory.handler;

import com.assessment.inventory.dto.InventoryUpdateRequest;
import com.assessment.inventory.entity.InventoryBatch;
import com.assessment.inventory.repository.InventoryBatchRepository;
import com.assessment.inventory.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Component("defaultInventoryHandler")
public class DefaultInventoryHandler implements InventoryHandler{
    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public List<InventoryBatch> handleUpdate(InventoryUpdateRequest request) {
        // minimal implementation so bean exists; returns current batches ordered by expiry
        Long productId = request == null ? null : request.getProductId();
        if (productId == null) {
            return List.of();
        }
        return inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);
    }
}
