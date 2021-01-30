package de.juplo.kafka.outbox.delivery;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;


@Repository
@AllArgsConstructor
public class OutboxRepository
{
  private static final String SQL_QUERY =
      "SELECT id, key, value FROM outbox WHERE id > :sequenceNumber ORDER BY id ASC";
  private static final String SQL_UPDATE =
      "INSERT INTO outbox (key, value, issued) VALUES (:key, :value, :issued)";
  private static final String SQL_DELETE =
      "DELETE FROM outbox WHERE id <= :id";

  private final NamedParameterJdbcTemplate jdbcTemplate;


  public void save(String key, String value, ZonedDateTime issued)
  {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("key", key);
    parameters.addValue("value", value);
    parameters.addValue("issued", Timestamp.from(issued.toInstant()));
    jdbcTemplate.update(SQL_UPDATE, parameters);
  }

  public int delete(Long id)
  {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("id", id);
    return jdbcTemplate.update(SQL_DELETE, parameters);
  }

  public List<OutboxItem> fetch(Long sequenceNumber)
  {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("sequenceNumber", sequenceNumber);
    return
        jdbcTemplate.query(
            SQL_QUERY,
            parameters,
            (resultSet, rowNumber) ->
            {
              return
                  OutboxItem
                      .builder()
                      .sequenceNumber(resultSet.getLong(1))
                      .key(resultSet.getString(2))
                      .value(resultSet.getString(3))
                      .build();
            });
  }
}
