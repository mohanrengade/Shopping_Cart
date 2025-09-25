package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.impl.ProductServiceImpl;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final ProductServiceImpl productServiceImpl;
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	AdminController(ProductServiceImpl productServiceImpl) {
		this.productServiceImpl = productServiceImpl;
	}

	@GetMapping({ "/", " " })
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
	public String category(Model m) {
		m.addAttribute("category", categoryService.getAllCategory());
		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) {
		try {
			String imageName = "default.jpg";

			if (file != null && !file.isEmpty()) {
				imageName = file.getOriginalFilename();

				// Save uploaded image in /static/img/category/
				File saveFile = new ClassPathResource("static/img/category_img/").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imageName);

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			// Set image name into DB object
			category.setImageName(imageName);

			// Check duplicate category
			if (categoryService.existCategory(category.getName())) {
				session.setAttribute("errorMsg", "Category name already exists. Please try another.");
			} else {
				Category savedCategory = categoryService.saveCategory(category);
				if (savedCategory != null) {
					session.setAttribute("successMsg", "Category saved successfully.");
				} else {
					session.setAttribute("errorMsg", "Not saved! Internal server error.");
				}
			}
		} catch (Exception e) {
			session.setAttribute("errorMsg", "Something went wrong: " + e.getMessage());
		}

		return "redirect:/admin/category";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);
		if (deleteCategory) {
			session.setAttribute("successMsg", "Category deleted successfully...");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server...");
		}
		return "redirect:/admin/category";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));

		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) {
		Category oldCategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(category)) {
			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}

		Category updateCategory = categoryService.saveCategory(oldCategory);

		if (!ObjectUtils.isEmpty(updateCategory)) {
			session.setAttribute("successMsg", "Category updated successfully...");
		} else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}

		return "redirect:/admin/loadEditCategory/" + category.getId();
	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			HttpSession session) {
		try {
			// 1️⃣ Set image name
			String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
			product.setImageName(imageName);

			// 2️⃣ Save product to DB
			System.out.println("Before:" +product);
			Product savedProduct = productService.saveProduct(product);
			System.out.println("After: "+product);
			// 3️⃣ Save uploaded image to folder
			if (savedProduct != null && !image.isEmpty()) {
				// Folder outside classpath (works in IDE and JAR)
				String uploadDir = System.getProperty("user.dir") + "/uploads/product_img/";
				Path uploadPath = Paths.get(uploadDir);

				// Create folder if it doesn't exist
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}

				// Save file
				Path filePath = uploadPath.resolve(imageName);
				Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			}

			// 4️⃣ Set success message
			if (savedProduct != null) {
				System.out.println("Saved product: " + savedProduct); // ID will be generated
				session.setAttribute("successMsg", "Product saved successfully!");
			} else {
				session.setAttribute("errorMsg", "Something went wrong on server");
			}

		} catch (Exception e) {
		    e.printStackTrace(); // Logs full stack trace in console
		    session.setAttribute("errorMsg", "Something went wrong: " + e.getMessage());
		}


		return "redirect:/admin/loadAddProduct";
	}
}