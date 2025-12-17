package com.jee.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "currencies")
public class Currency implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String code;  // Currency code (USD, EUR, GBP, etc.)
    private BigDecimal rate;  // Exchange rate relative to USD
    private LocalDateTime lastUpdate;  // Last update timestamp

    public Currency() {
        this.lastUpdate = LocalDateTime.now();
    }

    public Currency(String code, BigDecimal rate) {
        this.code = code;
        this.rate = rate;
        this.lastUpdate = LocalDateTime.now();
    }

    public Currency(String id, String code, BigDecimal rate, LocalDateTime lastUpdate) {
        this.id = id;
        this.code = code;
        this.rate = rate;
        this.lastUpdate = lastUpdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
        this.lastUpdate = LocalDateTime.now();
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", rate=" + rate +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}

