package pl.edu.wszib.dnogiec.spacecontroll.services;

import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface IReservationService {
    boolean isRoomAvailable(Long roomId, LocalDateTime from, LocalDateTime to);

    boolean canReserve(Long roomId, LocalDateTime from, LocalDateTime to);

    boolean cancelReservation(Long reservationId, Long userId);

    /**
     * Zwraca wszystkie rezerwacje dla podanej sali.
     */
    List<Reservation> getReservationsForRoom(Long roomId);

    /**
     * Tworzy nową rezerwację, po sprawdzeniu dostępności i poprawności czasu.
     * Zwraca true jeśli sukces.
     */
    boolean createReservation(Reservation reservation);

    boolean checkIn(Long reservationId, Long userId);

    List<Reservation> getReservationsForUser(Long userId);

    List<Reservation> getAllReservations();

    Reservation getReservationById(Long id);
}
