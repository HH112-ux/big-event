package com.example.bigevent.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "新增文章分类请求参数")
public class CategoryAddDTO {

    @Schema(description = "分类名称", example = "人文")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称长度不能超过32个字符")
    private String categoryName;

    @Schema(description = "分类别名", example = "rw")
    @NotBlank(message = "分类别名不能为空")
    @Size(max = 32, message = "分类别名长度不能超过32个字符")
    private String categoryAlias;
}
