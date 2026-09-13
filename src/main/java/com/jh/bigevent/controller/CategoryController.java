package com.jh.bigevent.controller;

import com.jh.bigevent.dto.category.CategoryAddDTO;
import com.jh.bigevent.service.CategoryService;
import com.jh.bigevent.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "文章分类模块")
@Validated
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "新增文章分类", description = "分类名称和分类别名在当前用户下不可重复")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "新增成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "分类名称已被占用", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "分类别名已被占用", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping
    public Result<Void> add(@RequestBody @Valid CategoryAddDTO categoryAddDTO) {
        categoryService.add(categoryAddDTO);
        return Result.success();
    }
}
