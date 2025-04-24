package com.example.additionalservice.service.clients;

import com.example.additionalservice.model.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class CarClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${main.service.url}")
    private String baseUrl;

    public List<Car> getAllCars() {
        String url = baseUrl + "/cars";
        Car[] cars = restTemplate.getForObject(url, Car[].class);
        return cars != null ? Arrays.asList(cars) : List.of();
    }

    public Car getCarById(Long id) {
        String url = baseUrl + "/cars/" + id;
        return restTemplate.getForObject(url, Car.class);
    }

    public List<Car> getCarsByCity(String city) {
        String url = baseUrl + "/cars?city=" + city;
        Car[] cars = restTemplate.getForObject(url, Car[].class);
        return cars != null ? Arrays.asList(cars) : List.of();
    }
}