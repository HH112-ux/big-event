package com.jh.bigevent.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "新增文章请求参数")
public class ArticleAddDTO {

    @Schema(description = "文章标题", example = "陕西旅游攻略")
    @NotBlank(message = "文章标题不能为空")
    @Size(min = 1, max = 10, message = "文章标题长度需在1~10个字符之间")
    private String title;

    @Schema(description = "文章正文", example = "兵马俑,华清池,法门寺...")
    @NotBlank(message = "文章正文不能为空")
    private String content;

    @Schema(description = "封面图像地址", example = "https://example.com/cover.png")
    @NotBlank(message = "封面图像地址不能为空")
    private String coverImg;

    @Schema(description = "发布状态: 已发布|草稿", example = "草稿")
    @NotBlank(message = "发布状态不能为空")
    private String state;

    @Schema(description = "文章分类ID", example = "1")
    @NotNull(message = "文章分类ID不能为空")
    private Long categoryId;
}
