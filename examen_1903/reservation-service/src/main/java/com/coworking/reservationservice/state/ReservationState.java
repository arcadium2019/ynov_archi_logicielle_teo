package com.coworking.reservationservice.state;

/**
 * State Pattern — interface définissant les transitions possibles
 * pour le cycle de vie d'une réservation.
 */
public interface ReservationState {

    /**
     * Annule la réservation.
     * @throws IllegalStateException si l'état courant ne permet pas l'annulation
     */
    void cancel();

    /**
     * Complète la réservation.
     * @throws IllegalStateException si l'état courant ne permet pas la complétion
     */
    void complete();
}
