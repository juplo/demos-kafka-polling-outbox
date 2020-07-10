package de.trion.kafka.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class OutboxController {

    private static final Logger LOG = LoggerFactory.getLogger(OutboxController.class);


    private final OutboxService service;


    public OutboxController(OutboxService service) {
        this.service = service;
    }


    @PostMapping("/create")
    public ResponseEntity<Void> getVorgang(@RequestBody String user) {
    }
}
