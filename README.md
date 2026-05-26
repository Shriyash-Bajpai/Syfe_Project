# Personal Finance Manager

A Spring Boot REST API for managing personal finances. Users can register, log in with session-based authentication, manage income and expense transactions, create custom categories, track savings goals, and generate monthly/yearly reports.

## Tech Stack

| Requirement | Implementation |
| --- | --- |
| Programming language | Java 17+ |
| Framework | Spring Boot 3.2 |
| Security | Spring Security, session-based authentication |
| Database | H2 |
| Persistence | Spring Data JPA / Hibernate |
| Build tool | Maven |
| Testing | JUnit 5, Mockito, Spring Boot Test, JaCoCo |
| Architecture | Controller -> Service -> Repository |

## Assignment Requirements Checklist

### Functional Requirements

- User registration with username/email, password, full name, and phone number.
- User login with username/email and password.
- Session-based authentication using cookies.
- Logout with session invalidation.
- Protected APIs require authentication except register and login.
- User data isolation for transactions, categories, reports, and savings goals.
- Full CRUD for transactions.
- Transaction amount must be positive.
- Transaction date must not be in the future.
- Transactions must use a valid category.
- Transactions are returned newest first.
- Transactions can be filtered by date range and category.
- Default categories are pre-seeded:
  - INCOME: Salary
  - EXPENSE: Food, Rent, Transportation, Entertainment, Healthcare, Utilities
- Default categories cannot be deleted.
- Users can create custom categories.
- Custom category names are unique per user.
- Custom categories referenced by transactions cannot be deleted.
- Users can create, view, update, and delete savings goals.
- Savings goal target amount must be positive.
- Savings goal target date must be in the future.
- Savings progress is calculated as total income minus total expenses since goal start date.
- Monthly reports show income by category, expenses by category, and net savings.
- Yearly reports show income by category, expenses by category, and net savings.

### Technical Requirements

- Java 17+ compatible source.
- Spring Boot 3.x.
- Spring Security.
- H2 database.
- Maven build.
- Request/response DTOs separate from entities.
- Global exception handling with `@ControllerAdvice`.
- Input validation using Jakarta Validation.
- Clear HTTP status codes for success and client errors.
- Unit/integration tests with coverage support through JaCoCo.
- Externalized configuration in `application.properties`.

## Project Structure

```text
src/main/java/com/finance/manager
  config/       Security, Jackson, and default data initialization
  controller/   REST controllers
  dto/          Request and response DTOs
  entity/       JPA entities
  enums/        TransactionType enum
  exception/    Custom exceptions and global handler
  repository/   Spring Data repositories
  security/     UserDetailsService
  service/      Service interfaces and implementations

src/test/java/com/finance/manager
  controller/   Controller tests
  service/      Service tests
  exception/    Exception handler tests
  FinanceManagerIntegrationTest.java
```

## Prerequisites

- Java 17 or newer.
- Maven 3.8 or newer.
- Recommended locally on this machine: Java 21.

Check versions:

```powershell
java -version
mvn -version
```

If your default Java is Java 25 and the app fails to start, use Java 21 explicitly:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn -version
```

## Run Locally

From the project folder:

```powershell
cd "C:\Users\ShriyashBajpai\Desktop\c\Programming\Syfe\Claude\finance-manager"
```

Run on default port `8080`:

```powershell
mvn spring-boot:run
```

Run on port `8081`:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Alternative: build and run the jar:

```powershell
mvn clean package -DskipTests
& "C:\Program Files\Java\jdk-21\bin\java.exe" -jar target\manager-1.0.0.jar --server.port=8081
```

The API base URL will be:

```text
http://localhost:8081/api
```

The terminal stays open while the server is running. Press `Ctrl+C` to stop the server.

## Run Tests

Run the full test suite:

```powershell
mvn test
```

Expected successful result:

```text
Tests run: 99, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Run tests and generate coverage report:

```powershell
mvn verify
```

Coverage report:

