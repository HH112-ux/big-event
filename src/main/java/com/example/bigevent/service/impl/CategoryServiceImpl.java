package com.example.bigevent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bigevent.dto.category.CategoryAddDTO;
import com.example.bigevent.entity.Category;
import com.example.bigevent.exception.BusinessException;
import com.example.bigevent.mapper.CategoryMapper;
import com.example.bigevent.service.CategoryService;
import com.example.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public void add(CategoryAddDTO categoryAddDTO) {
        Long userId = ThreadLocalUtil.get("id", Long.class);

        LambdaQueryWrapper<Category> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Category::getCategoryName, categoryAddDTO.getCategoryName())
                .eq(Category::getCreateUser, userId);
        if (categoryMapper.selectCount(nameWrapper) > 0) {
            throw new BusinessException("分类名称已被占用");
        }

        LambdaQueryWrapper<Category> aliasWrapper = new LambdaQueryWrapper<>();
        aliasWrapper.eq(Category::getCategoryAlias, categoryAddDTO.getCategoryAlias())
                .eq(Category::getCreateUser, userId);
        if (categoryMapper.selectCount(aliasWrapper) > 0) {
            throw new BusinessException("分类别名已被占用");
        }

        Category category = new Category();
        category.setCategoryName(categoryAddDTO.getCategoryName());
        category.setCategoryAlias(categoryAddDTO.getCategoryAlias());
        category.setCreateUser(userId);

        categoryMapper.insert(category);
    }
}
