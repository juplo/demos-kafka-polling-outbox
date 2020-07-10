package de.juplo.boot.data.jdbc;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Long> {
    @Query("select * from User u where u.username = :username")
    User findByUsername(@Param("username") String username);
}
