package de.juplo.kafka.outbox;

import com.google.common.primitives.Longs;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


@Component
public class OutboxProducer
{
  final static Logger LOG = LoggerFactory.getLogger(OutboxProducer.class);


  private final OutboxRepository repository;
  private final KafkaProducer<String, String> producer;
  private final String topic;

  private long sequenceNumber = 0l;

  public OutboxProducer(
      ApplicationProperties properties,
      OutboxRepository repository)
  {
    this.repository = repository;

    Properties props = new Properties();
    props.put("bootstrap.servers", properties.bootstrapServers);
    props.put("key.serializer", StringSerializer.class.getName());
    props.put("value.serializer", StringSerializer.class.getName());

    this.producer = new KafkaProducer<>(props);
    this.topic = properties.topic;
  }

  @Scheduled(fixedDelay = 500)
  public void poll()
  {
    List<OutboxItem> items;
    do
    {
      items = repository.fetch(sequenceNumber);
      LOG.debug("Polled {} new items", items.size());
      for (OutboxItem item : items)
        send(item);
    }
    while (items.size() > 0);
  }

  void send(OutboxItem item)
  {
    final ProducerRecord<String, String> record =
        new ProducerRecord<>(topic, item.getKey(), item.getValue());

    sequenceNumber = item.getSequenceNumber();
    record.headers().add("SEQ#", Longs.toByteArray(sequenceNumber));

    producer.send(record, (metadata, e) ->
    {
      if (metadata != null)
      {
        int deleted = repository.delete(item.getSequenceNumber());
        LOG.info(
            "{}/{}:{} - {}:{}={} - deleted: {}",
            metadata.topic(),
            metadata.partition(),
            metadata.offset(),
            item.getSequenceNumber(),
            record.key(),
            record.value(),
            deleted);
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
