# Magic Call - Voice Changer System

## Overview

Magic Call is a comprehensive voice changer system built with Spring Boot 3.2.5 and Next.js 14, integrating with FreeSWITCH for real-time voice transformation. The system includes user authentication, subscription-based voice type purchases, call history tracking, balance management, and a modern admin dashboard.

---

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.5
- **Database**: MySQL 8.0+
- **Authentication**: JWT (JSON Web Tokens)
- **Voice Processing**: FreeSWITCH with custom `mod_voicechanger` module
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with role-based access control
- **Build Tool**: Maven
- **Caching**: Spring Cache with in-memory caching

### Frontend
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: shadcn/ui
- **Icons**: Lucide React
- **Date Handling**: date-fns

---

## Features

### Core Features
- ✅ JWT-based authentication with automatic token extraction
- ✅ Role-based access control (Admin & User roles)
- ✅ User registration and login with admin setup utility
- ✅ Balance management system (track usage in seconds)
- ✅ Call history tracking with FreeSWITCH integration
- ✅ Real-time voice transformation via FreeSWITCH ESL (Event Socket Library)
- ✅ Modern responsive admin dashboard (Next.js)

### Voice Type Management
- ✅ Multiple voice types (Male, Female, Child, Robot)
- ✅ Subscription-based access (Monthly/Yearly)
- ✅ Trial system (3-day trial for Child voice)
- ✅ Default voice types auto-assigned on registration
- ✅ Purchase workflow with admin approval
- ✅ Default voice selection for automatic call processing
- ✅ Voice User Mapping HashMap with caching (auto-refresh every hour)

### Voice Expiry & Cleanup (NEW)
- ✅ Automatic expiry cleanup (runs at midnight daily and on startup)
- ✅ Historical tracking of expired voice mappings
- ✅ Expiry reasons: TRIAL_EXPIRED, SUBSCRIPTION_EXPIRED, BOTH_EXPIRED
- ✅ Admin frontend for viewing expired voices
- ✅ Cleanup statistics and preview functionality
- ✅ Manual cleanup trigger via API

### Admin Features
- ✅ Modern Next.js dashboard with responsive design
- ✅ User management interface
- ✅ Voice type management
- ✅ Top-up request approval system
- ✅ Voice purchase approval workflow
- ✅ Expired voices history viewer
- ✅ Call history analytics
- ✅ Comprehensive statistics and reporting

### Payment & Transactions
- ✅ Multiple payment methods (bKash, Nagad, Rocket)
- ✅ Top-up system with manual admin approval (60 sec per 3 BDT, minimum 20 BDT)
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
- Fields: `id`, `id_user`, `id_voice_type`, `is_purchased`, `assigned_at`, `trial_expiry_date`, `expiry_date`, `is_default`
- Supports both trial and subscription-based access
- Tracks default voice for automatic call processing

#### voice_mapping_history (NEW)
- Historical records of expired voice mappings
- Fields: `id`, `original_mapping_id`, `id_user`, `id_voice_type`, `is_purchased`, `assigned_at`, `trial_expiry_date`, `expiry_date`, `is_default`, `expired_at`, `expiry_reason`, `created_at`
- Tracks expiry reasons and maintains audit trail

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

#### Apply Voice Expiry Cleanup Migration
```bash
mysql -u tbuser -p'Takay1takaane$' magic_call < src/main/resources/database/create_voice_mapping_history_table.sql
```

### 2. Backend Configuration

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

### 3. Frontend Setup

```bash
cd src/main/resources/static/magic-call-frontend
npm install
npm run dev  # Development mode
npm run build  # Production build
```

### 4. FreeSWITCH Setup

Ensure FreeSWITCH is installed with the custom `mod_voicechanger` module enabled. The module code is in `src/main/cpp/mod_voicechanger/`.

### 5. Run the Application

Using Maven:
```bash
mvn clean install
mvn spring-boot:run
```

The backend API will start on `http://localhost:8080`
The frontend will start on `http://localhost:3000`

---

## API Endpoints

### Authentication
- `POST /api/auth/signup` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Admin Setup
- `GET /api/setup/test-admin` - Check if admin exists
- `POST /api/setup/create-admin` - Create or update admin user (no auth required)

