//package pl.edu.wszib.dnogiec.spacecontroll.dao.impl;
//
//import pl.edu.wszib.dnogiec.spacecontroll.model.Reservation;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface IReservationDAO {
//    Optional<Reservation> getById(Long id);
//    List<Reservation> getByConferenceRoomId(Long conferenceRoomId);
//    List<Reservation> getByUserId(Long userId);
//    List<Reservation> getByConferenceRoomIdAndStartTimeBetween(
//            Long conferenceRoomId, Long start, Long end);
//    List<Reservation> getByStatus(Reservation.ReservationStatus status);
//    Reservation save(Reservation reservation);
//    void delete(Long id);
//    void update(Reservation reservation);
//}