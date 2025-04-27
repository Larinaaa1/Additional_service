package com.example.additionalservice.service;

import com.example.additionalservice.model.Car;
import com.example.additionalservice.model.Rental;
import com.example.additionalservice.service.CarCacheService;
import com.example.additionalservice.service.clients.CarClient;
import com.example.additionalservice.service.clients.RentalClient;
import com.example.additionalservice.service.statistics.ObservabilityService;
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
    private final ObservabilityService observabilityService;

    public StatsService(CarClient carClient, RentalClient rentalClient, CarCacheService carCacheService, ObservabilityService observabilityService) {
        this.carClient = carClient;
        this.rentalClient = rentalClient;
        this.carCacheService = carCacheService;
        this.observabilityService = observabilityService;
    }

    public List<CarDTO> getAvailableCars(String city, LocalDate startDate, LocalDate endDate) {
        if (city == null || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }
        this.observabilityService.start(getClass().getSimpleName() + ":getAvailableCars");
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
        this.observabilityService.stop(getClass().getSimpleName() + ":getAvailableCars");
        return temp;
    }



    private boolean isAvailable(List<Rental> rentals, LocalDate startDate, LocalDate endDate) {
        this.observabilityService.start(getClass().getSimpleName() + ":isAvailable");
        boolean temp = rentals.stream().noneMatch(rental ->
                isOverlapping(startDate, endDate, rental.getStartDate(), rental.getEndDate())
        );
        this.observabilityService.stop(getClass().getSimpleName() + ":isAvailable");
        return temp;
    }

    private boolean isOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        this.observabilityService.start(getClass().getSimpleName() + ":isOverlapping");
        boolean temp = !end1.isBefore(start2) && !start1.isAfter(end2);
        this.observabilityService.stop(getClass().getSimpleName() + ":isOverlapping");
        return temp;
    }

    private CarDTO convertToCarDTO(Car car) {
        this.observabilityService.start(getClass().getSimpleName() + ":convertToCarDTO");
        CarDTO dto = new CarDTO();
        dto.setId(car.getId());
        dto.setVin(car.getVin());
        dto.setModel(car.getModel());
        dto.setColor(car.getColor());
        dto.setRentalCostPerDay(car.getRentalCostPerDay());
        dto.setCity(car.getCity());
        dto.setSalonName(car.getSalonName());
        CarDTO temp = dto;
        this.observabilityService.stop(getClass().getSimpleName() + ":convertToCarDTO");
        return temp;
    }
}