package com.jh.bigevent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jh.bigevent.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
