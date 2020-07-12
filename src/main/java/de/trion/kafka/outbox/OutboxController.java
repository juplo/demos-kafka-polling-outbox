package de.trion.kafka.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@RestController
@Transactional
@RequestMapping("/users")
public class OutboxController {

    private static final Logger LOG = LoggerFactory.getLogger(OutboxController.class);


    private final UserRepository repository;


    public OutboxController(UserRepository repository) {
        this.repository = repository;
    }


    @PostMapping()
    public ResponseEntity<Void> getVorgang(
            UriComponentsBuilder builder,
            @RequestBody String username) {
        User user = new User(username, LocalDateTime.now(), false);
        repository.save(user);
        // TODO: Not-Unique Fehler auslösen
        UriComponents uri = builder.path("/{username}").buildAndExpand(username);
        return ResponseEntity.created(uri.toUri()).build();
    }
}
