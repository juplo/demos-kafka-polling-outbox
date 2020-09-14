package de.juplo.boot.data.jdbc;

import org.springframework.context.ApplicationEvent;


public class UserEvent extends ApplicationEvent
{
    public enum Type { CREATED, LOGIN, LOGOUT, DELETED }

    final Type type;
    final String user;


    public UserEvent(Object source, Type type, String user)
    {
        super(source);
        this.type = type;
        this.user = user;
    }
}
