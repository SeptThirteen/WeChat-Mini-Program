package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderly.dto.BillQueryRequest;
import com.example.elderly.entity.BillQuery;
import com.example.elderly.mapper.BillQueryMapper;
import com.example.elderly.service.BillQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillQueryServiceImpl implements BillQueryService {

    private final BillQueryMapper billQueryMapper;

    @Override
    public Map<String, Object> createQuery(BillQueryRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("queryType", request.getQueryType());
        result.put("amount", 128.50);
        result.put("month", "2026-01");

        BillQuery query = new BillQuery();
        query.setUserId(request.getUserId());
        query.setQueryType(request.getQueryType());
        query.setQueryParams(request.getQueryParams());
        query.setResultSnapshot(result.toString());
        billQueryMapper.insert(query);

        return result;
    }

    @Override
    public List<BillQuery> getHistory(Long userId) {
        QueryWrapper<BillQuery> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_time");
        return billQueryMapper.selectList(wrapper);
    }
}
