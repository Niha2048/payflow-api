# PayFlow Backend API

PayFlow is a simplified fintech backend inspired by systems like PhonePe or Google Pay.  
It allows registering users, assigning wallet balances, and recording money transfers - all via REST APIs.The API is database-backed and ready for frontend integration.

#### Project Write‑Up

For detailed explanations, answers to assignment questions, and supporting screenshots,  
please refer to the attached **PayFlow_WriteUp.pdf** located in the repository root.

## Project Set up

###  Technology Stack
- Application Framework: Spring Boot 3.2.0  
- REST API Support: Spring Web  
- Database Abstraction: Spring Data JPA  
- In‑Memory Database: H2 Database  
- Build Tool: Maven  
- Programming Language: Java 21  




##  How to Run the App

1. Clone or download the project.
2. Navigate into the project folder.
3. Run with Maven:

```bash
mvn spring-boot:run
```

4. The app starts on **http://localhost:8080** using Spring Boot’s embedded Tomcat server.
5. Access the H2 console at **http://localhost:8080/h2-console**  
   - JDBC URL: `jdbc:h2:file:./data/payflowdb` (or `jdbc:h2:mem:testdb` if using in-memory)  
   - Username: `sa`  
   - Password: *(leave empty)* 



##  Architecture

The project is organized into four main layers, each with specific classes and responsibilities:

### 1. Entity Layer (`com.payflow.payflow_api.entity`)
Defines the data model and maps Java objects to database tables using JPA annotations.
- **User**  
  Represents a system user with fields: `id`, `name`, `upiId`, `phoneNumber`, `balance`.  
  Role: Maps to the `users` table and stores account details.
- **Transaction**  
  Represents a money transfer with fields: `id`, `senderUpiId`, `receiverUpiId`, `amount`, `timestamp`.  
  Role: Maps to the `transaction` table and records payment history.



### 2. Repository Layer (`com.payflow.payflow_api.repository`)
Provides database access using Spring Data JPA. Extends `JpaRepository` for CRUD operations.
- **UserRepository**  
  Role: Handles user data access. Includes derived query method `findByUpiId(String upiId)` to look up users by UPI ID.
- **TransactionRepository**  
  Role: Handles transaction data access. Provides CRUD operations for transaction records.



### 3. Service Layer (`com.payflow.payflow_api.service`)
Contains business logic and interacts with repositories.
- **UserService**  
  Role: Methods for registering users (`registerUser`), retrieving users by ID (`getUserById`), finding by UPI (`findByUpiId`), and listing all users (`getAllUsers`).  
  Uses `@Autowired` to inject `UserRepository`.
- **TransactionService**  
  Role: Method for sending money (`sendMoney`). Saves transaction records and can include validation or balance updates.  
  Uses `@Autowired` to inject `TransactionRepository`.



### 4. Controller Layer (`com.payflow.payflow_api.controller`)
Handles HTTP requests and responses. Defines REST endpoints and delegates work to the service layer.
- **UserController**  
  Role: Exposes endpoints at `/users` for user operations:  
  - `POST /users` → Register a new user  
  - `GET /users` → List all users  
  - `GET /users/{id}` → Retrieve user by ID  
  - `GET /users/upi/{upiId}` → Retrieve user by UPI ID
- **TransactionController**  
  Role: Exposes endpoints at `/transactions` for transaction operations:  
  - `POST /transactions` → Record a new transaction  
  - `GET /transactions` → List all transactions



##  Spring Boot Features in PayFlow

### 1. Embedded Server
Spring Boot ships with an embedded **Tomcat server**, so there’s no need to install or configure a separate servlet container.  
In PayFlow:
- Running `mvn spring-boot:run` automatically starts Tomcat on port **8080**.  
- The application is immediately accessible at `http://localhost:8080`.  
- This simplifies deployment and makes local development faster.



### 2. Auto-Configuration
Spring Boot automatically configures beans and dependencies based on what’s present in the classpath.  
In PayFlow:
- The `@SpringBootApplication` annotation enables auto-configuration.  
- **Spring Data JPA** is configured automatically when the dependency is present.  
- The **H2 in-memory database** is auto-configured using `application.properties`.  
- Entity scanning is automatic — all `@Entity` classes (`User`, `Transaction`) are detected without manual setup.  
- REST controllers (`UserController`, `TransactionController`) are registered automatically.



### 3. Production-Ready Defaults
Spring Boot provides sensible defaults that make applications ready for production.  
In PayFlow:
- Built-in error handling returns proper HTTP status codes.  
- Logging is pre-configured with useful startup and runtime information.  
- Health checks and metrics can be enabled via **Spring Boot Actuator**.  
- Database connection pooling is handled automatically.  
- Default configurations reduce boilerplate and improve maintainability.



### 4. Developer Productivity
Spring Boot improves developer experience with:
- **Spring Boot DevTools** (optional) for hot reload during development.  
- Simplified dependency management via **Spring Boot Starter POMs**.  
- Minimal XML configuration — most setup is annotation-driven.  




## Hibernate Auto‑Generated Tables

