package com.jh.bigevent.controller;

import com.jh.bigevent.dto.category.CategoryAddDTO;
import com.jh.bigevent.dto.category.CategoryUpdateDTO;
import com.jh.bigevent.entity.Category;
import com.jh.bigevent.service.CategoryService;
import com.jh.bigevent.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @Operation(summary = "文章分类列表", description = "获取当前登录用户创建的所有文章分类，按创建时间降序排列")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping
    public Result<List<Category>> list() {
        List<Category> categories = categoryService.list();
        return Result.success(categories);
    }

    @Operation(summary = "获取文章分类详情", description = "根据ID获取文章分类详情，只能查询当前用户自己创建的分类")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "分类不存在", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "只能查询自己创建的分类", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping("/detail")
    public Result<Category> detail(
            @Parameter(description = "分类主键ID", required = true, example = "1")
            @RequestParam Long id) {
        Category category = categoryService.detail(id);
        return Result.success(category);
    }

    @Operation(summary = "更新文章分类", description = "更新分类名称和分类别名，只能修改自己创建的分类，名称和别名在当前用户下不可重复")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "分类不存在", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "只能修改自己创建的分类", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "分类名称已被占用", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "分类别名已被占用", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PutMapping
    public Result<Void> update(@RequestBody @Valid CategoryUpdateDTO categoryUpdateDTO) {
        categoryService.update(categoryUpdateDTO);
        return Result.success();
    }

    @Operation(summary = "删除文章分类", description = "根据ID删除文章分类，只能删除自己创建的分类")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "分类不存在", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "只能删除自己创建的分类", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @DeleteMapping
    public Result<Void> delete(
            @Parameter(description = "分类主键ID", required = true, example = "1")
            @RequestParam Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
