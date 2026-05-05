package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderly.common.BusinessException;
import com.example.elderly.dto.WorkerLoginRequest;
import com.example.elderly.dto.WorkerRegisterRequest;
import com.example.elderly.entity.Order;
import com.example.elderly.entity.User;
import com.example.elderly.entity.Worker;
import com.example.elderly.mapper.OrderMapper;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.mapper.WorkerMapper;
import com.example.elderly.service.WorkerService;
import com.example.elderly.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkerServiceImpl implements WorkerService {

    private final WorkerMapper workerMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> register(WorkerRegisterRequest request) {
        QueryWrapper<Worker> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", request.getPhone());
        Worker existing = workerMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(409, "手机号已注册");
        }

        Worker worker = new Worker();
        worker.setName(request.getName());
        worker.setPhone(request.getPhone());
        worker.setPassword(request.getPassword());
        worker.setCategory(request.getCategory() != null ? request.getCategory() : "其他");
        worker.setCreatedTime(LocalDateTime.now());
        workerMapper.insert(worker);

        return buildLoginResult(worker);
    }

    @Override
    public Map<String, Object> login(WorkerLoginRequest request) {
        QueryWrapper<Worker> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", request.getPhone());
        Worker worker = workerMapper.selectOne(wrapper);
        if (worker == null) {
            throw new BusinessException(401, "手机号未注册");
        }
        if (!worker.getPassword().equals(request.getPassword())) {
            throw new BusinessException(401, "密码错误");
        }
        // 生成 JWT（带 role=worker 标识）
        Map<String, Object> claims = new HashMap<>();
        claims.put("workerId", worker.getWorkerId());
        claims.put("phone", worker.getPhone());
        claims.put("role", "worker");
        String token = jwtUtil.generateToken(claims);
        return buildLoginResult(worker, token);
    }

    private Map<String, Object> buildLoginResult(Worker worker) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("workerId", worker.getWorkerId());
        claims.put("phone", worker.getPhone());
        claims.put("role", "worker");
        String token = jwtUtil.generateToken(claims);
        return buildLoginResult(worker, token);
    }

    private Map<String, Object> buildLoginResult(Worker worker, String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("workerId", worker.getWorkerId());
        result.put("name", worker.getName());
        result.put("phone", worker.getPhone());
        result.put("category", worker.getCategory());
        return result;
    }

    // 服务类别 → 匹配的人员类别 映射（用于智能排序）
    private static final Map<String, String> SERVICE_WORKER_MATCH;
    static {
        Map<String, String> m = new HashMap<>();
        m.put("代购", "超市老板");
        m.put("跑腿帮买", "超市老板");
        m.put("维修", "物业工作人员");
        m.put("上门维修", "物业工作人员");
        m.put("日间照护", "志愿者");
        m.put("暖心陪伴", "志愿者");
        m.put("外出陪同", "社区工作人员");
        m.put("贴心出行", "社区工作人员");
        SERVICE_WORKER_MATCH = m;
    }

    @Override
    public List<Map<String, Object>> pendingOrders(Long workerId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "CREATED");
        wrapper.orderByAsc("created_time");
        List<Order> orders = orderMapper.selectList(wrapper);
        List<Map<String, Object>> enriched = enrichOrders(orders);

        // 根据工人类别智能排序：匹配类别的订单排前面
        if (workerId != null) {
            Worker worker = workerMapper.selectById(workerId);
            if (worker != null && worker.getCategory() != null) {
                String workerCat = worker.getCategory();
                enriched.sort((a, b) -> {
                    boolean aMatch = isMatchingCategory(String.valueOf(a.get("serviceName")), workerCat);
                    boolean bMatch = isMatchingCategory(String.valueOf(b.get("serviceName")), workerCat);
                    if (aMatch == bMatch) return 0;
                    return aMatch ? -1 : 1;
                });
            }
        }
        return enriched;
    }

    /** 判断订单服务名是否匹配该人员类别 */
    private boolean isMatchingCategory(String serviceName, String workerCategory) {
        if (serviceName == null || workerCategory == null) return false;
        for (Map.Entry<String, String> entry : SERVICE_WORKER_MATCH.entrySet()) {
            if (serviceName.contains(entry.getKey()) && workerCategory.equals(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> myOrders(Long workerId, String status) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("worker_id", workerId);
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("assigned_time");
        List<Order> orders = orderMapper.selectList(wrapper);
        return enrichOrders(orders);
    }

    @Override
    public Order acceptOrder(Long orderId, Long workerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!"CREATED".equals(order.getStatus())) {
            throw new BusinessException(409, "该订单已被其他人接单或已取消");
        }
        // 验证 worker 存在
        Worker worker = workerMapper.selectById(workerId);
        if (worker == null) {
            throw new BusinessException(404, "服务人员不存在");
        }
        order.setWorkerId(workerId);
        order.setAssignedTime(LocalDateTime.now());
        order.setStatus("ASSIGNED");
        orderMapper.updateById(order);
        return order;
    }

    @Override
    public Order completeOrder(Long orderId, Long workerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!workerId.equals(order.getWorkerId())) {
            throw new BusinessException(403, "该订单不属于你");
        }
        if (!"ASSIGNED".equals(order.getStatus())) {
            throw new BusinessException(409, "只能完成进行中的订单");
        }
        order.setStatus("COMPLETED");
        orderMapper.updateById(order);
        return order;
    }

    /**
     * 给订单列表补充用户姓名和电话，方便服务人员端展示
     */
    private List<Map<String, Object>> enrichOrders(List<Order> orders) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", order.getOrderId());
            map.put("userId", order.getUserId());
            map.put("serviceId", order.getServiceId());
            map.put("serviceName", order.getServiceName());
            map.put("scheduledDate", order.getScheduledDate());
            map.put("scheduledSlot", order.getScheduledSlot());
            map.put("address", order.getAddress());
            map.put("remark", order.getRemark());
            map.put("status", order.getStatus());
            map.put("workerId", order.getWorkerId());
            map.put("assignedTime", order.getAssignedTime());
            map.put("createdTime", order.getCreatedTime());
            map.put("rating", order.getRating());
            // 补充用户信息
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                map.put("userName", user.getName());
                map.put("userPhone", user.getPhone());
                map.put("userAddress", user.getAddress());
            }
            // 补充接单人员类别
            if (order.getWorkerId() != null) {
                Worker w = workerMapper.selectById(order.getWorkerId());
                if (w != null) {
                    map.put("workerCategory", w.getCategory());
                }
            }
            result.add(map);
        }
        return result;
    }
}
