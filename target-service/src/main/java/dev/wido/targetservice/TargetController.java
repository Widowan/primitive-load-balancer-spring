package dev.wido.targetservice;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class TargetController {
    public static final AtomicInteger connectionCounter = new AtomicInteger(0);

    @Value("${server.port:0}")
    private int port;

    @SneakyThrows
    @GetMapping("/")
    public String helloWorld() {
        connectionCounter.getAndIncrement();
        Thread.sleep(3);
        connectionCounter.getAndDecrement();
        return "Hello world! " + port + "\n";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong\n";
    }
}
