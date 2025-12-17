package com.jee.backend.controller;

import com.jee.backend.model.Currency;
import com.jee.backend.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currencies")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Currencies", description = "API de gestion des devises avec cache Redis distribué et auto-refresh")
public class CurrencyController {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Operation(
            summary = "Récupérer le taux de change entre deux devises",
            description = "Récupère le taux de change entre deux devises. " +
                    "Utilise le cache Redis distribué avec clé '#from+#to' et TTL de 1 heure. " +
                    "Les taux sont automatiquement rafraîchis toutes les heures."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Taux de change récupéré avec succès",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Code devise invalide"
            )
    })
    @GetMapping("/{from}/{to}")
    public ResponseEntity<?> getExchangeRate(
            @Parameter(description = "Code devise source (ex: USD, EUR)", required = true, example = "USD")
            @PathVariable String from,
            @Parameter(description = "Code devise cible (ex: EUR, GBP)", required = true, example = "EUR")
            @PathVariable String to) {
        try {
            logger.info("GET /api/currencies/{}/{} - Request received", from, to);

            if (from == null || to == null || from.trim().isEmpty() || to.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bad Request");
                error.put("message", "Currency codes cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            BigDecimal rate = currencyService.getExchangeRate(from, to);
            
            Map<String, Object> response = new HashMap<>();
            response.put("from", from.toUpperCase());
            response.put("to", to.toUpperCase());
            response.put("rate", rate);
            
            logger.info("GET /api/currencies/{}/{} - Success: rate={}", from, to, rate);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("GET /api/currencies/{}/{} - Validation error: {}", from, to, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("GET /api/currencies/{}/{} - Error: {}", from, to, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
            summary = "Convertir un montant d'une devise à une autre",
            description = "Convertit un montant d'une devise source vers une devise cible. " +
                    "Utilise le cache Redis distribué avec clé '#from+#to' et TTL de 1 heure."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Conversion effectuée avec succès",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Paramètres invalides"
            )
    })
    @GetMapping("/{from}/{to}/{amount}")
    public ResponseEntity<?> convert(
            @Parameter(description = "Code devise source", required = true, example = "USD")
            @PathVariable String from,
            @Parameter(description = "Code devise cible", required = true, example = "EUR")
            @PathVariable String to,
            @Parameter(description = "Montant à convertir", required = true, example = "100")
            @PathVariable BigDecimal amount) {
        try {
            logger.info("GET /api/currencies/{}/{}/{} - Conversion request received", from, to, amount);

            if (from == null || to == null || from.trim().isEmpty() || to.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bad Request");
                error.put("message", "Currency codes cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bad Request");
                error.put("message", "Amount must be positive");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            BigDecimal convertedAmount = currencyService.convert(from, to, amount);
            BigDecimal rate = currencyService.getExchangeRate(from, to);
            
            Map<String, Object> response = new HashMap<>();
            response.put("from", from.toUpperCase());
            response.put("to", to.toUpperCase());
            response.put("amount", amount);
            response.put("convertedAmount", convertedAmount);
            response.put("rate", rate);
            
            logger.info("GET /api/currencies/{}/{}/{} - Success: {} {} = {} {}", 
                    from, to, amount, amount, from.toUpperCase(), convertedAmount, to.toUpperCase());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("GET /api/currencies/{}/{}/{} - Validation error: {}", from, to, amount, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("GET /api/currencies/{}/{}/{} - Error: {}", from, to, amount, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
            summary = "Récupérer toutes les devises",
            description = "Récupère la liste de toutes les devises disponibles avec leurs taux de change. " +
                    "Utilise le cache Redis avec TTL de 1 heure. " +
                    "Les données sont mises en cache pour améliorer les performances."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des devises récupérée avec succès",
                    content = @Content(schema = @Schema(implementation = Currency.class))
            )
    })
    @GetMapping("/all")
    public ResponseEntity<?> getAllCurrencies() {
        try {
            logger.info("GET /api/currencies/all - Request received");

            List<Currency> currencies = currencyService.getAllCurrencies();
            
            logger.info("GET /api/currencies/all - Success: {} currencies retrieved", currencies.size());
            return ResponseEntity.ok(currencies);

        } catch (Exception e) {
            logger.error("GET /api/currencies/all - Error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
            summary = "Forcer la mise à jour des taux de change",
            description = "Force la mise à jour des taux de change en rafraîchissant les données " +
                    "et en invalidant le cache Redis. " +
                    "Cette opération simule un appel à une API externe et met à jour les taux. " +
                    "Le cache sera automatiquement rafraîchi toutes les heures."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Taux de change mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshRates() {
        try {
            logger.info("POST /api/currencies/refresh - Refresh request received");

            currencyService.refreshRates();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Exchange rates refreshed successfully");
            response.put("cache", "cleared");
            
            logger.info("POST /api/currencies/refresh - Success: Rates refreshed and cache cleared");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("POST /api/currencies/refresh - Error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
            summary = "Récupérer les devises supportées",
            description = "Retourne la liste des codes de devises supportées par l'API."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des devises supportées",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    @GetMapping("/supported")
    public ResponseEntity<?> getSupportedCurrencies() {
        try {
            logger.info("GET /api/currencies/supported - Request received");

            Map<String, Object> response = new HashMap<>();
            response.put("supportedCurrencies", currencyService.getSupportedCurrencies());
            response.put("count", currencyService.getSupportedCurrencies().size());
            
            logger.info("GET /api/currencies/supported - Success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("GET /api/currencies/supported - Error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

