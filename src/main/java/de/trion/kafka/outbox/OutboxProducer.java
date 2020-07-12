package de.trion.kafka.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxProducer {

    private final static Logger LOG = LoggerFactory.getLogger(OutboxProducer.class);

    private final ObjectMapper mapper;
    private final String topic;
    private final KafkaProducer<String, String> producer;


    public OutboxProducer(ObjectMapper mapper, String bootstrapServers, String topic) {
        this.mapper = mapper;
        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);
    }


    public void send(UserEvent event) {
        try {
            String json = mapper.writeValueAsString(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.user, json);
            producer.send(record, (metadata, e) -> {
                if (e != null) {
                    LOG.error("Could not send event {}!", json, e);
                }
                else {
                    LOG.debug(
                            "{}: send event {} with offset {} to partition {}",
                            event.user,
                            event.id,
                            metadata.offset(),
                            metadata.partition());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Senden des Events " + event.id, e);
        }
    }


    @PreDestroy
    public void stop(){
        LOG.info("Closing the KafkaProducer...");
        try {
            producer.close(5, TimeUnit.SECONDS);
            LOG.debug("Successfully closed the KafkaProducer");
        }
        catch (Exception e) {
            LOG.warn("Exception while closing the KafkaProducer!", e);
        }
    }
}
