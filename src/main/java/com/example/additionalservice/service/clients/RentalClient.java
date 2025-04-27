package com.example.additionalservice.service.clients;

import com.example.additionalservice.model.Rental;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.additionalservice.service.statistics.ObservabilityService;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;

@Component
public class RentalClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final ObservabilityService observabilityService;

    public RentalClient(RestTemplate restTemplate,
                        @Value("${main.service.url}") String baseUrl,
                        ObservabilityService observabilityService) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.observabilityService = observabilityService;
    }

    public List<Rental> getAllRentals() {
        this.observabilityService.start(getClass().getSimpleName() + ":getAllRentals");
        String url = baseUrl + "/rentals";
        Rental[] rentals = restTemplate.getForObject(url, Rental[].class);
        List<Rental> temp = rentals != null ? Arrays.asList(rentals) : List.of();
        this.observabilityService.stop(getClass().getSimpleName() + ":getAllRentals");
        return temp;
    }

    public List<Rental> getRentalsByCarId(Long carId) {
        this.observabilityService.start(getClass().getSimpleName() + ":getRentalsByCarId");
        String url = baseUrl + "/rentals/car/" + carId;
        Rental[] rentals = restTemplate.getForObject(url, Rental[].class);
        List<Rental> temp = rentals != null ? Arrays.asList(rentals) : List.of();
        this.observabilityService.stop(getClass().getSimpleName() + ":getRentalsByCarId");
        return temp;
    }

    public List<Rental> findOverlappingRentals(LocalDate startDate, LocalDate endDate) {
        this.observabilityService.start(getClass().getSimpleName() + ":findOverlappingRentals");
        String url = String.format("%s/rentals/overlapping?startDate=%s&endDate=%s",
                baseUrl, startDate, endDate);
        Rental[] rentals = restTemplate.getForObject(url, Rental[].class);
        List<Rental> temp = rentals != null ? Arrays.asList(rentals) : List.of();
        this.observabilityService.stop(getClass().getSimpleName() + ":findOverlappingRentals");
        return temp;
    }
}
