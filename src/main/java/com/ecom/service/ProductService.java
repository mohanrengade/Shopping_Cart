package com.ecom.service;

import org.springframework.stereotype.Service;

import com.ecom.model.Product;

@Service
public interface ProductService 
{
	public Product saveProduct(Product product);
}