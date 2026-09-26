# Order Management System

An event-driven microservices system for managing orders, built with **Spring Boot** and **Apache Kafka**.

The system consists of **4 independent services** that communicate asynchronously through Kafka.

## What It Does

When a client places an order, the process flows through several services in an **event-driven** manner:

1. **Order Service** accepts the order and publishes an `OrderCreated` event.
2. **Inventory Service** checks and reserves stock (or rejects it).
3. **Payment Service** simulates the payment.
4. **Notification Service** listens to every step and logs a notification.
5. If the payment fails, a **Saga pattern** kicks in and the reserved stock is automatically released through a compensating transaction.

## Architecture

```text
                        ┌──────────────────┐
                        │      Client       │
                        │   / Postman       │
                        └────────┬─────────┘
                                 │
                                 ▼
                        ┌──────────────────┐
                        │  Order Service   │
                        │     :8081        │
                        └────────┬─────────┘
                                 │
                         OrderCreated
                                 │
                                 ▼
                        ┌──────────────────┐
                        │      Kafka       │
                        └────────┬─────────┘
                                 │
                                 ▼
                        ┌──────────────────┐
                        │ Inventory Service│
                        │     :8083        │
                        └────────┬─────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
             StockReserved             StockNotAvailable
                    │                         │
                    ▼                         ▼
          ┌──────────────────┐        ┌──────────────────┐
          │ Payment Service  │        │  Order Cancelled │
          │      :8084       │        └──────────────────┘
          └────────┬─────────┘
                   │
          ┌────────┴────────┐
          │                 │
   PaymentProcessed    PaymentFailed
          │                 │
          ▼                 ▼
     Order Confirmed    Saga Compensation
                            │
                            ▼
                     Stock Released

                 ┌──────────────────────┐
                 │ Notification Service │
                 │        :8082         │
                 └──────────────────────┘
                         ▲
                         │
                  Listens to events
```

### Data Storage

* **PostgreSQL — Orders Database**
* **PostgreSQL — Products Database**

Each service manages its own data, following the **database-per-service** pattern.

## Tech Stack

* **Java 21**
* **Spring Boot 4**
* **Apache Kafka** — KRaft mode, without ZooKeeper, for asynchronous service-to-service messaging
* **PostgreSQL** — database-per-service pattern
* **Spring Data JPA / Hibernate**
* **Docker & Docker Compose** — the whole system starts with a single command
* **Kafka UI** — for visually inspecting Kafka topics and messages
* **Gradle**

## New Concepts I Learned

This project helped me learn and practice:

* Producer/Consumer architecture with **Apache Kafka**
* Handling multiple event types on a single topic using `@KafkaHandler`
* The **Saga pattern** and compensating transactions for distributed failure handling
* Handling "poison pill" messages with `ErrorHandlingDeserializer`
* Container-to-container communication in Docker
* Internal vs. external Kafka listeners
* Containerizing each service of a multi-service Gradle project with its own Dockerfile
* Event-driven communication between microservices

## How to Run

Make sure **Docker Desktop** is running, then execute:

```bash
docker-compose up -d --build
```

This starts the complete system with the required containers.

### Kafka UI

After the containers start, Kafka UI is available at:

**http://localhost:8090**

Kafka UI can be used to inspect Kafka topics, events, and messages.

## Services and Ports

| Service                  |   Port | Responsibility                             |
| ------------------------ | -----: | ------------------------------------------ |
| **Order Service**        | `8081` | Accepting orders and managing their status |
| **Notification Service** | `8082` | Logging notifications                      |
| **Inventory Service**    | `8083` | Managing product stock                     |
| **Payment Service**      | `8084` | Simulating payments                        |

## Event Flow

The main event flow is:

```text
OrderCreated
     │
     ▼
Inventory Service
     │
     ├── StockReserved ──► Payment Service
     │                         │
     │                         ├── PaymentProcessed ──► Order Confirmed
     │                         │
     │                         └── PaymentFailed ──► Saga Compensation
     │                                                   │
     │                                                   ▼
     │                                             Stock Released
     │
     └── StockNotAvailable ──► Order Cancelled
```

The **Notification Service** listens to the relevant events throughout the process and logs notifications.
