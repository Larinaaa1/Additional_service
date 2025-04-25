package com.example.additionalservice.service;

import com.example.additionalservice.model.Car;
import com.example.additionalservice.service.clients.CarClient;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CarCacheService {
    private final CarClient carClient;
    private final Map<Long, Car> carCache = new HashMap<>(); //ключ - ID машины, значение - объект Car

    public CarCacheService(CarClient carClient) {
        this.carClient = carClient;
    }

    public Car getCarById(Long id) {
        return carCache.computeIfAbsent(id, carClient::getCarById);  // сразу погружаем авто в кэш, если его там нет
    }

    public void putCar(Car car) {  // вручную добавить машину в кеш
        if (car != null) {
            carCache.putIfAbsent(car.getId(), car);
        }
    }

    public boolean isCarCached(Long id) {
        return carCache.containsKey(id);
    }

    public Map<Long, Car> getCacheSnapshot() {
        return new HashMap<>(carCache);
    }

    @Scheduled(fixedRate = 30_000) // Каждые 30 секунд
    public void printCacheStats() {
        System.out.println("[Car Cache] Current size: " + carCache.size());
    }

    // Обновление кеша, чтобы данные не устаревали
    @Scheduled(fixedRate = 10 * 60 * 1000) // каждые 10 минут
    public void refreshCache() {
        System.out.println("[Car Cache] Refreshing cache...");
        for (Long id : carCache.keySet()) {
            Car updatedCar = carClient.getCarById(id);
            if (updatedCar != null) {
                carCache.put(id, updatedCar);
            }
        }
    }
}
