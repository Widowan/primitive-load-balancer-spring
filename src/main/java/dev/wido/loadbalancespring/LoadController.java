package dev.wido.loadbalancespring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LoadController {
    final LoadService loadService;

    public LoadController(LoadService loadService) {
        this.loadService = loadService;
    }

    @GetMapping("/")
    public ResponseEntity<String> balance() {
        var processed = loadService.process();
        if (processed.isEmpty())
            return ResponseEntity.internalServerError().body("No alive target found\n");

        return ResponseEntity.ok(processed.get().body());
    }
}
