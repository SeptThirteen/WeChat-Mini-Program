package com.example.elderly.service;

import com.example.elderly.dto.ServiceCreateRequest;
import com.example.elderly.dto.ServiceUpdateRequest;
import com.example.elderly.entity.ServiceItem;

import java.util.List;

public interface ServiceItemService {
    List<ServiceItem> listAll();

    ServiceItem getById(Long id);

    ServiceItem create(ServiceCreateRequest request);

    ServiceItem update(Long id, ServiceUpdateRequest request);

    void delete(Long id);
}
