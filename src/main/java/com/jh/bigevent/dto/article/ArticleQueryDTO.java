package com.jh.bigevent.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文章列表分页查询参数")
public class ArticleQueryDTO {

    @Schema(description = "当前页码", example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum;

    @Schema(description = "每页条数", example = "3")
    @NotNull(message = "每页条数不能为空")
    @Min(value = 1, message = "每页条数最小为1")
    private Integer pageSize;

    @Schema(description = "文章分类ID（选填）", example = "2")
    private Long categoryId;

    @Schema(description = "发布状态: 已发布|草稿（选填）", example = "草稿")
    private String state;
}
