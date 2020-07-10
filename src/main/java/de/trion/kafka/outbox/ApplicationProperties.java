package de.trion.kafka.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("outbox.polling")
public class ApplicationProperties {
    public String bootstrapServers = "localhost:9092";
    public String topic = "outbox-polling";
    public String consumerGroup = "polling";


    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }
}
