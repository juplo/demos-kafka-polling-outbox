package de.juplo.boot.data.jdbc;

import de.juplo.kafka.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OutboxListener
{
    private static final Logger LOG = LoggerFactory.getLogger(OutboxListener.class);


    @TransactionalEventListener
    public void onUserEvent(OutboxEvent event)
    {
        LOG.info("{}: {}", event.getValue(), event.getKey());
    }
}
