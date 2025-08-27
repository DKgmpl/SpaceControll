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
        var overlaps = reservationRepository
                .findByConferenceRoomIdAndStatusAndEndTimeAfterAndStartTimeBefore(
                        roomId, Reservation.ReservationStatus.ACTIVE, from, to);
        return overlaps.isEmpty();
       /* // Pobierz wszystkie aktywne rezerwacje na daną salę
        List<Reservation> existingReservation = reservationRepository
                .findByConferenceRoomIdAndStatus(roomId, Reservation.ReservationStatus.ACTIVE);
        // Sprawdź kolizje
        for (Reservation r : existingReservation) {
            if (r.getStartTime().isBefore(to) && r.getEndTime().isAfter(from)) {
                return false; //kolizja
            }
        }
        return true;*/
    }

    @Override
    public boolean canReserve(Long roomId, LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return false;
        if (from.isBefore(LocalDateTime.now())) return false;
        if (from.isAfter(to) || from.isEqual(to)) return false;
        if (!from.toLocalDate().equals(to.toLocalDate())) return false; //Rezerwacja tylko w tym samym dniu
        return isRoomAvailable(roomId, from, to);
    }

    @Override
    public boolean createReservation(Reservation reservation) {
        //Sprawdzanie poprawności czasu oraz czy sala jest dostępna;
        if (reservation.getStartTime() == null || reservation.getEndTime() == null) return false;
        if (!canReserve(reservation.getConferenceRoom().getId(),
                reservation.getStartTime(),
                reservation.getEndTime())) {
            return false;
        }

        Integer expected = reservation.getExpectedAttendees();
        if (expected != null && expected > reservation.getConferenceRoom().getCapacity()) {
            return false;
        }

        reservation.setStatus(Reservation.ReservationStatus.ACTIVE);
        reservationRepository.save(reservation);
        return true;
    }

    @Override
    public boolean cancelReservation(Long reservationId, Long userId) {
        return reservationRepository.findById(reservationId).map(r -> {
            //Użytkownik może anulować tylko swoją rezerwację (lub zrobić dodatkową walidację na admina)
            if(r.getUser().getId().equals(userId)
                    && r.getStatus() == Reservation.ReservationStatus.ACTIVE
                    && r.getStartTime().isAfter(LocalDateTime.now())){
                r.setStatus(Reservation.ReservationStatus.CANCELLED);
                r.setCancelledAt(LocalDateTime.now());
                reservationRepository.save(r);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Override
    public boolean checkIn(Long reservationId, Long userId) {
        return reservationRepository.findById(reservationId).map(r -> {
            if (!r.getUser().getId().equals(userId)) return false;
            if (r.getStatus() != Reservation.ReservationStatus.ACTIVE) return false;
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(r.getStartTime().minusMinutes(15)) || now.isAfter(r.getStartTime().plusMinutes(15)))
                return false;
            if (r.getCheckInTime() != null) return true;
            r.setCheckInTime(now);
            reservationRepository.save(r);
            return true;
        }).orElse(false);
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
}
