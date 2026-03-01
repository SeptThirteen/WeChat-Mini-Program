package com.example.elderly.service;

import com.example.elderly.dto.GovTaskRequest;
import com.example.elderly.entity.GovTask;

import java.util.List;

public interface GovTaskService {
    GovTask submit(GovTaskRequest request);

    List<GovTask> listByUser(Long userId);
}
