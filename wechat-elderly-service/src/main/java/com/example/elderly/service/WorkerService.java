package com.example.elderly.service;

import com.example.elderly.dto.WorkerLoginRequest;
import com.example.elderly.dto.WorkerRegisterRequest;
import com.example.elderly.entity.Order;

import java.util.List;
import java.util.Map;

public interface WorkerService {

    /** 服务人员登录，返回 token + 基本信息 */
    Map<String, Object> login(WorkerLoginRequest request);

    /** 服务人员注册，返回 token + 基本信息 */
    Map<String, Object> register(WorkerRegisterRequest request);

    /** 获取所有待抢单（status=CREATED），根据人员类别智能排序 */
    List<Map<String, Object>> pendingOrders(Long workerId);

    /** 获取某服务人员的订单，支持可选 status 过滤 */
    List<Map<String, Object>> myOrders(Long workerId, String status);

    /** 抢单 */
    Order acceptOrder(Long orderId, Long workerId);

    /** 完成订单 */
    Order completeOrder(Long orderId, Long workerId);
}
