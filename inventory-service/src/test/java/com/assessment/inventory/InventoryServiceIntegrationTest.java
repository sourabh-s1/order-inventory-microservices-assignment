package com.assessment.inventory;

import com.assessment.inventory.dto.InventoryBatchDTO;
import com.assessment.inventory.dto.InventoryUpdateRequest;
import com.assessment.inventory.entity.InventoryBatch;
import com.assessment.inventory.entity.Product;
import com.assessment.inventory.handler.InventoryHandler;
import com.assessment.inventory.repository.InventoryBatchRepository;
import com.assessment.inventory.repository.ProductRepository;
import com.assessment.inventory.service.implementation.InventoryHandlerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class InventoryServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private InventoryHandlerFactory inventoryHandlerFactory;

    @MockitoBean
    private InventoryHandler inventoryHandler;

    private final String INVENTORY_BASE_URL = "http://inventory-service";

    @BeforeEach
    void beforeEach() {
        inventoryBatchRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void getBatches_endpoint_returnsSavedBatches_sortedByExpiry() {
        Product product = Product.builder()
                .id(10L)
                .name("Test Product")
                .build();
        product = productRepository.save(product);

        InventoryBatch b1 = InventoryBatch.builder()
                .product(product)
                .batchNumber("a")
                .quantity(5)
                .expiryDate(LocalDate.now().plusDays(10))
                .build();

        InventoryBatch b2 = InventoryBatch.builder()
                .product(product)
                .batchNumber("b")
                .quantity(3)
                .expiryDate(LocalDate.now().plusDays(5))
                .build();

        inventoryBatchRepository.saveAll(Arrays.asList(b1, b2));

        ResponseEntity<InventoryBatchDTO[]> resp = restTemplate.getForEntity(
                "http://localhost:" + port + "/inventory/" + product.getId(),
                InventoryBatchDTO[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        InventoryBatchDTO[] body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.length).isEqualTo(2);
        assertThat(body[0].getBatchNumber()).isEqualTo("b");
        assertThat(body[1].getBatchNumber()).isEqualTo("a");
    }

    @Test
    void updateInventory_endpoint_usesHandler_and_returnsUpdatedDtos() {
        InventoryUpdateRequest req = new InventoryUpdateRequest();
        req.setProductId(20L);
        Map<String,Integer> ded = new HashMap<>();
        ded.put("X", 1);
        req.setBatchQuantityToDeduct(ded);

        InventoryBatch updated = InventoryBatch.builder()
                .id(100L)
                .product(Product.builder().id(20L).build())
                .batchNumber("X")
                .quantity(2)
                .expiryDate(LocalDate.now().plusDays(7))
                .build();

        when(inventoryHandlerFactory.getHandler("defaultInventoryHandler")).thenReturn(inventoryHandler);
        when(inventoryHandler.handleUpdate(any(InventoryUpdateRequest.class)))
                .thenReturn(Collections.singletonList(updated));


        ResponseEntity<InventoryBatchDTO[]> resp = restTemplate.postForEntity(
                "http://localhost:" + port + "/inventory/update",
                req,
                InventoryBatchDTO[].class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        InventoryBatchDTO[] body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.length).isEqualTo(1);
        assertThat(body[0].getBatchNumber()).isEqualTo("X");

        verify(inventoryHandlerFactory, times(1)).getHandler("defaultInventoryHandler");
        verify(inventoryHandler, times(1)).handleUpdate(any(InventoryUpdateRequest.class));
    }
}
