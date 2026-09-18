package com.jh.bigevent.controller;

import com.jh.bigevent.service.FileService;
import com.jh.bigevent.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件上传模块")
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private FileService fileService;

    @Operation(summary = "文件上传", description = "上传单文件到阿里云 OSS，返回文件访问 URL")
    @PostMapping
    public Result<String> upload(MultipartFile file) {
        String url = fileService.upload(file);
        return Result.success(url);
    }
}
