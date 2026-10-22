# Scalability, Concurrency, and Transactional Reliability

## 1. Current design posture

This project is intentionally a bounded service slice. The implementation is a single-node in-memory service designed to preserve correctness for one course offering under a controlled workload. This is appropriate for the assignment, but it is not the final production architecture for a large enrollment platform.

## 2. Concurrency model

The service uses a single `ReentrantLock` around the critical transition logic for:

- enrollment checks
- duplicate state prevention
- waitlist insertion
- seat release and promotion

This guarantees that the following invariants remain true while the process is running:

- capacity is never exceeded
- a learner is never enrolled more than once
- a learner is never waitlisted more than once
- queue order remains stable for evaluation

## 3. Transactional reliability approach

The service methods are annotated with Spring `@Transactional`, which establishes a clear transaction boundary at the application-service layer. Because the state is in-memory, this is a process-local transactional contract rather than a durable database transaction.

In a production-grade deployment, this pattern would be replaced by a transactional datastore transaction, for example:

- course enrollment table
- waitlist table
- learner completion table
- atomic seat-release + promotion workflow

## 4. Scale-out strategy

When the system grows beyond a single application instance, the following changes are recommended:

- Move state to a transactional database.
- Use optimistic or pessimistic locking at the course-offering row level.
- Store waitlist records with sequence numbers for deterministic ordering.
- Run promotion logic as a separate transactional workflow or event-driven process.
- Add retry and idempotency keys for duplicate client submissions.

## 5. Operational readiness for production

To make this production-ready, the next round of work should add:

- persistence layer with audit logs
- idempotency keys for retry safety
- health and metrics endpoints
- queue-based promotion worker or scheduled reconciliation
- distributed locking or database transaction isolation for multi-instance deployments

## 6. Recommended next-phase architecture

A scalable future design would likely look like this:

- API layer: Spring Boot services and REST controllers
- Domain service: business rules and state validation
- Persistence layer: relational database with transactional consistency
- Queue/worker layer: asynchronous promotion processing
- Observability layer: logs, traces, and metrics

This keeps the current bounded slice reliable while offering a realistic path to scale.
