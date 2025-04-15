package pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

/**
 Repozytorium do operacji na encji Reservation.
 Umożliwia wyszukiwanie rezerwacji na podstawie różnych kryteriów:

 - identyfikatora sali konferencyjnej,
 - identyfikatora użytkownika,
 - statusu rezerwacji,
 - rezerwacji w określonym przedziale czasowym. */

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByConferenceRoomId(Long conferenceRoomId);
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findByConferenceRoomIdAndStartTimeBetween(
            Long conferenceRoomId, LocalDateTime start, LocalDateTime end);
}
