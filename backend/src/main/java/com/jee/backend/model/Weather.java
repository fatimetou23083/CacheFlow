package com.jee.backend.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Weather implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String city;
    private Double temp;
    private Double humidity;
    private LocalDateTime timestamp;

    public Weather() {
        this.timestamp = LocalDateTime.now();
    }

    public Weather(String city, Double temp, Double humidity) {
        this.city = city;
        this.temp = temp;
        this.humidity = humidity;
        this.timestamp = LocalDateTime.now();
    }

    public Weather(String city, Double temp, Double humidity, LocalDateTime timestamp) {
        this.city = city;
        this.temp = temp;
        this.humidity = humidity;
        this.timestamp = timestamp;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "city='" + city + '\'' +
                ", temp=" + temp +
                ", humidity=" + humidity +
                ", timestamp=" + timestamp +
                '}';
    }
}