```text
target/site/jacoco/index.html
```

## Authentication and Cookies

The project uses session-based authentication.

1. Register a user.
2. Log in using `/api/auth/login`.
3. The server returns a `JSESSIONID` cookie.
4. Postman stores this cookie automatically.
5. Protected requests use the same cookie.

Do not manually add the `Cookie` header in Postman unless needed. If adding manually, use this format:

```text
Cookie: JSESSIONID=<value>
```

## API Endpoints

Base URL:

```text
http://localhost:8081/api
```

### Auth

#### Register User

Purpose: Create a new user account.

```http
POST /api/auth/register
```

Headers:

```text
Content-Type: application/json
```

Body:

```json
{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```

Success:

```text
201 Created
```

```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

#### Login

Purpose: Authenticate user and create session cookie.

```http
POST /api/auth/login
```

Headers:

```text
Content-Type: application/json
```

Body:

```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

Success:

```text
200 OK
```

```json
{
  "message": "Login successful"
}
```

#### Logout

Purpose: Invalidate current user session.

```http
POST /api/auth/logout
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

Success:

```json
{
  "message": "Logout successful"
}
```

### Categories

#### Get All Categories

Purpose: Get default categories and current user's custom categories.

```http
GET /api/categories
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

Success:

```json
{
  "categories": [
    {
      "name": "Salary",
      "type": "INCOME",
      "isCustom": false
    },
    {
      "name": "Food",
      "type": "EXPENSE",
      "isCustom": false
    }
  ]
}
```

#### Create Custom Category

Purpose: Create a user-specific category.

```http
POST /api/categories
```

Headers:

```text
Content-Type: application/json
Cookie: JSESSIONID=<value>
```

Body:

```json
{
  "name": "Freelance",
  "type": "INCOME"
}
```

Another example:

```json
{
  "name": "Shopping",
  "type": "EXPENSE"
}
```

Success:

```json
{
  "name": "Freelance",
  "type": "INCOME",
  "isCustom": true
}
```

#### Delete Custom Category

Purpose: Delete a custom category if it is not used by existing transactions.

```http
DELETE /api/categories/{name}
```

Example:

```http
DELETE /api/categories/Freelance
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

Success:

```json
{
  "message": "Category deleted successfully"
}
```

### Transactions

#### Create Transaction

Purpose: Add income or expense transaction. Transaction type is derived from the selected category.

```http
POST /api/transactions
```

Headers:

```text
Content-Type: application/json
Cookie: JSESSIONID=<value>
```

Income body:

```json
{
  "amount": 50000.00,
  "date": "2026-05-26",
  "category": "Salary",
  "description": "Monthly salary"
}
```

Expense body:

```json
{
  "amount": 1200.00,
  "date": "2026-05-26",
  "category": "Food",
  "description": "Lunch and groceries"
}
```

Success:

```json
{
  "id": 1,
  "amount": 50000.00,
  "date": "2026-05-26",
  "category": "Salary",
  "description": "Monthly salary",
  "type": "INCOME"
}
```

#### Get Transactions

Purpose: List current user's transactions, newest first.

```http
GET /api/transactions
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

Optional filters:

```http
GET /api/transactions?startDate=2026-05-01&endDate=2026-05-31&categoryId=1&type=INCOME
```

`type` can be `INCOME` or `EXPENSE`.

Success:

```json
{
  "transactions": [
    {
      "id": 1,
      "amount": 50000.00,
      "date": "2026-05-26",
      "category": "Salary",
      "description": "Monthly salary",
      "type": "INCOME"
    }
  ]
}
```

#### Update Transaction

Purpose: Update amount, category, and/or description. The transaction date cannot be changed.

```http
PUT /api/transactions/{id}
```

Example:

```http
PUT /api/transactions/1
```

Headers:

```text
Content-Type: application/json
Cookie: JSESSIONID=<value>
```

Body:

```json
{
  "amount": 60000.00,
  "category": "Salary",
  "description": "Updated salary"
}
```

