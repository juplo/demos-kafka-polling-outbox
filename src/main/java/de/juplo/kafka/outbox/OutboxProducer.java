package de.juplo.kafka.outbox;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringSerializer;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
  private final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

  private long sequenceNumber = 0l;

  public OutboxProducer(OutboxRepository repository)
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

    record.headers().add("SEQ#", buffer.putLong(item.getSequenceNumber()).array());

    producer.send(record, (metadata, e) ->
    {
      if (e == null)
      {
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
    producer.close(5, TimeUnit.SECONDS);
  }
}
