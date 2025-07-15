package com.ecom.service;

import java.util.List;

import com.ecom.model.Cart;
import com.ecom.model.UserDtls;

public interface CartService {
	public Cart saveCart(Integer productId, Integer userId);
	public List<Cart> getCartsByUser(Integer userId);
	public Integer getCountCart(Integer id);
	public void updateQuantity(String sy, Integer cid);
	public void removeCart(UserDtls user);
	public void updateStock(UserDtls user);
}
