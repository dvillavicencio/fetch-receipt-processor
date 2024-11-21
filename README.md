# Receipt Processor
> Developed by Daniel Villavicencio

Receipt processor was made using Spring Boot, PostgresQL and Java 21.

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

## Troubleshooting
If you need to see the logs of one of the docker-compose services you can do so by using `docker logs` on the corresponding container
```bash
docker logs receipt-processor
docker logs postgres
```
