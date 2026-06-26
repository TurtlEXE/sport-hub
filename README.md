# SportHub
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

```text
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
- SQL scripts:
  - Schema definition: `database/sport_booking_marketplace.sql`
  - Sample data: `database/sample.sql`

## Getting Started

1. Clone the repository
2. Configure Database Connection:
   - The file `src/main/resources/application.properties` serves as a generic template and should **not** contain real database passwords.
   - Create a file named `application-local.properties` in `src/main/resources/`. (This file is added to `.gitignore` to prevent committing secrets to the repo).
   - Add your local database configuration to `application-local.properties`:
     ```properties
     spring.datasource.username=your_local_username
     spring.datasource.password=your_local_password
     ```
3. Run the SQL scripts in the `database/` folder to initialize your database structure.
4. Run `SportHubApplication.java` to start the Spring Boot server.

## Team Convention
- Branch naming: `feature/<feature-name>`, `fix/<bug-name>`
- Each member works on their own branch and creates a Pull Request to `main`
