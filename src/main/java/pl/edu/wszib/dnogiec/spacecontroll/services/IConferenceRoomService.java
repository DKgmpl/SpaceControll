package pl.edu.wszib.dnogiec.spacecontroll.services;

import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;

import java.util.List;

public interface IConferenceRoomService {
    List<ConferenceRoom> getAllRooms();

    ConferenceRoom getRoomById(Long id);

    ConferenceRoom save(ConferenceRoom room);

    void delete(Long id);
}