### User Management (Admin)
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `PUT /api/users/{id}/enable` - Enable user
- `PUT /api/users/{id}/disable` - Disable user

### User Details
- `GET /api/user-details/my` - Get current user details
- `PUT /api/user-details/my` - Update current user details
- `PUT /api/user-details/my/select-voice` - Set selected voice type

### Voice Types
- `GET /api/voice-types` - Get all voice types
- `GET /api/voice-types/{id}` - Get voice type by ID
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
- `GET /api/voice-purchase/my-voices` - Get my voice types (all)
- `GET /api/voice-purchase/my-active-voices` - Get my active voice types
- `GET /api/voice-purchase/available` - Get available voice types for purchase

### Voice User Mapping (NEW)
- `GET /api/voice-user-mapping/map` - Get username to voice codes HashMap
- `GET /api/voice-user-mapping/user/{username}/codes` - Get voice codes for user
- `GET /api/voice-user-mapping/voice-code/{voiceCode}/users` - Get users with access to voice code
- `GET /api/voice-user-mapping/check-access` - Check if user has access to voice code
- `GET /api/voice-user-mapping/statistics` - Get voice access statistics
- `GET /api/voice-user-mapping/user/{username}/details` - Get detailed mappings for user
- `GET /api/voice-user-mapping/default-voice` - Get default voice for authenticated user
- `POST /api/voice-user-mapping/default-voice?voiceCode=902` - Set default voice (JWT auth)
- `DELETE /api/voice-user-mapping/default-voice` - Clear default voice (JWT auth)
- `POST /api/voice-user-mapping/cache/clear` - Clear cache (force refresh)
- `GET /api/voice-user-mapping/debug/print` - Print HashMap to console

### Voice Expiry Cleanup (NEW)
- `GET /api/voice-cleanup/history` - Get all expired voice history
- `GET /api/voice-cleanup/history/user/{userId}` - Get history for specific user
- `GET /api/voice-cleanup/statistics` - Get cleanup statistics
- `GET /api/voice-cleanup/preview` - Preview what would be cleaned (dry run)
- `POST /api/voice-cleanup/run` - Run cleanup manually

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

### Voice Morphing (FreeSWITCH Integration)
- `POST /api/voice-morph/start-call` - Start call with voice morphing
- `POST /api/voice-morph/stop-call` - Stop call
- `POST /api/voice-morph/change-voice` - Change voice during call

---

## Admin Dashboard Pages

The Next.js admin frontend includes the following pages:

- `/login` - Admin login page
- `/dashboard` - Dashboard overview with statistics
- `/dashboard/users` - User management
- `/dashboard/voice-types` - Voice type management
- `/dashboard/topup` - Top-up request management
- `/dashboard/voice-purchases` - Voice purchase approval
- `/dashboard/expired-voices` - Expired voice history viewer (NEW)
- `/dashboard/call-history` - Call history and analytics

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
- User can set this voice as default

### 3. User Sets Default Voice
```http
POST /api/voice-user-mapping/default-voice?voiceCode=902
```

- Only one voice can be default at a time
- Previous default is automatically cleared
- Default voice is used automatically during calls

### 4. Automatic Expiry Cleanup
- Runs at midnight (12:00 AM) daily
- Also runs on application startup
- Expired mappings moved to `voice_mapping_history` table
- User loses access to expired voices
- History maintained for audit trail

---

## Voice Expiry Cleanup System

### Scheduled Tasks
- **Midnight Cleanup**: Runs daily at 12:00 AM
- **Startup Cleanup**: Runs when application starts
- **Manual Cleanup**: Can be triggered via admin API

### Expiry Logic
- **TRIAL_EXPIRED**: Trial period ended
- **SUBSCRIPTION_EXPIRED**: Subscription period ended
- **BOTH_EXPIRED**: Both trial and subscription expired
- **NOT EXPIRED**: At least one date is still valid or no expiry dates (permanent)

### History Tracking
All expired mappings are preserved in `voice_mapping_history` with:
- Original mapping ID
- User and voice type information
- Expiry dates (trial and subscription)
- Expiry reason
- Timestamp of when it expired

