package com.example.additionalservice.service.clients;

import com.example.additionalservice.model.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.additionalservice.ApiProperties;
import org.springframework.web.client.RestTemplate;
import com.example.additionalservice.service.statistics.ObservabilityService;

import java.util.Arrays;
import java.util.List;

@Component
public class CarClient {

    @Autowired
    private RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private final ObservabilityService observabilityService;
    public CarClient(ApiProperties apiProperties, ObservabilityService observabilityService) {
        this.apiProperties = apiProperties;
        this.observabilityService = observabilityService;
    }

    @Value("${main.service.url}")
    private String baseUrl;

    public List<Car> getAllCars() {
        this.observabilityService.start(getClass().getSimpleName() + ":getAllCars");
        String url = baseUrl + "/cars";
        Car[] cars = restTemplate.getForObject(url, Car[].class);
        List<Car> temp = cars != null ? Arrays.asList(cars) : List.of();
        this.observabilityService.stop(getClass().getSimpleName() + ":getAllCars");
        return temp;
    }

    public Car getCarById(Long id) {
        this.observabilityService.start(getClass().getSimpleName() + ":getCarById");
        String url = baseUrl + "/cars/" + id;
        Car temp = restTemplate.getForObject(url, Car.class);
        this.observabilityService.stop(getClass().getSimpleName() + ":getCarById");
        return temp;
    }

    public List<Car> getCarsByCity(String city) {
        this.observabilityService.start(getClass().getSimpleName() + ":getCarsByCity");
        String url = baseUrl + "/cars?city=" + city;
        Car[] cars = restTemplate.getForObject(url, Car[].class);
        List<Car> temp = cars != null ? Arrays.asList(cars) : List.of();
        this.observabilityService.stop(getClass().getSimpleName() + ":getCarsByCity");
        return temp;
    }
}