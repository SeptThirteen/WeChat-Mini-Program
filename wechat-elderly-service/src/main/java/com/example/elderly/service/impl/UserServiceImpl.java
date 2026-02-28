package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.common.BusinessException;
import com.example.elderly.dto.UpdateUserRequest;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    @Override
    public List<User> listAll() {
        return userMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public User getByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    @Override
    public User createUser(String phone) {
        User user = new User();
        user.setPhone(phone);
        String tail = phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone;
        user.setName("用户" + tail);
        userMapper.insert(user);
        return user;
    }

    @Override
    public void updateProfile(UpdateUserRequest request) {
        User user = getById(request.getUserId());
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        userMapper.updateById(user);
    }

    @Override
    public void deleteById(Long userId) {
        User user = getById(userId);
        userMapper.deleteById(user.getUserId());
    }
}
