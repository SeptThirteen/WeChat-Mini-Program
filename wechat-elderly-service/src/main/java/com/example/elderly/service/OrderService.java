package com.example.elderly.service;

import com.example.elderly.dto.CreateOrderRequest;
import com.example.elderly.entity.Order;

import java.util.List;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);

    List<Order> listOrders(Long userId);

    Order getById(Long orderId);

    void updateStatus(Long orderId, String status);

    void deleteById(Long orderId);
}
