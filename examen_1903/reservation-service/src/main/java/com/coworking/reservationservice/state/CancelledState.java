package com.coworking.reservationservice.state;

/**
 * État CANCELLED : aucune transition n'est possible depuis cet état.
 */
public class CancelledState implements ReservationState {

    @Override
    public void cancel() {
        throw new IllegalStateException("Impossible d'annuler une réservation déjà annulée.");
    }

    @Override
    public void complete() {
        throw new IllegalStateException("Impossible de compléter une réservation annulée.");
    }
}
