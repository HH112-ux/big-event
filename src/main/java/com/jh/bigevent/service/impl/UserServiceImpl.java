package com.jh.bigevent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jh.bigevent.dto.user.UserLoginDTO;
import com.jh.bigevent.dto.user.UserRegisterDTO;
import com.jh.bigevent.dto.user.UserUpdateDTO;
import com.jh.bigevent.dto.user.UpdatePwdDTO;
import com.jh.bigevent.entity.User;
import com.jh.bigevent.exception.BusinessException;
import com.jh.bigevent.mapper.UserMapper;
import com.jh.bigevent.service.UserService;
import com.jh.bigevent.utils.JwtUtil;
import com.jh.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void register(UserRegisterDTO registerDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已被占用");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes(StandardCharsets.UTF_8)));

        userMapper.insert(user);
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        String md5Password = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!md5Password.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public User getUserInfo() {
        Long userId = ThreadLocalUtil.get("id", Long.class);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void update(UserUpdateDTO updateDTO) {
        Long userId = ThreadLocalUtil.get("id", Long.class);
        if (!userId.equals(updateDTO.getId())) {
            throw new BusinessException("只能修改自己的信息");
        }

        if (updateDTO.getUsername() != null) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, updateDTO.getUsername())
                    .ne(User::getId, userId);
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("用户名已被占用");
            }
        }

        User user = new User();
        user.setId(updateDTO.getId());
        user.setUsername(updateDTO.getUsername());
        user.setNickname(updateDTO.getNickname());
        user.setEmail(updateDTO.getEmail());

        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Long userId = ThreadLocalUtil.get("id", Long.class);
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getUserPic, avatarUrl);
        userMapper.update(null, wrapper);
    }

    @Override
    public void updatePwd(UpdatePwdDTO updatePwdDTO) {
        if (!updatePwdDTO.getNewPwd().equals(updatePwdDTO.getRePwd())) {
            throw new BusinessException("两次新密码不一致");
        }

        Long userId = ThreadLocalUtil.get("id", Long.class);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String oldMd5 = DigestUtils.md5DigestAsHex(updatePwdDTO.getOldPwd().getBytes(StandardCharsets.UTF_8));
        if (!oldMd5.equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        String newMd5 = DigestUtils.md5DigestAsHex(updatePwdDTO.getNewPwd().getBytes(StandardCharsets.UTF_8));
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getPassword, newMd5);
        userMapper.update(null, wrapper);
    }
}
