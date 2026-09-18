package com.jh.bigevent.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.jh.bigevent.config.AliyunOssProperties;
import com.jh.bigevent.exception.BusinessException;
import com.jh.bigevent.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    /**
     * 允许的文件 Content-Type 白名单
     */
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp"
    );

    /**
     * 文件大小上限：5MB
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    /**
     * 文件校验失败错误码
     */
    private static final int FILE_VALIDATION_ERROR_CODE = 6;

    @Autowired
    private AliyunOssProperties ossProperties;

    @Override
    public String upload(MultipartFile file) {
        // 1. 基础校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException(FILE_VALIDATION_ERROR_CODE, "请选择要上传的文件");
        }

        // 2. 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(FILE_VALIDATION_ERROR_CODE, "文件大小不能超过 5MB");
        }

        // 3. 校验文件类型（Content-Type）
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(FILE_VALIDATION_ERROR_CODE,
                    "不支持的文件类型，仅支持 PNG、JPEG、GIF、WEBP 格式");
        }

        // 4. 校验文件头魔数
        validateMagicNumber(file);

        // 5. 生成 UUID 文件名，避免覆盖
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = UUID.randomUUID().toString() + extension;

        // 6. 上传到阿里云 OSS
        return uploadToOss(file, objectName);
    }

    /**
     * 校验文件头魔数，防止文件后缀伪造
     */
    private void validateMagicNumber(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int bytesRead = is.read(header);
            if (bytesRead < 3) {
                throw new BusinessException(FILE_VALIDATION_ERROR_CODE, "文件内容不完整");
            }

            // PNG: 89 50 4E 47
            if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50
                    && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
                return;
            }

            // JPEG: FF D8 FF
            if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8
                    && header[2] == (byte) 0xFF) {
                return;
            }

            // GIF: 47 49 46 38 (GIF8)
            if (header[0] == (byte) 0x47 && header[1] == (byte) 0x49
                    && header[2] == (byte) 0x46 && header[3] == (byte) 0x38) {
                return;
            }

            // WebP: 52 49 46 46 ... 57 45 42 50 (RIFF....WEBP)
            if (header[0] == (byte) 0x52 && header[1] == (byte) 0x49
                    && header[2] == (byte) 0x46 && header[3] == (byte) 0x46
                    && bytesRead >= 8
                    && header[4] == (byte) 0x57 && header[5] == (byte) 0x45
                    && header[6] == (byte) 0x42 && header[7] == (byte) 0x50) {
                return;
            }

            throw new BusinessException(FILE_VALIDATION_ERROR_CODE,
                    "文件内容与类型不匹配，请上传真实的图片文件");

        } catch (IOException e) {
            log.error("读取文件头失败", e);
            throw new BusinessException(FILE_VALIDATION_ERROR_CODE, "文件读取失败");
        }
    }

    /**
     * 上传文件到阿里云 OSS，返回公网访问 URL
     */
    private String uploadToOss(MultipartFile file, String objectName) {
        OSS ossClient = null;
        try (InputStream inputStream = file.getInputStream()) {
            ossClient = new OSSClientBuilder().build(
                    ossProperties.getEndpoint(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );

            ossClient.putObject(ossProperties.getBucketName(), objectName, inputStream);

            // 拼接公网访问 URL
            String endpoint = ossProperties.getEndpoint();
            String bucketName = ossProperties.getBucketName();
            // endpoint 格式如 https://oss-cn-beijing.aliyuncs.com
            // 生成 URL: https://bucketName.endpoint/objectName
            String urlEndpoint = endpoint.replace("https://", "https://" + bucketName + ".");
            return urlEndpoint + "/" + objectName;

        } catch (OSSException e) {
            log.error("OSS 上传失败，错误码: {}, 消息: {}", e.getErrorCode(), e.getMessage(), e);
            throw new BusinessException("文件上传失败，请稍后重试");
        } catch (IOException e) {
            log.error("文件流读取失败", e);
            throw new BusinessException("文件读取失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
