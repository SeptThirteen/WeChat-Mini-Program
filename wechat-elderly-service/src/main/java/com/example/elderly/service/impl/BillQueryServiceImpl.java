package com.example.elderly.service.impl;

import com.example.elderly.dto.BillQueryRequest;
import com.example.elderly.entity.BillQuery;
import com.example.elderly.mapper.BillQueryMapper;
import com.example.elderly.service.BillQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillQueryServiceImpl implements BillQueryService {

    private final BillQueryMapper billQueryMapper;

    @Override
    public Map<String, Object> createQuery(BillQueryRequest request) {
        Map<String, Object> result = Map.of(
                "queryType", request.getQueryType(),
                "amount", 128.50,
                "month", "2026-01"
        );

        BillQuery query = new BillQuery();
        query.setUserId(request.getUserId());
        query.setQueryType(request.getQueryType());
        query.setQueryParams(request.getQueryParams());
        query.setResultSnapshot(result.toString());
        billQueryMapper.insert(query);

        return result;
    }
}
