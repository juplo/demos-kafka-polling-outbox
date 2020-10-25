package de.juplo.kafka.outbox;

import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.validation.constraints.NotNull;
import java.util.List;


@AllArgsConstructor
public class OutboxRepository
{
  private static final String SQL_QUERY =
      "SELECT id, key, value FROM outbox WHERE id > ?";
  private static final String SQL_UPDATE =
      "INSERT INTO outbox VALUES (key ?, value, ?)";

  private final JdbcTemplate jdbcTemplate;


  public void save(@NotNull OutboxEvent event)
  {
    jdbcTemplate.update(SQL_UPDATE, event.getKey(), event.getValue());
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
