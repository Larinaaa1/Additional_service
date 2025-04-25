package com.example.additionalservice.service;

import com.example.additionalservice.model.Car;
import com.example.additionalservice.model.Rental;
import com.example.additionalservice.service.CarCacheService;
import com.example.additionalservice.service.clients.CarClient;
import com.example.additionalservice.service.clients.RentalClient;
import dto.CarDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {
    private final CarClient carClient;
    private final RentalClient rentalClient;
    private final CarCacheService carCacheService;

    public StatsService(CarClient carClient, RentalClient rentalClient, CarCacheService carCacheService) {
        this.carClient = carClient;
        this.rentalClient = rentalClient;
        this.carCacheService = carCacheService;
    }

    public List<CarDTO> getAvailableCars(String city, LocalDate startDate, LocalDate endDate) {
        if (city == null || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }

        // Получаем все аренды
        List<Rental> allRentals = Optional.ofNullable(rentalClient.getAllRentals()).orElse(Collections.emptyList());

        // Группируем аренды по ID машины
        Map<Long, List<Rental>> rentalsByCarId = allRentals.stream()
                .filter(r -> r.getCar() != null)
                .collect(Collectors.groupingBy(r -> r.getCar().getId()));

        // Получаем ID всех машин в городе
        List<Long> carIdsInCity = carClient.getCarsByCity(city).stream()
                .map(Car::getId)
                .collect(Collectors.toList());

        List<CarDTO> result = new ArrayList<>();

        for (Long carId : carIdsInCity) {
            Car car = carCacheService.getCarById(carId);
            if (car == null || !city.equalsIgnoreCase(car.getCity())) {
                continue;                                                  // пропускаем, если машина не найдена или из другого города
            }

            List<Rental> carRentals = rentalsByCarId.getOrDefault(carId, Collections.emptyList());

            if (isAvailable(carRentals, startDate, endDate)) {
                result.add(convertToCarDTO(car));
            }
        }

        return result;
    }

    private boolean isAvailable(List<Rental> rentals, LocalDate startDate, LocalDate endDate) {
        return rentals.stream().noneMatch(rental ->
                isOverlapping(startDate, endDate, rental.getStartDate(), rental.getEndDate())
        );
    }

    private boolean isOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !end1.isBefore(start2) && !start1.isAfter(end2);
    }

    private CarDTO convertToCarDTO(Car car) {
        CarDTO dto = new CarDTO();
        dto.setId(car.getId());
        dto.setVin(car.getVin());
        dto.setModel(car.getModel());
        dto.setColor(car.getColor());
        dto.setRentalCostPerDay(car.getRentalCostPerDay());
        dto.setCity(car.getCity());
        dto.setSalonName(car.getSalonName());
        return dto;
    }
}
