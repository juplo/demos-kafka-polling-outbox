package de.juplo.kafka.outbox.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;


@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableScheduling
public class Application
{
  @Bean
  public OutboxProducer outboxProducer(
      ApplicationProperties properties,
      OutboxRepository repository)
  {
    return new OutboxProducer(properties, repository, Clock.systemDefaultZone());
  }


  public static void main(String[] args) throws Exception
  {
    SpringApplication.run(Application.class, args);
  }
}
