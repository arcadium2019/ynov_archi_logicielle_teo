package com.coworking.roomservice.repository;

import com.coworking.roomservice.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByCity(String city);

    List<Room> findByAvailable(boolean available);
}
