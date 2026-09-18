package com.jh.bigevent.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件到阿里云 OSS
     * @param file 上传的文件
     * @return 文件在 OSS 上的公网访问 URL
     */
    String upload(MultipartFile file);
}
