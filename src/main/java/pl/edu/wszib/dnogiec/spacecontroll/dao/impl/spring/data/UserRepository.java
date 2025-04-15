package pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.wszib.dnogiec.spacecontroll.model.User;

import java.util.Optional;

/**
 Repozytorium do operacji na encji User.
 Implementuje podstawowe operacje CRUD oraz metodę wyszukującą użytkownika po loginie. */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
Optional<User> findByLogin(String login);
}
