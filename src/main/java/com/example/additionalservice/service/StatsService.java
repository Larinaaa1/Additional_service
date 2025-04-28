package com.example.additionalservice.service;

import com.example.additionalservice.model.Car;
import com.example.additionalservice.model.Rental;
import com.example.additionalservice.service.CarCacheService;
import com.example.additionalservice.service.clients.CarClient;
import com.example.additionalservice.service.clients.RentalClient;
import com.example.additionalservice.service.statistics.Obility2;
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
        long start = System.currentTimeMillis();
        try {
            // Получаем все аренды
            List<Rental> allRentals = Optional.ofNullable(rentalClient.getAllRentals())
                    .orElse(Collections.emptyList());

            // Получаем ID машин из аренд
            Set<Long> carIdsFromRentals = allRentals.stream()
                    .filter(r -> r.getCar() != null)
                    .map(r -> r.getCar().getId())
                    .collect(Collectors.toSet());

            // Сначала загружаем машины из аренд
            List<Car> carsToCheck = new ArrayList<>();

            for (Long carId : carIdsFromRentals) {
                Car car = carCacheService.getCarById(carId);
                if (car == null) {
                    car = carClient.getCarById(carId);
                    if (car != null) {
                        carCacheService.cacheCar(car);
                        carsToCheck.add(car);
                    }
                } else {
                    carsToCheck.add(car);
                }
            }

            // Теперь загружаем все машины
            List<Car> allCars = Optional.ofNullable(carClient.getAllCars()).orElse(Collections.emptyList());

            for (Car car : allCars) {
                if (!carCacheService.isCarCached(car.getId())) {
                    carCacheService.cacheCar(car);
                }

                // Добавляем в список, если её ещё нет (избегаем дублей)
                if (!carIdsFromRentals.contains(car.getId())) {
                    carsToCheck.add(car);
                }
            }

            // Фильтрация по городу
            List<Car> filteredByCity = carsToCheck.stream()
                    .filter(car -> city.trim().equalsIgnoreCase(car.getCity().trim()))
                    .toList();

            // Группировка аренд по машинам
            Set<Long> filteredCarIds = filteredByCity.stream()
                    .map(Car::getId)
                    .collect(Collectors.toSet());

            Map<Long, List<Rental>> rentalsByCarId = allRentals.stream()
                    .filter(r -> r.getCar() != null && filteredCarIds.contains(r.getCar().getId()))
                    .collect(Collectors.groupingBy(r -> r.getCar().getId()));

            // Фильтрация по доступности
            List<CarDTO> temp = filteredByCity.stream()
                    .filter(car -> {
                        List<Rental> carRentals = rentalsByCarId.getOrDefault(car.getId(), Collections.emptyList());
                        return isAvailable(carRentals, startDate, endDate);
                    })
                    .map(this::convertToCarDTO)
                    .collect(Collectors.toList());
            return temp;
        }
        finally {
            Obility2.recordTiming("getAvailableCars", System.currentTimeMillis() - start);
        }

    }


    private boolean isAvailable(List<Rental> rentals, LocalDate startDate, LocalDate endDate) {
        long start = System.currentTimeMillis();
        try{
            boolean temp = rentals.stream().noneMatch(rental ->
                    isOverlapping(startDate, endDate, rental.getStartDate(), rental.getEndDate())
            );
            return temp;
        }
        finally {
            Obility2.recordTiming("isAvailable", System.currentTimeMillis() - start);
        }
    }

    private boolean isOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        long start = System.currentTimeMillis();
        try{
            boolean temp = !end1.isBefore(start2) && !start1.isAfter(end2);
            return temp;
        }
        finally {
            Obility2.recordTiming("isOverlapping", System.currentTimeMillis() - start);
        }
    }

    private CarDTO convertToCarDTO(Car car) {
        long start = System.currentTimeMillis();
        try {
            CarDTO dto = new CarDTO();
            dto.setId(car.getId());
            dto.setVin(car.getVin());
            dto.setModel(car.getModel());
            dto.setColor(car.getColor());
            dto.setRentalCostPerDay(car.getRentalCostPerDay());
            dto.setCity(car.getCity());
            dto.setSalonName(car.getSalonName());
            CarDTO temp = dto;
            return temp;
        }
        finally {
            Obility2.recordTiming("isOverlapping", System.currentTimeMillis() - start);
        }
    }
}