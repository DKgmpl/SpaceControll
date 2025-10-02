package pl.edu.wszib.dnogiec.spacecontroll.services;

import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface IReservationService {
    boolean isRoomAvailable(Long roomId, LocalDateTime from, LocalDateTime to);

    boolean canReserve(Long roomId, LocalDateTime from, LocalDateTime to);

    boolean cancelReservation(Long reservationId, Long userId);

    List<Reservation> getReservationsForRoom(Long roomId);

    boolean createReservation(Reservation reservation);

    boolean checkIn(Long reservationId, Long userId);

    List<Reservation> getReservationsForUser(Long userId);

    List<Reservation> getAllReservations();

    Reservation getReservationById(Long id);
}
