package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderly.dto.GovTaskRequest;
import com.example.elderly.entity.GovTask;
import com.example.elderly.mapper.GovTaskMapper;
import com.example.elderly.service.GovTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GovTaskServiceImpl implements GovTaskService {

    private final GovTaskMapper govTaskMapper;

    @Override
    public GovTask submit(GovTaskRequest request) {
        GovTask task = new GovTask();
        task.setUserId(request.getUserId());
        task.setTaskType(request.getTaskType());
        task.setTaskDesc(request.getTaskDesc());
        task.setStatus("SUBMITTED");
        govTaskMapper.insert(task);
        return task;
    }

    @Override
    public List<GovTask> listByUser(Long userId) {
        QueryWrapper<GovTask> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("submitted_time");
        return govTaskMapper.selectList(wrapper);
    }
}
