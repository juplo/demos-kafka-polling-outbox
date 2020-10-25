package de.juplo.kafka.outbox;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;


@Repository
@AllArgsConstructor
public class OutboxRepository
{
  private static final String SQL_QUERY =
      "SELECT id, key, value FROM outbox WHERE id > ?";
  private static final String SQL_UPDATE =
      "INSERT INTO outbox (key, value) VALUES (?, ?)";

  private final JdbcTemplate jdbcTemplate;


  public void save(String key, String value)
  {
    jdbcTemplate.update(SQL_UPDATE, key, value);
  }

  public List<OutboxItem> fetch(@NotNull Long sequenceNumber)
  {
    return
        jdbcTemplate.query(SQL_QUERY, (resultSet, rowNumber) ->
            {
              return
                  OutboxItem
                      .builder()
                      .sequenceNumber(resultSet.getLong(0))
                      .key(resultSet.getString(1))
                      .value(resultSet.getString(2))
                      .build();
            },
            sequenceNumber);
  }
}
