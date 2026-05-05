package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.ServiceCreateRequest;
import com.example.elderly.dto.ServiceUpdateRequest;
import com.example.elderly.entity.ServiceItem;
import com.example.elderly.service.ServiceItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceItemService serviceItemService;

    @GetMapping("/api/service/list")
    public ApiResponse<List<ServiceItem>> list() {
        return ApiResponse.success(serviceItemService.listAll());
    }

    @GetMapping("/api/service/{id}")
    public ApiResponse<ServiceItem> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(serviceItemService.getById(id));
    }

    @PostMapping("/api/service/create")
    public ApiResponse<ServiceItem> create(@Valid @RequestBody ServiceCreateRequest request) {
        return ApiResponse.success(serviceItemService.create(request));
    }

    @PutMapping("/api/service/{id}")
    public ApiResponse<ServiceItem> update(@PathVariable("id") Long id, @Valid @RequestBody ServiceUpdateRequest request) {
        return ApiResponse.success(serviceItemService.update(id, request));
    }

    @DeleteMapping("/api/service/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        serviceItemService.delete(id);
        return ApiResponse.success(null);
    }
}
