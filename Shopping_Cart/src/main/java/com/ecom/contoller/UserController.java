package com.ecom.contoller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private CartService cartService;
	@Autowired
	private OrderService orderService; 
	@Autowired
	private CommonUtil commonUtil;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		String email = p.getName();
		if(p!=null) {
			UserDtls user = userService.findByEmail(email);
			m.addAttribute("user", user);
			Integer countCart = cartService.getCountCart(user.getId());
			m.addAttribute("countCart",countCart);
		}
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("categories", categories);
	}
	
	@GetMapping("/")
	public String home() {
		return "user/home";
	}
	
	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if(carts.size()>0) {
			m.addAttribute("totalOrderPrice", carts.get(carts.size()-1).getTotalOrderPrice());
		}
		return "/user/cart";
	}
	
	@GetMapping("/cartQuantityUpdate")
	public String cartQuantityUpdate(@RequestParam String sy, @RequestParam Integer cid) {
		cartService.updateQuantity(sy,cid);
		return "redirect:/user/cart";
	}
	
	@GetMapping("/orders")
	public String orders(Principal p, Model m) {
		UserDtls user = getLoggedInUserDetails(p);
		List<Cart> carts = cartService.getCartsByUser(user.getId());
		m.addAttribute("carts", carts);
		if(carts.size()>0) {
			m.addAttribute("orderPrice", carts.get(carts.size()-1).getTotalOrderPrice());
			m.addAttribute("totalOrderPrice", carts.get(carts.size()-1).getTotalOrderPrice()+250+100);
		}
		return "/user/order";
	}
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request, Principal p) throws UnsupportedEncodingException, MessagingException {
		//System.out.println(request);
		UserDtls user = getLoggedInUserDetails(p);
		orderService.saveOrder(user.getId(), request);
		cartService.updateStock(user);
		cartService.removeCart(user);
		return "redirect:/user/success";
	}
	
	@GetMapping("/success")
	public String loadSuccessOrder() {
		return "/user/success";
	}

	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		
		return userService.findByEmail(email);
	}
	@GetMapping("/user-orders")
	public String myOrders(Principal p, Model m,
	                       @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	                       @RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize) {

	    UserDtls user = getLoggedInUserDetails(p);

	    Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "orderDate"));

	    Page<ProductOrder> page = orderService.getAllOrderPagination1(user, pageable);

	    List<ProductOrder> orders = page.getContent();

	    m.addAttribute("orders", orders);
	    m.addAttribute("ordersSize", orders.size());
	    m.addAttribute("pageNo", page.getNumber());
	    m.addAttribute("pageSize", page.getSize());
	    m.addAttribute("totalElements", page.getTotalElements());
	    m.addAttribute("totalPages", page.getTotalPages());
	    m.addAttribute("isFirst", page.isFirst());
	    m.addAttribute("isLast", page.isLast());

	    return "/user/my_orders";
	}

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) throws UnsupportedEncodingException, MessagingException {
		OrderStatus[] values = OrderStatus.values();
		String status = null;
		for(OrderStatus orderSt: values) {
			if(orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}
		ProductOrder updateOrder = orderService.updateOrderStatus(id,status);
		commonUtil.sendMailForProductOrder(updateOrder, status);
		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status updated Successfully");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/user/user-orders";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return "user/profile";
	}
	
	@PostMapping("/update-profile")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) throws IOException{
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if(ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		}else {
			session.setAttribute("succMsg", "Profile updated Successfully");
		}
		return "redirect:/user/profile";
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p, HttpSession session) {
		UserDtls user = getLoggedInUserDetails(p);
		boolean matches =  passwordEncoder.matches(currentPassword,user.getPassword());
		if(matches) {
			String encodePassword = passwordEncoder.encode(newPassword);
			user.setPassword(encodePassword);
			userService.updateUser(user);
			session.setAttribute("succMsg", "Password changed Successfully");
		}else {
			session.setAttribute("errorMsg", "Current Password is incorrect");
		}
		return "redirect:/user/profile";
	}
	
}
