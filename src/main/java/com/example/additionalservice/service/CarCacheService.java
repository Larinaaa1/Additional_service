package com.example.additionalservice.service;

import com.example.additionalservice.model.Car;
import com.example.additionalservice.service.clients.CarClient;
import jakarta.annotation.PostConstruct;
import com.example.additionalservice.service.statistics.ObservabilityService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class CarCacheService {
    private final CarClient carClient;
    private final ObservabilityService observabilityService;
    private final Map<Long, Car> carCache = new HashMap<>(); //ключ - ID машины, значение - объект Car

    public CarCacheService(CarClient carClient, ObservabilityService observabilityService) {
        this.carClient = carClient;
        this.observabilityService = observabilityService;
    }

    public Car getCarById(Long id) {
        observabilityService.start(getClass().getSimpleName() + ":getCarById");
        Car car = carCache.computeIfAbsent(id, carClient::getCarById);
        observabilityService.stop(getClass().getSimpleName() + ":getCarById");
        return car;  // сразу погружаем авто в кэш, если его там нет
    }

    public void cacheCar(Car car) {  // вручную добавить машину в кеш
        this.observabilityService.start(getClass().getSimpleName() + ":cacheCar");
        if (car != null && !carCache.containsKey(car.getId())) {
            System.out.println("[Car Cache] Caching car: " + car.getId());
            carCache.putIfAbsent(car.getId(), car);
        }
        this.observabilityService.stop(getClass().getSimpleName() + ":cacheCar");
    }

    public boolean isCarCached(Long id) {
        this.observabilityService.start(getClass().getSimpleName() + ":isCarCached");
        boolean temp = carCache.containsKey(id);
        this.observabilityService.stop(getClass().getSimpleName() + ":isCarCached");
        return temp;
    }

    public Map<Long, Car> getCacheSnapshot() {
        this.observabilityService.start(getClass().getSimpleName() + ":getCacheSnapshot");
        Map<Long, Car> temp = new HashMap<>(carCache);
        this.observabilityService.stop(getClass().getSimpleName() + ":getCacheSnapshot");
        return temp;
    }

    @Scheduled(fixedRate = 30_000) // Каждые 30 секунд
    public void printCacheStats() {
        this.observabilityService.start(getClass().getSimpleName() + ":printCacheStats");
        System.out.println("[Car Cache] Current size: " + carCache.size());
        this.observabilityService.stop(getClass().getSimpleName() + ":printCacheStats");
    }

    // Обновление кеша, чтобы данные не устаревали
    @Scheduled(fixedRate = 10 * 60 * 1000) // каждые 10 минут
    public void refreshCache() {
        observabilityService.start(getClass().getSimpleName() + ":refreshCache");
        System.out.println("[Car Cache] Refreshing cache...");
        for (Long id : carCache.keySet()) {
            Car updatedCar = carClient.getCarById(id);
            if (updatedCar != null) {
                carCache.put(id, updatedCar);
            }
        }
        observabilityService.stop(getClass().getSimpleName() + ":refreshCache");
    }
}
