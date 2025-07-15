package com.ecom.service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;

import jakarta.mail.MessagingException;

public interface OrderService {
	public void saveOrder(Integer userId, OrderRequest orderRequest) throws UnsupportedEncodingException, MessagingException;

	public List<ProductOrder> getOrdersByUsers(Integer id);

	public ProductOrder updateOrderStatus(Integer id, String status);
	
	public List<ProductOrder> getAllOrders();

	public List<ProductOrder> searchOrder(String ch);

	public Page<ProductOrder> getAllOrderPagination(Integer pageNo, Integer pageSize);
	Page<ProductOrder> searchOrderPaginated(String keyword, int pageNo, int pageSize);

	public Page<ProductOrder> getAllOrderPagination1(UserDtls user, Pageable pageable);
}
