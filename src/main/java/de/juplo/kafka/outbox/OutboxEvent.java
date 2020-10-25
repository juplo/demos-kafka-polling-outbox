package de.juplo.kafka.outbox;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.springframework.context.ApplicationEvent;


@Builder
@Data
@Value
public class OutboxEvent extends ApplicationEvent
{
  private final String key;
  private final String value;
}
