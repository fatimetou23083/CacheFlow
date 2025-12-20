package com.jee.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @JsonProperty("main")
    private Main main;

    @JsonProperty("name")
    private String name;

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main implements Serializable {
        
        private static final long serialVersionUID = 1L;

        @JsonProperty("temp")
        private Double temp;

        @JsonProperty("humidity")
        private Double humidity;

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
    }
}

