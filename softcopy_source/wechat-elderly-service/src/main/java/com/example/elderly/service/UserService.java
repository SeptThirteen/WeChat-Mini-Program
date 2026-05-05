package com.example.elderly.service;

import com.example.elderly.dto.UpdateUserRequest;
import com.example.elderly.entity.User;

import java.util.List;

public interface UserService {
    User getById(Long userId);

    List<User> listAll();

    User getByPhone(String phone);

    User createUser(String phone);

    void updateProfile(UpdateUserRequest request);

    void deleteById(Long userId);
}
