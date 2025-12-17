package com.jee.backend.repository;

import com.jee.backend.model.Currency;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends MongoRepository<Currency, String> {
    
    /**
     * Find currency by code (e.g., "USD", "EUR")
     */
    Optional<Currency> findByCode(String code);
}

