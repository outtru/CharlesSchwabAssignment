# API Reference

## Base path

All versioned endpoints are exposed under:

- /api/v1

## Route map

| Operation | HTTP method | Endpoint | Purpose | Workflow stage |
| --- | --- | --- | --- | --- |
| Enroll learner | POST | /api/v1/courses/{courseId}/enrollments | Request a seat or queue placement | Enrollment request |
| Check roster | GET | /api/v1/courses/{courseId}/enrollments | Inspect current enrolled learners | Capacity and state validation |
| Check waitlist | GET | /api/v1/courses/{courseId}/waitlist | Inspect the queued learners in FIFO order | Queue monitoring |
| Drop learner | DELETE | /api/v1/courses/{courseId}/enrollments/{learnerId} | Remove a learner and evaluate queue promotion | Seat release and promotion |

## Request payload for enrollment

```json
{
  "learnerId": "learner-1",
  "completedCourses": ["CS100"]
}
```

## Response contract

The service returns an `EnrollmentResult` payload with the following shape:

```json
{
  "success": true,
  "status": "ENROLLED",
  "message": "Enrollment successful."
}
```

Possible status values:

- ENROLLED
- WAITLISTED
- ALREADY_ENROLLED
- ALREADY_WAITLISTED
- INELIGIBLE
- INVALID_REQUEST
- NOT_FOUND
- DROPPED
- WAITLIST_REMOVED

## Workflow mapping

### 1. Enrollment workflow

POST /api/v1/courses/{courseId}/enrollments

- Validates the course identifier.
- Validates learner ID and eligibility.
- Returns `ENROLLED` if capacity is available.
- Returns `WAITLISTED` if the course is full and the learner is eligible.
- Returns `INELIGIBLE` if the learner is missing the required prerequisite.

### 2. Capacity and roster inspection workflow

GET /api/v1/courses/{courseId}/enrollments

- Used by administrators or clients to view the current enrolled set.
- Helps confirm seat availability and business state before extra operations.

### 3. Waitlist monitoring workflow

GET /api/v1/courses/{courseId}/waitlist

- Returns the queue in FIFO order.
- Supports operational review or UI display of the waiting candidates.

### 4. Drop and promotion workflow

DELETE /api/v1/courses/{courseId}/enrollments/{learnerId}

- If the learner is enrolled, they are removed and the queue is evaluated.
- If the learner is waitlisted, they are removed from the queue.
- If no matching learner is found, the service returns `NOT_FOUND`.

## Notes on versioning

The application is intentionally versioned under `/api/v1` so future changes can be deployed without breaking consumers that depend on the current contract. This is the recommended base for a compatibility strategy as the service evolves.
