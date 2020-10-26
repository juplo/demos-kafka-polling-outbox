package de.juplo.kafka.outbox;

import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class JavaProducer implements ApplicationRunner
{
  final static Logger LOG = LoggerFactory.getLogger(JavaProducer.class);


  @Override
  public void run(ApplicationArguments args) throws Exception
  {
    Properties props = new Properties();
    props.put("bootstrap.servers", "kafka:9092");
    props.put("metadata.max.age.ms", 1000);
    props.put("linger.ms", 20);
    props.put("key.serializer", LongSerializer.class.getName());
    props.put("value.serializer", StringSerializer.class.getName());

    KafkaProducer<Long, String> producer = new KafkaProducer<>(props);

    try
    {
      for (long i = 0; true; i++)
      {
        final ProducerRecord<Long, String> record
            = new ProducerRecord<>("test", null, "Hallo Welt #" + i);


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

        Thread.sleep(100);
      }
    }
    finally
    {
      LOG.info("P - Closing...");
      producer.close();
      LOG.info("P - DONE!");
    }
  }
}
