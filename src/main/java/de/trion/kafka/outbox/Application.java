package de.trion.kafka.outbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class Application {

    @Autowired
    ApplicationProperties properties;


    @Bean
    public String bootstrapServers() { return properties.bootstrapServers; }

    @Bean
    public String topic() {
        return properties.topic;
    }

    @Bean
    public String consumerGroup() {
        return properties.consumerGroup;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/**")
                        .allowedOrigins("http://localhost:4200");
            }
        };
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
