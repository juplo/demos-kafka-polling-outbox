package de.juplo.kafka.outbox.delivery;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;


@ConfigurationProperties("de.juplo.kafka.outbox")
@Getter
@Setter
public class ApplicationProperties
{
  String bootstrapServers = "localhost:9092";
  String topic = "outbox";
  Duration cleanupInterval = Duration.ofSeconds(10);
}
