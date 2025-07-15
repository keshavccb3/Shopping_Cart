package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Cart;

public interface CartRepository extends JpaRepository<Cart,Integer>{

	Cart findByProductIdAndUserId(Integer productId, Integer userId);

	Integer countByUserId(Integer id);

	List<Cart> findByUserId(Integer userId);

	void deleteByUserId(Integer id);
	
}
