# tippspiel-ddd

A football tournament prediction game, implemented with **Domain-Driven Design**,
**Spring Boot**, **Kafka** and **PostgreSQL**.

This is the implementation repository. The full DDD planning process (domain discovery,
context mapping, tactical design) is documented in
[enke5-ddd-planning-1](https://github.com/enke5/enke5-ddd-planning-1).

---

## Architecture

Two Bounded Contexts, each deployed as an independent Spring Boot service:

| Service | Port | Role |
|---------|------|------|
| `tournament-management-service` | 8081 | Upstream — manages matches, tournament groups, results |
| `betting-scoring-service` | 8082 | Core Domain — manages predictions, scoring, prize distribution |

Integration via **Kafka** (KRaft, no ZooKeeper):
```
tippspiel.tournament-management.events  →  consumed by betting-scoring-service
tippspiel.betting-scoring.events        →  (future: notifications, UI push)
```

Shared event contracts live in `:shared:domain-events`.

---

## Quick Start (Docker Compose)

Requirements: Docker, Docker Compose

```bash
# Build both services
./gradlew bootJar

# Start everything (Kafka + PostgreSQL + both services)
docker compose up
```

Services will be available at:
- Tournament Management: http://localhost:8081
- Betting & Scoring: http://localhost:8082

---

## Local Development

Requirements: Java 21, Gradle 8+, a running PostgreSQL and Kafka instance.

```bash
# Run PostgreSQL and Kafka only
docker compose up kafka postgres

# Start tournament-management-service
./gradlew :tournament-management-service:bootRun

# Start betting-scoring-service (in another terminal)
./gradlew :betting-scoring-service:bootRun
```

---

## Project Structure

```
tippspiel-ddd/
├── shared/
│   └── domain-events/              # Shared Kafka event contracts (Java records)
│       └── .../events/tournament/  # 8 integration events from Tournament Management
│
├── tournament-management-service/
│   ├── domain/model/               # Match aggregate, Score, MatchRound, TeamRef
│   ├── domain/repository/          # MatchRepository interface
│   ├── application/command/        # ScheduleMatch, StartMatch, FinishMatch, ...
│   ├── infrastructure/persistence/ # JPA implementations
│   ├── infrastructure/kafka/       # Kafka producer configuration
│   └── infrastructure/web/         # REST controllers (admin API)
│
├── betting-scoring-service/
│   ├── domain/model/               # BettingGroup, MatchPrediction, Stake, ...
│   ├── domain/repository/          # Repository interfaces
│   ├── application/command/        # CreateBettingGroup, SubmitPrediction, ...
│   ├── application/eventhandler/   # Kafka listeners (MatchStarted → lock predictions)
│   ├── infrastructure/persistence/ # JPA implementations
│   └── infrastructure/web/         # REST controllers (player API)
│
├── docker/
│   └── postgres/init.sql           # Creates both application databases
├── docker-compose.yml              # Full stack for beginner deployment
└── build.gradle.kts                # Root build (Gradle multi-module)
```

---

## Domain Language

All code uses English. The authoritative German → English translation is in the
[Ubiquitous Language Glossary](https://github.com/enke5/enke5-ddd-planning-1/blob/main/docs/tactical-design/session-10-ubiquitous-language.md).

Key terms: `BettingGroup`, `Participation`, `MatchPrediction`, `ChampionshipPrediction`,
`GroupStageContest`, `Stake`, `PrizePot`, `PrizeTier`.

---

## Technology Stack

| Layer | Choice |
|-------|--------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Messaging | Apache Kafka 3.x (KRaft) |
| Persistence | PostgreSQL 16 + Flyway migrations |
| Build | Gradle 8 (Kotlin DSL) |
| Container | Docker / Docker Compose |
| Orchestration | Kubernetes (manifests coming) |

---

## Deployment

### Docker Compose (beginner-friendly)
See Quick Start above. One command starts the full stack.

### Kubernetes
Kubernetes manifests and a Helm chart will be added in a later iteration.
The services are stateless and read configuration from environment variables —
they are Kubernetes-ready by design.

---

## Status

- [x] Project scaffold (both services, shared events, Docker Compose)
- [x] Domain model: `Match` aggregate with full lifecycle
- [x] Domain model: `BettingGroup`, `MatchPrediction`, `Stake` value object
- [x] Kafka integration: `MatchStarted` → lock predictions
- [x] Database schemas (Flyway migrations)
- [ ] Full REST API (tournament admin + player endpoints)
- [ ] Scoring service implementation
- [ ] Prize distribution algorithm
- [ ] Read model / CQRS projections (leaderboard)
- [ ] Frontend (multilingual)
- [ ] Kubernetes manifests
