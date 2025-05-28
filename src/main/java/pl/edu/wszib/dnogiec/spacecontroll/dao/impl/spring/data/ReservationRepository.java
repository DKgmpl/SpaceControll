package pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByConferenceRoomId(Long roomId);

    List<Reservation> findByConferenceRoomIdAndStatus(Long roomId, Reservation.ReservationStatus status);

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    List<Reservation> findByConferenceRoomIdAndStartTimeBetween(Long conferenceRoomId, LocalDateTime start, LocalDateTime end);
}
