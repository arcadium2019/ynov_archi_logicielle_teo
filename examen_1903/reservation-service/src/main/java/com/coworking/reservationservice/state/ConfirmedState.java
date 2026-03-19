package com.coworking.reservationservice.state;

import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.model.ReservationStatus;

/**
 * État CONFIRMED : la réservation peut être annulée ou complétée.
 */
public class ConfirmedState implements ReservationState {

    private final Reservation reservation;

    public ConfirmedState(Reservation reservation) {
        this.reservation = reservation;
    }

    @Override
    public void cancel() {
        reservation.setStatus(ReservationStatus.CANCELLED);
    }

    @Override
    public void complete() {
        reservation.setStatus(ReservationStatus.COMPLETED);
    }
}
