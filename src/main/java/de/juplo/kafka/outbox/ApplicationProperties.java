package de.juplo.kafka.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties("de.juplo.kafka.outbox")
@Getter
@Setter
public class ApplicationProperties
{
  String bootstrapServers = "localhost:9092";
  String topic = "outbox";
}
