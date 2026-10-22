# Course Waitlist Case Study

## Background

A single course offering can be modeled as a bounded capacity system with a small queue for overflow learners. The business rule is simple but operationally important: learners should not exceed capacity, should not be double-enrolled, and should only be queued when they are eligible for the course.

## Problem

A course with capacity 1 or 5 can experience race conditions when multiple learners attempt enrollment at the same time. In the absence of a lock or database transaction, the in-memory set can oversubscribe and create duplicate records.

## Solution

The service centralizes state updates under a single `ReentrantLock`. It keeps a linked set for enrollment and a queue for waiting learners while enforcing prerequisite validation before any state mutation. When a seat opens, the service promotes the first eligible learner in the waitlist.

## Outcome

The bounded service guarantees:

- no over-capacity enrollment
- no duplicate membership records
- FIFO promotion for eligible waitlisted learners
- clear invalid-request handling

## Key lesson

The simplest reliable pattern for a bounded offering is a centralized state machine with explicit validation and a transaction-like lock. It is fast, easy to validate, and gives a strong baseline before introducing persistence, distributed coordination, or event-driven promotion.
