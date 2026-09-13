package com.jh.bigevent.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新密码请求参数")
public class UpdatePwdDTO {

    @Schema(description = "原密码", example = "123456")
    @NotBlank(message = "原密码不能为空")
    @Size(min = 5, max = 16, message = "原密码长度需在5~16位之间")
    private String oldPwd;

    @Schema(description = "新密码", example = "654321")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 5, max = 16, message = "新密码长度需在5~16位之间")
    private String newPwd;

    @Schema(description = "确认新密码", example = "654321")
    @NotBlank(message = "确认密码不能为空")
    @Size(min = 5, max = 16, message = "确认密码长度需在5~16位之间")
    private String rePwd;
}
