# 🏦 VIRTBANK — Banking Management System

A full-stack Banking Management System built as a semester project.


---

## Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17, Spring Boot 3, Spring Security 6, Hibernate/JPA, MySQL 8, Maven |
| **Frontend** | React 18, Vite, JavaScript (ES2022), CSS3, HTML5 |
| **Tools** | Liquibase (DB migrations), Swagger/OpenAPI 3, JavaMail (SMTP), JWT |

---

## Project Structure

```
virtbank/
├── pom.xml                          # Maven build file
├── src/
│   ├── main/
│   │   ├── java/com/virtbank/
│   │   │   ├── VirtBankApplication.java
│   │   │   ├── config/              # Spring config & beans
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── dto/                 # Request/Response DTOs
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── exception/           # Exception handling
│   │   │   ├── repository/          # Spring Data JPA repos
│   │   │   ├── security/            # Security & JWT
│   │   │   ├── service/             # Business logic
│   │   │   └── util/                # Utilities
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/changelog/        # Liquibase migrations
│   └── test/java/com/virtbank/
├── frontend/                        # React + Vite app
│   └── src/
│       ├── api/                     # API helper modules
│       ├── assets/                  # Static assets
│       ├── components/              # Reusable UI components
│       ├── context/                 # React Context providers
│       ├── hooks/                   # Custom hooks
│       ├── pages/                   # Page components
│       ├── routes/                  # Route definitions
│       └── utils/                   # Utility functions
└── .gitignore
```

---

## Getting Started

### Prerequisites

- **Java 17+** and **Maven 3.9+**
- **Node.js 18+** and **npm 9+**
- **MySQL 8** running locally

### Database Setup

```sql
CREATE DATABASE virtbank;
```

### Backend

```bash
# From the project root
mvn clean install
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server will be available at `http://localhost:5173`.

---

## License

This project is for educational purposes (semester project).
