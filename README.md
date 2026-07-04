# VolunteerHub - Volunteer Registration Campaign Application

A modern Spring Boot application for managing volunteer registration campaigns, services, and volunteer signups.

## Features

- **Campaign Management**: Create and manage volunteer campaigns with multiple services
- **Service Catalog**: Browse available volunteer opportunities by campaign and category
- **Volunteer Registration**: Easy registration process for new volunteers
- **Service Signup**: Volunteers can sign up for specific services
- **Registration Tracking**: View and manage service registrations
- **Admin Dashboard**: Overview of all volunteers, campaigns, services, and registrations
- **Modern UI**: Beautiful, responsive frontend with smooth user experience

## Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Database**: H2 (in-memory for development)
- **ORM**: Spring Data JPA / Hibernate
- **Template Engine**: Thymeleaf
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **UI Components**: Font Awesome Icons, Inter Font

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Quick Start

### 1. Clone and Navigate to Project
```bash
cd volunteer-registration-app
```

### 2. Build the Application
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

### 4. Access the Application
Open your browser and navigate to:
- **Main Site**: http://localhost:8080
- **Admin Dashboard**: http://localhost:8080/admin
- **H2 Console**: http://localhost:8080/h2-console

## H2 Database Console

Access the H2 database console at `/h2-console` with:
- **JDBC URL**: `jdbc:h2:mem:volunteerdb`
- **Username**: `sa`
- **Password**: *(leave empty)*

## Project Structure

```
volunteer-registration-app/
├── src/
│   ├── main/
│   │   ├── java/com/volunteer/registration/
│   │   │   ├── controller/          # REST & Web Controllers
│   │   │   ├── service/             # Business Logic
│   │   │   ├── repository/          # Data Access Layer
│   │   │   ├── model/               # JPA Entities
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── config/              # Configuration Classes
│   │   │   └── VolunteerRegistrationApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/styles.css   # Application Styles
│   │       │   └── js/main.js       # Frontend JavaScript
│   │       ├── templates/           # Thymeleaf Templates
│   │       └── application.properties
│   └── test/
└── pom.xml
```

## API Endpoints

### Campaigns
- `GET /api/campaigns` - Get all campaigns
- `GET /api/campaigns/active` - Get active campaigns
- `GET /api/campaigns/{id}` - Get campaign by ID
- `POST /api/campaigns` - Create campaign
- `PUT /api/campaigns/{id}` - Update campaign
- `DELETE /api/campaigns/{id}` - Delete campaign

### Services
- `GET /api/services` - Get all services
- `GET /api/services/available` - Get available services
- `GET /api/services/campaign/{campaignId}` - Get services by campaign
- `GET /api/services/categories` - Get all categories
- `POST /api/services` - Create service
- `PUT /api/services/{id}` - Update service
- `DELETE /api/services/{id}` - Delete service

### Volunteers
- `GET /api/volunteers` - Get all volunteers
- `GET /api/volunteers/{id}` - Get volunteer by ID
- `GET /api/volunteers/email/{email}` - Get volunteer by email
- `POST /api/volunteers/register` - Register new volunteer
- `PUT /api/volunteers/{id}` - Update volunteer
- `DELETE /api/volunteers/{id}` - Delete volunteer

### Registrations
- `GET /api/registrations` - Get all registrations
- `GET /api/registrations/volunteer/email/{email}` - Get registrations by volunteer email
- `POST /api/registrations` - Register for a service
- `PUT /api/registrations/{id}/confirm` - Confirm registration
- `PUT /api/registrations/{id}/cancel` - Cancel registration
- `PUT /api/registrations/{id}/complete` - Mark registration complete

## Sample Data

The application initializes with sample data including:
- 2 Active Campaigns (Spring Cleanup, Summer Youth Program)
- 10 Volunteer Services across different categories
- Categories: Environment, Education, Sports, Arts, Community

## User Workflow

1. **Register as Volunteer**: Go to `/register` and fill out the registration form
2. **Browse Services**: Visit `/services` to see available volunteer opportunities
3. **Sign Up for Service**: Click "Sign Up" on any service and enter your registered email
4. **Track Registrations**: Go to `/my-registrations` and enter your email to view your signups

## Admin Features

Access the admin dashboard at `/admin` to:
- View all registered volunteers
- Manage campaigns and services
- Track and manage registrations
- Confirm, complete, or cancel registrations

## Configuration

Key configuration options in `application.properties`:
- `server.port`: Server port (default: 8080)
- `spring.datasource.url`: Database connection URL
- `spring.jpa.hibernate.ddl-auto`: Database schema management
- `spring.h2.console.enabled`: H2 console access

## Future Enhancements

- User authentication and authorization
- Email notifications for registrations
- Volunteer hour tracking
- Certificate generation
- Advanced reporting and analytics
- Mobile app integration

## License

This project is open-source and available under the MIT License.