#### Delete Transaction

Purpose: Delete a transaction. Deleted transactions are not included in reports or savings calculations.

```http
DELETE /api/transactions/{id}
```

Example:

```http
DELETE /api/transactions/1
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

Success:

```json
{
  "message": "Transaction deleted successfully"
}
```

### Savings Goals

#### Create Goal

Purpose: Create a savings goal.

```http
POST /api/goals
```

Headers:

```text
Content-Type: application/json
Cookie: JSESSIONID=<value>
```

Body:

```json
{
  "goalName": "Emergency Fund",
  "targetAmount": 50000.00,
  "targetDate": "2026-12-31",
  "startDate": "2026-05-26"
}
```

`startDate` is optional. If omitted, it defaults to the creation date.

Success:

```json
{
  "id": 1,
  "goalName": "Emergency Fund",
  "targetAmount": 50000.00,
  "targetDate": "2026-12-31",
  "startDate": "2026-05-26",
  "currentProgress": 0,
  "progressPercentage": 0.0,
  "remainingAmount": 50000.00
}
```

#### Get All Goals

Purpose: List all goals with calculated progress.

```http
GET /api/goals
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

#### Get Goal By ID

Purpose: Get one savings goal.

```http
GET /api/goals/{id}
```

Example:

```http
GET /api/goals/1
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

#### Update Goal

Purpose: Update target amount and/or target date.

```http
PUT /api/goals/{id}
```

Example:

```http
PUT /api/goals/1
```

Headers:

```text
Content-Type: application/json
Cookie: JSESSIONID=<value>
```

Body:

```json
{
  "targetAmount": 60000.00,
  "targetDate": "2027-01-31"
}
```

#### Delete Goal

Purpose: Delete a savings goal.

```http
DELETE /api/goals/{id}
```

Example:

```http
DELETE /api/goals/1
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

Success:

```json
{
  "message": "Goal deleted successfully"
}
```

### Reports

#### Monthly Report

Purpose: Get income by category, expenses by category, and net savings for a month.

```http
GET /api/reports/monthly/{year}/{month}
```

Example:

```http
GET /api/reports/monthly/2026/5
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

Success:

```json
{
  "month": 5,
  "year": 2026,
  "totalIncome": {
    "Salary": 50000.00
  },
  "totalExpenses": {
    "Food": 1200.00
  },
  "netSavings": 48800.00
}
```

#### Yearly Report

Purpose: Get income by category, expenses by category, and net savings for a year.

```http
GET /api/reports/yearly/{year}
```

Example:

```http
GET /api/reports/yearly/2026
```

Headers:

```text
Cookie: JSESSIONID=<value>
```

Body: none.

Success:

```json
{
  "year": 2026,
  "totalIncome": {
    "Salary": 50000.00
  },
  "totalExpenses": {
    "Food": 1200.00
  },
  "netSavings": 48800.00
}
```

## HTTP Status Codes

| Code | Meaning |
| --- | --- |
| 200 | OK |
| 201 | Created |
| 400 | Bad Request: validation or business rule error |
| 401 | Unauthorized: not logged in or invalid credentials |
| 403 | Forbidden: authenticated but not allowed |
| 404 | Resource not found |
| 409 | Conflict: duplicate user/category |

## H2 Console

When running locally:

```text
http://localhost:8081/h2-console
```

Use:

```text
JDBC URL: jdbc:h2:mem:financedb
Username: sa
Password:
```

Leave password empty.

## Deployment

The repository includes `render.yaml`.

Render settings:

```text
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/manager-1.0.0.jar
Environment Variable: SPRING_PROFILES_ACTIVE=prod
```

After deployment, test with:

```bash
bash financial_manager_tests.sh https://your-app.onrender.com/api
```

## Notes

- A frontend is not required by the API-focused assignment specification.
- Postman automatically stores and sends the `JSESSIONID` cookie after login.
- If a protected endpoint returns `401`, log in again and retry the request.
