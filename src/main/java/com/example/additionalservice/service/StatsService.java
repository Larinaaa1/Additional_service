package com.example.additionalservice.service;

import com.example.additionalservice.model.Car;
import com.example.additionalservice.model.Rental;
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
<<<<<<< Updated upstream

    public StatsService(CarClient carClient, RentalClient rentalClient) {
        this.carClient = carClient;
        this.rentalClient = rentalClient;
=======
    private final CarCacheService carCacheService;
    private final ObservabilityService observabilityService;

    public StatsService(CarClient carClient, RentalClient rentalClient, CarCacheService carCacheService, ObservabilityService observabilityService) {
        this.carClient = carClient;
        this.rentalClient = rentalClient;
        this.carCacheService = carCacheService;
        this.observabilityService = observabilityService;
>>>>>>> Stashed changes
    }

    /**
     * Возвращает список машин в указанном городе, свободных для аренды в заданный период
     */
    public List<CarDTO> getAvailableCars(String city, LocalDate startDate, LocalDate endDate) {
        if (city == null || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }

<<<<<<< Updated upstream
        // Получаем все машины
=======
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
>>>>>>> Stashed changes
        List<Car> allCars = Optional.ofNullable(carClient.getAllCars()).orElse(Collections.emptyList());

        // Фильтруем машины по городу
        List<Car> carsInCity = allCars.stream()
                .filter(car -> city.equalsIgnoreCase(car.getCity()))
                .collect(Collectors.toList());

        if (carsInCity.isEmpty()) return Collections.emptyList();

        // Получаем все аренды
        List<Rental> allRentals = Optional.ofNullable(rentalClient.getAllRentals()).orElse(Collections.emptyList());

        // Группируем аренды по ID машины
        Map<Long, List<Rental>> rentalsByCarId = allRentals.stream()
                .filter(r -> r.getCar() != null)
                .collect(Collectors.groupingBy(r -> r.getCar().getId()));

<<<<<<< Updated upstream
        // Оставляем только свободные машины
        return carsInCity.stream()
                .filter(car -> isAvailable(car.getId(), rentalsByCarId.getOrDefault(car.getId(), Collections.emptyList()), startDate, endDate))
=======
        // Фильтрация по доступности
        List<CarDTO> temp = filteredByCity.stream()
                .filter(car -> {
                    List<Rental> carRentals = rentalsByCarId.getOrDefault(car.getId(), Collections.emptyList());
                    return isAvailable(carRentals, startDate, endDate);
                })
>>>>>>> Stashed changes
                .map(this::convertToCarDTO)
                .collect(Collectors.toList());
        this.observabilityService.stop(getClass().getSimpleName() + ":getAvailableCars");
        return temp;
    }

<<<<<<< Updated upstream
    /**
     * Проверка, свободна ли машина на указанный период
     */
    private boolean isAvailable(Long carId, List<Rental> rentals, LocalDate startDate, LocalDate endDate) {
        return rentals.stream().noneMatch(rental ->
=======

    private boolean isAvailable(List<Rental> rentals, LocalDate startDate, LocalDate endDate) {
        this.observabilityService.start(getClass().getSimpleName() + ":isAvailable");
        boolean temp = rentals.stream().noneMatch(rental ->
>>>>>>> Stashed changes
                isOverlapping(startDate, endDate, rental.getStartDate(), rental.getEndDate())
        );
        this.observabilityService.stop(getClass().getSimpleName() + ":isAvailable");
        return temp;
    }

    /**
     * Проверка пересечения дат
     */
    private boolean isOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        this.observabilityService.start(getClass().getSimpleName() + ":isOverlapping");
        boolean temp = !end1.isBefore(start2) && !start1.isAfter(end2);
        this.observabilityService.stop(getClass().getSimpleName() + ":isOverlapping");
        return temp;
    }

    /**
     * Конвертация Car → CarDTO
     */
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