### Admin Features
- View all expired voice history
- Filter by expiry reason
- Search by username or voice name
- View statistics (total expired, recent expired, etc.)
- Preview what would be cleaned up (dry run)
- Manually trigger cleanup

---

## Default User Accounts

### Admin Account
- **Username**: `admin`
- **Password**: `admin123`
- **Roles**: `ROLE_ADMIN`, `ROLE_USER`

### Creating Admin User
If admin doesn't exist, use the setup endpoint:
```bash
curl -X POST http://localhost:8080/api/setup/create-admin \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","firstName":"Admin","lastName":"User"}'
```

**Note**: Change default password in production!

---

## Default Voice Types

New users are automatically assigned:
1. **Voice Type 3 (Child Voice)** - 3-day trial (code: 903)
2. **Voice Type 4 (Robot Voice)** - Permanent free access (code: 904)

Users can purchase additional voice types:
- **Voice Type 1 (Male Voice)** - Code: 901 (Requires purchase)
- **Voice Type 2 (Female Voice)** - Code: 902 (Requires purchase)

---

## Voice User Mapping HashMap

### Caching Strategy
- HashMap cached in memory for fast access
- Auto-refreshes every hour
- Cache invalidated on:
  - Voice purchase approval/rejection
  - Manual cache clear
  - Voice expiry cleanup

### Usage
```java
// Get all voice codes for a user
Map<String, List<String>> userVoiceMap = voiceUserMappingService.getUserVoiceCodesMap();
List<String> voiceCodes = userVoiceMap.get("username");

// Check access
boolean hasAccess = voiceUserMappingService.hasUserAccessToVoiceCode("username", "902");

// Get default voice
Optional<String> defaultVoice = voiceUserMappingService.getDefaultVoiceCodeForUser("username");
```

---

## Security

### Authentication
- JWT tokens expire after 24 hours (configurable)
- Passwords encrypted with BCrypt
- Role-based authorization on endpoints
- Token automatically extracted from Authorization header

### Roles
- **ROLE_ADMIN**: Full access to all endpoints
- **ROLE_USER**: Access to user-specific endpoints only

### FreeSWITCH Integration
- User registration automatically creates FreeSWITCH configuration
- If FreeSWITCH config fails, user creation is rolled back
- Configuration files stored in `/usr/local/freeswitch/conf/directory/default/`

---

## Payment Methods

Supported payment methods:
- **bKash** - Mobile financial service
- **Nagad** - Digital payment platform
- **Rocket** - Mobile banking service

---

## Call Duration Tracking

- Balance stored in **seconds**
- Real-time deduction during calls via FreeSWITCH events
- Call history with detailed CDR information
- Duration tracking from FreeSWITCH ESL
- Low balance warnings

---

## Logging

The application uses SLF4J with Logback for comprehensive logging:

### Backend Logging
- Info level for normal operations
- Debug level for detailed debugging
- Error level for exceptions

### Voice Expiry Cleanup Logging
- Startup cleanup logs with 🚀 emoji
- Scheduled cleanup logs with 🕛 emoji
- Detailed per-mapping logs
- Summary statistics after cleanup
- Error tracking and reporting

Check logs for:
- User authentication
- Purchase approvals
- Call events
- ESL connections
- Voice expiry cleanup operations
- Cache refresh events

---

## Troubleshooting

### Database Connection Issues
- Verify MySQL is running: `sudo systemctl status mysql`
- Check credentials in `application.properties`
- Ensure database exists: `SHOW DATABASES;`
- Run migrations: Import SQL files from `src/main/resources/database/`

### FreeSWITCH Connection Issues
- Verify FreeSWITCH is running: `fs_cli -x "status"`
- Check ESL configuration in `application.properties`
- Ensure `mod_voicechanger` is loaded: `fs_cli -x "module_exists mod_voicechanger"`
- Check FreeSWITCH logs: `/var/log/freeswitch/freeswitch.log`

### Authentication Issues
- Verify JWT secret is configured
- Check token expiration settings
- Ensure user has correct roles assigned
- Use admin setup endpoint to create/reset admin

