package de.juplo.boot.data.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OutboxListener
{
    private static final Logger LOG = LoggerFactory.getLogger(OutboxListener.class);


    @TransactionalEventListener
    public void onUserEvent(UserEvent event)
    {
        LOG.info("{}: {}", event.getV, event.user);
    }
}
