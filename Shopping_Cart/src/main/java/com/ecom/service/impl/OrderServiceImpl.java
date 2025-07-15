package com.ecom.service.impl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.OrderRepository;
import com.ecom.service.OrderService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.mail.MessagingException;

@Service
public class OrderServiceImpl implements OrderService{
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private CartRepository cartRepository;
	@Autowired
	private CommonUtil commonUtil;

	@Override
	public void saveOrder(Integer userId, OrderRequest orderRequest) throws UnsupportedEncodingException, MessagingException {
		List<Cart> carts = cartRepository.findByUserId(userId);
		for(Cart cart: carts) {
			ProductOrder order = new ProductOrder();
			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());
			order.setProduct(cart.getProduct());
			order.setPrice(cart.getProduct().getDiscountPrice());
			order.setQuantity(cart.getQuantity());
			order.setUser(cart.getUser());
			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());
			OrderAddress address = new OrderAddress();
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			address.setEmail(orderRequest.getEmail());
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setMobileNumber(orderRequest.getMobileNumber());
			address.setPincode(orderRequest.getPincode());
			address.setState(orderRequest.getState());
			order.setOrderAddress(address);
			ProductOrder saveOrder = orderRepository.save(order);
			commonUtil.sendMailForProductOrder(saveOrder, "Success");
		}
		return;
	}

	@Override
	public List<ProductOrder> getOrdersByUsers(Integer id) {
		return orderRepository.findByUserId(id);
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String status) {
		Optional<ProductOrder> findById = orderRepository.findById(id);
		if(findById.isPresent()) {
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder order = orderRepository.save(productOrder);
			return order;
		}
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	public List<ProductOrder> searchOrder(String ch) {
		return orderRepository.findByOrderIdExactIgnoringSpaces(ch);
	}

	@Override
	public Page<ProductOrder> getAllOrderPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return orderRepository.findAll(pageable);
	}

	@Override
	public Page<ProductOrder> searchOrderPaginated(String keyword, int pageNo, int pageSize) {
	    Pageable pageable = PageRequest.of(pageNo, pageSize);
	    return orderRepository.findByOrderIdContainingIgnoreCaseOrUserNameContainingIgnoreCase(keyword, keyword, pageable);
	}

	@Override
	public Page<ProductOrder> getAllOrderPagination1(UserDtls user, Pageable pageable) {
		return orderRepository.findByUser(user, pageable);
	}


}
