# Receipt Processor
> Developed by Daniel Villavicencio

Receipt processor was made using Spring Boot and Java 21.

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

### 4. Access the Application
```bash
curl http://localhost:8080
```
___

## Troubleshooting
If you need to see the logs of one of the docker-compose services you can do so by using `docker logs` on the corresponding container
```bash
docker logs receipt-processor
docker logs postgres
```
