package com.jh.bigevent.controller;

import com.jh.bigevent.dto.user.UserLoginDTO;
import com.jh.bigevent.dto.user.UserRegisterDTO;
import com.jh.bigevent.dto.user.UserUpdateDTO;
import com.jh.bigevent.dto.user.UpdatePwdDTO;
import com.jh.bigevent.entity.User;
import com.jh.bigevent.service.UserService;
import com.jh.bigevent.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户模块")
@Validated
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册", description = "用户名5~16位，密码5~16位，用户名不可重复")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "用户名已被占用", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid UserRegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    @Operation(summary = "用户登录", description = "校验用户名密码后签发JWT令牌，令牌有效期12小时")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功，返回JWT令牌", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "用户名或密码错误", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success(token);
    }

    @Operation(summary = "获取用户详细信息", description = "根据JWT令牌解析当前登录用户ID，查询并返回用户信息（不含密码）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping("/userInfo")
    public Result<User> userInfo() {
        User user = userService.getUserInfo();
        return Result.success(user);
    }

    @Operation(summary = "更新用户基本信息", description = "更新当前登录用户的基本信息，id必须与当前登录用户一致；nickname和email必填，username选填")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "用户名已被占用", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "只能修改自己的信息", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid UserUpdateDTO updateDTO) {
        userService.update(updateDTO);
        return Result.success();
    }

    @Operation(summary = "更新用户头像", description = "更新当前登录用户的头像URL，avatarUrl参数通过queryString传递，不能为空")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "头像URL不能为空", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PatchMapping("/updateAvatar")
    public Result<Void> updateAvatar(
            @Parameter(description = "头像URL地址", required = true, example = "https://example.com/avatar.png")
            @RequestParam @NotBlank(message = "头像URL不能为空") String avatarUrl) {
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    @Operation(summary = "更新用户密码", description = "校验原密码正确且两次新密码一致后，更新当前登录用户的密码")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "原密码错误", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "两次新密码不一致", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PatchMapping("/updatePwd")
    public Result<Void> updatePwd(@RequestBody @Valid UpdatePwdDTO updatePwdDTO) {
        userService.updatePwd(updatePwdDTO);
        return Result.success();
    }
}
