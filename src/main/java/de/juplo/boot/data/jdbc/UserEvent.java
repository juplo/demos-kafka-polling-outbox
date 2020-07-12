package de.juplo.boot.data.jdbc;

public class UserEvent {
    public enum Type { CREATED, LOGIN, LOGOUT, DELETED }

    Long id;
    Type type;
    String user;
}
