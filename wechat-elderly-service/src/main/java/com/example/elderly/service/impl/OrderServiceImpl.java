package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderly.common.BusinessException;
import com.example.elderly.dto.CreateOrderRequest;
import com.example.elderly.entity.Order;
import com.example.elderly.entity.ServiceItem;
import com.example.elderly.entity.User;
import com.example.elderly.entity.Worker;
import com.example.elderly.mapper.OrderMapper;
import com.example.elderly.mapper.ServiceItemMapper;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.mapper.WorkerMapper;
import com.example.elderly.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final WorkerMapper workerMapper;

    @Override
    public Order createOrder(CreateOrderRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        ServiceItem item = serviceItemMapper.selectById(request.getServiceId());
        if (item == null) {
            throw new BusinessException(404, "服务不存在");
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setServiceId(request.getServiceId());
        // 优先使用友好名称，回退到 category
        String displayName = item.getDisplayName() != null ? item.getDisplayName() : item.getCategory();
        order.setServiceName(displayName + " - " + item.getDescription());
        order.setScheduledDate(request.getScheduledDate());
        order.setScheduledSlot(request.getScheduledSlot());
        order.setAddress(request.getAddress());
        order.setRemark(request.getRemark());
        order.setContactPhone(user.getPhone());
        order.setStatus("CREATED");
        orderMapper.insert(order);
        return order;
    }

    @Override
    public List<Order> listOrders(Long userId, String status) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("created_time");
        return orderMapper.selectList(wrapper);
    }

    @Override
    public Order getById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        // 关联查询接单服务人员信息
        if (order.getWorkerId() != null) {
            Worker worker = workerMapper.selectById(order.getWorkerId());
            if (worker != null) {
                order.setWorkerName(worker.getName());
                order.setWorkerPhone(worker.getPhone());
            }
        }
        return order;
    }

    @Override
    public void updateStatus(Long orderId, String status) {
        Order order = getById(orderId);
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    @Override
    public void rateOrder(Long orderId, Integer rating) {
        Order order = getById(orderId);
        order.setStatus("RATED");
        if (rating != null) {
            order.setRating(rating);
        }
        orderMapper.updateById(order);
    }

    @Override
    public void deleteById(Long orderId) {
        Order order = getById(orderId);
        orderMapper.deleteById(order.getOrderId());
    }
}
