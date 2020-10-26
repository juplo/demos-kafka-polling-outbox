package de.juplo.kafka.outbox;

import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;


@Component
public class JavaProducer
{
  final static Logger LOG = LoggerFactory.getLogger(JavaProducer.class);


  private final OutboxRepository repository;
  private final KafkaProducer<String, String> producer;

  private long sequenceNumber = 0l;

  public JavaProducer(OutboxRepository repository)
  {
    this.repository = repository;

    Properties props = new Properties();
    props.put("bootstrap.servers", "kafka:9092");
    props.put("metadata.max.age.ms", 1000);
    props.put("linger.ms", 20);
    props.put("key.serializer", StringSerializer.class.getName());
    props.put("value.serializer", StringSerializer.class.getName());

    this.producer = new KafkaProducer<>(props);
  }

  @Scheduled(fixedDelay = 500)
  public void poll()
  {
    List<OutboxItem> items;
    do
    {
      items = repository.fetch(sequenceNumber);
      for (OutboxItem item : items)
        send(item);
    }
    while (items.size() > 0);
  }

  void send(OutboxItem item)
  {
    final ProducerRecord<String, String> record =
        new ProducerRecord<>("test", item.getKey(), item.getValue());

    producer.send(record, (metadata, e) ->
        {
          if (e == null)
          {
            // HANDLE SUCCESS
            LOG.info(
                "P - {}: {}/{} - {}",
                metadata.offset(),
                metadata.topic(),
                metadata.partition(),
                record.value()
            );
          }
          else
          {
            // HANDLE ERROR
            LOG.error("P - ERROR {}/{}: {}", record.topic(), record.partition(), e.toString());
          }
        });
  }

  @PreDestroy
  public void close()
  {
    producer.close(5, TimeUnit.SECONDS);
  }
}
