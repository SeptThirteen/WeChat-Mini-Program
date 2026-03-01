package com.example.elderly.service;

import com.example.elderly.dto.BillQueryRequest;
import com.example.elderly.entity.BillQuery;

import java.util.List;
import java.util.Map;

public interface BillQueryService {
    Map<String, Object> createQuery(BillQueryRequest request);

    List<BillQuery> getHistory(Long userId);
}
