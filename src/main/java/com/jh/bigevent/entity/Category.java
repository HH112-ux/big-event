package com.jh.bigevent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("category")
@Schema(description = "文章分类信息")
public class Category {

    @TableId(type = IdType.AUTO)
    @Schema(description = "分类主键", example = "1")
    private Long id;

    @TableField("category_name")
    @Schema(description = "分类名称", example = "人文")
    private String categoryName;

    @TableField("category_alias")
    @Schema(description = "分类别名", example = "rw")
    private String categoryAlias;

    @TableField("create_user")
    @Schema(description = "创建者用户ID", example = "1")
    private Long createUser;

    @TableField("create_time")
    @Schema(description = "创建时间", example = "2026-09-13 10:00:00")
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "更新时间", example = "2026-09-13 10:00:00")
    private LocalDateTime updateTime;
}
