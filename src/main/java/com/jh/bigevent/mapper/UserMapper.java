package com.jh.bigevent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jh.bigevent.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
