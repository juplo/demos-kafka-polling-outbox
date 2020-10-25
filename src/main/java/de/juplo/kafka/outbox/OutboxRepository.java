package de.juplo.kafka.outbox;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.ZonedDateTime;


@Repository
@AllArgsConstructor
public class OutboxRepository
{
  private static final String SQL_QUERY =
      "SELECT id, key, value FROM outbox WHERE id > ?";
  private static final String SQL_UPDATE =
      "INSERT INTO outbox (key, value, issued) VALUES (:key, :value, :issued)";

  private final NamedParameterJdbcTemplate jdbcTemplate;


  public void save(String key, String value, ZonedDateTime issued)
  {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("key", key);
    parameters.addValue("value", value);
    parameters.addValue("issued", Timestamp.from(issued.toInstant()));
    jdbcTemplate.update(SQL_UPDATE, parameters);
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
