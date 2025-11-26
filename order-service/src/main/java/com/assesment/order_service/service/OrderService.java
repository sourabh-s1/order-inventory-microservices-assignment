package com.assesment.order_service.service;

import com.assesment.order_service.dto.InventoryUpdateRequest;
import com.assesment.order_service.dto.PlaceOrderRequest;
import com.assesment.order_service.entity.OrderEntity;
import com.assesment.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository;

    private final String INVENTORY_BASE_URL = "http://inventory-service";

    public OrderEntity placeOrder(PlaceOrderRequest request) {

        String url = INVENTORY_BASE_URL + "/inventory/" + request.getProductId();
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

        List<Map<String, Object>> batches = response.getBody();
        if (batches == null || batches.isEmpty())
            throw new RuntimeException("Product not found in inventory!");

        int required = request.getQuantity();
        Map<String, Integer> deduct = new HashMap<>();

        for (Map<String, Object> batch : batches) {
            int available = (Integer) batch.get("quantity");
            String batchNumber = (String) batch.get("batchNumber");

            if (required <= 0) break;

            int toDeduct = Math.min(available, required);
            deduct.put(batchNumber, toDeduct);
            required -= toDeduct;
        }

        if (required > 0) {
            return orderRepository.save(
                    new OrderEntity(null,
                            request.getProductId(),
                            request.getQuantity(),
                            "Not enough stock!")
            );
        }

        InventoryUpdateRequest updateRequest =
                new InventoryUpdateRequest(request.getProductId(), deduct);

        restTemplate.postForEntity(
                INVENTORY_BASE_URL + "/inventory/update",
                updateRequest,
                Void.class
        );

        return orderRepository.save(
                new OrderEntity(null,
                        request.getProductId(),
                        request.getQuantity(),
                        "SUCCESS")
        );
    }
}

