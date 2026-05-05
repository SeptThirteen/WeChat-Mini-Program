package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.CreateOrderRequest;
import com.example.elderly.dto.OrderStatusRequest;
import com.example.elderly.entity.Order;
import com.example.elderly.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/order/create")
    public ApiResponse<Order> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    @GetMapping("/api/order/list")
    public ApiResponse<List<Order>> list(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) String status) {
        return ApiResponse.success(orderService.listOrders(userId, status));
    }

    @GetMapping("/api/order/{id}")
    public ApiResponse<Order> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(orderService.getById(id));
    }

    @PostMapping("/api/order/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable("id") Long id) {
        orderService.updateStatus(id, "CANCELLED");
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", id);
        result.put("status", "CANCELLED");
        return ApiResponse.success(result);
    }

    @PostMapping("/api/order/{id}/rate")
    public ApiResponse<Map<String, Object>> rate(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        Integer rating = body.get("rating") != null ? Integer.parseInt(body.get("rating").toString()) : null;
        orderService.rateOrder(id, rating);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", id);
        result.put("status", "RATED");
        return ApiResponse.success(result);
    }

    @PutMapping("/api/order/{id}/status")
    public ApiResponse<Map<String, Object>> updateStatus(@PathVariable("id") Long id, @Valid @RequestBody OrderStatusRequest request) {
        orderService.updateStatus(id, request.getStatus());
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", id);
        result.put("status", request.getStatus());
        return ApiResponse.success(result);
    }

    @DeleteMapping("/api/order/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        orderService.deleteById(id);
        return ApiResponse.success(null);
    }
}
