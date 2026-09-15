package com.jh.bigevent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
@Schema(description = "文章信息")
public class Article {

    @TableId(type = IdType.AUTO)
    @Schema(description = "文章主键", example = "1")
    private Long id;

    @TableField("title")
    @Schema(description = "文章标题", example = "陕西旅游攻略")
    private String title;

    @TableField("content")
    @Schema(description = "文章正文", example = "兵马俑,华清池,法门寺...")
    private String content;

    @TableField("cover_img")
    @Schema(description = "封面图像地址", example = "https://example.com/cover.png")
    private String coverImg;

    @TableField("state")
    @Schema(description = "发布状态: 已发布|草稿", example = "草稿")
    private String state;

    @TableField("category_id")
    @Schema(description = "文章分类ID", example = "1")
    private Long categoryId;

    @TableField("create_user")
    @Schema(description = "作者用户ID", example = "1")
    private Long createUser;

    @TableField("create_time")
    @Schema(description = "创建时间", example = "2026-09-15 10:00:00")
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "更新时间", example = "2026-09-15 10:00:00")
    private LocalDateTime updateTime;

    @Version
    @TableField("version")
    @Schema(description = "乐观锁版本号", example = "1")
    private Integer version;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标记: 0-正常 1-已删除", example = "0")
    private Integer isDeleted;
}
