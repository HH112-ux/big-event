package com.example.bigevent.service;

import com.example.bigevent.dto.UserLoginDTO;
import com.example.bigevent.dto.UserRegisterDTO;

public interface UserService {

    void register(UserRegisterDTO registerDTO);

    String login(UserLoginDTO loginDTO);
}
