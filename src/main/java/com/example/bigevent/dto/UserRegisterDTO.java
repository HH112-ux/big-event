package com.example.bigevent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 5, max = 16, message = "用户名长度需在5~16位之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 5, max = 16, message = "密码长度需在5~16位之间")
    private String password;
}
