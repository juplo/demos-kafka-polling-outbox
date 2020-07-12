package de.trion.kafka.outbox;

public class UserEvent {
    public enum Type { CREATED, LOGIN, LOGOUT, DELETED }

    Long id;
    Type type;
    String user;
}
