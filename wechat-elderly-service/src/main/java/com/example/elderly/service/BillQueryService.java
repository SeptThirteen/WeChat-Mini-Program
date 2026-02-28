package com.example.elderly.service;

import com.example.elderly.dto.BillQueryRequest;

import java.util.Map;

public interface BillQueryService {
    Map<String, Object> createQuery(BillQueryRequest request);
}
