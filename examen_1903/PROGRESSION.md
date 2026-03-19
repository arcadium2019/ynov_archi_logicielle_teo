# Progression — Examen Architecture Microservices

## Étape 1 — Créer les projets Spring Boot ✅
- [x] `pom.xml` parent multi-module
- [x] `config-server` (port 8888) — `@EnableConfigServer`, configs natives dans `resources/configs/`
- [x] `discovery-server` (port 8761) — `@EnableEurekaServer`
- [x] `api-gateway` (port 8080) — routes vers les 3 services via Eureka (`lb://`)
- [x] `room-service` (port 8081) — structure de base
- [x] `member-service` (port 8082) — structure de base
- [x] `reservation-service` (port 8083) — structure de base

---

## Étape 2 — Implémenter les microservices ✅
- [x] **Room Service** : entité `Room` + CRUD + `checkAvailability` + `updateAvailability` + Kafka producer `room-deleted`
- [x] **Member Service** : entité `Member` + CRUD + quotas auto via `@PrePersist` (BASIC=2, PRO=5, ENTERPRISE=10) + `suspendMember`/`unsuspendMember` + Kafka producer `member-deleted` + Kafka consumer `member-suspend`/`member-unsuspend`
- [x] **Reservation Service** : entité `Reservation` + création avec vérif REST (salle dispo + membre non suspendu + conflit créneau) + annulation + complétion + Kafka producer `member-suspend`/`member-unsuspend` + Kafka consumer `room-deleted`/`member-deleted`

---

## Étape 3 — Intégrer Kafka ✅
- [x] Suppression salle → annulation automatique réservations CONFIRMED (topic : `room-deleted`)
- [x] Suppression membre → suppression toutes ses réservations (topic : `member-deleted`)
- [x] Création réservation → quota atteint → `suspended = true` (topic : `member-suspend`)
- [x] Annulation/complétion → repasse sous quota → `suspended = false` (topic : `member-unsuspend`)

---

## Étape 4 — Design Pattern ✅
- [x] Implémenter un pattern dans `reservation-service`
  - Piste retenue : **State Pattern** pour le cycle de vie (CONFIRMED → COMPLETED / CANCELLED)
  - Classes : `ReservationState` (interface), `ConfirmedState`, `CancelledState`, `CompletedState`, `ReservationStateFactory`
  - Intégré dans `ReservationService.cancelReservation()` et `completeReservation()`
- [x] Rédiger `DESIGN_PATTERN.md`

---

## Étape 5 — Tests Postman ⬜
- [ ] Créer des salles (villes, types variés)
- [ ] Inscrire des membres (BASIC, PRO, ENTERPRISE)
- [ ] Réserver une salle → vérifier disponibilité change
- [ ] Tenter double réservation sur même créneau → doit échouer
- [ ] Atteindre quota BASIC (2 réservations) → `suspended = true`
- [ ] Annuler une réservation → membre désuspendu
- [ ] Supprimer une salle → propagation Kafka sur réservations
- [ ] Supprimer un membre → propagation Kafka

---

## Étape 6 — Documentation Swagger ✅
- [x] `springdoc-openapi` déjà dans les `pom.xml`
- [x] `OpenApiConfig` créée dans `room-service`, `member-service`, `reservation-service`
- [x] Swagger UI disponible sur `/swagger-ui/index.html` pour chaque service

---

## Étape 7 — Livrables ✅
- [x] `README.md` avec instructions de lancement, ports, exemples Postman, URLs Swagger
- [x] `DESIGN_PATTERN.md` à la racine (et dans `reservation-service/`)
- [x] Code source complet

---

## Ports & ordre de démarrage
| Service            | Port |
|--------------------|------|
| config-server      | 8888 |
| discovery-server   | 8761 |
| api-gateway        | 8080 |
| room-service       | 8081 |
| member-service     | 8082 |
| reservation-service| 8083 |
| Kafka (externe)    | 9092 |

**Ordre :** config-server → discovery-server → api-gateway → room/member/reservation
