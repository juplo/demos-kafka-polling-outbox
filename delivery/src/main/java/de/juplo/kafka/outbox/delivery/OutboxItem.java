package de.juplo.kafka.outbox.delivery;

import lombok.Builder;
import lombok.Data;
import lombok.Value;


@Data
@Value
@Builder
public class OutboxItem
{
  private final Long sequenceNumber;
  private final String key;
  private final String value;
}
