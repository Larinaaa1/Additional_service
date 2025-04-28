package com.example.additionalservice.service;

import com.example.additionalservice.model.Car;
import com.example.additionalservice.service.clients.CarClient;
import com.example.additionalservice.service.statistics.Obility2;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class CarCacheService {
    private final CarClient carClient;
    private final Map<Long, Car> carCache = new HashMap<>(); //ключ - ID машины, значение - объект Car
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CarCacheService(CarClient carClient) {
        this.carClient = carClient;
        scheduler.scheduleAtFixedRate(() ->
                System.out.println("[Car Cache] Current size: " + this.carCache.size()), 0, 30, TimeUnit.SECONDS);
    }

    public Car getCarById(Long id) {
        long start = System.currentTimeMillis();
        try {
            Car car = carCache.computeIfAbsent(id, carClient::getCarById);
            return car;
        }
        finally
         {
            Obility2.recordTiming("getCarById", System.currentTimeMillis() - start);
          }
    }

    public void cacheCar(Car car) {  // вручную добавить машину в кеш
        long start = System.currentTimeMillis();
        try {
            if (car != null && !carCache.containsKey(car.getId())) {
                System.out.println("[Car Cache] Caching car: " + car.getId());
                carCache.putIfAbsent(car.getId(), car);
            }
        }
        finally
        {
            Obility2.recordTiming("cacheCar", System.currentTimeMillis() - start);
        }

    }

    public boolean isCarCached(Long id) {
        long start = System.currentTimeMillis();
        try {
            boolean temp = carCache.containsKey(id);
            return temp;
        }
        finally
        {
            Obility2.recordTiming("isCarCached", System.currentTimeMillis() - start);
        }

    }

    public Map<Long, Car> getCacheSnapshot() {
        long start = System.currentTimeMillis();
        try {
            Map<Long, Car> temp = new HashMap<>(carCache);
            return temp;
        }
        finally
        {
            Obility2.recordTiming("getCacheSnapshot", System.currentTimeMillis() - start);
        }

    }

    @Scheduled(fixedRate = 30_000) // Каждые 30 секунд
    public void printCacheStats() {

        long start = System.currentTimeMillis();
        try {
            System.out.println("[Car Cache] Current size: " + carCache.size());
        }
        finally
        {
            Obility2.recordTiming("printCacheStats", System.currentTimeMillis() - start);
        }

    }



    // Обновление кеша, чтобы данные не устаревали
    @Scheduled(fixedRate = 10 * 60 * 1000) // каждые 10 минут
    public void refreshCache() {
        long start = System.currentTimeMillis();
        try {
            System.out.println("[Car Cache] Refreshing cache...");
            for (Long id : carCache.keySet()) {
                Car updatedCar = carClient.getCarById(id);
                if (updatedCar != null) {
                    carCache.put(id, updatedCar);
                }
            }
        }
        finally
        {
            Obility2.recordTiming("refreshCache", System.currentTimeMillis() - start);
        }

    }
}
