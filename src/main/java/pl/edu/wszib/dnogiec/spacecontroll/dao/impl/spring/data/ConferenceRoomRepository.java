package pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;

/**
 Repozytorium do operacji na encji ConferenceRoom.
 Umożliwia m.in. wyszukiwanie sali konferencyjnej po nazwie. */

@Repository
public interface ConferenceRoomRepository extends JpaRepository<ConferenceRoom, Long> {
    ConferenceRoom findByName(String name);
}
