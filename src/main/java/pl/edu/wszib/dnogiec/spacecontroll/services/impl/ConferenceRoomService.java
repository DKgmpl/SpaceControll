package pl.edu.wszib.dnogiec.spacecontroll.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.spring.data.ConferenceRoomRepository;
import pl.edu.wszib.dnogiec.spacecontroll.model.ConferenceRoom;
import pl.edu.wszib.dnogiec.spacecontroll.services.IConferenceRoomService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConferenceRoomService implements IConferenceRoomService {

    private final ConferenceRoomRepository roomRepository;

    @Override
    public List<ConferenceRoom> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public ConferenceRoom getRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    @Override
    public ConferenceRoom save(ConferenceRoom room) {
        return roomRepository.save(room);
    }

    @Override
    public void delete(Long id) {
        roomRepository.deleteById(id);
    }
}
