package com.example.additionalservice.service.clients;

import com.example.additionalservice.model.Rental;
import com.example.additionalservice.service.statistics.Obility2;
import dto.CarDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;

@Component
public class RentalClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RentalClient(RestTemplate restTemplate,
                        @Value("${main.service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<Rental> getAllRentals() {
        long start = System.currentTimeMillis();
        try {
            String url = baseUrl + "/rentals";
            Rental[] rentals = restTemplate.getForObject(url, Rental[].class);
            List<Rental> temp = rentals != null ? Arrays.asList(rentals) : List.of();
            return temp;
        }
        finally {
            Obility2.recordTiming("getAllRentals", System.currentTimeMillis() - start);
        }

    }

    public List<Rental> getRentalsByCarId(Long carId) {
        long start = System.currentTimeMillis();
        try {
            String url = baseUrl + "/rentals/car/" + carId;
            Rental[] rentals = restTemplate.getForObject(url, Rental[].class);
            List<Rental> temp = rentals != null ? Arrays.asList(rentals) : List.of();
            return temp;
        }
        finally {
            Obility2.recordTiming("getRentalsByCarId", System.currentTimeMillis() - start);
        }

    }

    public List<Rental> findOverlappingRentals(LocalDate startDate, LocalDate endDate) {
        long start = System.currentTimeMillis();
        try {
            String url = String.format("%s/rentals/overlapping?startDate=%s&endDate=%s",
                    baseUrl, startDate, endDate);
            Rental[] rentals = restTemplate.getForObject(url, Rental[].class);
            List<Rental> temp = rentals != null ? Arrays.asList(rentals) : List.of();
            return temp;
        }
        finally {
            Obility2.recordTiming("findOverlappingRentals", System.currentTimeMillis() - start);
        }

    }
}
