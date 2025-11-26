package com.assesment.order_service;

import com.assesment.order_service.dto.PlaceOrderRequest;
import com.assesment.order_service.entity.OrderEntity;
import com.assesment.order_service.repository.OrderRepository;
import com.assesment.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    OrderService orderService;

    private final String INVENTORY_URL = "http://inventory-service";


    @Test
    void placeOrder_success_deductsFromMultipleBatches_and_savesSuccessOrder() {
        PlaceOrderRequest req = new PlaceOrderRequest(1, 7);

        Map<String, Object> batchA = new HashMap<>();
        batchA.put("batchNumber", "A");
        batchA.put("quantity", 5);

        Map<String, Object> batchB = new HashMap<>();
        batchB.put("batchNumber", "B");
        batchB.put("quantity", 4);

        List<Map<String, Object>> batches = Arrays.asList(batchA, batchB);
        ResponseEntity<List> resp = new ResponseEntity<>(batches, HttpStatus.OK);

        when(restTemplate.getForEntity(INVENTORY_URL + "/inventory/" + req.getProductId(), List.class))
                .thenReturn(resp);

        when(restTemplate.postForEntity(eq(INVENTORY_URL + "/inventory/update"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderEntity saved = orderService.placeOrder(req);

        assertThat(saved).isNotNull();
        assertThat(saved.getProductId()).isEqualTo(1);
        assertThat(saved.getQuantity()).isEqualTo(7);
        assertThat(saved.getStatus()).isEqualTo("SUCCESS");

        verify(restTemplate, times(1)).postForEntity(eq(INVENTORY_URL + "/inventory/update"), any(), eq(Void.class));
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void placeOrder_notEnoughStock_savesNotEnoughStockOrder() {
        PlaceOrderRequest req = new PlaceOrderRequest(2, 10);

        Map<String, Object> batch = new HashMap<>();
        batch.put("batchNumber", "X");
        batch.put("quantity", 3);

        List<Map<String, Object>> batches = Collections.singletonList(batch);
        ResponseEntity<List> resp = new ResponseEntity<>(batches, HttpStatus.OK);

        when(restTemplate.getForEntity(INVENTORY_URL + "/inventory/" + req.getProductId(), List.class))
                .thenReturn(resp);

        when(orderRepository.save(any(OrderEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OrderEntity saved = orderService.placeOrder(req);

        assertThat(saved).isNotNull();
        assertThat(saved.getProductId()).isEqualTo(2);
        assertThat(saved.getQuantity()).isEqualTo(10);
        assertThat(saved.getStatus()).containsIgnoringCase("Not enough stock");

        verify(restTemplate, never()).postForEntity(eq(INVENTORY_URL + "/inventory/update"), any(), eq(Void.class));
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void placeOrder_productNotFound_throwsRuntimeException() {
        PlaceOrderRequest req = new PlaceOrderRequest(3, 1);
        ResponseEntity<List> resp = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);

        when(restTemplate.getForEntity(INVENTORY_URL + "/inventory/" + req.getProductId(), List.class))
                .thenReturn(resp);

        assertThatThrownBy(() -> orderService.placeOrder(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found in inventory");

        verify(orderRepository, never()).save(any());
    }
}