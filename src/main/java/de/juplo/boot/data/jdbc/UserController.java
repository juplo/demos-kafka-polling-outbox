package de.juplo.boot.data.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import static de.juplo.boot.data.jdbc.UserEvent.Type.CREATED;
import static de.juplo.boot.data.jdbc.UserEvent.Type.DELETED;

@RestController
@Transactional
@RequestMapping("/users")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);


    private final UserRepository repository;
    private final ApplicationEventPublisher publisher;


    public UserController(
            UserRepository repository,
            ApplicationEventPublisher publisher)
    {
        this.repository = repository;
        this.publisher = publisher;
    }


    @PostMapping
    public ResponseEntity<Void> createUser(
            ServletUriComponentsBuilder builder,
            @RequestBody String username) {
        String sanitizedUsername = UserController.sanitize(username);
        User user = new User(sanitizedUsername, LocalDateTime.now(), false);

        // Triggering a unique-error for username prevents persistence
        repository.save(user);
        publisher.publishEvent(new UserEvent(this, CREATED, sanitizedUsername));
        user = repository.findByUsername(sanitizedUsername);

        UriComponents uri =
            builder
                .fromCurrentRequest()
                .pathSegment("{username}")
                .buildAndExpand(sanitizedUsername);
        return ResponseEntity.created(uri.toUri()).build();
    }

    @GetMapping("{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        User user = repository.findByUsername(UserController.sanitize(username));

        if (user == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("{username}")
    public ResponseEntity<User> removeUser(@PathVariable String username) {
        User user = repository.findByUsername(UserController.sanitize(username));

        if (user == null)
            return ResponseEntity.notFound().build();

        repository.delete(user);
        publisher.publishEvent(new UserEvent(this, DELETED, username));

        return ResponseEntity.ok(user);
    }

    @GetMapping()
    public ResponseEntity<Iterable<User>> getUsers() {
        return ResponseEntity.ok(repository.findAll());
    }


    private static String sanitize(String string) {
        if (string == null)
            return "";

        return string.trim().toLowerCase();
    }

    @ExceptionHandler
    public ResponseEntity<?> incorrectResultSizeDataAccessException(
        HttpServletRequest request,
        IncorrectResultSizeDataAccessException e
        )
    {
      String username;
      try {
          username = StreamUtils.copyToString(request.getInputStream(), Charset.defaultCharset());
      }
      catch (IOException ioe)
      {
        username = e.getMessage() + " -> " + ioe.getMessage();
      }
      LOG.info("User {} already exists!", username);
      return ResponseEntity.badRequest().build();
    }
}
