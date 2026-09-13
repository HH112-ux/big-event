package com.example.bigevent.service;

import com.example.bigevent.dto.user.UserLoginDTO;
import com.example.bigevent.dto.user.UserRegisterDTO;
import com.example.bigevent.dto.user.UserUpdateDTO;
import com.example.bigevent.dto.user.UpdatePwdDTO;
import com.example.bigevent.entity.User;

public interface UserService {

    void register(UserRegisterDTO registerDTO);

    String login(UserLoginDTO loginDTO);

    User getUserInfo();

    void update(UserUpdateDTO updateDTO);

    void updateAvatar(String avatarUrl);

    void updatePwd(UpdatePwdDTO updatePwdDTO);
}
