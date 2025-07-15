package com.ecom.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Product;
import com.ecom.model.Category;
import com.ecom.repository.CategoryRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.service.ProductService;

import jakarta.transaction.Transactional;

@Service
public class ProductServiceImpl implements ProductService{
	
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public Product saveProduct(Product product) {
		
		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@Override
	public Boolean deleteProduct(Integer id) {
		Product product = productRepository.findById(id).orElse(null);
		if(ObjectUtils.isEmpty(product)) {
			return false;
		}else {
			productRepository.delete(product);
			return true;
		}
	}

	@Override
	public Product getProductById(int id) {
		
		Product product = productRepository.findById(id).orElse(null);
		return product;
	}

	@Override
	public List<Product> getAllActiveProducts(String category) {
		List<Product> products = null;
		if(ObjectUtils.isEmpty(category)) {
			products = productRepository.findByIsActiveTrue();
		}else {
			products = productRepository.findByCategoryAndIsActiveTrue(category);
		}
		return products;
	}

	@Override
	public List<Product> searchProduct(String ch) {
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,ch);
	}

	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {
		Page<Product> pageProduct = null;
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		if(ObjectUtils.isEmpty(category)) {
			pageProduct = productRepository.findByIsActiveTrue(pageable);
		}else {
			pageProduct = productRepository.findByCategoryAndIsActiveTrue(category,pageable);
		}
		return pageProduct;
	}

	@Override
	public Page<Product> getAllProductPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findAll(pageable);
	}
	
	@Override
	public Page<Product> searchProductPaginated(String keyword, int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findByTitleContainingIgnoreCase(keyword, pageable);
	}

	@Override
	public Boolean existsProduct(String title, String category) {
		return productRepository.existsByTitleAndCategory(title,category);
	}

	@Override
	public void InActiveByCategory(int id) {
		Category category = categoryRepository.findById(id).orElse(null);
		List<Product> products = productRepository.findByCategory(category.getName());
		for (Product product : products) {
		    product.setIsActive(false);
		    productRepository.save(product);
		}
	}
	@Override
	public void ActiveByCategory(int id) {
		Category category = categoryRepository.findById(id).orElse(null);
		List<Product> products = productRepository.findByCategory(category.getName());
		for (Product product : products) {
		    product.setIsActive(true);
		    productRepository.save(product);
		}
	}

}
