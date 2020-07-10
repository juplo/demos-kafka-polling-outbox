package de.trion.kafka.outbox;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

public class User {
    @Id
    Long id;
    String username;
    LocalDateTime created;
    boolean loggedIn;

    public User(String username, LocalDateTime created, boolean loggedIn) {
        this.username = username;
        this.created = created;
        this.loggedIn = loggedIn;
    }
}
