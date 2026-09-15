package com.jh.bigevent.service;

import com.jh.bigevent.dto.article.ArticleAddDTO;
import com.jh.bigevent.dto.article.ArticleQueryDTO;
import com.jh.bigevent.entity.Article;
import com.jh.bigevent.utils.PageBean;

public interface ArticleService {

    void add(ArticleAddDTO articleAddDTO);

    PageBean<Article> list(ArticleQueryDTO articleQueryDTO);

    Article detail(Long id);
}
