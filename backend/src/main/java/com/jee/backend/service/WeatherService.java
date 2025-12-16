package com.jee.backend.service;

import com.jee.backend.dto.OpenWeatherResponse;
import com.jee.backend.model.Weather;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String CACHE_NAME = "weather";

    private final RestTemplate restTemplate;

    @Value("${weather.api.url:https://api.openweathermap.org/data/2.5/weather}")
    private String weatherApiUrl;

    @Value("${weather.api.key:demo}")
    private String weatherApiKey;

    @Value("${weather.api.demo-mode:true}")
    private boolean demoMode;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Récupère la météo pour une ville donnée depuis le cache ou l'API externe
     * Utilise @Cacheable pour mettre en cache automatiquement les résultats
     */
    @Cacheable(value = CACHE_NAME, key = "#city.toLowerCase()", unless = "#result == null")
    public Weather getWeather(String city) {
        logger.info("Cache MISS - Fetching weather for city: {}", city);
        return fetchWeatherFromApi(city);
    }

    /**
     * Force la mise à jour du cache en récupérant les données depuis l'API externe
     * Utilise @CachePut pour mettre à jour le cache avec les nouvelles données
     */
    @CachePut(value = CACHE_NAME, key = "#city.toLowerCase()")
    public Weather refreshWeather(String city) {
        logger.info("Cache REFRESH - Forcing update for city: {}", city);
        return fetchWeatherFromApi(city);
    }

    /**
     * Récupère les données météo depuis l'API externe ou génère des données de
     * démonstration
     */
    private Weather fetchWeatherFromApi(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }

        // Mode démo : générer des données fictives pour les tests
        if (demoMode || "demo".equals(weatherApiKey)) {
            logger.info("DEMO MODE - Generating mock weather data for city: {}", city);
            return generateMockWeather(city);
        }

        try {
            String url = String.format("%s?q=%s&appid=%s&units=metric&lang=fr",
                    weatherApiUrl, city, weatherApiKey);

            logger.debug("Calling weather API: {}", url.replace(weatherApiKey, "***"));

            ResponseEntity<OpenWeatherResponse> response = restTemplate.getForEntity(
                    url, OpenWeatherResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                OpenWeatherResponse weatherResponse = response.getBody();
                OpenWeatherResponse.Main main = weatherResponse.getMain();

                if (main == null) {
                    logger.error("Invalid response from weather API: main data is null");
                    throw new RuntimeException("Invalid weather data received from API");
                }

                Weather weather = new Weather(
                        weatherResponse.getName() != null ? weatherResponse.getName() : city,
                        main.getTemp(),
                        main.getHumidity(),
                        LocalDateTime.now());

                logger.info("Successfully fetched weather for {}: temp={}°C, humidity={}%",
                        city, weather.getTemp(), weather.getHumidity());

                return weather;
            } else {
                logger.error("Unexpected response status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to fetch weather data: unexpected response status");
            }

        } catch (HttpClientErrorException.NotFound e) {
            logger.error("City not found: {}", city);
            throw new RuntimeException("City '" + city + "' not found. Please check the city name.", e);
        } catch (HttpClientErrorException.Unauthorized e) {
            logger.warn("Unauthorized access to weather API. Falling back to demo mode.");
            logger.info(
                    "To use real weather data, please configure WEATHER_API_KEY environment variable or set weather.api.key in application.yml");
            // Fallback vers le mode démo en cas d'erreur d'authentification
            return generateMockWeather(city);
        } catch (HttpClientErrorException e) {
            logger.error("Client error when fetching weather for {}: {}", city, e.getMessage());
            // Fallback vers le mode démo en cas d'erreur client
            logger.info("Falling back to demo mode due to client error");
            return generateMockWeather(city);
        } catch (HttpServerErrorException e) {
            logger.error("Server error when fetching weather for {}: {}", city, e.getMessage());
            throw new RuntimeException("Weather API server error. Please try again later.", e);
        } catch (RestClientException e) {
            logger.warn("Network error when fetching weather for {}: {}. Falling back to demo mode.", city,
                    e.getMessage());
            // Fallback vers le mode démo en cas d'erreur réseau
            return generateMockWeather(city);
        } catch (Exception e) {
            logger.error("Unexpected error when fetching weather for {}: {}", city, e.getMessage(), e);
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage(), e);
        }
    }

    /**
     * Génère des données météo fictives pour le mode démo
     * Les données varient selon le nom de la ville pour simuler un comportement
     * réaliste
     */
    private Weather generateMockWeather(String city) {
        // Générer des valeurs basées sur le hash de la ville pour avoir des données
        // cohérentes
        int cityHash = city.toLowerCase().hashCode();

        // Température entre 5°C et 30°C selon le hash
        double temp = 5 + (Math.abs(cityHash) % 25);

        // Humidité entre 30% et 90% selon le hash
        double humidity = 30 + (Math.abs(cityHash) % 60);

        Weather weather = new Weather(
                city,
                Math.round(temp * 10.0) / 10.0, // Arrondir à 1 décimale
                Math.round(humidity * 10.0) / 10.0,
                LocalDateTime.now());

        logger.info("Generated mock weather for {}: temp={}°C, humidity={}%",
                city, weather.getTemp(), weather.getHumidity());

        return weather;
    }
}
