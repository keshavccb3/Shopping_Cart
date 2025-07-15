package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ecom.model.Category;
import com.ecom.model.Product;

public interface CategoryService {
	public Category saveCategory(Category category);
	public List<Category> getAllCategory();
	public Boolean existCategory(String name);
	public Boolean deleteCategory(int id);
	public Category getCategoryById(int id);
	public List<Category> getAllActiveCategory();
	public Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize);
	public Boolean existsCategory(String name);
}
