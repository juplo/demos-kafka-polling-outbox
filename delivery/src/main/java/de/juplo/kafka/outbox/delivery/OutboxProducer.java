package de.juplo.kafka.outbox.delivery;

import com.google.common.primitives.Longs;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PreDestroy;

import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;


public class OutboxProducer
{
  final static Logger LOG = LoggerFactory.getLogger(OutboxProducer.class);

  public final static String HEADER = "#";

  private final OutboxRepository repository;
  private final KafkaProducer<String, String> producer;
  private final String topic;
  private final Watermarks watermarks;
  private final Clock clock;
  private final Duration cleanupInterval;

  private long sequenceNumber = 0l;
  private LocalTime nextCleanup;

  public OutboxProducer(
      ApplicationProperties properties,
      OutboxRepository repository,
      Clock clock)
  {
    this.repository = repository;

    Properties props = new Properties();
    props.put(BOOTSTRAP_SERVERS_CONFIG, properties.bootstrapServers);
    props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ENABLE_IDEMPOTENCE_CONFIG, true);

    this.producer = new KafkaProducer<>(props);
    this.topic = properties.topic;

    this.watermarks = new Watermarks();
    this.clock = clock;
    this.cleanupInterval = properties.cleanupInterval;
    this.nextCleanup = LocalTime.now(clock).plus(cleanupInterval);
  }

  @Scheduled(fixedDelayString = "${de.juplo.kafka.outbox.interval}")
  public void poll()
  {
    List<OutboxItem> items;
    do
    {
      items = repository.fetch(sequenceNumber);
      LOG.debug("Polled {} new items", items.size());
      for (OutboxItem item : items)
        send(item);
      if (nextCleanup.isBefore(LocalTime.now(clock)))
      {
        int deleted = repository.delete(watermarks.getLowest());
        nextCleanup = LocalTime.now(clock).plus(cleanupInterval);
        LOG.info(
            "Cleaned up {} entries from outbox, next clean-up: {}",
            deleted,
            nextCleanup);
      }
    }
    while (items.size() > 0);
  }

  void send(OutboxItem item)
  {
    final ProducerRecord<String, String> record =
        new ProducerRecord<>(topic, item.getKey(), item.getValue());

    sequenceNumber = item.getSequenceNumber();
    record.headers().add(HEADER, Longs.toByteArray(sequenceNumber));

    producer.send(record, (metadata, e) ->
    {
      if (metadata != null)
      {
        watermarks.set(metadata.partition(), item.getSequenceNumber());
        LOG.info(
            "{}/{}:{} - {}:{}={}",
            metadata.topic(),
            metadata.partition(),
            metadata.offset(),
            item.getSequenceNumber(),
            record.key(),
            record.value());
      }
      else
      {
        // HANDLE ERROR
        LOG.error(
            "{}/{} - {}:{}={} -> ",
            record.topic(),
            record.partition(),
            item.getSequenceNumber(),
            record.key(),
            record.value(),
            e);
      }
    });
  }


  @PreDestroy
  public void close()
  {
    producer.close(Duration.ofSeconds(5));
  }
}
