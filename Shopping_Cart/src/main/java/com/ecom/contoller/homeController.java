package com.ecom.contoller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class homeController {
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private CartRepository cartRepository;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDtls user = userService.findByEmail(email);
			Integer countCart = cartService.getCountCart(user.getId());
			m.addAttribute("countCart", countCart);
			m.addAttribute("user", user);
		}
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("categories", categories);
	}

	@GetMapping("/")
	public String index(Model m) {
		List<Category> allCategories = categoryService.getAllActiveCategory();
		Collections.reverse(allCategories);
		List<Category> categories = allCategories.subList(0, Math.min(6, allCategories.size()));

		List<Product> allProducts = productService.getAllActiveProducts("");
		Collections.reverse(allProducts);
		List<Product> products = allProducts.subList(0, Math.min(8, allProducts.size()));
		m.addAttribute("categories", categories);
		m.addAttribute("products", products);
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/products")
	public String product(Model m, @RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize) {
		m.addAttribute("categories", categoryService.getAllActiveCategory());
		// m.addAttribute("products",productService.getAllActiveProducts(category));
		m.addAttribute("paramValue", category);
		Page<Product> page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		List<Product> products = page.getContent();
		m.addAttribute("products", products);
		m.addAttribute("productsSize", products.size());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", page.getSize());
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "product";
	}

	@GetMapping("/product/{id}")
	public String product_detail(@PathVariable int id, Model m, Principal p) {
		UserDtls user = getLoggedInUserDetails(p);
		Product productById = productService.getProductById(id);
		m.addAttribute("product", productById);

		boolean isAddDisabled = false;

		if (user != null) {
			List<Cart> carts = cartService.getCartsByUser(user.getId());
			m.addAttribute("carts", carts);
			for (Cart cart : carts) {
				if (cart.getProduct().getId() == id && cart.getQuantity() >= productById.getStock()) {
					isAddDisabled = true;
					break;
				}
			}
		}

		m.addAttribute("isAddDisabled", isAddDisabled);
		return "product_detail";
	}

	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
			throws IOException {

		Boolean existsEmail = userService.existsEmail(user.getEmail());
		if (existsEmail) {
			session.setAttribute("errorMsg", "Email is already exists");
		} else {
			String imageName = !file.isEmpty() ? file.getOriginalFilename() : "default.jpg";
			user.setProfileImage(imageName);
			UserDtls saveUser = userService.saveUser(user);
			if (!ObjectUtils.isEmpty(saveUser)) {
				if (!file.isEmpty()) {
					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
							+ file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				}
				session.setAttribute("succMsg", "Saved successfully!");
			} else {
				session.setAttribute("errorMsg", "Not saved! internal server error");
			}
		}

		return "redirect:/register";
	}

	@GetMapping("/forget-password")
	public String showForgetPassword() {
		return "forget_password";
	}

	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {
		UserDtls user = userService.getUserByToken(token);
		if (user == null) {
			m.addAttribute("msg", "Your link is Invalid or Expired!");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-password")
	public String processResetPassword(@RequestParam String token, @RequestParam String password, HttpSession session,
			Model m) {
		UserDtls user = userService.getUserByToken(token);
		if (user == null) {
			m.addAttribute("msg", "Your link is Invalid or Expired!");
			return "message";
		} else {
			user.setPassword(passwordEncoder.encode(password));
			user.setResetToken(null);
			userService.updateUser(user);
			m.addAttribute("msg", "Password changed successfully");
			return "message";
		}
	}

	@PostMapping("/forget-password")
	public String processForgetPassword(@RequestParam String email, HttpSession session, HttpServletRequest request)
			throws UnsupportedEncodingException, MessagingException {
		UserDtls user = userService.findByEmail(email);
		if (ObjectUtils.isEmpty(user)) {
			session.setAttribute("errorMsg", "Invalid Email");
		} else {
			String token = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, token);
			String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + token;
			Boolean sendMail = commonUtil.sendMail(url, email);
			if (sendMail) {
				session.setAttribute("succMsg", "Mail send successfully! Please check your mail");
			} else {
				session.setAttribute("errorMsg", "Internal server error! Please try again");
			}
		}
		return "redirect:/forget-password";
	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, HttpSession session) {
		Cart cart = cartService.saveCart(pid, uid);
		if (ObjectUtils.isEmpty(cart)) {
			session.setAttribute("errorMsg", "Failed to add!!");
		} else {
			session.setAttribute("succMsg", "Successfully Added!!");
		}
		return "redirect:/product/" + pid;
	}

	@GetMapping("/search")
	public String search(@RequestParam String ch, @RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize, Model m) {

		m.addAttribute("paramValue", category);
		m.addAttribute("categories", categoryService.getAllActiveCategory());

		Page<Product> page;

		if (ch.isEmpty()) {
			page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
		} else {
			page = productService.searchProductPaginated(ch, pageNo, pageSize);
		}

		List<Product> products = page.getContent();

		m.addAttribute("products", products);
		m.addAttribute("productsSize", products.size());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", page.getSize());
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		m.addAttribute("searchQuery", ch);

		return "product";
	}
	private UserDtls getLoggedInUserDetails(Principal p) {
		if (p == null) {
	        return null;
	    }
		String email = p.getName();
		return userService.findByEmail(email);
	}

}
