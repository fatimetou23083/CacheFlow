package com.jee.backend.service;

import com.jee.backend.model.Product;
import com.jee.backend.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(value = "products", unless = "#result == null")
    public List<Product> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            // S'assurer de retourner une liste non-null
            return products != null ? new ArrayList<>(products) : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error in getAllProducts: " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur, retourner une liste vide plutôt que null
            return new ArrayList<>();
        }
    }

    @Cacheable(value = "product", key = "#id")
    public Optional<Product> getProductById(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return Optional.empty();
            }
            return productRepository.findById(id);
        } catch (Exception e) {
            System.err.println("Error in getProductById: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public Product createProduct(Product product) {
        try {
            if (product == null) {
                throw new IllegalArgumentException("Product cannot be null");
            }
            // S'assurer que l'ID est null pour la création (MongoDB générera un nouvel ID)
            product.setId(null);
            return productRepository.save(product);
        } catch (Exception e) {
            System.err.println("Error in createProduct: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create product: " + e.getMessage(), e);
        }
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public Product updateProduct(String id, Product product) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Product ID cannot be null or empty");
            }
            if (product == null) {
                throw new IllegalArgumentException("Product cannot be null");
            }
            // Vérifier si le produit existe
            Optional<Product> existingProduct = productRepository.findById(id);
            if (existingProduct.isEmpty()) {
                throw new RuntimeException("Product with id " + id + " not found");
            }
            product.setId(id);
            return productRepository.save(product);
        } catch (Exception e) {
            System.err.println("Error in updateProduct: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    @CacheEvict(value = {"products", "product"}, allEntries = true)
    public void deleteProduct(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Product ID cannot be null or empty");
            }
            // Vérifier si le produit existe
            if (!productRepository.existsById(id)) {
                throw new RuntimeException("Product with id " + id + " not found");
            }
            productRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("Error in deleteProduct: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete product: " + e.getMessage(), e);
        }
    }
}
