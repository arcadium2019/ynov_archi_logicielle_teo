package com.coworking.reservationservice.service;

import com.coworking.reservationservice.kafka.ReservationEventProducer;
import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.model.ReservationStatus;
import com.coworking.reservationservice.repository.ReservationRepository;
import com.coworking.reservationservice.state.ReservationState;
import com.coworking.reservationservice.state.ReservationStateFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationEventProducer reservationEventProducer;
    private final RestTemplate restTemplate;

    private static final String ROOM_SERVICE_URL = "http://localhost:8081";
    private static final String MEMBER_SERVICE_URL = "http://localhost:8082";

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationEventProducer reservationEventProducer,
                              RestTemplate restTemplate) {
        this.reservationRepository = reservationRepository;
        this.reservationEventProducer = reservationEventProducer;
        this.restTemplate = restTemplate;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found with id: " + id));
    }

    public List<Reservation> findByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId);
    }

    public Reservation create(Reservation reservation) {
        // 1. Check room availability
        Boolean roomAvailable = restTemplate.getForObject(
                ROOM_SERVICE_URL + "/api/rooms/" + reservation.getRoomId() + "/available",
                Boolean.class
        );
        if (roomAvailable == null || !roomAvailable) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room not available");
        }

        // 2. Check member suspension
        Boolean memberSuspended = restTemplate.getForObject(
                MEMBER_SERVICE_URL + "/api/members/" + reservation.getMemberId() + "/suspended",
                Boolean.class
        );
        if (memberSuspended != null && memberSuspended) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member is suspended");
        }

        // 3. Check time slot conflicts
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                reservation.getRoomId(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime()
        );
        if (!conflicts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room already booked for this time slot");
        }

        // 4. Save with CONFIRMED status
        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepository.save(reservation);

        // 5. Check member quota
        List<Reservation> memberConfirmedReservations = reservationRepository
                .findByMemberIdAndStatus(reservation.getMemberId(), ReservationStatus.CONFIRMED);

        Map memberData = restTemplate.getForObject(
                MEMBER_SERVICE_URL + "/api/members/" + reservation.getMemberId(),
                Map.class
        );

        if (memberData != null) {
            Object maxBookingsObj = memberData.get("maxConcurrentBookings");
            if (maxBookingsObj != null) {
                int maxBookings = ((Number) maxBookingsObj).intValue();
                if (memberConfirmedReservations.size() >= maxBookings) {
                    reservationEventProducer.sendMemberSuspend(reservation.getMemberId());
                }
            }
        }

        // 6. Update room availability to false
        restTemplate.put(
                ROOM_SERVICE_URL + "/api/rooms/" + reservation.getRoomId() + "/availability?available=false",
                null
        );

        return saved;
    }

    public Reservation cancelReservation(Long id) {
        Reservation reservation = findById(id);

        // State Pattern : délégue la transition à l'état courant
        ReservationState state = ReservationStateFactory.getState(reservation);
        try {
            state.cancel();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Reservation saved = reservationRepository.save(reservation);

        // Restore room availability
        restTemplate.put(
                ROOM_SERVICE_URL + "/api/rooms/" + reservation.getRoomId() + "/availability?available=true",
                null
        );

        // Check if member should be unsuspended
        checkAndUnsuspendMember(reservation.getMemberId());

        return saved;
    }

    public Reservation completeReservation(Long id) {
        Reservation reservation = findById(id);

        // State Pattern : délégue la transition à l'état courant
        ReservationState state = ReservationStateFactory.getState(reservation);
        try {
            state.complete();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Reservation saved = reservationRepository.save(reservation);

        // Restore room availability
        restTemplate.put(
                ROOM_SERVICE_URL + "/api/rooms/" + reservation.getRoomId() + "/availability?available=true",
                null
        );

        // Check if member should be unsuspended
        checkAndUnsuspendMember(reservation.getMemberId());

        return saved;
    }

    private void checkAndUnsuspendMember(Long memberId) {
        try {
            Boolean memberSuspended = restTemplate.getForObject(
                    MEMBER_SERVICE_URL + "/api/members/" + memberId + "/suspended",
                    Boolean.class
            );

            if (memberSuspended != null && memberSuspended) {
                Map memberData = restTemplate.getForObject(
                        MEMBER_SERVICE_URL + "/api/members/" + memberId,
                        Map.class
                );

                if (memberData != null) {
                    Object maxBookingsObj = memberData.get("maxConcurrentBookings");
                    if (maxBookingsObj != null) {
                        int maxBookings = ((Number) maxBookingsObj).intValue();
                        List<Reservation> confirmed = reservationRepository
                                .findByMemberIdAndStatus(memberId, ReservationStatus.CONFIRMED);
                        if (confirmed.size() < maxBookings) {
                            reservationEventProducer.sendMemberUnsuspend(memberId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log and continue - unsuspend check is non-critical
        }
    }

    /**
     * Cancel all CONFIRMED reservations for a deleted room (no secondary Kafka events).
     */
    public void cancelAllReservationsForRoom(Long roomId) {
        List<Reservation> confirmed = reservationRepository
                .findByRoomIdAndStatus(roomId, ReservationStatus.CONFIRMED);
        for (Reservation reservation : confirmed) {
            reservation.setStatus(ReservationStatus.CANCELLED);
        }
        reservationRepository.saveAll(confirmed);
    }

    /**
     * Delete all reservations for a deleted member.
     */
    public void deleteAllReservationsForMember(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        reservationRepository.deleteAll(reservations);
    }
}
