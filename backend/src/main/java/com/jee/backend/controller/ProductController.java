package com.jee.backend.controller;

import com.jee.backend.model.Product;
import com.jee.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "API de gestion des produits avec cache Redis")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Récupérer tous les produits", description = "Récupère la liste de tous les produits. Utilise le cache Redis avec TTL de 10 minutes.")
    @ApiResponse(responseCode = "200", description = "Liste des produits récupérée avec succès",
            content = @Content(schema = @Schema(implementation = Product.class)))
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            // Toujours retourner une liste, même si elle est vide
            if (products == null) {
                products = java.util.Collections.emptyList();
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            System.err.println("Error in getAllProducts controller: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getClass().getSimpleName());
            error.put("message", "Failed to retrieve products");
            error.put("details", e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "ProductController is working");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debug() {
        Map<String, Object> debug = new HashMap<>();
        try {
            // Test direct du repository (sans cache)
            List<Product> productsFromDb = productService.getAllProducts();
            debug.put("productsFromService", productsFromDb != null ? productsFromDb.size() : 0);
            debug.put("productsList", productsFromDb);
            debug.put("status", "ok");
        } catch (Exception e) {
            debug.put("status", "error");
            debug.put("error", e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.ok(debug);
    }

    @Operation(summary = "Récupérer un produit par ID", description = "Récupère un produit spécifique par son identifiant. Utilise le cache Redis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produit trouvé",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(
            @Parameter(description = "ID du produit", required = true) @PathVariable String id) {
        try {
            Optional<Product> product = productService.getProductById(id);
            if (product.isPresent()) {
                return ResponseEntity.ok(product.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not Found");
                error.put("message", "Product with id " + id + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            System.err.println("Error in getProductById controller: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getClass().getSimpleName());
            error.put("message", "Failed to retrieve product");
            error.put("details", e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Créer un nouveau produit", description = "Crée un nouveau produit et invalide le cache Redis.")
    @ApiResponse(responseCode = "201", description = "Produit créé avec succès",
            content = @Content(schema = @Schema(implementation = Product.class)))
    @PostMapping
    public ResponseEntity<?> createProduct(
            @Parameter(description = "Données du produit à créer", required = true) @RequestBody Product product) {
        try {
            // Validation basique
            if (product == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bad Request");
                error.put("message", "Product data is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation Error");
                error.put("message", "Product name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation Error");
                error.put("message", "Product price must be positive");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Product createdProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            System.err.println("Error in createProduct controller: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getClass().getSimpleName());
            error.put("message", "Failed to create product");
            error.put("details", e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Mettre à jour un produit", description = "Met à jour un produit existant et invalide le cache Redis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produit mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "ID du produit", required = true) @PathVariable String id,
            @Parameter(description = "Données du produit à mettre à jour", required = true) @RequestBody Product product) {
        try {
            // Vérifier si le produit existe
            Optional<Product> existingProduct = productService.getProductById(id);
            if (existingProduct.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not Found");
                error.put("message", "Product with id " + id + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Validation basique
            if (product == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bad Request");
                error.put("message", "Product data is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation Error");
                error.put("message", "Product name is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation Error");
                error.put("message", "Product price must be positive");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            Product updatedProduct = productService.updateProduct(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            System.err.println("Error in updateProduct controller: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getClass().getSimpleName());
            error.put("message", "Failed to update product");
            error.put("details", e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(summary = "Supprimer un produit", description = "Supprime un produit et invalide le cache Redis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produit supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @Parameter(description = "ID du produit à supprimer", required = true) @PathVariable String id) {
        try {
            // Vérifier si le produit existe
            Optional<Product> existingProduct = productService.getProductById(id);
            if (existingProduct.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not Found");
                error.put("message", "Product with id " + id + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("Error in deleteProduct controller: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getClass().getSimpleName());
            error.put("message", "Failed to delete product");
            error.put("details", e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
