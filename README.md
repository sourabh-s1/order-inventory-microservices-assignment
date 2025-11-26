# Order & Inventory Microservices Assignment

This repository contains two Spring Boot microservices: Order Service and Inventory Service, along with a Discovery Server (Eureka).
The project demonstrates microservices architecture, REST communication, H2 database usage, and the Factory Design Pattern for extensible inventory processing.

A single root Dockerfile is provided to build Docker images for all modules using multiple build targets.

# Technology Stack

Spring Boot 3.x

H2 in-memory database

Eureka Discovery Server (Spring Cloud Netflix)

REST communication using RestTemplate

Factory Design Pattern (Inventory Service)

Maven build

JUnit 5, Mockito, SpringBootTest

Docker multi-stage build (single root Dockerfile)

# Project Structure
order-inventory-microservices-assignment/
|
|-- Dockerfile
|-- docker-compose.yml
|
|-- discovery-server/
|-- inventory-service/
|-- order-service/
|
|-- README.md

# Microservices Overview
## 1. Inventory Service

### Responsibilities:

Store products and batches

Return available batches for a product

Deduct inventory

Support extensibility using Factory Pattern

Endpoints:

GET ``` /inventory/{productId}```

Example Response:
```bash
[
{
"id": 5,
"batchNumber": "V-002",
"quantity": 150,
"expiryDate": "2025-03-18"
},
{
"id": 4,
"batchNumber": "V-001",
"quantity": 200,
"expiryDate": "2025-05-20"
}
]
```
POST ```/inventory/update```

Example Payload:
```bash
{
"productId": 2,
"batchQuantityToDeduct": {
"V-002": 50,
"V-001": 2
}
}
```
Example Response:
```bash
{
"id": 1,
"productId": 1,
"quantity": 3,
"status": "SUCCESS"
}
```
## 2. Order Service

### Responsibilities:

Accept order requests

Communicate with Inventory Service

Calculate deductions

Save order status

Endpoint:

POST ```/order```

Example Payload:
```bash
{
"productId": 2,
"quantity": 200
}
```
Example Response:
```bash
{
"id": 1,
"productId": 1,
"quantity": 3,
"status": "SUCCESS"
}
```
## 3. Discovery Server (Eureka)

Registers all services

Runs on: http://localhost:8761

Running Locally (Without Docker)
Start Discovery Server
cd discovery-server
mvn spring-boot:run

Start Inventory Service
cd ../inventory-service
mvn spring-boot:run

Start Order Service
cd ../order-service
mvn spring-boot:run


Service URLs:

Discovery: http://localhost:8761

Inventory: http://localhost:8071

Order: http://localhost:8081

# Docker Instructions

A single root Dockerfile builds all modules using build targets.

Build all modules
```bash
mvn clean package -DskipTests
docker build -t microservices-all 
```

Or build individual modules
```bash
docker build --target discovery -t discovery-service .
docker build --target inventory -t inventory-service .
docker build --target order -t order-service .
```
Docker Compose (Optional)

To run all services:
```bash
docker-compose up --build
```

Testing

Run all tests:
```bash
mvn test
```

Run tests per module:
```bash
cd order-service
mvn test
```
```bash
cd /inventory-service
mvn test
```

## Factory Design Pattern

### Inventory Service uses the Factory Pattern to allow new batch-handling strategies without modifying core logic.
