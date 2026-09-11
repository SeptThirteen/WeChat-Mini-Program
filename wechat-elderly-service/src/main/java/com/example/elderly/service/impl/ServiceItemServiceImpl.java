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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemMapper serviceItemMapper;

    @Override
    public List<ServiceItem> listAll() {
        // display_name 已于 v1.1 迁移全量回填, 全列查询即可
        return serviceItemMapper.selectList(
                new QueryWrapper<ServiceItem>().orderByDesc("created_time")
        );
    }

    @Override
    public ServiceItem getById(Long id) {
        ServiceItem item = serviceItemMapper.selectById(id);
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
        item.setDisplayName(request.getDisplayName());
        item.setCreatedTime(LocalDateTime.now());
        serviceItemMapper.insert(item);
        return item;
    }

    @Override
    public ServiceItem update(Long id, ServiceUpdateRequest request) {
        ServiceItem item = getById(id);
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        if (request.getDisplayName() != null) {
            item.setDisplayName(request.getDisplayName());
        }
        serviceItemMapper.updateById(item);
        return item;
    }

    @Override
    public void delete(Long id) {
        ServiceItem item = getById(id);
        serviceItemMapper.deleteById(item.getServiceId());
    }
}
