package com.example.additionalservice.controller;

import com.example.additionalservice.service.StatsService;
import dto.CarDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.example.additionalservice.service.statistics.ObservabilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService statsService;
    private final ObservabilityService observabilityService;
    @Autowired
    public StatsController(ObservabilityService observabilityService, StatsService statsService) {
        this.observabilityService = observabilityService;
        this.statsService = statsService;
    }

    @GetMapping("/availability")
    public List<CarDTO> getAvailableCarInfo(

        @RequestParam String city,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) {
        this.observabilityService.start(getClass().getSimpleName() + ":getAvailableCarInfo");
        List<CarDTO> temp = statsService.getAvailableCars(city, startDate, endDate);
        this.observabilityService.stop(getClass().getSimpleName() + ":getAvailableCarInfo");
        return temp;
    }
}

