package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.BillQueryRequest;
import com.example.elderly.service.BillQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BillController {

    private final BillQueryService billQueryService;

    @PostMapping("/api/bill/query")
    public ApiResponse<Map<String, Object>> query(@Valid @RequestBody BillQueryRequest request) {
        return ApiResponse.success(billQueryService.createQuery(request));
    }
}
