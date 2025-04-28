package com.example.additionalservice.controller;

import com.example.additionalservice.service.StatsService;
import com.example.additionalservice.service.statistics.Obility2;
import dto.CarDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    public StatsController( StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/availability")
    public List<CarDTO> getAvailableCarInfo(

        @RequestParam String city,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate) {
        long start = System.currentTimeMillis();
        try {
            List<CarDTO> temp = statsService.getAvailableCars(city, startDate, endDate);

            return temp;
        }
        finally {
            Obility2.recordTiming("getAvailableCarInfo", System.currentTimeMillis() - start);
        }

    }
}

