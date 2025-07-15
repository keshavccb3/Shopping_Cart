package com.ecom.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;

public interface OrderRepository extends JpaRepository<ProductOrder,Integer>{

	List<ProductOrder> findByUserId(Integer id);

	@Query(value = """
		    SELECT * FROM product_order 
		    WHERE LOWER(REPLACE(order_id, ' ', '')) LIKE LOWER(CONCAT('%', REPLACE(:ch, ' ', ''), '%'))
		""", nativeQuery = true)
	List<ProductOrder> findByOrderIdContainingIgnoreCase(String ch);
	@Query(value = """
		    SELECT * FROM product_order 
		    WHERE REPLACE(order_id, ' ', '') = REPLACE(:orderId, ' ', '')
		""", nativeQuery = true)
		List<ProductOrder> findByOrderIdExactIgnoringSpaces(@Param("orderId") String orderId);

	Page<ProductOrder> findByOrderIdContainingIgnoreCaseOrUserNameContainingIgnoreCase(String keyword, String keyword2,
			Pageable pageable);

	Page<ProductOrder> findByUser(UserDtls user, Pageable pageable);

}
