# Customer Management System

A two-part application: a **Spring Boot REST backend** and a **JavaFX desktop client** that talks to it.

---

## Project Structure

```
OrderKingTask/
├── Customer_Managment_System/      ← Spring Boot backend (REST API)
│   ├── src/main/java/
│   │   └── com/CustomerManagmentApp/Customer_Managment_System/
│   │       ├── Controller/         CustomerController.java
│   │       ├── Service/            CustomerService.java
│   │       ├── repository/         CustomerRepository.java
│   │       ├── Entity/             Customer.java
│   │       ├── DTOs/               CustomerDTO, PagedResponse, ApiError
│   │       ├── Exception/          GlobalExceptionHandler + custom exceptions
│   │       └── Security/           ApiTokenFilter, SecurityConfig
│   └── src/main/resources/
│       └── application.properties
│
└── CustomerManagmentDesktop/       ← JavaFX desktop client
    └── src/main/java/
        └── com/javap/customermanagmentdesktop/
            ├── MainApp.java
            ├── model/              Customer, ApiResponse
            ├── service/            CustomerApiService (OkHttp + Jackson)
            ├── ui/                 MainWindow, CustomerDialog
            └── util/               AppConfig (base URL + token)
```

The desktop app has **no direct database connection**. All data flows through the REST API.

---

## Prerequisites

| Tool  | Version |
| ----- | ------- |
| Java  | 21+     |
| Maven | 3.9+    |
| MySQL | 8.0+    |

---

## Part 1 — Start the Spring Boot Backend

### 1. Create the database

Run the provided `schema.sql` file against your MySQL server:

```bash
mysql -u root -p < schema.sql
```

Or paste it directly into MySQL Workbench / DBeaver.

### 2. Configure credentials

Open `Customer_Managment_System/src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/customer_db
spring.datasource.username=root
spring.datasource.password=${DB_Password}
```

The API token and port are already set:

```properties
server.port=8181
api.security.token=order_king
```

### 3. Run the backend

```bash
cd Customer_Managment_System
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8181`.

### API Endpoints

| Method | Endpoint                          | Description                |
| ------ | --------------------------------- | -------------------------- |
| GET    | `/customers`                      | List all customers         |
| GET    | `/customers?search=query`         | Search by name/email/phone |
| GET    | `/customers/{id}`                 | Get customer by ID         |
| POST   | `/customers`                      | Create a customer          |
| PUT    | `/customers/{id}`                 | Update a customer          |
| DELETE | `/customers/{id}`                 | Delete a customer          |
| GET    | `/customers/paged?page=0&size=10` | Paginated list             |

All endpoints require the header:

```
Authorization: Bearer order_king
```

---

## Part 2 — Start the Desktop Application

### 1. Ensure the backend is running first

The desktop app connects to `http://localhost:8181` by default (configured in `AppConfig.java`).

### 2. Run the desktop app

```bash
cd CustomerManagmentDesktop
./mvnw javafx:run
```

Or build a runnable JAR:

```bash
./mvnw package
java -jar target/CustomerManagmentDesktop-*.jar
```

### 3. Features

- View all customers in a sortable table
- Search by name, email, or phone (live search via API)
- Add a new customer (dialog with client-side validation)
- Edit an existing customer (double-click a row or click Edit)
- Delete a customer (with confirmation dialog)
- Loading spinner during API calls
- Status bar showing operation results
- Error dialogs when the API is unreachable or returns an error

---

## How the Desktop App Communicates with the Backend

The desktop client uses **OkHttp** for HTTP and **Jackson** for JSON:

1. `AppConfig.java` holds `BASE_URL = "http://localhost:8181"` and `API_TOKEN = "order_king"`.
2. `CustomerApiService.java` builds OkHttp requests with `Authorization: Bearer order_king` on every call.
3. All API calls run on a background thread (`ScheduledExecutorService`) and update the UI via `Platform.runLater()` to keep the JavaFX thread responsive.
4. Responses are deserialized into `Customer` model objects using Jackson with the `JavaTimeModule` for `LocalDateTime` support.
5. Errors (network failures, 4xx/5xx responses) are surfaced to the user as dialog alerts and status bar messages.

---
