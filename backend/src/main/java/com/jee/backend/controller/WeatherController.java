package com.jee.backend.controller;

import com.jee.backend.model.Weather;
import com.jee.backend.service.WeatherService;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Weather", description = "API de gestion de la météo avec cache Redis dynamique par saison")
public class WeatherController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Operation(
            summary = "Récupérer la météo d'une ville",
            description = "Récupère les données météorologiques pour une ville donnée. " +
                    "Utilise le cache Redis avec TTL dynamique selon la saison : " +
                    "5 minutes en été (juin-août), 30 minutes en hiver (décembre-février), " +
                    "15 minutes en printemps/automne. " +
                    "Les hits et misses du cache sont loggés automatiquement."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Données météorologiques récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = Weather.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nom de ville invalide"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ville non trouvée"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur lors de la récupération des données météo"
            )
    })
    @GetMapping("/{city}")
    public ResponseEntity<?> getWeather(
            @Parameter(description = "Nom de la ville", required = true, example = "Paris")
            @PathVariable String city) {
        try {
            logger.info("GET /api/weather/{} - Request received", city);

            if (city == null || city.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bad Request");
                error.put("message", "City name cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Weather weather = weatherService.getWeather(city);
            
            logger.info("GET /api/weather/{} - Success: temp={}°C, humidity={}%", 
                    city, weather.getTemp(), weather.getHumidity());
            
            return ResponseEntity.ok(weather);

        } catch (IllegalArgumentException e) {
            logger.error("GET /api/weather/{} - Validation error: {}", city, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            logger.error("GET /api/weather/{} - Error: {}", city, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getClass().getSimpleName());
            error.put("message", e.getMessage());
            
            // Déterminer le code HTTP approprié selon le type d'erreur
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage() != null && e.getMessage().contains("authentication")) {
                status = HttpStatus.UNAUTHORIZED;
            }
            
            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            logger.error("GET /api/weather/{} - Unexpected error: {}", city, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @Operation(
            summary = "Forcer la mise à jour du cache météo",
            description = "Force la mise à jour des données météorologiques pour une ville donnée " +
                    "en récupérant les données depuis l'API externe et en mettant à jour le cache Redis. " +
                    "Cette opération utilise @CachePut pour mettre à jour le cache avec les nouvelles données. " +
                    "Le TTL du cache sera recalculé selon la saison actuelle."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cache mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = Weather.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nom de ville invalide"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ville non trouvée"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur lors de la mise à jour du cache"
            )
    })
    @PostMapping("/refresh/{city}")
    public ResponseEntity<?> refreshWeather(
            @Parameter(description = "Nom de la ville à rafraîchir", required = true, example = "Paris")
            @PathVariable String city) {
        try {
            logger.info("POST /api/weather/refresh/{} - Refresh request received", city);

            if (city == null || city.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Bad Request");
                error.put("message", "City name cannot be null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Weather weather = weatherService.refreshWeather(city);
            
            logger.info("POST /api/weather/refresh/{} - Cache refreshed: temp={}°C, humidity={}%", 
                    city, weather.getTemp(), weather.getHumidity());
            
            return ResponseEntity.ok(weather);

        } catch (IllegalArgumentException e) {
            logger.error("POST /api/weather/refresh/{} - Validation error: {}", city, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            logger.error("POST /api/weather/refresh/{} - Error: {}", city, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getClass().getSimpleName());
            error.put("message", e.getMessage());
            
            // Déterminer le code HTTP approprié selon le type d'erreur
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (e.getMessage() != null && e.getMessage().contains("authentication")) {
                status = HttpStatus.UNAUTHORIZED;
            }
            
            return ResponseEntity.status(status).body(error);
        } catch (Exception e) {
            logger.error("POST /api/weather/refresh/{} - Unexpected error: {}", city, e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

