# Design Pattern — State Pattern

Le design pattern utilisé dans ce projet est le **State Pattern**, implémenté dans le `reservation-service`.

Pour la documentation complète, voir : [`reservation-service/DESIGN_PATTERN.md`](reservation-service/DESIGN_PATTERN.md)

## Résumé

Le **State Pattern** (GoF — Behavioral) est appliqué au cycle de vie des réservations.

Une réservation peut être dans l'un des trois états suivants :
- **CONFIRMED** : état actif, peut être annulé ou complété
- **CANCELLED** : état terminal, aucune transition possible
- **COMPLETED** : état terminal, aucune transition possible

L'interface `ReservationState` définit les opérations `cancel()` et `complete()`. Chaque état concret (`ConfirmedState`, `CancelledState`, `CompletedState`) implémente ces méthodes selon ses règles métier.

`ReservationStateFactory` instancie l'état courant à partir du statut de la réservation, et `ReservationService` délègue les transitions à cet état.

Ce pattern garantit que les règles métier de transition sont encapsulées dans des classes dédiées, évitant les `if/switch` dispersés dans le service.
