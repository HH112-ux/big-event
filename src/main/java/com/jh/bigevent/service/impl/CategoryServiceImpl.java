package com.jh.bigevent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jh.bigevent.dto.category.CategoryAddDTO;
import com.jh.bigevent.dto.category.CategoryUpdateDTO;
import com.jh.bigevent.entity.Category;
import com.jh.bigevent.exception.BusinessException;
import com.jh.bigevent.mapper.CategoryMapper;
import com.jh.bigevent.service.CategoryService;
import com.jh.bigevent.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<Category> list() {
        Long userId = ThreadLocalUtil.get("id", Long.class);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getCreateUser, userId)
                .orderByDesc(Category::getCreateTime);

        return categoryMapper.selectList(wrapper);
    }

    @Override
    public Category detail(Long id) {
        Long userId = ThreadLocalUtil.get("id", Long.class);

        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getCreateUser().equals(userId)) {
            throw new BusinessException("只能查询自己创建的分类");
        }
        return category;
    }

    @Override
    public void update(CategoryUpdateDTO categoryUpdateDTO) {
        Long userId = ThreadLocalUtil.get("id", Long.class);

        Category category = categoryMapper.selectById(categoryUpdateDTO.getId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getCreateUser().equals(userId)) {
            throw new BusinessException("只能修改自己创建的分类");
        }

        LambdaQueryWrapper<Category> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(Category::getCategoryName, categoryUpdateDTO.getCategoryName())
                .eq(Category::getCreateUser, userId)
                .ne(Category::getId, categoryUpdateDTO.getId());
        if (categoryMapper.selectCount(nameWrapper) > 0) {
            throw new BusinessException("分类名称已被占用");
        }

        LambdaQueryWrapper<Category> aliasWrapper = new LambdaQueryWrapper<>();
        aliasWrapper.eq(Category::getCategoryAlias, categoryUpdateDTO.getCategoryAlias())
                .eq(Category::getCreateUser, userId)
                .ne(Category::getId, categoryUpdateDTO.getId());
        if (categoryMapper.selectCount(aliasWrapper) > 0) {
            throw new BusinessException("分类别名已被占用");
        }

        Category updateCategory = new Category();
        updateCategory.setId(categoryUpdateDTO.getId());
        updateCategory.setCategoryName(categoryUpdateDTO.getCategoryName());
        updateCategory.setCategoryAlias(categoryUpdateDTO.getCategoryAlias());

        categoryMapper.updateById(updateCategory);
    }
}
