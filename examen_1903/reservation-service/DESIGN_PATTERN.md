# Design Pattern — State Pattern dans reservation-service

## Contexte

Une réservation possède un cycle de vie avec trois états possibles :
- **CONFIRMED** : état initial après création, la réservation est active
- **CANCELLED** : la réservation a été annulée
- **COMPLETED** : la réservation s'est déroulée et est terminée

## Problème

Sans pattern, le code utiliserait des `if/switch` dans `ReservationService` pour vérifier l'état courant avant chaque transition :

```java
if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
    reservation.setStatus(ReservationStatus.CANCELLED);
} else {
    throw new IllegalStateException("...");
}
```

Ce code est fragile : à chaque nouvel état ou nouvelle transition, il faut modifier le service directement, violant le principe Open/Closed.

## Solution : State Pattern

Le **State Pattern** (GoF — Behavioral) encapsule le comportement lié à chaque état dans une classe dédiée. L'objet contexte (`Reservation`) délègue les appels de transition à son état courant.

### Structure

```
ReservationState (interface)
├── cancel()
└── complete()

ConfirmedState implements ReservationState
  → cancel()   : passe le statut à CANCELLED
  → complete() : passe le statut à COMPLETED

CancelledState implements ReservationState
  → cancel()   : lève IllegalStateException
  → complete() : lève IllegalStateException

CompletedState implements ReservationState
  → cancel()   : lève IllegalStateException
  → complete() : lève IllegalStateException

ReservationStateFactory
  → getState(Reservation) : retourne l'instance d'état correspondant au statut courant
```

### Fichiers créés

| Fichier | Rôle |
|---------|------|
| `state/ReservationState.java` | Interface du pattern |
| `state/ConfirmedState.java` | État CONFIRMED — transitions autorisées |
| `state/CancelledState.java` | État CANCELLED — transitions interdites |
| `state/CompletedState.java` | État COMPLETED — transitions interdites |
| `state/ReservationStateFactory.java` | Fabrique instanciant le bon état |

### Intégration dans ReservationService

```java
public Reservation cancelReservation(Long id) {
    Reservation reservation = findById(id);

    ReservationState state = ReservationStateFactory.getState(reservation);
    try {
        state.cancel(); // Modifie reservation.status si autorisé
    } catch (IllegalStateException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    return reservationRepository.save(reservation);
}
```

## Avantages

1. **Open/Closed** : ajouter un nouvel état (ex. `PENDING`) ne nécessite que créer une nouvelle classe, sans modifier le service.
2. **Single Responsibility** : chaque classe d'état gère uniquement ses transitions.
3. **Lisibilité** : les règles métier sont explicites et localisées dans chaque classe d'état.
4. **Robustesse** : les transitions invalides lèvent des exceptions typées, rendant les erreurs claires côté API.

## Diagramme de transitions

```
        ┌─────────────┐
        │  CONFIRMED  │
        └──────┬──────┘
               │
       ┌───────┴───────┐
       ▼               ▼
 ┌──────────┐    ┌───────────┐
 │CANCELLED │    │ COMPLETED │
 └──────────┘    └───────────┘
   (terminal)      (terminal)
```

Les états CANCELLED et COMPLETED sont terminaux : aucune transition n'est possible depuis ces états.
