# Coworking Platform — Architecture Microservices

Plateforme de gestion de salles de coworking construite avec Spring Boot 3.2 / Spring Cloud 2023.

## Description

Le projet est composé de 6 microservices :

| Service | Port | Rôle |
|---|---|---|
| `config-server` | 8888 | Centralisation de la configuration (Spring Cloud Config) |
| `discovery-server` | 8761 | Registre de services (Netflix Eureka) |
| `api-gateway` | 8080 | Point d'entrée unique — routage vers les services (Spring Cloud Gateway) |
| `room-service` | 8081 | CRUD salles + disponibilités |
| `member-service` | 8082 | CRUD membres + gestion quotas/suspension |
| `reservation-service` | 8083 | Création/annulation/complétion de réservations |

### Communication entre services

- **REST** (RestTemplate) : reservation-service appelle room-service et member-service pour vérifier disponibilité et suspension
- **Kafka** : événements asynchrones pour la propagation d'effets de bord (suppression salle/membre, dépassement de quota)

### Flux Kafka

| Topic | Producteur | Consommateur | Effet |
|---|---|---|---|
| `room-deleted` | room-service | reservation-service | Annule toutes les réservations CONFIRMED de la salle |
| `member-deleted` | member-service | reservation-service | Supprime toutes les réservations du membre |
| `member-suspend` | reservation-service | member-service | Suspend le membre (quota atteint) |
| `member-unsuspend` | reservation-service | member-service | Désuspend le membre (quota repassé sous la limite) |

### Quotas de réservations simultanées

| Abonnement | Quota |
|---|---|
| BASIC | 2 |
| PRO | 5 |
| ENTERPRISE | 10 |

## Prérequis

- **Java 17**
- **Maven 3.8+**
- **Apache Kafka** (avec Zookeeper) accessible sur `localhost:9092`

### Démarrer Kafka (si non disponible)

```bash
# Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Kafka broker
bin/kafka-server-start.sh config/server.properties
```

## Lancement des services

Les services doivent être démarrés **dans l'ordre suivant** :

```bash
# 1. Config Server
cd config-server && mvn spring-boot:run

# 2. Discovery Server (Eureka)
cd discovery-server && mvn spring-boot:run

# 3. API Gateway
cd api-gateway && mvn spring-boot:run

# 4. Services métier (dans n'importe quel ordre)
cd room-service && mvn spring-boot:run
cd member-service && mvn spring-boot:run
cd reservation-service && mvn spring-boot:run
```

Ou depuis la racine du projet :

```bash
mvn spring-boot:run -pl config-server
mvn spring-boot:run -pl discovery-server
mvn spring-boot:run -pl api-gateway
mvn spring-boot:run -pl room-service
mvn spring-boot:run -pl member-service
mvn spring-boot:run -pl reservation-service
```

## URLs Swagger / OpenAPI

| Service | URL Swagger UI | URL spec JSON |
|---|---|---|
| room-service | http://localhost:8081/swagger-ui/index.html | http://localhost:8081/v3/api-docs |
| member-service | http://localhost:8082/swagger-ui/index.html | http://localhost:8082/v3/api-docs |
| reservation-service | http://localhost:8083/swagger-ui/index.html | http://localhost:8083/v3/api-docs |

## Consoles H2 (base de données en mémoire)

| Service | URL |
|---|---|
| room-service | http://localhost:8081/h2-console |
| member-service | http://localhost:8082/h2-console |
| reservation-service | http://localhost:8083/h2-console |

## Exemples de requêtes Postman

### 1. Créer une salle

```
POST http://localhost:8081/api/rooms
Content-Type: application/json

{
  "name": "Salle Horizon",
  "city": "Paris",
  "capacity": 10,
  "type": "MEETING_ROOM",
  "hourlyRate": 50.00,
  "available": true
}
```

Types disponibles : `OPEN_SPACE`, `MEETING_ROOM`, `PRIVATE_OFFICE`

### 2. Créer un membre BASIC

```
POST http://localhost:8082/api/members
Content-Type: application/json

{
  "fullName": "Alice Martin",
  "email": "alice@example.com",
  "subscriptionType": "BASIC",
  "suspended": false
}
```

Types disponibles : `BASIC`, `PRO`, `ENTERPRISE`

### 3. Créer une réservation

```
POST http://localhost:8083/api/reservations
Content-Type: application/json

{
  "roomId": 1,
  "memberId": 1,
  "startDateTime": "2026-04-01T09:00:00",
  "endDateTime": "2026-04-01T11:00:00"
}
```

La réservation vérifie automatiquement :
- La disponibilité de la salle
- La suspension du membre
- L'absence de conflit horaire sur la même salle

### 4. Tenter une double réservation sur le même créneau (doit échouer)

```
POST http://localhost:8083/api/reservations
Content-Type: application/json

{
  "roomId": 1,
  "memberId": 2,
  "startDateTime": "2026-04-01T10:00:00",
  "endDateTime": "2026-04-01T12:00:00"
}
```

Réponse attendue : `400 Bad Request — "Room already booked for this time slot"`

### 5. Tester le quota BASIC (2 réservations max)

Créer 2 réservations pour le même membre BASIC sur des créneaux différents.
À la 2e réservation, le membre passe automatiquement à `suspended = true` via Kafka.

```
POST http://localhost:8083/api/reservations
Content-Type: application/json

{
  "roomId": 1,
  "memberId": 1,
  "startDateTime": "2026-04-02T09:00:00",
  "endDateTime": "2026-04-02T11:00:00"
}
```

Vérifier la suspension :
```
GET http://localhost:8082/api/members/1/suspended
```

### 6. Annuler une réservation

```
PUT http://localhost:8083/api/reservations/1/cancel
```

Après annulation, le membre est désuspendu si son quota repasse sous la limite.

### 7. Compléter une réservation

```
PUT http://localhost:8083/api/reservations/1/complete
```

### 8. Supprimer une salle (propagation Kafka → annulation réservations)

```
DELETE http://localhost:8081/api/rooms/1
```

Toutes les réservations CONFIRMED pour cette salle sont annulées automatiquement via le topic `room-deleted`.

### 9. Supprimer un membre (propagation Kafka → suppression réservations)

```
DELETE http://localhost:8082/api/members/1
```

Toutes les réservations du membre sont supprimées automatiquement via le topic `member-deleted`.

### 10. Lister toutes les réservations d'un membre

```
GET http://localhost:8083/api/reservations/member/1
```

## Design Pattern

Le projet implémente le **State Pattern** dans `reservation-service` pour gérer le cycle de vie des réservations.

Voir la documentation détaillée : [`DESIGN_PATTERN.md`](DESIGN_PATTERN.md) et [`reservation-service/DESIGN_PATTERN.md`](reservation-service/DESIGN_PATTERN.md)

## Structure du projet

```
examen_1903/
├── pom.xml                  (parent multi-module)
├── config-server/           (port 8888)
├── discovery-server/        (port 8761)
├── api-gateway/             (port 8080)
├── room-service/            (port 8081)
├── member-service/          (port 8082)
├── reservation-service/     (port 8083)
├── README.md
├── DESIGN_PATTERN.md
└── PROGRESSION.md
```
