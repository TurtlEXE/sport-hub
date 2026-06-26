# THIS IS SPORTHUB
# Multi-Sport Court Booking Platform

## Tech Stack
- Java 21
- Spring Boot 4.1.0
- Spring Security
- Spring Data JPA + Hibernate 7
- MySQL / SQL Server
- Thymeleaf
- Lombok

## Project Structure

```
src/main/java/com/mvc/mock_project/
├── config/          # Spring config (Security, etc.)
├── controller/      # REST Controllers
├── dto/
│   ├── request/     # Request DTOs
│   └── response/    # Response DTOs
├── entities/        # JPA Entities
│   └── enums/       # Enum types
├── exception/       # Custom exceptions & global handler
├── mapper/          # Entity ↔ DTO mappers
├── repository/      # Spring Data JPA Repositories
├── security/        # JWT, UserDetails, filters
├── service/         # Service interfaces
│   └── impl/        # Service implementations
└── util/            # Utility classes
```

## Database
- Schema: `sport_booking_marketplace`
- SQL script: `src/main/resources/court_booking_db_sqlserver.sql`

## Getting Started

1. Clone the repository
2. Configure `src/main/resources/application.properties`:
   - Update `spring.datasource.username` and `spring.datasource.password`
3. Run the SQL script to initialize the database
4. Run `MockProjectApplication.java`

## Team Convention
- Branch naming: `feature/<feature-name>`, `fix/<bug-name>`
- Each member works on their own branch and creates a Pull Request to `main`
