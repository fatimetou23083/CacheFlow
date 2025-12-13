package com.jee.backend.service;

import com.jee.backend.model.Product;
import com.jee.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(ProductServiceCacheTest.CacheTestConfig.class)
@ExtendWith(MockitoExtension.class)
class ProductServiceCacheTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProduct = new Product("1", "Test Product", new BigDecimal("99.99"), "Electronics");
        testProducts = Arrays.asList(
                testProduct,
                new Product("2", "Another Product", new BigDecimal("149.99"), "Books")
        );
    }

    @Test
    void testGetAllProducts_CacheHit() {
        // First call - should hit repository
        when(productRepository.findAll()).thenReturn(testProducts);
        List<Product> firstResult = productService.getAllProducts();
        
        // Second call - should use cache, repository should not be called again
        List<Product> secondResult = productService.getAllProducts();
        
        // Verify repository was called only once
        verify(productRepository, times(1)).findAll();
        
        // Verify results are the same
        assertEquals(testProducts.size(), firstResult.size());
        assertEquals(testProducts.size(), secondResult.size());
    }

    @Test
    void testGetProductById_CacheHit() {
        // First call - should hit repository
        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));
        Optional<Product> firstResult = productService.getProductById("1");
        
        // Second call - should use cache, repository should not be called again
        Optional<Product> secondResult = productService.getProductById("1");
        
        // Verify repository was called only once
        verify(productRepository, times(1)).findById("1");
        
        // Verify results are the same
        assertTrue(firstResult.isPresent());
        assertTrue(secondResult.isPresent());
        assertEquals(testProduct.getId(), firstResult.get().getId());
        assertEquals(testProduct.getId(), secondResult.get().getId());
    }

    @Test
    void testCreateProduct_CacheEviction() {
        // First, populate cache
        when(productRepository.findAll()).thenReturn(testProducts);
        productService.getAllProducts();
        verify(productRepository, times(1)).findAll();
        
        // Create a new product - should evict cache
        Product newProduct = new Product("3", "New Product", new BigDecimal("199.99"), "Clothing");
        when(productRepository.save(any(Product.class))).thenReturn(newProduct);
        productService.createProduct(newProduct);
        
        // Next call to getAllProducts should hit repository again (cache was evicted)
        productService.getAllProducts();
        verify(productRepository, times(2)).findAll();
    }

    @Test
    void testUpdateProduct_CacheEviction() {
        // First, populate cache
        when(productRepository.findById("1")).thenReturn(Optional.of(testProduct));
        productService.getProductById("1");
        verify(productRepository, times(1)).findById("1");
        
        // Update product - should evict cache
        Product updatedProduct = new Product("1", "Updated Product", new BigDecimal("129.99"), "Electronics");
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        productService.updateProduct("1", updatedProduct);
        
        // Next call to getProductById should hit repository again (cache was evicted)
        productService.getProductById("1");
        verify(productRepository, times(2)).findById("1");
    }

    @Test
    void testDeleteProduct_CacheEviction() {
        // First, populate cache
        when(productRepository.findAll()).thenReturn(testProducts);
        productService.getAllProducts();
        verify(productRepository, times(1)).findAll();
        
        // Delete product - should evict cache
        doNothing().when(productRepository).deleteById("1");
        productService.deleteProduct("1");
        
        // Next call to getAllProducts should hit repository again (cache was evicted)
        productService.getAllProducts();
        verify(productRepository, times(2)).findAll();
    }

    @Configuration
    @EnableCaching
    static class CacheTestConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("products", "product");
        }
    }
}
