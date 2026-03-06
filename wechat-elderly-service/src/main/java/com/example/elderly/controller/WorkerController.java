package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.WorkerLoginRequest;
import com.example.elderly.dto.WorkerRegisterRequest;
import com.example.elderly.entity.Order;
import com.example.elderly.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    /** 服务人员登录（公开接口） */
    @PostMapping("/api/worker/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody WorkerLoginRequest request) {
        return ApiResponse.success(workerService.login(request));
    }

    /** 服务人员注册（公开接口） */
    @PostMapping("/api/worker/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody WorkerRegisterRequest request) {
        return ApiResponse.success(workerService.register(request));
    }

    /** 获取所有待抢订单（根据人员类别智能排序） */
    @GetMapping("/api/worker/orders/pending")
    public ApiResponse<List<Map<String, Object>>> pendingOrders(
            @RequestParam(value = "workerId", required = false) Long workerId) {
        return ApiResponse.success(workerService.pendingOrders(workerId));
    }

    /** 获取我的订单（按 workerId） */
    @GetMapping("/api/worker/orders/mine")
    public ApiResponse<List<Map<String, Object>>> myOrders(
            @RequestParam("workerId") Long workerId,
            @RequestParam(value = "status", required = false) String status) {
        return ApiResponse.success(workerService.myOrders(workerId, status));
    }

    /** 抢单/接单 */
    @PostMapping("/api/worker/orders/{id}/accept")
    public ApiResponse<Order> acceptOrder(
            @PathVariable("id") Long orderId,
            @RequestBody Map<String, Object> body) {
        Long workerId = Long.parseLong(body.get("workerId").toString());
        return ApiResponse.success(workerService.acceptOrder(orderId, workerId));
    }

    /** 完成订单 */
    @PostMapping("/api/worker/orders/{id}/complete")
    public ApiResponse<Order> completeOrder(
            @PathVariable("id") Long orderId,
            @RequestBody Map<String, Object> body) {
        Long workerId = Long.parseLong(body.get("workerId").toString());
        return ApiResponse.success(workerService.completeOrder(orderId, workerId));
    }
}
