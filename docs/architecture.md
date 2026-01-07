# Architecture Overview

## Purpose

This project is a **self-hosted retail back-office sidecar** designed to ingest POS exports,
process them asynchronously, and provide actionable dashboards and alerts for small retailers.

The architecture is intentionally modular to:
- keep business logic framework-agnostic
- support long-running background jobs
- reflect real-world retail software patterns
- allow gradual extraction of reusable core modules

This is **not** a POS system.  
It complements existing POS solutions by automating back-office analytics.

---

## High-level flow

           [ Web / Android ]
                   |
                   v
               [ apps/api ]
                   |
                   v
           [ processing-core ]
             ^            |
             |            v
      [ scheduler-core ] [ ingest-core ]
                               |
                               v
                        [ normalized data ]
                               |
                               v
                         [ analytics-core ]
                               |
                               v
                             [ alerts ]

         eventbus-core listens to processing, ingest, analytics


Key rule:
> Dependencies flow **downward only**.  
Core modules must never depend on Spring, HTTP, or controllers.

---

## Module responsibilities

### apps/api (Spring Boot application)

**Role:** Composition root and system boundary.

**Responsibilities**
- Expose REST APIs (imports, dashboards, alerts, auth)
- Integrate Spring Security (JWT, filters, config)
- Configure persistence (Postgres, JPA, migrations)
- Wire core modules together
- Define transaction boundaries
- Map domain errors → HTTP responses
- Application lifecycle management

**Must NOT**
- Contain CSV parsing logic
- Contain KPI formulas
- Manage thread pools directly
- Contain heavy business logic

This is the **only module aware of Spring**.

---

### modules/common

**Role:** Shared domain language and primitives.

**Responsibilities**
- Domain enums (`ImportType`, `ImportStatus`, `AlertSeverity`)
- Common exception hierarchy
- Time abstractions (`Clock`, `TimeProvider`)
- Identifiers (`BatchId`, `StoreId`) if needed
- Small shared value objects

**Must NOT**
- Become a dumping ground for utilities
- Contain business logic
- Depend on infrastructure

---

### modules/ingest-core

**Role:** Convert raw POS exports into validated, typed records.

**Responsibilities**
- Stream-read CSV files (line-by-line)
- Validate headers and required columns
- Parse rows into typed records
- Capture row-level errors with line numbers
- Produce ingest statistics

**Inputs**
- `InputStream` or file path
- Ingest configuration (delimiter, date formats, column mapping)

**Outputs**
- Valid record stream or batches
- Row error list
- Counters (rows read / valid / invalid)

**Must NOT**
- Write to the database
- Compute analytics
- Manage threads
- Depend on Spring or persistence

---

### modules/processing-core

**Role:** Reliable execution of long-running background jobs.

**Responsibilities**
- Own thread pools and execution model
- Submit and execute jobs
- Track job lifecycle (QUEUED → RUNNING → DONE / FAILED / CANCELLED)
- Report progress
- Support cancellation
- Coordinate retries (basic)

**Concepts**
- Job
- JobHandle / JobId
- JobContext
- JobProgress

**Must NOT**
- Define KPI formulas
- Parse CSV formats directly
- Expose HTTP endpoints

---

### modules/eventbus-core

**Role:** Internal, lightweight domain event communication.

**Responsibilities**
- Publish domain events
- Support synchronous and asynchronous listeners
- Decouple producers from consumers
- Enable progress updates, alerts, logging

**Example events**
- `ImportStarted`
- `ImportProgress`
- `ImportCompleted`
- `KpiComputed`
- `AlertCreated`

**Must NOT**
- Persist events
- Guarantee delivery across restarts
- Replace message brokers (Kafka/RabbitMQ)

---

### modules/analytics-core

**Role:** Business value module (core of the product).

**Responsibilities**
- Aggregate normalized sales data into KPIs
- Compute metrics:
    - revenue
    - units sold
    - receipts
    - average basket
    - top products
    - hourly distribution
- Materialize daily aggregates
- Generate alert candidates based on rules

**Inputs**
- Access to normalized sales data
- Time ranges and rule thresholds

**Outputs**
- KPI domain objects
- Persisted aggregates
- Alert candidate events

**Must NOT**
- Parse files
- Manage execution threads
- Handle notifications or HTTP

---

### modules/scheduler-core

**Role:** Time-based execution support (non-core feature).

**Responsibilities**
- Schedule recurring tasks (daily, fixed delay)
- Retry scheduled tasks with backoff
- Graceful shutdown coordination

**Use cases**
- Nightly KPI recomputation
- Periodic health checks
- Scheduled imports

**Must NOT**
- Contain business logic
- Replace enterprise schedulers (Quartz)
- Provide distributed scheduling (single-node MVP)

---

### Persistence strategy

#### MVP approach (recommended)
- JPA entities and repositories live in `apps/api`
- Core modules depend on repository *interfaces* only
- Implementation is provided by Spring

This keeps early development fast while preserving boundaries.

---

## Client applications

### clients/web (React)

**Responsibilities**
- Authentication UI
- CSV import upload and status
- KPI dashboards
- Alerts list and acknowledgment

No business logic lives here.

---

### clients/android (Kotlin)

**Responsibilities**
- Authentication
- Dashboard summary
- Alerts viewing and acknowledgment
- Optional import status monitoring

Acts as a companion app, not an admin console.

---

## Deployment / infrastructure

### infra

**Responsibilities**
- docker-compose setup
- Environment templates
- Data volumes
- Sample CSV files

Goal:
> A non-developer can run the system locally and get value.

---

## Implementation strategy (important)

To avoid early complexity:
1. Start with everything in `apps/api`
2. Keep module boundaries in this document
3. Extract modules gradually:
    - `common`
    - `ingest-core`
    - `processing-core`
    - `analytics-core`
    - `eventbus-core`
    - `scheduler-core`

This mirrors how real production systems evolve.

---

## Design principles

- Business logic over frameworks
- Streaming over loading into memory
- Explicit background processing
- Fail-soft ingestion (bad rows don’t kill imports)
- Boring, understandable solutions

---

## Non-goals (MVP)

- Multi-tenant SaaS
- Distributed scheduling
- Real-time analytics
- AI-driven insights
- Complex RBAC

These can be added later if needed.

---

## Summary

This architecture supports:
- real retail data volumes
- asynchronous processing
- clear ownership boundaries
- incremental growth
- maintainability over hype

It is designed to feel familiar to engineers working on POS and retail back-office systems.