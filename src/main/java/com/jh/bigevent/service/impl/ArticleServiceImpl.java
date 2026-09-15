package com.jh.bigevent.service.impl;

import com.jh.bigevent.dto.article.ArticleAddDTO;
import com.jh.bigevent.entity.Article;
import com.jh.bigevent.mapper.ArticleMapper;
import com.jh.bigevent.service.ArticleService;
import com.jh.bigevent.service.CategoryService;
import com.jh.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CategoryService categoryService;

    @Override
    @Transactional
    public void add(ArticleAddDTO articleAddDTO) {
        Long userId = ThreadLocalUtil.get("id", Long.class);

        categoryService.detail(articleAddDTO.getCategoryId());

        Article article = new Article();
        article.setTitle(articleAddDTO.getTitle());
        article.setContent(articleAddDTO.getContent());
        article.setCoverImg(articleAddDTO.getCoverImg());
        article.setState(articleAddDTO.getState());
        article.setCategoryId(articleAddDTO.getCategoryId());
        article.setCreateUser(userId);

        articleMapper.insert(article);
    }
}
