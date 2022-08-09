package dev.wido.loadbalancespring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.net.http.HttpResponse.*;

@Service
@Slf4j
public class LoadService {
    private static final AtomicInteger roundRobinCounter = new AtomicInteger(0);
    private static final AtomicReference<List<HttpRequest>> aliveDataRequests = new AtomicReference<>(List.of());
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public LoadService(@Value("${target.ports:8081 8082 8083 8084 8085}") String portsProp) {
        var targetHosts = Arrays.stream(portsProp.split(" "))
            .map(p -> "http://localhost:" + p);

        var statusRequests = targetHosts
            .map(t -> HttpRequest.newBuilder()
                .uri(URI.create(t + "/ping"))
                .timeout(Duration.ofMillis(100))
                .build())
            .toList();

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        var dataRequestsMap = statusRequests.stream().collect(Collectors.toMap(
            statusRequest -> statusRequest,
            statusRequest -> HttpRequest.newBuilder()
                .uri(uncheckedLift(() -> new URI(
                    statusRequest.uri().getScheme(),
                    null,
                    statusRequest.uri().getHost(),
                    statusRequest.uri().getPort(),
                    null, null, null))
                    .get())
                .timeout(Duration.ofMillis(100))
                .build()
        ));

        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(
            () -> aliveDataRequests.lazySet(getDataRequestsOfAliveTargets(statusRequests, dataRequestsMap)),
            0, 1, TimeUnit.MILLISECONDS
        );

    }

    public Optional<HttpResponse<String>> process() {
        var startTime = Instant.now();
        Optional<HttpResponse<String>> result;
        do {
            result = distributeRequest();
        } while (result.isEmpty() && Duration.between(startTime, Instant.now()).toSeconds() < 5);

        return result;
    }

    private Optional<HttpResponse<String>> distributeRequest() {
        var adr = aliveDataRequests.get();
        if (adr.size() == 0) return Optional.empty();

        var i = roundRobinCounter.getAndIncrement() % adr.size();
        var req =
            uncheckedLift(() -> httpClient.send(adr.get(i), BodyHandlers.ofString()));
        if (req.isEmpty() || req.get().statusCode() != 200) return Optional.empty();

        roundRobinCounter.getAndUpdate(rr -> rr % adr.size());
        return req;
    }

    private List<HttpRequest> getDataRequestsOfAliveTargets(
        List<HttpRequest> aliveRequests, Map<HttpRequest, HttpRequest> dataRequests)
    {
        return aliveRequests.stream()
            .map(httpRequest -> uncheckedLift(() -> httpClient.send(httpRequest, BodyHandlers.ofString())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(r -> r.statusCode() == 200)
            .map(HttpResponse::request)
            .map(dataRequests::get)
            .toList();
    }

    private static <T> Optional<T> uncheckedLift(CheckedSupplier<T> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Throwable;
    }
}
