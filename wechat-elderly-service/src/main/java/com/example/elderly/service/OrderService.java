package com.example.elderly.service;

import com.example.elderly.dto.CreateOrderRequest;
import com.example.elderly.entity.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);

    List<Order> listOrders(Long userId, String status);

    Order getById(Long orderId);

    void updateStatus(Long orderId, String status);

    void rateOrder(Long orderId, Integer rating);

    void deleteById(Long orderId);
}
