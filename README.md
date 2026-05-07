# Make-a-Schedule

A modern appointment scheduling application built with Spring Boot and React, featuring real-time conflict detection, email notifications, and group meeting support.

## Features

- **User Authentication**: Secure JWT-based authentication system
- **Appointment Management**: Create, view, and delete appointments
- **Conflict Detection**: Real-time validation to prevent scheduling conflicts
- **Group Meetings**: Support for multi-participant appointments
- **Email Notifications**: Automated email reminders for appointments
- **Interactive Calendar**: FullCalendar integration for intuitive scheduling
- **Responsive Design**: Modern UI built with React and Tailwind CSS

## Tech Stack

### Backend

- **Java 21** with Spring Boot 4.0.5
- **Spring Security** with JWT authentication
- **Spring Data JPA** with PostgreSQL
- **Flyway** for database migrations
- **Spring Mail** for email notifications
- **SpringDoc OpenAPI** for API documentation
- **Maven** for dependency management

### Frontend

- **React 19** with TypeScript
- **Vite** for fast development and building
- **FullCalendar** for calendar UI
- **Tailwind CSS** for styling
- **ESLint** for code quality

### Infrastructure

- **PostgreSQL 17** database
- **Docker & Docker Compose** for containerization
- **Nginx** for frontend serving

## Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Node.js 18+ (for local development)
- Maven 3.8+ (for local development)

## Getting Started

### Using Docker Compose (Recommended)

1. Clone the repository:

```bash
git clone https://github.com/DamPhuQuy/Make-a-schedule.git
cd Make-a-schedule
```

2. Create a `.env` file based on `.env.example`:

```bash
cp .env.example .env
```

3. Configure your environment variables in `.env`:

```env
DB_HOST=db
DB_NAME=your_database_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
DB_PORT=5432
JWT_SECRET=your_jwt_secret_key

EMAIL_USERNAME=your_email@example.com
EMAIL_PASSWORD=your_email_password
```

4. Start the application:

```bash
docker-compose up -d
```

5. Access the application:

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui.html

### Local Development

#### Backend

1. Navigate to the backend directory:

```bash
cd backend/api
```

2. Configure `application.properties` with your database credentials

3. Run the application:

```bash
mvn spring-boot:run
```

#### Frontend

1. Navigate to the frontend directory:

```bash
cd frontend
```

2. Install dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm run dev
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and receive JWT token

### Appointments

- `GET /api/appointments` - Get all appointments for the current user
- `GET /api/appointments/{id}` - Get a specific appointment
- `POST /api/appointments` - Create a new appointment
- `POST /api/appointments/validate` - Validate appointment for conflicts
- `POST /api/appointments/group` - Create a group meeting
- `DELETE /api/appointments/{id}` - Delete an appointment

## Project Structure

```
Make-a-schedule/
├── backend/
│   └── api/
│       ├── src/
│       │   └── main/
│       │       └── java/com/schedule/app/
│       │           ├── adapter/        # Controllers and web layer
│       │           ├── application/    # Use cases and DTOs
│       │           ├── domain/         # Domain entities
│       │           ├── infrastructure/ # Repositories
│       │           ├── security/       # Security configuration
│       │           └── config/         # Application configuration
│       └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── App.tsx
│   │   ├── CalendarView.tsx
│   │   ├── AppointmentModal.tsx
│   │   ├── AuthView.tsx
│   │   └── ConflictWarning.tsx
│   └── package.json
├── docker-compose.yaml
└── .env.example
```

## Architecture

The application follows Clean Architecture principles:

- **Adapter Layer**: REST controllers handling HTTP requests
- **Application Layer**: Use cases containing business logic
- **Domain Layer**: Core entities and business rules
- **Infrastructure Layer**: Database repositories and external services

## Database

The application uses PostgreSQL with Flyway for version-controlled migrations. The database schema includes:

- Users table with authentication details
- Appointments table with scheduling information
- Participants table for group meetings

## Security

- JWT-based authentication
- Password encryption using BCrypt
- CORS configuration for cross-origin requests
- Secure session management

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Dam Phu Quy**
**Le Huynh Thanh Thinh**
**Tran Le Phi Long**

## Acknowledgments

- Spring Boot team for the excellent framework
- FullCalendar for the calendar component
- React and Vite communities for modern frontend tooling
