package pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repozytorium do operacji na encji ReservationRepository.
 */

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByConferenceRoomId(Long roomId);
    List<Reservation> findByConferenceRoomIdAndStatus(Long roomId, Reservation.ReservationStatus status);
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
    List<Reservation> findByEndTimeBetween(LocalDateTime from, LocalDateTime to);
    List<Reservation> findByConferenceRoomIdAndStatusAndEndTimeAfterAndStartTimeBefore(
            Long roomId, Reservation.ReservationStatus status, LocalDateTime from, LocalDateTime to);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findByStatusAndEndTimeBefore(Reservation.ReservationStatus status, LocalDateTime to);
    List<Reservation> findByStatusAndStartTimeBeforeAndCheckInTimeIsNull(
            Reservation.ReservationStatus status, LocalDateTime threshold);
}
