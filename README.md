Order Management System

An event-driven microservices system for managing orders, built with Spring Boot and Apache Kafka. It consists of 4 independent services that communicate asynchronously via Kafka.

What it does

When a client places an order, the process flows through several services in an event-driven manner:

Order Service accepts the order and publishes an OrderCreated event
Inventory Service checks and reserves stock (or rejects it)
Payment Service simulates the payment
Notification Service listens to every step and logs a notification
If the payment fails, a Saga pattern kicks in — the reserved stock is automatically released (compensating transaction)
Architecture
POST /orders
OrderCreated
StockReserved /StockNotAvailable
PaymentProcessed /PaymentFailed
CONFIRMED / CANCELLED
stock levels
Client / Postman
Order Service
Kafka
Inventory Service
Payment Service
Notification Service
PostgreSQL - orders
PostgreSQL - products
Tech stack
Java 21, Spring Boot 4
Apache Kafka (KRaft mode — no Zookeeper) for asynchronous service-to-service messaging
PostgreSQL — one database per service (database-per-service pattern)
Spring Data JPA / Hibernate
Docker & Docker Compose — the whole system starts with a single command
Kafka UI — for visually inspecting topics and messages
New concepts I learned in this project
Producer/consumer architecture with Apache Kafka
Handling multiple event types on a single topic (@KafkaHandler type-based routing)
The Saga pattern — compensating transactions for distributed failure handling
Handling "poison pill" messages with ErrorHandlingDeserializer
Container-to-container communication in Docker (internal vs. external listeners)
Containerizing each service of a multi-module Gradle project with its own Dockerfile
How to run
bash
docker-compose up -d --build

This spins up 8 containers: 4 services, 2 PostgreSQL instances, Kafka, and Kafka UI.

Kafka UI: http://localhost:8090

Services and ports
Service	Port	Responsibility
Order Service	8081	Accepting orders and managing their status
Notification Service	8082	Logging notifications to the console
Inventory Service	8083	Managing stock
Payment Service	8084	Simulating payments