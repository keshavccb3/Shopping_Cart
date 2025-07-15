package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ecom.model.Product;

public interface ProductService {
	public Product saveProduct(Product product);
	public List<Product> getAllProducts();
	public Boolean deleteProduct(Integer id);
	public Product getProductById(int id);
	public List<Product> getAllActiveProducts(String category);
	public List<Product> searchProduct(String ch);
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category);
	public Page<Product> getAllProductPagination(Integer pageNo, Integer pageSize);
	Page<Product> searchProductPaginated(String keyword, int pageNo, int pageSize);
	public Boolean existsProduct(String title, String category);
	public void InActiveByCategory(int id);
	public void ActiveByCategory(int id);
}