When the application starts, Hibernate automatically creates the following tables based on the entity classes:

### Transaction Table
```sql
Hibernate: create table transaction (
    id bigint generated by default as identity,
    amount float(53),
    note varchar(255),
    receiver_upi_id varchar(255),
    sender_upi_id varchar(255),
    timestamp timestamp(6),
    primary key (id)
);
```

### Users Table
```sql
Hibernate: create table users (
    id bigint generated by default as identity,
    balance float(53),
    name varchar(255),
    phone_number varchar(255),
    upi_id varchar(255),
    primary key (id)
);
```


## Custom Query Method — findByUpiId

### SQL Generated by JPA
```sql
select u1_0.id, u1_0.name, u1_0.upi_id, u1_0.phone_number, u1_0.balance
from users u1_0
where u1_0.upi_id = ?
```


### Explanation
- **(a) How JPA derives it from the method name**  
  JPA parses the repository method name `findByUpiId`.  
  - `findBy` → tells JPA to generate a `SELECT ... WHERE` query.  
  - `UpiId` → matches the entity field `upiId`, which maps to the database column `upi_id`.  
  - Together, JPA builds a query equivalent to: *“SELECT user WHERE upi_id = ?”*.

- **(b) What the `?` placeholder means**  
  The `?` is a **parameter placeholder**. At runtime, Hibernate substitutes it with the actual value passed into the method.  
  Example: calling `findByUpiId("alice@upi")` results in:
  ```sql
  select u1_0.id, u1_0.name, u1_0.upi_id, u1_0.phone_number, u1_0.balance
  from users u1_0
  where u1_0.upi_id = 'alice@upi';
  ```




### We implemented two custom queries in addition to the derived method:

1. **Derived Method Name**  
   - `findByUpiId(String upiId)`  
   - Exposed via `GET /users/upi/{upiId}` to return the user matching a given UPI ID.  
   - JPA automatically parses the method name and generates the SQL.

2. **@Query with JPQL**  
   - `@Query("SELECT u FROM User u WHERE u.balance > :amount")`  
   - `findUsersWithBalanceAbove(double amount)`  
   - This uses JPQL, which is object oriented and works with entity fields rather than table columns.

3. **Native SQL (for comparison)**  
   - `@Query(value = "SELECT * FROM users WHERE balance > ?1", nativeQuery = true)`  
   - Directly executes database specific SQL.

### Comparison
- **Derived method names** are concise and ideal for simple queries. They rely on Spring Data JPA’s naming conventions to generate SQL automatically.  
- **JPQL with @Query** offers flexibility for more complex queries while remaining database agnostic, since JPQL operates on entities rather than raw tables.  
- **Native SQL** is the least preferred because it ties the application to a specific database dialect, reduces portability, and bypasses JPA’s abstraction layer. It should only be used when JPQL cannot express the query or for performance critical cases.


##  Testing with Curl

After starting the application (`mvn spring-boot:run`), you can test all endpoints directly from the terminal.

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","upiId":"alice@upi","phoneNumber":"9876543210","balance":1000}'
```

### 2. List All Users
```bash
curl -X GET http://localhost:8080/users
```

### 3. Get User by ID
```bash
curl -X GET http://localhost:8080/users/1
```

### 4. Get User by UPI ID
```bash
curl -X GET http://localhost:8080/users/upi/alice@upi
```

### 5. Record a Transaction
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"senderUpiId":"alice@upi","receiverUpiId":"bob@upi","amount":250,"note":"Dinner payment"}'
```

### 6. List All Transactions
```bash
curl -X GET http://localhost:8080/transactions
```



## ✅ Verification
- Check the **H2 Console** at [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
- Use JDBC URL: `jdbc:h2:mem:testdb`  
- Username: `sa`  
- Password: *(leave empty)*  
- Verify that the `users` and `transaction` tables contain the inserted records.

##  Future Enhancements

The PayFlow application can be extended with several improvements to make it more robust and production‑ready:

- **Enhanced Security**  
  Add authentication and authorization using Spring Security and JWT tokens to protect endpoints.

- **Persistent Database**  
  Replace the in‑memory H2 database with a production‑grade database (e.g., PostgreSQL, MySQL) for real deployments.

- **Transaction Validation**  
  Implement stricter validation rules (e.g., sufficient balance checks, duplicate transaction prevention).

- **Error Handling & Logging**  
  Provide custom exception handling with meaningful error messages and integrate structured logging (e.g., Logback/SLF4J).

- **API Documentation**  
  Integrate Swagger/OpenAPI for interactive API documentation and easier testing.

- **Unit & Integration Tests**  
  Add JUnit and Mockito tests to ensure code quality and reliability.

- **Monitoring & Metrics**  
  Enable Spring Boot Actuator endpoints for health checks, metrics, and monitoring in production.

- **Scalability**  
  Containerize the application with Docker and orchestrate with Kubernetes for horizontal scaling.

- **Frontend Integration**  
  Build a simple React/Angular frontend to interact with the backend APIs.

- **Notifications**  
  Add email/SMS notifications for transactions using external services (e.g., Twilio, SendGrid).
