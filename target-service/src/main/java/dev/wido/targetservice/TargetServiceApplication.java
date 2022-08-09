package dev.wido.targetservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class TargetServiceApplication {
    public static void main(String[] args) {
        var app = new SpringApplication(TargetServiceApplication.class);

        var exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(
            () -> log.info("Current connections: {}", TargetController.connectionCounter.get()),
            10, 10, TimeUnit.SECONDS
        );

        app.run(args);
    }
}
