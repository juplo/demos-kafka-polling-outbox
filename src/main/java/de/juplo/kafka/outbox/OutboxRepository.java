package de.juplo.kafka.outbox;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
@AllArgsConstructor
public class OutboxRepository
{
  private static final String SQL_UPDATE =
      "INSERT INTO outbox (key, value) VALUES (?, ?)";

  private final JdbcTemplate jdbcTemplate;


  public void save(String key, String value)
  {
    jdbcTemplate.update(SQL_UPDATE, key, value);
  }
}
