# ZeroMonos Web Interface - Complete Guide

## Overview
Web interface for the ZeroMonos garbage collection booking system. Citizens can book collection services and staff can manage requests through a simple, functional interface.

## Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for loading municipalities from external API)

## Installation & Running

### 1. Navigate to the project directory
```bash
cd ZeroMonos
```

### 2. Build the project
```bash
mvn clean install
```

### 3. Run the application
```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

## Accessing the Application

### Main Portal
Open your browser: **http://localhost:8080**

Choose between:
- **Citizen Portal** - Book and track collection services
- **Staff Portal** - Manage service requests

## Citizen Portal

**URL:** http://localhost:8080/citizen

### Login
All users must login before accessing features.

**Test Credentials:**
- Username: `John Doe` | Password: `password123`
- Username: `Maria Silva` | Password: `password123`
- Username: `Carlos Santos` | Password: `password123`

### Features

#### 1. Book a Collection
- Select municipality from dropdown (loaded from Portuguese Municipalities API)
- Choose collection date (no service on Sundays)
- Enter time slot between 09:00 and 18:00
- Describe items to be collected
- Receive unique access token upon booking

**Business Rules:**
- No service on Sundays
- Service hours: 09:00 - 18:00
- Time slots must be at least 1 hour apart

#### 2. Check Request Status
- Enter your access token
- View request details (municipality, date, time, description)
- See complete status history with timestamps
- Cancel request if needed

#### 3. My Requests
- View all your bookings in one place
- See tokens with creation timestamps
- Quick access to check status with "Check Status" button
- Automatically refreshes after creating new bookings

### User Flow
1. Login with credentials
2. Book a service (save the token)
3. View all your requests in "My Requests" section
4. Check status or cancel requests
5. Logout when done

## Staff Portal

**URL:** http://localhost:8080/staff

### Login
Staff members must login before accessing features.

**Test Credentials:**
- Username: `Staff Member` | Password: `staff123`

### Features

#### 1. Browse Requests
- Select municipality from dropdown
- Load all requests for that municipality
- View requests in table format

#### 2. View Request Details
- Click "View" button on any request
- See complete request information
- View status history with timestamps

#### 3. Update Request Status
- Click "Update" button on any request
- Select new status:
  - RECEIVED
  - ASSIGNED
  - IN_PROGRESS
  - COMPLETED
  - CANCELLED
- Status is saved and timestamped

### User Flow
1. Login with staff credentials
2. Select municipality
3. Load requests
4. View or update request status
5. Logout when done

## API Endpoints

The web interface uses the following REST API endpoints:

### Service Requests
- `POST /requests` - Create new request
- `GET /requests/{token}` - Get request by token
- `GET /requests/user/{username}` - Get all requests for a user
- `GET /requests?municipality={name}` - Get requests by municipality
- `DELETE /requests/{token}` - Cancel request
- `PUT /requests/{token}/status` - Update status
- `GET /requests/{token}/history` - Get status history
- `GET /requests/municipalities` - Get all municipalities

### Authentication
- `POST /users/authenticate` - Login endpoint

## Database

Uses H2 in-memory database for development.

**H2 Console:** http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:zeromonos`
- Username: `sa`
- Password: (leave empty)

**Note:** Database resets on application restart.

## Test Users

Created automatically on startup:

**Citizens (password: password123):**
- John Doe
- Maria Silva
- Carlos Santos

**Staff (password: staff123):**
- Staff Member

## Technical Details

### Technology Stack
- **Backend:** Spring Boot 3.3.4
- **Database:** H2 (in-memory)
- **Frontend:** HTML5, Vanilla JavaScript
- **Templating:** Thymeleaf
- **External API:** json.geoapi.pt (Portuguese municipalities)

### Session Management
- Client-side sessions using `sessionStorage`
- Sessions persist across page refreshes
- Separate sessions for citizen and staff portals

### Time Input
- HTML5 `<input type="time">` with validation
- Min: 09:00, Max: 18:00
- 15-minute step increments
- Server validates 1-hour spacing between slots

## Troubleshooting

### Port Already in Use
Edit `src/main/resources/application.properties`:
```properties
server.port=8081
```

### Municipalities Not Loading
- Check internet connection
- Verify access to https://json.geoapi.pt/municipios
- Check application logs for errors

### Login Issues
- Ensure usernames are exact (case-sensitive)
- Use correct passwords
- Check browser console for errors

### Database Issues
```bash
# Clean rebuild
mvn clean install
mvn spring-boot:run
```

## Features Summary

### Citizen Portal
✅ Login system with session management
✅ Book collection services
✅ Flexible time slot input (09:00-18:00)
✅ Dynamic municipality dropdown
✅ Check request status by token
✅ View status history
✅ Cancel requests
✅ **My Requests section** - view all your bookings
✅ Shows creation timestamps
✅ Quick status check from My Requests
✅ Logout functionality

### Staff Portal
✅ Login system with session management
✅ Browse requests by municipality
✅ View detailed request information
✅ Update request status
✅ View status history
✅ Logout functionality

## Security Notes

- No actual authentication beyond API call
- Passwords stored as SHA-256 hash
- Session data stored client-side
- No password reset functionality
- No user registration (test users only)

## Known Limitations

- In-memory database (data lost on restart)
- No real user management
- Client-side session only
- No concurrent booking prevention (handled server-side)
- No email notifications

## Support

For issues:
1. Check application logs
2. Verify database connection (H2 console)
3. Check browser console for JavaScript errors
4. Ensure all test users are created (check logs on startup)

## File Structure

```
ZeroMonos/
├── src/main/
│   ├── java/org/hw1/
│   │   ├── boundary/
│   │   │   ├── FrontendController.java
│   │   │   ├── ServiceRequestRestController.java
│   │   │   └── UserRestController.java
│   │   └── DataInitializer.java
│   └── resources/
│       ├── templates/
│       │   ├── index.html
│       │   ├── citizen.html
│       │   └── staff.html
│       └── application.properties
└── pom.xml
```

## Version History

**Current Version:**
- Login system for both portals
- Time slot as user input (09:00-18:00)
- My Requests section showing all user bookings
- Creation timestamps displayed
- Quick status check functionality