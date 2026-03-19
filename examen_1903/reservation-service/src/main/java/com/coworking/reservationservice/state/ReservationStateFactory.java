package com.coworking.reservationservice.state;

import com.coworking.reservationservice.model.Reservation;
import com.coworking.reservationservice.model.ReservationStatus;

/**
 * Fabrique qui instancie le bon état en fonction du statut courant de la réservation.
 */
public class ReservationStateFactory {

    private ReservationStateFactory() {}

    public static ReservationState getState(Reservation reservation) {
        ReservationStatus status = reservation.getStatus();
        if (status == null) {
            throw new IllegalArgumentException("Le statut de la réservation ne peut pas être null.");
        }
        switch (status) {
            case CONFIRMED:
                return new ConfirmedState(reservation);
            case CANCELLED:
                return new CancelledState();
            case COMPLETED:
                return new CompletedState();
            default:
                throw new IllegalArgumentException("Statut inconnu : " + status);
        }
    }
}
