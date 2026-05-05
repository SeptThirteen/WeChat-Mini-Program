package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderly.common.BusinessException;
import com.example.elderly.dto.ServiceCreateRequest;
import com.example.elderly.dto.ServiceUpdateRequest;
import com.example.elderly.entity.ServiceItem;
import com.example.elderly.mapper.ServiceItemMapper;
import com.example.elderly.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemMapper serviceItemMapper;

    @Override
    public List<ServiceItem> listAll() {
        // Keep query compatible with older schemas that don't have display_name yet.
        return serviceItemMapper.selectList(
                new QueryWrapper<ServiceItem>()
                        .select("service_id", "category", "description", "price", "created_time")
                        .orderByDesc("created_time")
        );
    }

    @Override
    public ServiceItem getById(Long id) {
        ServiceItem item = serviceItemMapper.selectOne(
                new QueryWrapper<ServiceItem>()
                        .select("service_id", "category", "description", "price", "created_time")
                        .eq("service_id", id)
        );
        if (item == null) {
            throw new BusinessException(404, "服务不存在");
        }
        return item;
    }

    @Override
    public ServiceItem create(ServiceCreateRequest request) {
        ServiceItem item = new ServiceItem();
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        serviceItemMapper.insert(item);
        return item;
    }

    @Override
    public ServiceItem update(Long id, ServiceUpdateRequest request) {
        ServiceItem item = getById(id);
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        serviceItemMapper.updateById(item);
        return item;
    }

    @Override
    public void delete(Long id) {
        ServiceItem item = getById(id);
        serviceItemMapper.deleteById(item.getServiceId());
    }
}
