package com.jh.bigevent.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分页结果")
public class PageBean<T> {

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "数据列表")
    private List<T> items;

    public PageBean(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }
}
