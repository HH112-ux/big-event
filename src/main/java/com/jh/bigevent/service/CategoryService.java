package com.jh.bigevent.service;

import com.jh.bigevent.dto.category.CategoryAddDTO;
import com.jh.bigevent.dto.category.CategoryUpdateDTO;
import com.jh.bigevent.entity.Category;

import java.util.List;

public interface CategoryService {

    void add(CategoryAddDTO categoryAddDTO);

    List<Category> list();

    Category detail(Long id);

    void update(CategoryUpdateDTO categoryUpdateDTO);
}
