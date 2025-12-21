# Magic Call - Voice Changer Backend

## Overview

Magic Call is a comprehensive voice changer system built with Spring Boot 3.2.5, integrating with FreeSWITCH for real-time voice transformation. The system includes user authentication, subscription-based voice type purchases, call history tracking, and balance management.

---

## Technology Stack

- **Backend Framework**: Spring Boot 3.2.5
- **Database**: MySQL 8.0+
- **Authentication**: JWT (JSON Web Tokens)
- **Voice Processing**: FreeSWITCH with custom `mod_voicechanger` module
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with role-based access control
- **Build Tool**: Maven

---

## Features

### Core Features
- ✅ JWT-based authentication and authorization
- ✅ Role-based access control (Admin & User roles)
- ✅ User registration and login
- ✅ Balance management system (purchase and track usage in seconds)
- ✅ Call history tracking with FreeSWITCH integration
- ✅ Real-time voice transformation via FreeSWITCH ESL (Event Socket Library)

### Voice Type Management
- ✅ Multiple voice types (Male, Female, Child, Robot)
- ✅ Subscription-based access (Monthly/Yearly)
- ✅ Trial system (3-day trial for specific voice types)
- ✅ Default voice types auto-assigned on registration
- ✅ Purchase workflow with admin approval

### Admin Features
- ✅ Manage users and voice types
- ✅ Approve/reject top-up requests
- ✅ Approve/reject voice purchase requests
- ✅ View all transactions and call history
- ✅ Comprehensive admin dashboard

### Payment & Transactions
- ✅ Multiple payment methods (bKash, Nagad, Rocket)
- ✅ Top-up system with manual admin approval
- ✅ Voice purchase system separate from top-ups
- ✅ Transaction tracking with status management

---

## Database Schema

### Key Tables

#### users
- User authentication and profile information
- Fields: `id`, `username`, `password`, `first_name`, `last_name`, `enabled`, `created_at`, `updated_at`

#### roles & user_roles
- Role-based access control
- Roles: `ROLE_ADMIN`, `ROLE_USER`

#### voice_types
- Available voice transformations
- Fields: `id`, `voice_name`, `code`, `created_at`, `updated_at`
- Default types: Male (901), Female (902), Child (903), Robot (904)

#### voice_user_mapping
- Maps users to their accessible voice types
- Fields: `id`, `id_user`, `id_voice_type`, `is_purchased`, `assigned_at`, `trial_expiry_date`, `expiry_date`
- Supports both trial and subscription-based access

#### voice_purchases
- Voice type purchase requests and approvals
- Fields: `id`, `id_user`, `id_voice_type`, `transaction_method`, `tnx_id`, `subscription_type`, `amount`, `purchase_date`, `expiry_date`, `status`, `updated_at`
- Status: `pending`, `approved`, `rejected`

#### balances
- User balance tracking (in seconds)
- Fields: `id`, `id_user`, `purchase_amount`, `last_used_amount`, `total_used_amount`, `remain_amount`

#### transactions
- Top-up transactions with admin approval
- Fields: `id`, `id_user`, `transaction_method`, `amount`, `tnx_id`, `date`, `status`, `updated_at`

#### call_history
- CDR (Call Detail Records) from FreeSWITCH
- Fields: `id`, `aparty`, `bparty`, `uuid`, `source_ip`, `create_time`, `start_time`, `end_time`, `duration`, `status`, `hangup_cause`, `codec`, `id_user`

---

## Setup Instructions

### 1. Database Setup

#### Create Database and User
```bash
mysql -u root -p
```

```sql
CREATE DATABASE magic_call CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tbuser'@'localhost' IDENTIFIED BY 'Takay1takaane$';
GRANT ALL PRIVILEGES ON magic_call.* TO 'tbuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### Import Schema
```bash
mysql -u tbuser -p'Takay1takaane$' magic_call < src/main/resources/database/magic_call_backup.sql
```

### 2. Configuration

Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/magic_call?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=tbuser
spring.datasource.password=Takay1takaane$

# FreeSWITCH ESL Configuration
freeswitch.esl.host=127.0.0.1
freeswitch.esl.port=8021
freeswitch.esl.password=ClueCon

# JWT Configuration
jwt.secret=your-secret-key-here
jwt.expiration=86400000
```

### 3. FreeSWITCH Setup

Ensure FreeSWITCH is installed with the custom `mod_voicechanger` module enabled. Refer to the documentation PDF for detailed FreeSWITCH configuration.

### 4. Run the Application

Using Maven:
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## API Endpoints

### Authentication
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### User Management (Admin)
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### User Details
- `GET /api/user-details/my` - Get current user details
- `PUT /api/user-details/my` - Update current user details

### Voice Types
- `GET /api/voice-types` - Get all voice types
- `POST /api/voice-types` - Create voice type (Admin)
- `PUT /api/voice-types/{id}` - Update voice type (Admin)
- `DELETE /api/voice-types/{id}` - Delete voice type (Admin)

### Voice Purchases
- `POST /api/voice-purchase` - Request to purchase voice type
- `GET /api/voice-purchase/my` - Get my purchase history
- `GET /api/voice-purchase` - Get all purchases (Admin)
- `GET /api/voice-purchase/pending` - Get pending purchases (Admin)
- `PUT /api/voice-purchase/{id}/approve` - Approve purchase (Admin)
- `PUT /api/voice-purchase/{id}/reject` - Reject purchase (Admin)

