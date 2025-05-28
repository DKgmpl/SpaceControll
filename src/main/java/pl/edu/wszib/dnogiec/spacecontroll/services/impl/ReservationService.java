package pl.edu.wszib.dnogiec.spacecontroll.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ReservationRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;
import pl.edu.wszib.dnogiec.spacecontroll.services.IReservationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService implements IReservationService {

    private final ReservationRepository reservationRepository;

    @Override
    public boolean isRoomAvailable(Long roomId, LocalDateTime from, LocalDateTime to) {
        // Pobierz wszystkie aktywne rezerwacje na daną salę
        List<Reservation> existingReservation = reservationRepository
                .findByConferenceRoomIdAndStatus(roomId, Reservation.ReservationStatus.ACTIVE);
        // Sprawdź kolizje
        for (Reservation r : existingReservation) {
            if (r.getStartTime().isBefore(to) && r.getEndTime().isAfter(from)) {
                return false; //kolizja
            }
        }
        return true;
    }

    @Override
    public boolean canReserve(Long roomId, LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return false;
        if (from.isBefore(LocalDateTime.now())) return false;
        if (from.isAfter(to) || from.isEqual(to)) return false;
        return isRoomAvailable(roomId, from, to);
    }

    @Override
    public boolean createReservation(Reservation reservation) {
        //Sprawdzanie poprawności czasu oraz czy sala jest dostępna;
        if (reservation.getStartTime() == null || reservation.getEndTime() == null
                || !canReserve(reservation.getConferenceRoom().getId(),
                reservation.getStartTime(),
                reservation.getEndTime())) {
            return false;
        }
        reservation.setStatus(Reservation.ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);
        return true;
    }

    @Override
    public List<Reservation> getReservationsForRoom(Long roomId) {
        return reservationRepository.findByConferenceRoomId(roomId);
    }

    @Override
    public List<Reservation> getReservationsForUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation getReservationById(Long id) {
        Optional<Reservation> res = reservationRepository.findById(id);
        return res.orElse(null);
    }

    @Override
    public boolean cancelReservation(Long reservationId, Long userId) {
        Optional<Reservation> resOpt = reservationRepository.findById(reservationId);
        if (resOpt.isPresent()) {
            Reservation reservation = resOpt.get();
            //Użytkownik może anulować tylko swoją rezerwację (lub zrobić dodatkową walidację na admina)
            if (reservation.getUser().getId().equals(userId)
                    && reservation.getStatus() == Reservation.ReservationStatus.ACTIVE
                    && reservation.getStartTime().isAfter(LocalDateTime.now())) {
                reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);
                return true;
            }
        }
        return false;
    }
}
