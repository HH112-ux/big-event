package com.example.bigevent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新用户基本信息请求参数")
public class UserUpdateDTO {

    @Schema(description = "用户ID", example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    @Size(min = 5, max = 16, message = "用户名长度需在5~16位之间")
    private String username;

    @Schema(description = "昵称", example = "张三")
    @NotNull(message = "昵称不能为空")
    @Size(min = 1, max = 10, message = "昵称长度需在1~10位之间")
    private String nickname;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @NotNull(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