### Voice User Mapping
- `GET /api/voice-purchase/my-voices` - Get my voice types (all)
- `GET /api/voice-purchase/my-active-voices` - Get my active voice types
- `GET /api/voice-purchase/available` - Get available voice types for purchase

### Balance Management
- `GET /api/balance/my` - Get my balance
- `POST /api/balance/topup` - Request balance top-up
- `GET /api/balance/topup/pending` - Get pending top-up requests (Admin)
- `PUT /api/balance/topup/{id}/approve` - Approve top-up (Admin)
- `PUT /api/balance/topup/{id}/reject` - Reject top-up (Admin)

### Call History
- `GET /api/call-history` - Get all call history (Admin)
- `GET /api/call-history/my` - Get my call history
- `GET /api/call-history/user/{userId}` - Get call history by user ID (Admin)

### Transactions
- `GET /api/transactions` - Get all transactions (Admin)
- `GET /api/transactions/my` - Get my transactions
- `PUT /api/transactions/{id}/status` - Update transaction status (Admin)

---

## Voice Purchase & Subscription Flow

### 1. User Purchases Voice Type
```json
POST /api/voice-purchase
{
  "idVoiceType": 1,
  "transactionMethod": "bkash",
  "tnxId": "TXN123456789",
  "subscriptionType": "monthly"
}
```

**Subscription Types:**
- `monthly` - Access for 1 month
- `yearly` - Access for 1 year

### 2. Admin Approves Purchase
```http
PUT /api/voice-purchase/{id}/approve
```

Upon approval:
- User gets access to the voice type
- `voice_user_mapping` record created with `expiry_date`
- Status changes from `pending` to `approved`

### 3. User Uses Voice Type
- User can use the voice type until `expiry_date`
- After expiry, user needs to renew subscription

---

## Default User Accounts

### Admin Account
- **Username**: `admin`
- **Password**: `admin123`
- **Roles**: `ROLE_ADMIN`, `ROLE_USER`

### Test User Account
- **Username**: `testuser`
- **Password**: `password123`
- **Roles**: `ROLE_USER`

**Note**: Change default passwords in production!

---

## Default Voice Types

New users are automatically assigned:
1. **Voice Type 3 (Child Voice)** - 3-day trial (code: 903)
2. **Voice Type 4 (Robot Voice)** - Permanent free access (code: 904)

---

## Admin Dashboard

Static HTML admin pages are available at:
- `/admin/dashboard.html` - Dashboard overview
- `/admin/users.html` - User management
- `/admin/voice-types.html` - Voice type management
- `/admin/packages.html` - Package management
- `/admin/purchases.html` - Top-up purchase management
- `/admin/voice-purchases.html` - Voice purchase management

---

## Security

### Authentication
- JWT tokens expire after 24 hours (configurable)
- Passwords encrypted with BCrypt
- Role-based authorization on endpoints

### Roles
- **ROLE_ADMIN**: Full access to all endpoints
- **ROLE_USER**: Access to user-specific endpoints only

---

## Payment Methods

Supported payment methods:
- **bKash** - Mobile financial service
- **Nagad** - Digital payment platform
- **Rocket** - Mobile banking service

---

## Call Duration Tracking

- Balance stored in **seconds**
- Real-time deduction during calls
- Call history with detailed CDR information
- Duration tracking from FreeSWITCH events

---

## Logging

The application uses SLF4J with Logback for logging:
- Info level for normal operations
- Debug level for detailed debugging
- Error level for exceptions

Check logs for:
- User authentication
- Purchase approvals
- Call events
- ESL connections

---

## Troubleshooting

### Database Connection Issues
- Verify MySQL is running: `sudo systemctl status mysql`
- Check credentials in `application.properties`
- Ensure database exists: `SHOW DATABASES;`

### FreeSWITCH Connection Issues
- Verify FreeSWITCH is running: `fs_cli -x "status"`
- Check ESL configuration in `application.properties`
- Ensure `mod_voicechanger` is loaded

### Authentication Issues
- Verify JWT secret is configured
- Check token expiration settings
- Ensure user has correct roles assigned

---

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/example/voicechanger/
│   │   ├── config/          # Security, JWT, CORS configuration
│   │   ├── controller/      # REST API endpoints
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Custom exceptions
│   │   ├── repository/      # Spring Data repositories
│   │   ├── security/        # JWT filters and utilities
│   │   ├── service/         # Business logic
│   │   └── nativelib/       # Native voice processing
│   └── resources/
│       ├── database/        # SQL scripts
│       ├── static/          # Static web resources
│       └── application.properties
```

### Building from Source
```bash
mvn clean package
java -jar target/VoicechangerBackend-1.0-SNAPSHOT.jar
```

---

## API Documentation

For detailed API documentation and testing, import the Postman collection:
```
src/main/resources/database/Magic_Call_API.postman_collection.json
```

---

## Support

For issues or questions, contact: **Humayun Ahmed**

---

## License

Proprietary - All rights reserved

---

## Version History

### v1.0.0 (2025-12-21)
- Initial release with voice changer functionality
- JWT authentication and authorization
- Voice purchase system with subscriptions
- Balance management and top-up system
- Call history tracking
- Admin dashboard and management tools
- FreeSWITCH ESL integration
