package com.ecom.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.service.CategoryService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private CategoryService categoryService;

	@GetMapping({ "/", " " })
	public String index() {
		return "admin/index";
	}

	@GetMapping("/loadAddProduct")
	public String loadAddProduct() {
		return "admin/add_product";
	}

	@GetMapping("/category")
	public String category() {
		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) {

		try {
			// 1. Handle file upload
			String imageName = "default.jpg";
			if (file != null && !file.isEmpty()) {
				imageName = file.getOriginalFilename();

				// Save file in local "uploads" folder
				Path uploadPath = Paths.get("uploads/");
				if (!Files.exists(uploadPath)) {
					Files.createDirectories(uploadPath);
				}
				file.transferTo(uploadPath.resolve(imageName));
			}

			// 2. Set image name into category
			category.setImageName(imageName);

			// 3. Check duplicate category
			if (categoryService.existCategory(category.getName())) 
			{
				session.setAttribute("errorMsg", "Category name already exists. Please try another.");
			} 
			else 
			{
				Category savedCategory = categoryService.saveCategory(category);
				if (ObjectUtils.isEmpty(savedCategory)) 
				{
					File saveFile = new ClassPathResource("static/img").getFile();
					
					Path path = Paths.get(saveFile.getAbsolutePath()+ File.separator+"category_img"+File.separator+file.getOriginalFilename());
					
					System.out.println(path);
					
					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					
					session.setAttribute("errorMsg", "Not saved! Internal server error.");
				} 
				else 
				{
					session.setAttribute("successMsg", "Category saved successfully.");
				}
			}
		} 
		catch (Exception e) 
		{
			session.setAttribute("errorMsg", "Something went wrong: " + e.getMessage());
		}

		return "redirect:/admin/category";
	}
}