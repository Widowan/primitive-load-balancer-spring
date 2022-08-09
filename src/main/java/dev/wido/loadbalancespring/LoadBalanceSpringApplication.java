package dev.wido.loadbalancespring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoadBalanceSpringApplication {
    public static void main(String[] args) {
        var app = new SpringApplication(LoadBalanceSpringApplication.class);
        app.run(args);
    }
}