### Frontend Issues
- Ensure backend is running on port 8080
- Check API base URL in frontend config
- Verify CORS settings allow frontend origin
- Clear browser cache and localStorage
- Check browser console for errors

### Voice Expiry Cleanup Issues
- Check application logs for cleanup execution
- Verify cron expression is correct
- Ensure `@EnableScheduling` is present
- Test with manual cleanup endpoint
- Check database permissions for history table

---

## Development

### Project Structure
```
VoicechnagerBackend/
├── src/
│   ├── main/
│   │   ├── cpp/
│   │   │   └── mod_voicechanger/    # FreeSWITCH module
│   │   ├── java/com/example/voicechanger/
│   │   │   ├── config/              # Security, JWT, CORS, Cache
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── repository/          # Spring Data repositories
│   │   │   ├── security/            # JWT filters and utilities
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── VoiceUserMappingService.java
│   │   │   │   └── VoiceExpiryCleanupService.java
│   │   │   └── nativelib/           # Native voice processing
│   │   └── resources/
│   │       ├── database/            # SQL scripts and migrations
│   │       ├── static/
│   │       │   └── magic-call-frontend/  # Next.js frontend
│   │       │       ├── app/
│   │       │       │   ├── login/
│   │       │       │   └── dashboard/
│   │       │       │       ├── page.tsx
│   │       │       │       ├── users/
│   │       │       │       ├── voice-types/
│   │       │       │       ├── topup/
│   │       │       │       ├── voice-purchases/
│   │       │       │       ├── expired-voices/  # NEW
│   │       │       │       └── call-history/
│   │       │       ├── components/
│   │       │       │   ├── DashboardLayout.tsx
│   │       │       │   └── ui/
│   │       │       └── lib/
│   │       │           ├── auth.ts
│   │       │           └── api.ts
│   │       └── application.properties
```

### Building from Source
```bash
# Backend
mvn clean package
java -jar target/VoicechangerBackend-1.0-SNAPSHOT.jar

# Frontend
cd src/main/resources/static/magic-call-frontend
npm run build
```

---

## API Documentation

### Postman Collection
For detailed API documentation and testing, import the Postman collection:
```
src/main/resources/database/Magic_Call_API.postman_collection.json
```

**Total Endpoints**: 73
- Authentication: 2
- Admin Setup: 2
- User Management: 9
- User Details: 9
- Voice Processing: 4
- Voice Types: 6
- Balance: 2
- Call History: 9
- Voice Purchase: 11
- Top-Up: 8
- Voice User Mapping: 11
- Voice Expiry Cleanup: 5

---

## Environment Variables

```bash
# Database
DB_URL=jdbc:mysql://localhost:3306/magic_call
DB_USERNAME=tbuser
DB_PASSWORD=Takay1takaane$

# JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# FreeSWITCH
FREESWITCH_HOST=127.0.0.1
FREESWITCH_PORT=8021
FREESWITCH_PASSWORD=ClueCon
```

---

## Support

For issues or questions, contact: **Humayun Ahmed**

---

## License

Proprietary - All rights reserved

---

## Version History

### v1.3.0 (2025-12-22)
- Added automatic voice expiry cleanup service
- Added voice mapping history tracking
- Added expired voices admin dashboard page
- Enhanced logging with structured output
- Added cleanup statistics and preview
- Simplified default voice API (JWT token based)
- Cache invalidation on cleanup

### v1.2.0 (2025-12-21)
- Added Voice User Mapping HashMap service
- Added default voice selection functionality
- Added caching with auto-refresh
- Added admin frontend with Next.js 14
- Improved API documentation
- Enhanced security with JWT token extraction

### v1.1.0 (2025-12-21)
- Added top-up approval system
- Added voice purchase approval workflow
- Enhanced admin dashboard
- Improved transaction tracking

### v1.0.0 (2025-12-20)
- Initial release with voice changer functionality
- JWT authentication and authorization
- Voice purchase system with subscriptions
- Balance management and top-up system
- Call history tracking
- Admin dashboard and management tools
- FreeSWITCH ESL integration
