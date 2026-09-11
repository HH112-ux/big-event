package com.example.bigevent.service;

import com.example.bigevent.dto.UserLoginDTO;
import com.example.bigevent.dto.UserRegisterDTO;
import com.example.bigevent.dto.UserUpdateDTO;
import com.example.bigevent.entity.User;

public interface UserService {

    void register(UserRegisterDTO registerDTO);

    String login(UserLoginDTO loginDTO);

    User getUserInfo();

    void update(UserUpdateDTO updateDTO);
}
