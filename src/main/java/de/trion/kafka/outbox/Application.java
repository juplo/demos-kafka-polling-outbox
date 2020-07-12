package de.trion.kafka.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@Component
public class Application implements CommandLineRunner {

    private final static Logger LOG = LoggerFactory.getLogger(Application.class);


    @Autowired
    ApplicationProperties properties;
    @Autowired
    JdbcTemplate jdbcTemplate;


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


    @Override
    public void run(String... strings) throws Exception {

        LOG.info("Creating tables");
        jdbcTemplate.execute("DROP TABLE users IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE users(id SERIAL, username VARCHAR(255), loggedIn BOOLEAN)");
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
