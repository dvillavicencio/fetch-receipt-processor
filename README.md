# Receipt Processor
> Authored by Daniel Villavicencio

This project is a receipt processing application built using Java and Spring Boot. Java was chosen as the primary language due to familiarity and efficiency in creating web applications. While Go is a language I've explored for smaller tasks, Java allowed me to develop this project more quickly and effectively.
___

## Setup and Run Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/dvillavicencio/fetch-receipt-processor
cd fetch-receipt-processor
```

### 2. Build the Project
```bash
./gradlew clean && ./gradlew build
```

### 3. Start the application with Docker Compose
```bash
docker compose up -d
```

### 4. Test the Application
```bash
curl --location 'http://localhost:8080/receipts/process' \
--header 'Content-Type: application/json' \
--data '{
    "retailer": "Target",
    "purchaseDate": "2022-01-01",
    "purchaseTime": "13:01",
    "items": [
        {
            "shortDescription": "Mountain Dew 12PK",
            "price": "6.49"
        },
        {
            "shortDescription": "Emils Cheese Pizza",
            "price": "12.25"
        },
        {
            "shortDescription": "Knorr Creamy Chicken",
            "price": "1.26"
        },
        {
            "shortDescription": "Doritos Nacho Cheese",
            "price": "3.35"
        },
        {
            "shortDescription": "   Klarbrunn 12-PK 12 FL OZ  ",
            "price": "12.00"
        }
    ],
    "total": "35.35"
}'

curl --location 'http://localhost:8080/receipts/1/points'
```

### 5. Tear down the application
```bash
docker compose down
```
___

## File Structure

### Main Source Code
The project follows a standard package structure in a Java Spring Boot application. Below is an overview of the file organization:
```
src/main/java/com/example/receiptprocessor
├── controller
│   ├── ReceiptController.java              // Handles HTTP requests for receipts
│   ├── GlobalExceptionHandler.java         // Handles global exception and error messages
├── dto
│   ├── *.java                              // Data Transfer Objects (DTOs) for request and response payloads
├── entity
│   ├── *.java                              // Database entity representations
├── exception
│   ├── ReceiptNotFoundException.java       // Custom exception for missing receipts
├── service
│   ├── ReceiptProcessingService.java       // Business logic for processing receipts
├── repository
│   ├── ReceiptRepository.java              // Data access layer (JDBC or ORM abstraction)
├── resources
│   ├── application.yml                     // Primary application configuration
│   ├── application-local.yml               // Configuration for local development without Docker
│   ├── schema.sql                          // Database schema initialized at runtime

```

### Test Code
The test package is organized to separate integration and service-level tests:
```
src/test/java/com/example/receiptprocessor
├── integration
│   └── ReceiptControllerTest.java          // Integration tests
├── service
│   └── ReceiptProcessingServiceTest.java   // Business logic tests for processing receipts 
├── resources
│   └── application.yml                     // Application configuration properties
│   └── schema.sql                          // Initial schema at application runtime
```

### Business Logic
The core logic for processing receipts resides in the ReceiptProcessingService.java class within the /service package. This class handles the application of business rules to the Receipt object.

### Key Design Decisions
Rule-Based Processing:
Each rule is implemented as a simple method that returns an integer based on the properties of a Receipt object. These rules are then applied in sequence to generate a final result.

### Logging for Transparency
To ensure the behavior of the application is traceable, logging is implemented at every stage of the rule processing. Each rule logs the specific changes made to the payload as it passes through.

### Extendability
The design allows for easy addition of new rules. New methods can be added without major changes to the existing structure.

## Notes for Developers
### Spring Boot Abstractions
The project leverages Spring Boot's powerful abstractions for handling HTTP requests, dependency injection, and database interactions. While these abstractions simplify development, they might require additional explanation for developers unfamiliar with Spring.

### Rule Logging
Each rule is implemented in a straightforward manner, but the addition of extensive logging means there is some boilerplate code to enhance debugging and traceability.

___

## Troubleshooting
### 1. Logs
If you need to see the logs of one of the docker-compose services you can do so by using `docker logs` on the corresponding container
```bash
docker logs receipt-processor
docker logs postgres
```
### 2. Running the application
Make sure there's nothing running on port `8080` and port `5432` in your local machine. By default the fetch-receipt-processor waits until PostgresQL is in a healthy state.

> This README provides an overview of the project, its structure, and how to run it locally. For additional details or questions, feel free to check the source code or raise issues in the repository.

