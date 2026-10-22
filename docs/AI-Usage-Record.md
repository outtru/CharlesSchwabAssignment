# AI Prompt and Use Record

## Prompt 1

"Create a focused Java implementation for a single-course enrollment, drop, and waitlist feature. The service should enforce prerequisite checks, avoid duplicate enrollment records, maintain FIFO waitlist behavior, and include a unit test suite for capacity, duplicate requests, invalid drops, and prerequisite failures."

## Response summary

The model proposed a minimal service contract with:

- `CourseOffering` metadata including capacity and prerequisite
- `CourseEnrollmentService` with `enroll` and `drop`
- a `ReentrantLock` around state changes
- a waitlist queue plus membership set to avoid duplicates
- strict rejection of ineligible requests so the queue contains only valid candidates

## Review and acceptance

Accepted and edited:

- The lock-based state protection was accepted as the right concurrency control for this bounded in-memory service.
- The duplicate-guard pattern by membership sets was accepted.
- The ineligible-learner policy was strengthened to reject such learners immediately instead of tracking them in the queue.

Rejected or revised:

- A first draft that retained ineligible waitlist entries was rejected because it created ambiguous queue state and violated the service's invariant that the queue should contain only valid candidates.
- The original wording of the waitlist rule was rewritten to explicitly state the stricter assumption: ineligible learners do not enter the queue.

## Files informed by the AI assistance

- `src/main/java/com/cs/assignment/coursewaitlist/CourseEnrollmentService.java`
- `src/test/java/com/cs/assignment/coursewaitlist/CourseEnrollmentServiceTest.java`
- `docs/CourseWaitlistDesign.md`

## Verification evidence

The final implementation was verified with the command below:

```bash
cd /Users/Sahildeep/CharlesSchwabAssignment
mvn -q test
```

This completed successfully with no test failures.
