# Architectural Decisions

## ADR-1: Single-offering scope

Decision: implement only one course offering.

Reasoning: the assignment requires a bounded feature slice. A single offering keeps the service easier to test and reason about while still delivering the core registration workflow.

## ADR-2: SQLite-backed relational persistence

Decision: store course, learner, and enrollment data in SQLite using Flyway-managed schema migrations.

Reasoning: the service needs durable state without introducing the operational overhead of a full enterprise database. SQLite fits the bounded assignment scope while still providing a real persistence layer and safer schema evolution.

## ADR-3: Explicit prerequisite enforcement

Decision: validate learner eligibility before enrollment or waitlisting.

Reasoning: capacity and queue semantics are meaningless if an ineligible learner can occupy a seat or queue position. This keeps the queue valid and ensures no invalid state is created.

## ADR-4: Transactional enforcement with database sequencing

Decision: protect queue updates through a transactional service layer and a persisted sequence number.

Reasoning: in a database-backed system, correctness comes from transaction boundaries and ordering metadata rather than relying only on single-process locking. This makes the flow safer under concurrent requests and future scale-out scenarios.

## ADR-5: Strict rejection for ineligible learners

Decision: reject ineligible enrollment requests immediately.

Reasoning: the service does not maintain a queue of ineligible learners. This is simpler and avoids stale queue records or unintended seat blocking.

## ADR-6: Scale-out path

Decision: treat the SQLite-backed service as the bounded first iteration, not the final platform architecture.

Reasoning: production scale would require a shared transactional store, stronger queue orchestration, and possibly a distributed eventing or messaging layer for multi-node deployments.
