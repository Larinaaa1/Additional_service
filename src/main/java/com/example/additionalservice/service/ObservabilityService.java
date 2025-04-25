//package com.example.additionalservice.service;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.stream.Collectors;
//
//@Service
//public class ObservabilityService {
//
//    @Value("${observability.window.seconds:60}")
//    private long statsWindowSeconds;
//
//    private final List<TimingEntry> timingEntries = new CopyOnWriteArrayList<>();
//
//    public void recordTiming(String label, long durationMillis) {
//        timingEntries.add(new TimingEntry(label, durationMillis, Instant.now()));
//    }
//
//    @Scheduled(fixedRateString = "${observability.report.interval:10000}")
//    public void printStats() {
//        Instant cutoff = Instant.now().minusSeconds(statsWindowSeconds);
//        List<TimingEntry> recent = timingEntries.stream()
//                .filter(entry -> entry.timestamp().isAfter(cutoff))
//                .toList();
//
//        System.out.println("=== Observability Stats (last " + statsWindowSeconds + "s) ===");
//        recent.stream()
//                .collect(Collectors.groupingBy(TimingEntry::label, Collectors.averagingLong(TimingEntry::durationMillis)))
//                .forEach((label, avg) -> System.out.println(label + " - avg: " + avg + "ms"));
//
//        // Можно ещё чистить старые записи:
//        timingEntries.removeIf(e -> e.timestamp().isBefore(cutoff));
//    }
//
//    public record TimingEntry(String label, long durationMillis, Instant timestamp) {}
//}
