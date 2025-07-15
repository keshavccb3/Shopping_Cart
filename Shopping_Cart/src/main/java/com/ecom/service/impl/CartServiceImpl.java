package com.ecom.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.CartService;

import jakarta.transaction.Transactional;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CartRepository cartRepository;

	@Override
	public Cart saveCart(Integer productId, Integer userId) {
		UserDtls user = userRepository.findById(userId).get();
		Product product = productRepository.findById(productId).get();
		Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);
		Cart cart = null;
		if (ObjectUtils.isEmpty(cartStatus)) {
			cart = new Cart();
			cart.setProduct(product);
			cart.setUser(user);
			cart.setQuantity(1);
			cart.setTotalPrice(product.getDiscountPrice());
		} else {
			cart = cartStatus;
			cart.setQuantity(cart.getQuantity() + 1);
			cart.setTotalPrice(cart.getQuantity() * product.getDiscountPrice());
		}
		return cartRepository.save(cart);
	}

	@Override
	public List<Cart> getCartsByUser(Integer userId) {
		List<Cart> carts = cartRepository.findByUserId(userId);

		double totalOrderPrice = 0.0;
		List<Cart> updateCarts = new ArrayList<>();
		for (Cart c : carts) {
			Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
			c.setTotalPrice(totalPrice);
			totalOrderPrice += totalPrice;
			c.setTotalOrderPrice(totalOrderPrice);
			updateCarts.add(c);
		}
		return updateCarts;

	}

	@Override
	public Integer getCountCart(Integer id) {
		return cartRepository.countByUserId(id);
	}

	@Override
	public void updateQuantity(String sy, Integer cid) {
		Cart cart = cartRepository.findById(cid).get();
		int update;
		if (sy.equalsIgnoreCase("de")) {
			update = cart.getQuantity() - 1;
			if (update <= 0) {
				cartRepository.deleteById(cid);
				return;
			}
		} else {
			update = cart.getQuantity() + 1;
		}
		cart.setQuantity(update);
		cartRepository.save(cart);
		return;
	}

	@Override
	@Transactional
	public void removeCart(UserDtls user) {
		cartRepository.deleteByUserId(user.getId());

	}

	@Override
	public void updateStock(UserDtls user) {
		List<Cart> carts = cartRepository.findByUserId(user.getId());
		for (Cart c : carts) {
			Product p = c.getProduct();
			Integer q = c.getQuantity();
			p.setStock(p.getStock()-q);
		}
	}

}
