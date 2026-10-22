# Course Waitlist Service

This Spring Boot project implements a bounded course-offering enrollment flow with enforcement of capacity, prerequisite eligibility, duplicate prevention, and FIFO waitlist promotion.

## What this workflow does

This service covers one course offering and the core operations required to manage enrollment safely:

1. Learner requests to enroll in a specific course.
2. The service validates the learner and prerequisite.
3. If capacity is available, the learner is enrolled.
4. If capacity is full, only eligible learners are queued.
5. A drop creates a seat and promotes the earliest eligible queued learner.
6. Invalid requests and duplicate operations fail gracefully without corrupting state.

## Versioning and API route conventions

The API is versioned under the v1 namespace:

- Base path: /api/v1
- Course resource: /api/v1/courses/{courseId}

This keeps the service easy to evolve without breaking existing consumers while preserving a stable public contract.

## API documentation and observability

The project exposes OpenAPI/Swagger documentation:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

This is useful for testing contract-level behavior and documenting the waitlist lifecycle without external tooling.

## Prerequisite and waitlist assumptions

- Prerequisite rule: a learner can enroll only if their supplied completed-course set contains the configured prerequisite code.
- Ineligible learner rule: ineligible learners are rejected immediately and never remain in the waitlist. This avoids stale queue entries and keeps queue semantics meaningful.
- Promotion rule: after a seat opens, the service scans the waitlist in FIFO order and promotes the earliest eligible learner.
- Duplicate requests are idempotent at the learner state level: a learner cannot be enrolled twice or waitlisted twice.

## Run the application

```bash
cd /Users/Sahildeep/CharlesSchwabAssignment
mvn clean test
mvn spring-boot:run
```

After startup, the application listens on port 8080 and exposes the REST APIs described below.

## API reference

For a full endpoint map and workflow mapping, see [docs/APIReference.md](docs/APIReference.md).

### Core endpoints

- POST /api/v1/courses/{courseId}/enrollments
  - Enroll a learner in the course.
  - Body: learnerId and completedCourses.
  - Used when a learner requests enrollment.

- DELETE /api/v1/courses/{courseId}/enrollments/{learnerId}
  - Drop an enrolled learner or remove a waitlisted learner.
  - Used when a seat becomes available or a learner cancels.

- GET /api/v1/courses/{courseId}/enrollments
  - Return currently enrolled learners.
  - Used to inspect capacity and current roster state.

- GET /api/v1/courses/{courseId}/waitlist
  - Return the current waitlist in FIFO order.
  - Used to monitor queue sequencing.

Example curl requests:

```bash
curl -X POST http://localhost:8080/api/v1/courses/CS101/enrollments \
  -H "Content-Type: application/json" \
  -d '{"learnerId":"learner-1","completedCourses":["CS100"]}'

curl http://localhost:8080/api/v1/courses/CS101/enrollments
curl http://localhost:8080/api/v1/courses/CS101/waitlist
curl -X DELETE http://localhost:8080/api/v1/courses/CS101/enrollments/learner-1
```

## Project structure

- [src/main/java/com/cs/assignment/coursewaitlist/CourseWaitlistApplication.java](src/main/java/com/cs/assignment/coursewaitlist/CourseWaitlistApplication.java) — Spring Boot bootstrap
- [src/main/java/com/cs/assignment/coursewaitlist/CourseEnrollmentService.java](src/main/java/com/cs/assignment/coursewaitlist/CourseEnrollmentService.java) — core business logic and transaction boundary
- [src/main/java/com/cs/assignment/coursewaitlist/CourseOffering.java](src/main/java/com/cs/assignment/coursewaitlist/CourseOffering.java) — course metadata
- [src/main/java/com/cs/assignment/coursewaitlist/EnrollmentResult.java](src/main/java/com/cs/assignment/coursewaitlist/EnrollmentResult.java) — response contract
- [src/main/java/com/cs/assignment/coursewaitlist/EnrollmentStatus.java](src/main/java/com/cs/assignment/coursewaitlist/EnrollmentStatus.java) — state enum
- [src/main/java/com/cs/assignment/coursewaitlist/controller/CourseEnrollmentController.java](src/main/java/com/cs/assignment/coursewaitlist/controller/CourseEnrollmentController.java) — API entry points
- [src/main/java/com/cs/assignment/coursewaitlist/config/CourseConfiguration.java](src/main/java/com/cs/assignment/coursewaitlist/config/CourseConfiguration.java) — definition of the configured course offering
- [src/main/java/com/cs/assignment/coursewaitlist/dto/EnrollmentRequest.java](src/main/java/com/cs/assignment/coursewaitlist/dto/EnrollmentRequest.java) — request shape
- [src/main/resources/application.properties](src/main/resources/application.properties) — runtime properties and service defaults
- [src/test/java/com/cs/assignment/coursewaitlist/CourseEnrollmentServiceTest.java](src/test/java/com/cs/assignment/coursewaitlist/CourseEnrollmentServiceTest.java) — service-level verification
- [src/test/java/com/cs/assignment/coursewaitlist/CourseEnrollmentControllerTest.java](src/test/java/com/cs/assignment/coursewaitlist/CourseEnrollmentControllerTest.java) — API/HTTP contract verification

## Concurrency, scalability, and reliability

This service is designed for a single-node, single-course workflow and uses a SQLite-backed relational model with explicit transactional boundaries. The key reliability traits are:

- capacity is enforced against a persisted course record
- duplicate enrollments are prevented by immutable learner and course identifiers
- queue ordering is preserved using a persisted sequence number
- drop operations trigger waitlist promotion within a transaction

For higher scale or multiple application instances, the next step would be a distributed queue and/or a separate orchestration service. This is covered in [docs/ScalabilityAndReliability.md](docs/ScalabilityAndReliability.md).

## Database migration strategy

The project uses Flyway for schema versioning:

- migration scripts: [src/main/resources/db/migration](src/main/resources/db/migration)
- current schema version: V1__create_waitlist_schema.sql
- seed data: V2__seed_default_course.sql

This creates separate tables for courses, learners, and enrollment records instead of relying on a single flat table. It makes future schema changes safer and clearer than ad-hoc SQL initialization.

## Limitations

- This remains intentionally bounded to one course offering.
- The service does not include identity federation, payment integration, or multi-term registration.
- Course completion data is still passed as a request payload rather than fetched from a separate learning record system.
