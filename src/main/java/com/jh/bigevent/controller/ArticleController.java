package com.jh.bigevent.controller;

import com.jh.bigevent.dto.article.ArticleAddDTO;
import com.jh.bigevent.dto.article.ArticleQueryDTO;
import com.jh.bigevent.entity.Article;
import com.jh.bigevent.service.ArticleService;
import com.jh.bigevent.utils.PageBean;
import com.jh.bigevent.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "文章管理模块")
@Validated
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Operation(summary = "新增文章", description = "发布或保存草稿文章，文章标题1~10个字符，分类ID必须属于当前登录用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "新增成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "分类不存在", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "只能查询自己创建的分类", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @PostMapping
    public Result<Void> add(@RequestBody @Valid ArticleAddDTO articleAddDTO) {
        articleService.add(articleAddDTO);
        return Result.success();
    }

    @Operation(summary = "文章列表(条件分页)", description = "根据分类ID和发布状态条件查询当前用户的文章，带分页返回total和items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping
    public Result<PageBean<Article>> list(@Valid ArticleQueryDTO articleQueryDTO) {
        PageBean<Article> page = articleService.list(articleQueryDTO);
        return Result.success(page);
    }

    @Operation(summary = "获取文章详情", description = "根据ID获取文章详细信息，只能查询当前用户自己创建的文章")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "文章不存在", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "200", description = "只能查询自己创建的文章", content = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "未授权：令牌缺失、过期或无效", content = @Content(schema = @Schema(implementation = Result.class)))
    })
    @GetMapping("/detail")
    public Result<Article> detail(
            @Parameter(description = "文章主键ID", required = true, example = "1")
            @RequestParam Long id) {
        Article article = articleService.detail(id);
        return Result.success(article);
    }
}
