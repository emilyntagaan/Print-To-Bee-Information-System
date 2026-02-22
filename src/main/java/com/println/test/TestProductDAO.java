package com.println.test;

import com.println.dao.ProductDAO;
import com.println.model.Product;
import java.math.BigDecimal;
import java.util.List;

public class TestProductDAO {
    public static void main(String[] args) {
        ProductDAO dao = new ProductDAO();

        // --- Add a new product ---
        Product p = new Product();
        p.setProductName("Custom Print T-Shirt");
        p.setDescription("High quality cotton t-shirt with custom print");
        p.setCategory("Clothing");

        // FIXED: setPrice requires a BigDecimal
        p.setPrice(new BigDecimal("250.00"));   // BEST practice

        p.setUnit("piece");
        p.setMaterialUsed("Cotton");
        p.setQuantityUsed(100);
        p.setReorderLevel(10);
        p.setStatus("Active");
        p.setAddedBy(1);
        p.setPrintTime("2 hours");
        p.setSize("Medium");
        p.setNotes("Initial product sample");

        boolean added = dao.addProduct(p);
        System.out.println(added ? "✅ Product added!" : "Failed to add product.");

        // --- Read all products ---
        List<Product> list = dao.getAllProducts();
        for (Product prod : list) {
            System.out.println(prod);
        }

        // --- Search products ---
        System.out.println("\n🔍 Search results for 'shirt':");
        List<Product> searchResults = dao.searchProducts("shirt");
        for (Product prod : searchResults) {
            System.out.println(prod);
        }
    }
}
