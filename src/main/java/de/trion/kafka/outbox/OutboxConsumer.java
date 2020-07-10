package de.trion.kafka.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.lvm.tx.Event;
import de.lvm.tx.Command;
import de.lvm.tx.Command.Action;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import static de.lvm.tx.Event.Type.*;

@Component
public class OutboxConsumer implements ApplicationRunner, Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(OutboxConsumer.class);

    private final OutboxService service;
    private final OutboxProducer sender;
    private final ObjectMapper mapper;
    private final String topic;
    private final KafkaConsumer<Long, String> consumer;
    private final Thread thread;

    private long internalState = 1;


    public OutboxConsumer(
            OutboxService service,
            OutboxProducer sender,
            ObjectMapper mapper,
            String bootstrapServers,
            String consumerGroup,
            String topic) {

        this.service = service;
        this.sender = sender;
        this.mapper = mapper;
        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", consumerGroup);
        props.put("auto.commit.interval.ms", 15000);
        props.put("metadata.max.age.ms", 1000);
        props.put("key.deserializer", LongDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        consumer = new KafkaConsumer<>(props);

        thread = new Thread(this);
    }


    @Override
    public void run() {
        try
        {
            LOG.info("Subscribing to topic " + topic);
            consumer.subscribe(Arrays.asList(topic));

            while (true)
            {
                ConsumerRecords<Long, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<Long, String> record : records) {
                    byte code = record.headers().lastHeader("messageType").value()[0];
                    Action action = Action.from(code);

                    if (action == null)
                    {
                        LOG.debug("Ignoring unknown action {} for {}", code, record.value());
                        continue;
                    }

                    switch(action) {
                        case SAVE_DLZ:
                            dlzSaveReceived(toCommand(record.value()));
                            continue;
                        default:
                            LOG.debug("Ignoring message {}", record.value());
                    }
                    byte[] bytes = record.headers().lastHeader("messageType").value();
                    String type = new String(bytes, StandardCharsets.UTF_8);

                    if (type.endsWith("DlzAction")) {
                        dlzSaveReceived(toCommand(record.value()));
                        continue;
                    }

                    LOG.debug("Ignoring command {}", record.value());
                }
            }
        }
        catch (WakeupException e) {}
        catch (Exception e) {
            LOG.error("Unexpected exception!", e);
        }
        finally
        {
            LOG.info("Closing the KafkaConsumer...");
            try {
                consumer.close(Duration.ofSeconds(5));
                LOG.debug("Successfully closed the KafkaConsumer");
            }
            catch (Exception e) {
                LOG.warn("Exception while closing the KafkaConsumer!", e);
            }
        }
    }

    public Command toCommand(String message) throws IOException {
        Command command = mapper.readValue(message, Command.class);
        LOG.info("{}: {}", command.getAction(), command.getVorgangId());
        return command;
    }

    public void dlzSaveReceived(Command command) throws InterruptedException {
        try
        {
            String result =
                    service.bearbeiteVorgang(
                            command.getVorgangId(),
                            command.getVbId(),
                            command.getData());
            reply(command, result);
        }
        catch (Exception e) {
            LOG.error("Exception during processing!", e);
        }
    }

    public void reply(Command command, String message) {
        String vorgangId = command.getVorgangId();
        String vbId = command.getVbId();
        Event event = new Event(DLZ_SAVED, vorgangId, vbId);
        event.getZustand().put(Event.DLZ, message);
        sender.send(event);
    }


    @Override
    public void run(ApplicationArguments args) {
        thread.start();
        try {
            thread.join();
            LOG.info("Successfully joined the consumer-thread");
        }
        catch (InterruptedException e) {
            LOG.info("Main-thread was interrupted while joining the consumer-thread");
        }
    }

    @PreDestroy
    public void stop()
    {
        LOG.info("Stopping the KafkaConsumer...");
        consumer.wakeup();
    }
}
