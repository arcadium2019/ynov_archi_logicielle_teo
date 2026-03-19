package com.coworking.reservationservice.repository;

import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByRoomIdAndStatus(Long roomId, ReservationStatus status);

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByMemberIdAndStatus(Long memberId, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.roomId = :roomId AND r.status = 'CONFIRMED' AND r.startDateTime < :end AND r.endDateTime > :start")
    List<Reservation> findConflictingReservations(@Param("roomId") Long roomId,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
}
