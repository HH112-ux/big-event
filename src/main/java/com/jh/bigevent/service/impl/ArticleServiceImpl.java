package com.jh.bigevent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jh.bigevent.dto.article.ArticleAddDTO;
import com.jh.bigevent.dto.article.ArticleQueryDTO;
import com.jh.bigevent.dto.article.ArticleUpdateDTO;
import com.jh.bigevent.entity.Article;
import com.jh.bigevent.mapper.ArticleMapper;
import com.jh.bigevent.service.ArticleService;
import com.jh.bigevent.service.CategoryService;
import com.jh.bigevent.utils.PageBean;
import com.jh.bigevent.exception.BusinessException;
import com.jh.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public PageBean<Article> list(ArticleQueryDTO articleQueryDTO) {
        Long userId = ThreadLocalUtil.get("id", Long.class);

        PageHelper.startPage(articleQueryDTO.getPageNum(), articleQueryDTO.getPageSize());

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getCreateUser, userId);
        if (articleQueryDTO.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, articleQueryDTO.getCategoryId());
        }
        if (articleQueryDTO.getState() != null) {
            wrapper.eq(Article::getState, articleQueryDTO.getState());
        }
        wrapper.orderByDesc(Article::getCreateTime);

        List<Article> articles = articleMapper.selectList(wrapper);
        PageInfo<Article> pageInfo = new PageInfo<>(articles);

        return new PageBean<>(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public Article detail(Long id) {
        Long userId = ThreadLocalUtil.get("id", Long.class);

        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getCreateUser().equals(userId)) {
            throw new BusinessException("只能查询自己创建的文章");
        }
        return article;
    }

    @Override
    @Transactional
    public void update(ArticleUpdateDTO articleUpdateDTO) {
        Long userId = ThreadLocalUtil.get("id", Long.class);

        Article article = articleMapper.selectById(articleUpdateDTO.getId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getCreateUser().equals(userId)) {
            throw new BusinessException("只能修改自己创建的文章");
        }

        categoryService.detail(articleUpdateDTO.getCategoryId());

        Article updateArticle = new Article();
        updateArticle.setId(articleUpdateDTO.getId());
        updateArticle.setTitle(articleUpdateDTO.getTitle());
        updateArticle.setContent(articleUpdateDTO.getContent());
        updateArticle.setCoverImg(articleUpdateDTO.getCoverImg());
        updateArticle.setState(articleUpdateDTO.getState());
        updateArticle.setCategoryId(articleUpdateDTO.getCategoryId());
        updateArticle.setVersion(article.getVersion());

        int rows = articleMapper.updateById(updateArticle);
        if (rows == 0) {
            throw new BusinessException("文章已被他人修改，请刷新后重试");
        }
    }

    @Override
    public void delete(Long id) {
        Long userId = ThreadLocalUtil.get("id", Long.class);

        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        if (!article.getCreateUser().equals(userId)) {
            throw new BusinessException("只能删除自己创建的文章");
        }

        articleMapper.deleteById(id);
    }
}
