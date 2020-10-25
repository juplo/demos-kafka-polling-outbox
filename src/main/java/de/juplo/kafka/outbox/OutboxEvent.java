package de.juplo.kafka.outbox;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;


@ToString
@EqualsAndHashCode
public class OutboxEvent extends ApplicationEvent
{
  @Getter
  private final String key;
  @Getter private final Object value;


  public OutboxEvent(Object source, String key, Object value)
  {
    super(source);
    this.key = key;
    this.value = value;
  }
}
