package com.jee.backend.service;

import com.jee.backend.model.Currency;
import com.jee.backend.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    private static final String CURRENCY_CACHE = "currency";
    private static final String CURRENCIES_CACHE = "currencies";

    private final CurrencyRepository currencyRepository;

    // Mock exchange rates relative to USD (base currency)
    private static final Map<String, BigDecimal> MOCK_RATES = new HashMap<>();
    
    static {
        MOCK_RATES.put("USD", BigDecimal.ONE);  // Base currency
        MOCK_RATES.put("EUR", new BigDecimal("0.91"));  // 1 USD = 0.91 EUR
        MOCK_RATES.put("GBP", new BigDecimal("0.79"));  // 1 USD = 0.79 GBP
        MOCK_RATES.put("JPY", new BigDecimal("150.0"));  // 1 USD = 150 JPY
        MOCK_RATES.put("CAD", new BigDecimal("1.35"));  // 1 USD = 1.35 CAD
        MOCK_RATES.put("AUD", new BigDecimal("1.50"));  // 1 USD = 1.50 AUD
        MOCK_RATES.put("CHF", new BigDecimal("0.88"));  // 1 USD = 0.88 CHF
        MOCK_RATES.put("CNY", new BigDecimal("7.20"));  // 1 USD = 7.20 CNY
    }

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    /**
     * Initialize exchange rates in database on startup
     * Handles MongoDB connection errors gracefully
     */
    @PostConstruct
    public void initializeRates() {
        logger.info("Initializing currency exchange rates...");
        try {
            for (Map.Entry<String, BigDecimal> entry : MOCK_RATES.entrySet()) {
                try {
                    Optional<Currency> existing = currencyRepository.findByCode(entry.getKey());
                    if (existing.isEmpty()) {
                        Currency currency = new Currency(entry.getKey(), entry.getValue());
                        currencyRepository.save(currency);
                        logger.info("Initialized currency: {} with rate: {}", entry.getKey(), entry.getValue());
                    } else {
                        logger.debug("Currency {} already exists, skipping initialization", entry.getKey());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to initialize currency {}: {}. Will use mock rates.", entry.getKey(), e.getMessage());
                }
            }
            logger.info("Currency exchange rates initialized successfully");
        } catch (Exception e) {
            logger.warn("MongoDB not available for currency initialization. Currency service will use mock rates: {}", e.getMessage());
            logger.info("Currency service will continue to work with mock exchange rates");
        }
    }

    /**
     * Get exchange rate between two currencies
     * Uses @Cacheable with key "#from+#to" for distributed caching
     */
    @Cacheable(value = CURRENCY_CACHE, key = "#from.toUpperCase() + '+' + #to.toUpperCase()", unless = "#result == null")
    public BigDecimal getExchangeRate(String from, String to) {
        logger.info("Cache MISS - Fetching exchange rate from {} to {}", from, to);
        
        if (from == null || to == null) {
            throw new IllegalArgumentException("Currency codes cannot be null");
        }
        
        String fromUpper = from.toUpperCase();
        String toUpper = to.toUpperCase();
        
        if (fromUpper.equals(toUpper)) {
            return BigDecimal.ONE;
        }
        
        // Get rates relative to USD
        BigDecimal fromRate = getRateForCurrency(fromUpper);
        BigDecimal toRate = getRateForCurrency(toUpper);
        
        // Convert: toRate / fromRate
        // Example: EUR to GBP = (GBP/USD) / (EUR/USD) = 0.79 / 0.91 = 0.868
        BigDecimal exchangeRate = toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
        
        logger.info("Exchange rate from {} to {}: {}", fromUpper, toUpper, exchangeRate);
        return exchangeRate;
    }

    /**
     * Convert amount from one currency to another
     * Uses cached exchange rate from getExchangeRate method
     */
    public BigDecimal convert(String from, String to, BigDecimal amount) {
        logger.info("Converting {} {} to {}", amount, from, to);
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        // This will use the cached rate from getExchangeRate
        BigDecimal exchangeRate = getExchangeRate(from, to);
        BigDecimal convertedAmount = amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
        
        logger.info("Converted {} {} to {} {}", amount, from.toUpperCase(), convertedAmount, to.toUpperCase());
        return convertedAmount;
    }

    /**
     * Get all currencies with their rates
     * Cached for the /currencies/all endpoint
     */
    @Cacheable(value = CURRENCIES_CACHE, unless = "#result == null || #result.isEmpty()")
    public List<Currency> getAllCurrencies() {
        logger.info("Cache MISS - Fetching all currencies");
        List<Currency> currencies = currencyRepository.findAll();
        logger.info("Retrieved {} currencies", currencies.size());
        return currencies;
    }

    /**
     * Refresh exchange rates (simulates fetching from external API)
     * Clears cache and updates rates
     */
    @CacheEvict(value = {CURRENCY_CACHE, CURRENCIES_CACHE}, allEntries = true)
    public void refreshRates() {
        logger.info("Refreshing exchange rates - Clearing cache");
        
        // In a real scenario, this would fetch rates from an external API
        // For demo purposes, we'll just update the lastUpdate timestamp
        List<Currency> currencies = currencyRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        for (Currency currency : currencies) {
            // Simulate slight rate variation for demo (add small random variation)
            BigDecimal currentRate = currency.getRate();
            BigDecimal variation = new BigDecimal(Math.random() * 0.02 - 0.01); // ±1% variation
            BigDecimal newRate = currentRate.add(variation).setScale(2, RoundingMode.HALF_UP);
            
            // Ensure rate stays within reasonable bounds
            if (newRate.compareTo(BigDecimal.ZERO) > 0) {
                currency.setRate(newRate);
                currency.setLastUpdate(now);
                currencyRepository.save(currency);
                logger.info("Updated rate for {}: {} (was {})", currency.getCode(), newRate, currentRate);
            }
        }
        
        logger.info("Exchange rates refreshed successfully");
    }

    /**
     * Scheduled task to auto-refresh rates every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000 milliseconds
    public void scheduledRefresh() {
        logger.info("Scheduled auto-refresh triggered - Refreshing exchange rates");
        try {
            refreshRates();
            logger.info("Scheduled auto-refresh completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled auto-refresh: {}", e.getMessage(), e);
        }
    }

    /**
     * Get rate for a specific currency code
     * Returns rate relative to USD
     */
    private BigDecimal getRateForCurrency(String code) {
        Optional<Currency> currency = currencyRepository.findByCode(code);
        if (currency.isPresent()) {
            return currency.get().getRate();
        }
        
        // Fallback to mock rates if not in database
        BigDecimal rate = MOCK_RATES.get(code);
        if (rate != null) {
            logger.warn("Currency {} not found in database, using mock rate: {}", code, rate);
            return rate;
        }
        
        throw new IllegalArgumentException("Currency code not supported: " + code);
    }

    /**
     * Get supported currency codes
     */
    public Set<String> getSupportedCurrencies() {
        return MOCK_RATES.keySet();
    }
}

