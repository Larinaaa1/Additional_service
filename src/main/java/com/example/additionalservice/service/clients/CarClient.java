package com.example.additionalservice.service.clients;

import com.example.additionalservice.model.Car;
import com.example.additionalservice.model.Rental;
import com.example.additionalservice.service.statistics.Obility2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.additionalservice.ApiProperties;
import org.springframework.web.client.RestTemplate;


import java.util.Arrays;
import java.util.List;

@Component
public class CarClient {

    @Autowired
    private RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    public CarClient(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Value("${main.service.url}")
    private String baseUrl;

    public List<Car> getAllCars() {
        long start = System.currentTimeMillis();
        try {
            String url = baseUrl + "/cars";
            Car[] cars = restTemplate.getForObject(url, Car[].class);
            List<Car> temp = cars != null ? Arrays.asList(cars) : List.of();
            return temp;
        }
        finally {
            Obility2.recordTiming("getAllCars", System.currentTimeMillis() - start);
        }

    }

    public Car getCarById(Long id) {
        long start = System.currentTimeMillis();
        try {
            String url = baseUrl + "/cars/" + id;
            Car temp = restTemplate.getForObject(url, Car.class);
            return temp;
        }
        finally {
            Obility2.recordTiming("getCarById", System.currentTimeMillis() - start);
        }

    }

    public List<Car> getCarsByCity(String city) {
        long start = System.currentTimeMillis();
        try {
            String url = baseUrl + "/cars?city=" + city;
            Car[] cars = restTemplate.getForObject(url, Car[].class);
            List<Car> temp = cars != null ? Arrays.asList(cars) : List.of();
            return temp;
        }
        finally {
            Obility2.recordTiming("getCarsByCity", System.currentTimeMillis() - start);
        }

    }
}