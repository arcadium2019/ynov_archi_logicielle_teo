package com.coworking.reservationservice.kafka;

import com.coworking.reservationservice.service.ReservationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventConsumer {

    private final ReservationService reservationService;

    public ReservationEventConsumer(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @KafkaListener(topics = "room-deleted", groupId = "reservation-group")
    public void handleRoomDeleted(String message) {
        Long roomId = Long.parseLong(message.trim());
        reservationService.cancelAllReservationsForRoom(roomId);
    }

    @KafkaListener(topics = "member-deleted", groupId = "reservation-group")
    public void handleMemberDeleted(String message) {
        Long memberId = Long.parseLong(message.trim());
        reservationService.deleteAllReservationsForMember(memberId);
    }
}
