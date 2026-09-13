package com.jh.bigevent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
@Schema(description = "用户信息")
public class User {

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户主键", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "wangba")
    private String username;

    @Schema(description = "密码 (MD5加密)", hidden = true)
    private String password;

    @Schema(description = "昵称", example = "王霸")
    private String nickname;

    @Schema(description = "邮箱", example = "wangba@example.com")
    private String email;

    @TableField("user_pic")
    @Schema(description = "头像URL", example = "https://example.com/avatar.png")
    private String userPic;

    @TableField("create_time")
    @Schema(description = "创建时间", example = "2026-09-11 10:00:00")
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "更新时间", example = "2026-09-11 10:00:00")
    private LocalDateTime updateTime;
}
