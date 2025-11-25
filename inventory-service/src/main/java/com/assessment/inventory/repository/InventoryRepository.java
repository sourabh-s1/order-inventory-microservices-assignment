package com.assessment.inventory.repository;

import com.assessment.inventory.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryBatch, Long> {
    @Query("select b from InventoryBatch b where b.product.id = :productId order by b.expiryDate asc")
    List<InventoryBatch> findByProductIdOrderByExpiryDateAsc(@Param("productId") Long productId);
}
