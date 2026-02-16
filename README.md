# ShopperMart - Event-Driven Microservices E-Commerce Platform

A modern, scalable e-commerce platform built with **Event-Driven Architecture** using Apache Kafka, Spring Boot Microservices, Angular Frontend, and Docker.

## 🎯 Project Overview

ShopperMart is a distributed microservices application designed for handling online retail operations using the **Saga Orchestration Pattern**. The platform leverages Apache Kafka for asynchronous event-driven communication, eliminating tight coupling and enabling high scalability and resilience.

## 🏗️ Architecture

The project follows an **Event-Driven Microservices Architecture** with Saga Orchestration pattern:

![Architecture Diagram](image)

### Architecture Highlights

- **Event-Driven Communication**: All inter-service communication happens through Kafka topics
- **Saga Orchestration**: Order Service acts as the orchestrator managing distributed transactions
- **Async Processing**: Non-blocking operations for improved performance
- **Compensating Transactions**: Automatic rollback mechanisms for failure scenarios
- **Decoupled Services**: Services communicate only through events, enabling independent scaling

## 📋 Table of Contents

- [Architecture](#-architecture)
- [Microservices](#-microservices)
- [Technology Stack](#-technology-stack)
- [Event Flow](#-event-flow)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Running the Application](#-running-the-application)
- [Kafka Topics](#-kafka-topics)
- [API Documentation](#-api-documentation)
- [Monitoring](#-monitoring--observability)
- [Testing](#-testing)
- [Troubleshooting](#-troubleshooting)

## 🧩 Microservices

### 1. **Order Service** (Saga Orchestrator)
- Orchestrates the complete order workflow
- Manages distributed transactions through Kafka events
- Handles compensating transactions for failures
- **Port**: 8087
- **Database**: MySQL
- **Events Published**: 
  - `OrderPlaceEvent`
  - `OrderPlacedSuccessEvent`
  - `inventory-reserved`
  - `inventory-failed`
  - `notification-sent`
  - `notification-failed`

### 2. **Inventory Service**
- Manages product stock levels
- Reserves/releases inventory based on events
- **Port**: 8083
- **Database**: MySQL
- **Events Consumed**: `OrderPlaceEvent`
- **Events Published**: `inventory-reserved`, `inventory-failed`

### 3. **Product Service**
- Manages product catalog
- Product CRUD operations
- **Port**: 8085
- **Database**: MongoDB

### 4. **User Service**
- User account management
- Authentication and authorization
- **Port**: 8084
- **Database**: MySQL

### 5. **Notification Service**
- Sends order confirmation emails/notifications
- Asynchronous notification processing
- **Port**: 8086
- **Events Consumed**: `notification-sent`, `notification-failed`

### 6. **API Gateway**
- Single entry point for all client requests
- Request routing and load balancing
- Keycloak integration for authentication
- **Port**: 8080

### 7. **Config Server**
- Centralized configuration management
- Provides configuration to all services
- **Port**: 8888

### 8. **Eureka Server**
- Service registry and discovery
- Dynamic service registration
- **Port**: 8761

## 🛠️ Technology Stack

### Frontend
- **Angular** - Modern web application framework
- **TypeScript** - Type-safe JavaScript

### Backend
- **Spring Boot 3.x** - Microservices framework
- **Spring Cloud** - Distributed system patterns
- **Spring Kafka** - Kafka integration
- **Spring Security** - Authentication & authorization
- **Spring Cloud Config** - Centralized configuration
- **Spring Cloud Netflix Eureka** - Service discovery
- **Resilience4j** - Circuit breaker pattern

### Message Broker
- **Apache Kafka 7.5.0** - Event streaming platform (KRaft mode)
- **Kafka UI** - Web-based Kafka management

### Authentication & Security
- **Keycloak** - Identity and access management
- **OAuth 2.0 / JWT** - Token-based authentication

### Databases
- **MySQL 8.3.0** - Relational database (User, Order, Inventory services)
- **MongoDB 7.0.5** - NoSQL database (Product service)

### Monitoring & Observability
- **Prometheus** - Metrics collection
- **Grafana** - Visualization dashboard
- **Loki** - Log aggregation
- **Tempo** - Distributed tracing

### DevOps & Containerization
- **Docker** - Container runtime
- **Docker Compose** - Multi-container orchestration
- **Maven** - Build automation

## 🔄 Event Flow

### Successful Order Flow

```
1. User places order (Angular Frontend)
   ↓
2. API Gateway → Order Service
   ↓
3. Order Service publishes → OrderPlaceEvent (Kafka)
   ↓
4. Inventory Service consumes OrderPlaceEvent
   ↓
5. Inventory Service reserves stock → publishes inventory-reserved (Kafka)
   ↓
6. Order Service consumes inventory-reserved
   ↓
7. Order Service publishes → notification-sent (Kafka)
   ↓
8. Notification Service consumes → sends email/notification
   ↓
9. Notification Service publishes → notification-sent (Kafka)
   ↓
10. Order Service updates status → OrderPlacedSuccessEvent
```

### Failure & Compensation Flow

```
1. Inventory Service detects insufficient stock
   ↓
2. Publishes → inventory-failed (Kafka)
   ↓
3. Order Service consumes inventory-failed
   ↓
4. Order Service triggers compensation (rollback)
   ↓
5. Order status updated to FAILED
   ↓
6. Notification Service sends failure notification
```

## 📁 Project Structure

```
ShopperMart/
├── frontend/                    # Angular frontend application
├── api-gateway/                 # API Gateway service
├── config-server/               # Configuration server
├── eureka-server/               # Service registry
├── order-service/               # Order management (Orchestrator)
├── inventory-service/           # Inventory management
├── product-service/             # Product catalog
├── user-service/                # User management
├── notification-service/        # Notification handler
├── configs/                     # Centralized configuration files
│   ├── application.yml
│   ├── application-dev.yml
│   ├── order-service.yml
│   ├── inventory-service.yml
│   └── ...
├── docker/                      # Docker configurations
│   ├── prometheus/
│   ├── grafana/
│   └── loki/
├── data/                        # Persistent data volumes
│   ├── mysql-order/
│   ├── mysql-inventory/
│   ├── mysql-user/
│   └── mongodb/
├── docker-compose-infra.yml     # Infrastructure services
├── docker-compose-services.yml  # Microservices
├── populate-all-data.ps1        # Data initialization script
├── pom.xml                      # Root Maven POM
└── README.md
```

## ✅ Prerequisites

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Node.js 16+** (for Angular frontend)
- **Docker & Docker Compose**
- **Git**
- **4GB+ RAM** (recommended for running all services)

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/ashbirdhiman/ShopperMart.git
cd ShopperMart
git checkout dev-with-only-async-with-docker
```

### 2. Start Infrastructure Services

Start databases, Kafka, Keycloak, and monitoring tools:

```bash
docker-compose -f docker-compose-infra.yml up -d
```

This will start:
- MySQL (Order, Inventory, User databases)
- MongoDB (Product database)
- Apache Kafka + Kafka UI
- Keycloak
- Prometheus, Grafana, Loki, Tempo

**Wait for all services to be healthy** (~2-3 minutes):

```bash
docker-compose -f docker-compose-infra.yml ps
```

### 3. Build All Microservices

```bash
mvn clean install -DskipTests
```

### 4. Start Microservices

**Option A: Using Docker Compose (Recommended)**

```bash
docker-compose -f docker-compose-services.yml up -d
```

**Option B: Run Individually (For Development)**

Open separate terminals for each service:

```bash
# Terminal 1 - Eureka Server
cd eureka-server && mvn spring-boot:run

# Terminal 2 - Config Server
cd config-server && mvn spring-boot:run

# Terminal 3 - API Gateway
cd api-gateway && mvn spring-boot:run

# Terminal 4 - Order Service
cd order-service && mvn spring-boot:run

# Terminal 5 - Inventory Service
cd inventory-service && mvn spring-boot:run

# Terminal 6 - Product Service
cd product-service && mvn spring-boot:run

# Terminal 7 - User Service
cd user-service && mvn spring-boot:run

# Terminal 8 - Notification Service
cd notification-service && mvn spring-boot:run
```

### 5. Initialize Database with Sample Data

```powershell
# Windows PowerShell
./populate-all-data.ps1
```

```bash
# Linux/Mac
chmod +x populate-all-data.sh
./populate-all-data.sh
```

### 6. Start Frontend (Optional)

```bash
cd frontend
npm install
ng serve
```

Access frontend at: `http://localhost:4200`

## 🌐 Service URLs

| Service               | URL                          | Port | Credentials       |
|-----------------------|------------------------------|------|-------------------|
| **Frontend**          | http://localhost:4200        | 4200 | -                 |
| **API Gateway**       | http://localhost:8080        | 8080 | -                 |
| **Eureka Dashboard**  | http://localhost:8761        | 8761 | -                 |
| **Config Server**     | http://localhost:8888        | 8888 | -                 |
| **Kafka UI**          | http://localhost:8085        | 8085 | -                 |
| **Keycloak**          | http://localhost:8180        | 8180 | admin/admin       |
| **Prometheus**        | http://localhost:9090        | 9090 | -                 |
| **Grafana**           | http://localhost:3000        | 3000 | admin/admin       |
| **Order Service**     | http://localhost:8087        | 8087 | -                 |
| **Inventory Service** | http://localhost:8083        | 8083 | -                 |
| **Product Service**   | http://localhost:8085        | 8085 | -                 |
| **User Service**      | http://localhost:8084        | 8084 | -                 |
| **Notification**      | http://localhost:8086        | 8086 | -                 |

## 📨 Kafka Topics

The following Kafka topics are used for event-driven communication:

| Topic Name                  | Producer           | Consumer             | Purpose                    |
|-----------------------------|--------------------|----------------------|----------------------------|
| `order-placed`              | Order Service      | Inventory Service    | Trigger inventory check    |
| `inventory-reserved`        | Inventory Service  | Order Service        | Confirm inventory reserved |
| `inventory-failed`          | Inventory Service  | Order Service        | Inventory reservation fail |
| `notification-sent`         | Order Service      | Notification Service | Send order notification    |
| `notification-failed`       | Notification Svc   | Order Service        | Notification failure       |
| `order-placed-success`      | Order Service      | Analytics (future)   | Order completed            |

### View Kafka Topics

Access Kafka UI at: **http://localhost:8085**

Or use Kafka CLI:

```bash
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### Monitor Consumer Groups

```bash
docker exec -it kafka kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

## 📚 API Documentation

### Swagger UI (OpenAPI)

Once services are running, access API documentation:

- **API Gateway Swagger**: http://localhost:8080/swagger-ui.html

### Sample API Endpoints

#### User Service
```
POST   /api/user/register          - Register new user
POST   /api/user/login             - Login user
GET    /api/user/{id}              - Get user by ID
PUT    /api/user/{id}              - Update user
```

#### Product Service
```
GET    /api/products               - List all products
GET    /api/products/{id}          - Get product by ID
POST   /api/products               - Create product
PUT    /api/products/{id}          - Update product
DELETE /api/products/{id}          - Delete product
```

#### Order Service
```
POST   /api/orders                 - Place new order (triggers saga)
GET    /api/orders                 - List all orders
GET    /api/orders/{id}            - Get order by ID
GET    /api/orders/customer/{id}   - Get orders by customer
```

#### Inventory Service
```
GET    /api/inventory              - List all inventory
GET    /api/inventory/{skuCode}    - Check stock by SKU
POST   /api/inventory              - Add inventory
PUT    /api/inventory/{skuCode}    - Update inventory
```

## 📊 Monitoring & Observability

### Prometheus

- **URL**: http://localhost:9090
- **Purpose**: Collects metrics from all microservices
- **Key Metrics**:
  - JVM metrics (heap, threads, GC)
  - HTTP request metrics
  - Kafka consumer lag
  - Circuit breaker states

### Grafana

- **URL**: http://localhost:3000
- **Credentials**: admin/admin
- **Pre-configured Dashboards**:
  - Spring Boot Statistics
  - Kafka Metrics
  - JVM Metrics
  - Service Health

### Loki (Log Aggregation)

- Accessible through Grafana
- Centralized logs from all services
- Search and filter capabilities

### Distributed Tracing (Tempo)

- Track requests across microservices
- Visualize event flow through Kafka
- Performance bottleneck identification

### Kafka UI

- **URL**: http://localhost:8085
- Monitor topics, consumer groups, and messages
- View message payloads
- Track consumer lag

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify
```

### Test Order Placement Flow

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "customerId": "user123",
    "skuCode": "PROD-001",
    "quantity": 2,
    "totalPrice": 99.99
  }'
```

Check Kafka UI to see events flowing through topics.

## 🔧 Configuration

### Centralized Configuration

All configurations are managed by **Config Server** and stored in the `configs/` directory:

```
configs/
├── application.yml              # Global defaults
├── application-dev.yml          # Development profile
├── application-prod.yml         # Production profile
├── order-service.yml            # Order service config
├── inventory-service.yml        # Inventory service config
├── product-service.yml          # Product service config
├── user-service.yml             # User service config
└── notification-service.yml     # Notification service config
```

### Environment Variables

Key environment variables for Docker Compose:

```yaml
# Kafka Configuration
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Eureka Configuration
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Config Server
SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/database_name
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=mysql
```

## 🐛 Troubleshooting

### Services Not Registering with Eureka

**Problem**: Services show as DOWN in Eureka dashboard

**Solutions**:
```bash
# Check Eureka is running
curl http://localhost:8761

# Verify service configuration
# Ensure application.yml has:
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

### Kafka Connection Issues

**Problem**: Services can't connect to Kafka

**Solutions**:
```bash
# Check Kafka is running
docker ps | grep kafka

# Verify Kafka logs
docker logs kafka

# Test Kafka connectivity
docker exec -it kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Database Connection Failures

**Problem**: Services fail to connect to MySQL/MongoDB

**Solutions**:
```bash
# Check database containers
docker ps | grep mysql
docker ps | grep mongodb

# Verify database credentials in config files
# Check database initialization
docker logs order-service-mysql
docker logs mongodb
```

### Port Conflicts

**Problem**: Ports already in use

**Solutions**:
```bash
# Find processes using ports
# Linux/Mac
lsof -i :8080

# Windows
netstat -ano | findstr :8080

# Stop conflicting processes or modify docker-compose port mappings
```

### Order Service Not Processing Events

**Problem**: Orders stuck in PENDING status

**Solutions**:
```bash
# Check Kafka consumer groups
docker exec -it kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe --group order-service-group

# Check service logs
docker logs order-service

# Verify Kafka topics exist
docker exec -it kafka kafka-topics \
  --bootstrap-server localhost:9092 --list
```

### Out of Memory Errors

**Problem**: Services crashing with OOM

**Solutions**:
```bash
# Increase Docker memory allocation (Docker Desktop Settings)
# Or modify JVM heap in docker-compose:

environment:
  - JAVA_OPTS=-Xmx512m -Xms256m
```

## 🎯 Key Design Patterns Used

### 1. Saga Orchestration Pattern
- Order Service acts as orchestrator
- Manages distributed transactions
- Implements compensating transactions

### 2. Event-Driven Architecture
- Asynchronous communication via Kafka
- Loose coupling between services
- Event sourcing capabilities

### 3. Circuit Breaker Pattern
- Resilience4j implementation
- Prevents cascading failures
- Graceful degradation

### 4. API Gateway Pattern
- Single entry point
- Request routing
- Authentication/Authorization

### 5. Service Discovery
- Dynamic service registration
- Client-side load balancing
- Health checking

## 🚀 Benefits of This Architecture

### Performance
- ⚡ **Non-blocking operations**: No waiting for synchronous responses
- ⚡ **Parallel processing**: Services process events concurrently
- ⚡ **Reduced latency**: Orders confirmed immediately

### Scalability
- 📈 **Horizontal scaling**: Add service instances without code changes
- 📈 **Independent scaling**: Scale services based on load
- 📈 **Kafka partitioning**: Distribute event processing

### Resilience
- 🛡️ **Fault isolation**: One service failure doesn't break the system
- 🛡️ **Automatic retries**: Kafka consumer groups handle retries
- 🛡️ **Compensating transactions**: Automatic rollback on failures

### Maintainability
- 🔧 **Loose coupling**: Services evolve independently
- 🔧 **Event replay**: Debug by replaying Kafka events
- 🔧 **Audit trail**: Complete event history in Kafka

## 📝 Future Enhancements

- [ ] Implement CQRS pattern for read/write separation
- [ ] Add GraphQL API Gateway
- [ ] Implement rate limiting
- [ ] Add caching layer (Redis)
- [ ] Kubernetes deployment manifests
- [ ] CI/CD pipeline (GitHub Actions/Jenkins)
- [ ] Advanced analytics dashboard
- [ ] Payment gateway integration
- [ ] Multi-region deployment

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Standards
- Follow Spring Boot best practices
- Write unit and integration tests
- Document public APIs
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Ashbir Dhiman**
- GitHub: [@ashbirdhiman](https://github.com/ashbirdhiman)
- LinkedIn: [Connect with me](https://linkedin.com/in/ashbirdhiman)

## 🙏 Acknowledgments

- Spring Boot Team for excellent microservices framework
- Apache Kafka community for robust event streaming
- Netflix OSS for Eureka and other cloud patterns
- Keycloak team for identity management

---

## 📞 Support

For issues, questions, or contributions:
- 🐛 **Report bugs**: [GitHub Issues](https://github.com/ashbirdhiman/ShopperMart/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/ashbirdhiman/ShopperMart/discussions)
- 📧 **Email**: ashbirashu@gmail.com

---

**Last Updated**: February 2026

**⭐ If you find this project helpful, please give it a star!**
