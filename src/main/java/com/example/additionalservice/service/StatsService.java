package com.example.additionalservice.service;

import com.example.additionalservice.model.Car;
import com.example.additionalservice.model.Rental;
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

    public StatsService(CarClient carClient, RentalClient rentalClient) {
        this.carClient = carClient;
        this.rentalClient = rentalClient;
    }

    /**
     * Возвращает список машин в указанном городе, свободных для аренды в заданный период
     */
    public List<CarDTO> getAvailableCars(String city, LocalDate startDate, LocalDate endDate) {
        if (city == null || startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }

        // Получаем все машины
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

        // Оставляем только свободные машины
        return carsInCity.stream()
                .filter(car -> isAvailable(car.getId(), rentalsByCarId.getOrDefault(car.getId(), Collections.emptyList()), startDate, endDate))
                .map(this::convertToCarDTO)
                .collect(Collectors.toList());
    }

    /**
     * Проверка, свободна ли машина на указанный период
     */
    private boolean isAvailable(Long carId, List<Rental> rentals, LocalDate startDate, LocalDate endDate) {
        return rentals.stream().noneMatch(rental ->
                isOverlapping(startDate, endDate, rental.getStartDate(), rental.getEndDate())
        );
    }

    /**
     * Проверка пересечения дат
     */
    private boolean isOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !end1.isBefore(start2) && !start1.isAfter(end2);
    }

    /**
     * Конвертация Car → CarDTO
     */
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
