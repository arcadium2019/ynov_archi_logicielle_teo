package com.coworking.reservationservice.state;

/**
 * État COMPLETED : aucune transition n'est possible depuis cet état.
 */
public class CompletedState implements ReservationState {

    @Override
    public void cancel() {
        throw new IllegalStateException("Impossible d'annuler une réservation déjà complétée.");
    }

    @Override
    public void complete() {
        throw new IllegalStateException("Impossible de compléter une réservation déjà complétée.");
    }
}
