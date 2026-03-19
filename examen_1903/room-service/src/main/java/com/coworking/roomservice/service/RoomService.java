package com.coworking.roomservice.service;

import com.coworking.roomservice.kafka.RoomEventProducer;
import com.coworking.roomservice.model.Room;
import com.coworking.roomservice.repository.RoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomEventProducer roomEventProducer;

    public RoomService(RoomRepository roomRepository, RoomEventProducer roomEventProducer) {
        this.roomRepository = roomRepository;
        this.roomEventProducer = roomEventProducer;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found with id: " + id));
    }

    public Room create(Room room) {
        return roomRepository.save(room);
    }

    public Room update(Long id, Room roomDetails) {
        Room room = findById(id);
        room.setName(roomDetails.getName());
        room.setCity(roomDetails.getCity());
        room.setCapacity(roomDetails.getCapacity());
        room.setType(roomDetails.getType());
        room.setHourlyRate(roomDetails.getHourlyRate());
        room.setAvailable(roomDetails.isAvailable());
        return roomRepository.save(room);
    }

    public void delete(Long id) {
        Room room = findById(id);
        roomRepository.delete(room);
        roomEventProducer.sendRoomDeleted(id);
    }

    public boolean checkAvailability(Long roomId, LocalDateTime start, LocalDateTime end) {
        Room room = findById(roomId);
        return room.isAvailable();
    }

    public Room updateAvailability(Long roomId, boolean available) {
        Room room = findById(roomId);
        room.setAvailable(available);
        return roomRepository.save(room);
    }
}
