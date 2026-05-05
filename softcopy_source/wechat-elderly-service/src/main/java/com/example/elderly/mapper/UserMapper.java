package com.example.elderly.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderly.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
