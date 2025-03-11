# Stock Order Service

### Overview
The **Order Service** is a Spring Boot microservice designed for managing stock orders, such as creating, listing, and matching buy/sell requests. It ensures role-based security, robust business logic, and smooth deployment via Docker Compose. The service is fully documented with Swagger for API interaction.

---

### Features
- **Order Management**:
    - Create `BUY` and `SELL` orders.
    - List orders for customers within specific date ranges.
    - Match and process pending orders.
    - Delete existing orders while reversing their effects on assets.
- **Role-Based Security**:
    - Secure endpoints for `ROLE_USER` and `ROLE_ADMIN`.
- **Authentication**:
    - JWT-based token authentication.
- **Swagger Documentation**:
    - Auto-generated API documentation with an interactive interface.
- **Containerized Deployment**:
    - Docker Compose support for easy service deployment.
- **High-Quality Code**:
    - Clean and modular design following **SOLID principles**.

---

### Prerequisites
- **Java 17** (JDK installed on your machine)
- **Maven 3.8+**
- **Docker** and **Docker Compose**

---

### Build & Run Instructions

#### **1. Clone the Repository**
```bash
git clone https://github.com/your-repo/order-service.git
cd order-service
```

#### **2. Build the Project**
Compile and package the project using Maven:
```bash
mvn clean install
```

#### **3. Run Locally**
Start the service using Maven:
```bash
mvn spring-boot:run
```

Alternatively, run the JAR file:
```bash
java -jar target/order-service-1.0.0.jar
```

#### **4. Run with Docker Compose**
The application can be containerized and run with Docker Compose:
```bash
docker-compose up --build
```

This will spin up the Order Service and H2 database in containers.

---

### API Documentation

#### **Swagger UI**
Swagger UI provides an interactive way to explore and test API endpoints. Access it at:
```
http://localhost:8080/swagger-ui/index.html
```
When you want to use the API, you need to use the login endpoint with username and password credentials which are defined in schema.sql per admin and customers. 

#### **OpenAPI JSON**
For OpenAPI specification in JSON format:
```
http://localhost:8080/v3/api-docs
```

#### **Key Endpoints**
| HTTP Method | Endpoint                                     | Description                         | Access Role            |
|-------------|----------------------------------------------|-------------------------------------|------------------------|
| `POST`      | `/auth/login`                                | Login to get token                  |                        |
| `POST`      | `/api/orders`                                | Create a new order.                 | ROLE_USER / ROLE_ADMIN |
| `GET`       | `/api/orders`                                | List customer orders by date range. | ROLE_USER / ROLE_ADMIN |
| `DELETE`    | `/api/orders/{customerId}/orders/{orderId}}` | Delete an order.                    | ROLE_USER / ROLE_ADMIN |
| `POST`      | `/api/orders/match`                          | Match pending buy/sell orders.      | ROLE_ADMIN             |

---

### Security
The service uses **Spring Security** for authentication and authorization:
- **JWT Authentication**: Secure token-based authentication.
- **Role-Based Authorization**:
    - `ROLE_USER`: Customers can manage their orders.
    - `ROLE_ADMIN`: Admins can perform order-matching operations.

---

### Testing

#### **Unit & Integration Tests**
Comprehensive test coverage ensures robust functionality:
- **Service Layer**: Tests for `OrderService`, `BuyOrderHandler`, and `SellOrderHandler`.
- **Aspect Layer**: Authorization aspects (`AuthorizationAspect`).
- **Controller Layer**: REST API endpoint testing with MockMvc.
- **Repository Layer**: CRUD operations with in-memory H2 database.

#### **Run Tests**
Execute tests using Maven:
```bash
mvn test
```

#### **Testing with Docker Compose**
Run tests in a containerized environment:
```bash
docker-compose up --build
```

---

### Project Directory Structure
```
src/main/java/com/broker/stock
├── aspect                  // AuthorizationAspect for access control
├── config                  // Configurations (e.g., Seecurity)
├── constant                // Constants of the project
├── controller              // REST controllers
├── entity                  // JPA entities (e.g., Order, Asset, Customer)
├── exception               // Exception Advices
├── filter                  // Request filters
├── mapper                  // Mapper classes to prepare DTOs
├── model                   // Data Transfer Objects (e.g., OrderRequest, OrderResponse)
├── repository              // Spring Data JPA repositories
└── service                 // Service classes (e.g., OrderService)
```

---

### Deployment with Docker Compose

The service is pre-configured for containerized deployment with Docker Compose. Below is an example `docker-compose.yml` file:

```yaml
version: "3.9"
services:
  order-service:
    image: openjdk:17-jdk-slim
    container_name: order-service
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:h2:mem:stockdb
      SPRING_DATASOURCE_USERNAME: sa
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_JPA_DATABASE-PLATFORM: org.hibernate.dialect.H2Dialect
    depends_on:
      - h2-database

  h2-database:
    image: oscarfonts/h2
    container_name: h2-database
    ports:
      - "9092:9092"
      - "8090:8090"
    environment:
      H2_OPTIONS: -tcp -web -webAllowOthers -tcpAllowOthers
```

#### **To Deploy:**
```bash
docker-compose up --build
```

---

### Improvements

#### **How to Improve This Project:**
1. Add an endpoint AdminOrderController to fund customer TRY assets directly. 
2. Add more unit tests to cover all rest endpoints
3. Define business specific exceptions to have a better understanding of the exceptions

---

### Contact
**Author**: Serdar Dündar  
**Email**: [thisisserdardundar@gmail.com](mailto:thisisserdardundar@gmail.com)  
**GitHub**: [https://github.com/serdardundar](https://github.com/serdardundar)
