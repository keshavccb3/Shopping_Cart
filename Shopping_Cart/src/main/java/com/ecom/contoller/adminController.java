package com.ecom.contoller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;
import com.ecom.util.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class adminController {
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;

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
		if (p != null) {
			UserDtls user = userService.findByEmail(email);
			m.addAttribute("user", user);
			Integer countCart = cartService.getCountCart(user.getId());
			m.addAttribute("countCart", countCart);
		}
		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("categories", categories);
	}

	@GetMapping("/")
	public String index() {
		return "admin/index";
	}

	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
	}

	@GetMapping("/category")
	public String category(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize) {
		// m.addAttribute("categories", categoryService.getAllCategory());
		Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
		List<Category> categories = page.getContent();
		m.addAttribute("categories", categories);
		m.addAttribute("categoriesSize", categories.size());
		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", page.getSize());
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {
		Boolean exists = categoryService.existCategory(category.getName());
		if(exists) {
			session.setAttribute("errorMsg", "Category is already exists");
		}else {
			String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
			category.setImageName(imageName);
			Boolean categoryExist = categoryService.existCategory(category.getName());
			if (categoryExist) {
				session.setAttribute("errorMsg", "Category Name already exists");
			} else {
				Category saveCategory = categoryService.saveCategory(category);
				if (ObjectUtils.isEmpty(saveCategory)) {
					session.setAttribute("errorMsg", "Not saved! internal server error");
				} else {
					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
							+ file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					session.setAttribute("succMsg", "Saved successfully!");
				}
				if(saveCategory.getIsActive()==true) {
					productService.ActiveByCategory(saveCategory.getId());
				}
			}
		}
		

		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		productService.InActiveByCategory(id);
		Boolean deleteCategory = categoryService.deleteCategory(id);
		
		if (deleteCategory) {
			session.setAttribute("succMsg", "Category deleted Successfully");
		} else {
			session.setAttribute("errorMsg", "Something wroong on server");
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "/admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {
		Category oldCategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();
		if (!ObjectUtils.isEmpty(category)) {
			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}
		Category updateCategory = categoryService.saveCategory(oldCategory);
		if(category.getIsActive()==true) {
			productService.ActiveByCategory(updateCategory.getId());
		}else {
			productService.InActiveByCategory(updateCategory.getId());
		}
		if (!ObjectUtils.isEmpty(updateCategory)) {
			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("succMsg", "Saved successfully!");
			}
			session.setAttribute("succMsg", "Category updated Successfully");
		} else {
			session.setAttribute("errorMsg", "Something wroong on server");
		}

		return "redirect:/admin/loadEditCategory/" + category.getId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session) throws IOException {
		Boolean exists = productService.existsProduct(product.getTitle(), product.getCategory());
		if(exists) {
			session.setAttribute("errorMsg", "Product is already exists");
		}else {
			String imageName = !image.isEmpty() ? image.getOriginalFilename() : "default.jpg";
			product.setImage(imageName);
			product.setDiscount(0);
			product.setDiscountPrice(product.getPrice());
			Product saveProduct = productService.saveProduct(product);
			if (ObjectUtils.isEmpty(saveProduct)) {
				session.setAttribute("errorMsg", "Not saved! internal server error");
			} else {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
						+ image.getOriginalFilename());
				Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				session.setAttribute("succMsg", "Saved successfully!");
			}
		}
		

		return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products")
	public String loadViewProducts(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize) {
		//m.addAttribute("products", productService.getAllProducts());
		Page<Product> page = productService.getAllProductPagination(pageNo, pageSize);
		List<Product> products = page.getContent();
		m.addAttribute("products",products);
		m.addAttribute("productsSize", products.size());
		m.addAttribute("pageNo",page.getNumber());
		m.addAttribute("pageSize", page.getSize());
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "/admin/products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		if (deleteProduct) {
			session.setAttribute("succMsg", "Product deleted Successfully");
		} else {
			session.setAttribute("errorMsg", "Something wroong on server");
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "/admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {
		Product oldProduct = productService.getProductById(product.getId());
		String imageName = file.isEmpty() ? oldProduct.getImage() : file.getOriginalFilename();
		if (!ObjectUtils.isEmpty(product) && (product.getDiscount() < 0 || product.getDiscount() > 100)) {
			session.setAttribute("errorMsg", "Invalid Discount");
		} else {
			if (!ObjectUtils.isEmpty(product)) {
				oldProduct.setTitle(product.getTitle());
				oldProduct.setDescription(product.getDescription());
				oldProduct.setCategory(product.getCategory());
				oldProduct.setPrice(product.getPrice());
				oldProduct.setStock(product.getStock());
				oldProduct.setImage(imageName);
				oldProduct.setDiscount(product.getDiscount());
				oldProduct
						.setDiscountPrice(product.getPrice() - (product.getPrice()) * (product.getDiscount()) / 100.0);
				oldProduct.setIsActive(product.getIsActive());
			}
			Product updateProduct = productService.saveProduct(oldProduct);
			if (!ObjectUtils.isEmpty(updateProduct)) {
				if (!file.isEmpty()) {
					File saveFile = new ClassPathResource("static/img").getFile();
					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
							+ file.getOriginalFilename());
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					session.setAttribute("succMsg", "Update successfully!");
				}
				session.setAttribute("succMsg", "Product updated Successfully");
			} else {
				session.setAttribute("errorMsg", "Something wroong on server");
			}

		}
		return "redirect:/admin/editProduct/" + product.getId();
	}

	@GetMapping("/users")
	public String getAllUsers(Model m) {
		List<UserDtls> users = userService.getUsers("ROLE_USER");
		m.addAttribute("users", users);
		return "/admin/users";
	}

	@GetMapping("/updateSts")
	public String updateUserAcountStatus(@RequestParam Boolean status, @RequestParam Integer id, HttpSession session) {
		Boolean f = userService.updateAccountStatus(id, status);
		if (f) {
			session.setAttribute("succMsg", "Status updated Successfully");
		} else {
			session.setAttribute("errorMsg", "Something wroong on server");
		}
		return "redirect:/admin/users";
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize) {
		//m.addAttribute("orders", orderService.getAllOrders());
		Page<ProductOrder> page = orderService.getAllOrderPagination(pageNo, pageSize);
		List<ProductOrder> orders = page.getContent();
		m.addAttribute("orders",orders);
		m.addAttribute("ordersSize", orders.size());
		m.addAttribute("pageNo",page.getNumber());
		m.addAttribute("pageSize", page.getSize());
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());
		return "/admin/orders";
	}

	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session)
			throws UnsupportedEncodingException, MessagingException {
		OrderStatus[] values = OrderStatus.values();
		String status = null;
		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}
		if (status == null) {
			session.setAttribute("errorMsg", "Select a option for update status");
		} else {

			ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
			commonUtil.sendMailForProductOrder(updateOrder, status);
			if (!ObjectUtils.isEmpty(updateOrder)) {
				session.setAttribute("succMsg", "Status updated Successfully");
			} else {
				session.setAttribute("errorMsg", "Something wrong on server");
			}
		}

		return "redirect:/admin/orders";
	}

	@GetMapping("/search-order")
	public String searchOrder(@RequestParam String ch,
	                          @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	                          @RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize,
	                          Model m) {
	    Page<ProductOrder> page;

	    if (ch.isEmpty()) {
	        page = orderService.getAllOrderPagination(pageNo, pageSize);
	    } else {
	        page = orderService.searchOrderPaginated(ch, pageNo, pageSize);
	    }

	    List<ProductOrder> orders = page.getContent();
	    m.addAttribute("orders", orders);
	    m.addAttribute("ordersSize", orders.size());
	    m.addAttribute("pageNo", page.getNumber());
	    m.addAttribute("pageSize", page.getSize());
	    m.addAttribute("totalElements", page.getTotalElements());
	    m.addAttribute("totalPages", page.getTotalPages());
	    m.addAttribute("isFirst", page.isFirst());
	    m.addAttribute("isLast", page.isLast());
	    m.addAttribute("searchQuery", ch);

	    return "/admin/orders";
	}


	@GetMapping("/search-product")
	public String searchProduct(@RequestParam String ch,
	                            @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
	                            @RequestParam(name = "pageSize", defaultValue = "8") Integer pageSize,
	                            Model m) {
		Page<Product> page;

		if (ch.isEmpty()) {
			page = productService.getAllProductPagination(pageNo, pageSize);
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

		return "/admin/products";
	}
	
	@GetMapping("/add-admin")
	public String loadAddAdmin() {
		return "/admin/add_admin";
	}
	
	@PostMapping("/save-admin")
	public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
			throws IOException {
		Boolean existsEmail = userService.existsEmail(user.getEmail());
		if(existsEmail) {
			session.setAttribute("errorMsg", "Email is already exists");
		}else {
			String imageName = !file.isEmpty() ? file.getOriginalFilename() : "default.jpg";
			user.setProfileImage(imageName);
			UserDtls saveUser = userService.saveAdmin(user);
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
		
		return "redirect:/admin/add-admin";
	}
	
	@GetMapping("/profile")
	public String profile() {
		return "admin/profile";
	}
	
	@PostMapping("/update-profile")
	public String saveUser(@ModelAttribute UserDtls user, @RequestParam MultipartFile img, HttpSession session) throws IOException{
		UserDtls updateUserProfile = userService.updateUserProfile(user, img);
		if(ObjectUtils.isEmpty(updateUserProfile)) {
			session.setAttribute("errorMsg", "Profile not updated");
		}else {
			session.setAttribute("succMsg", "Profile updated Successfully");
		}
		return "redirect:/admin/profile";
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
		return "redirect:/admin/profile";
	}
	private UserDtls getLoggedInUserDetails(Principal p) {
		String email = p.getName();
		
		return userService.findByEmail(email);
	}

}
