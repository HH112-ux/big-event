package com.jh.bigevent.service;

import com.jh.bigevent.dto.user.UserLoginDTO;
import com.jh.bigevent.dto.user.UserRegisterDTO;
import com.jh.bigevent.dto.user.UserUpdateDTO;
import com.jh.bigevent.dto.user.UpdatePwdDTO;
import com.jh.bigevent.entity.User;

public interface UserService {

    void register(UserRegisterDTO registerDTO);

    String login(UserLoginDTO loginDTO);

    User getUserInfo();

    void update(UserUpdateDTO updateDTO);

    void updateAvatar(String avatarUrl);

    void updatePwd(UpdatePwdDTO updatePwdDTO);
}
