package com.ecom.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ecom.model.Product;

public interface ProductRepository extends JpaRepository<Product,Integer>{

	List<Product> findByIsActiveTrue();

	List<Product> findByCategory(String category);

	List<Product> findByCategoryAndIsActiveTrue(String category);

	@Query(value = """
		    SELECT * FROM product 
		    WHERE LOWER(REPLACE(title, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:ch, ' ', ''), '%'))
		       OR LOWER(REPLACE(category, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:ch, ' ', ''), '%'))
		""", nativeQuery = true)
	List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2);


	Page<Product> findByCategoryAndIsActiveTrue(String category, Pageable pageable);

	Page<Product> findByIsActiveTrue(Pageable pageable);
	Page<Product> findByTitleContainingIgnoreCase(String title, Pageable pageable);

	Boolean existsByTitleAndCategory(String title, String category);

	void deleteByCategory(String categoryName);
	
}
