package com.assessment.inventory.handler;

import com.assessment.inventory.dto.InventoryUpdateRequest;
import com.assessment.inventory.entity.InventoryBatch;
import com.assessment.inventory.repository.InventoryBatchRepository;
import com.assessment.inventory.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Component("defaultInventoryHandler")
public class DefaultInventoryHandler implements InventoryHandler{
    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public List<InventoryBatch> handleUpdate(InventoryUpdateRequest request) {
        Long productId = request.getProductId();
        Map<String, Integer> deductMap = request.getBatchQuantityToDeduct();
        if (deductMap == null || deductMap.isEmpty()) {
            return inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);
        }

        List<InventoryBatch> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);
        for (Map.Entry<String, Integer> e : deductMap.entrySet()) {
            String batchNumber = e.getKey();
            int toDeduct = e.getValue() == null ? 0 : e.getValue();
            batches.stream()
                    .filter(b -> batchNumber.equals(b.getBatchNumber()))
                    .findFirst()
                    .ifPresent(b -> {
                        int newQty = Math.max(0, b.getQuantity() - toDeduct);
                        b.setQuantity(newQty);
                        inventoryBatchRepository.save(b);
                    });
        }
        // reload to ensure ordering and latest values
        return inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);
    }
}
