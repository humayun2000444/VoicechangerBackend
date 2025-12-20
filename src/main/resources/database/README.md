# Magic Call Database & API Documentation

This directory contains database schemas, backup files, and API testing resources.

## Files

### Database Files
- **magic_call_backup.sql** - Complete database schema with tables for users, roles, and user_roles

### API Testing
- **Magic_Call_API.postman_collection.json** - Postman collection with all API endpoints

---

## Database Setup

### 1. Create Database and Grant Permissions

Run as MySQL root user:
```bash
sudo mysql
```

```sql
CREATE DATABASE IF NOT EXISTS magic_call;
GRANT ALL PRIVILEGES ON magic_call.* TO 'tbuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Import Database Schema

```bash
mysql -u tbuser -p'Takay1takaane$' < /home/prototype/Downloads/VoicechnagerBackend/src/main/resources/database/magic_call_backup.sql
```

### 3. Verify Tables Created

```bash
mysql -u tbuser -p'Takay1takaane$' magic_call -e "SHOW TABLES;"
```

Expected output:
```
+----------------------+
| Tables_in_magic_call |
+----------------------+
| roles                |
| user_roles           |
| users                |
+----------------------+
```

---

## Database Schema

### Tables

#### roles
- `id` - Primary key
- `name` - Role name (ROLE_ADMIN, ROLE_USER)

#### users
- `id` - Primary key
- `username` - Unique username
- `password` - BCrypt encrypted password
- `first_name` - User's first name
- `last_name` - User's last name
- `created_at` - Registration timestamp
- `updated_at` - Last update timestamp
- `enabled` - Account status

#### user_roles
- `user_id` - Foreign key to users
- `role_id` - Foreign key to roles

---

## Postman Collection Setup

### Import Collection

1. Open Postman
2. Click **Import** button
3. Select file: `Magic_Call_API.postman_collection.json`
4. Click **Import**

### Configure Environment Variables

The collection uses these variables:
- `base_url` - Default: `http://localhost:8080`
- `jwt_token` - Auto-populated after login
- `username` - Auto-populated after login

### Test the APIs

#### 1. Register a New User
- Request: **POST** `/api/auth/signup`
- Body:
```json
{
  "username": "testuser123",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```
- Response includes JWT token (auto-saved to `jwt_token` variable)

#### 2. Login
- Request: **POST** `/api/auth/login`
- Body:
```json
{
  "username": "testuser123",
  "password": "SecurePass123!"
}
```
- Response includes JWT token and user roles

#### 3. Voice Processing Endpoints
- **Voice Test - Male to Female (901)**
- **Voice Test - Female to Male (902)**
- **Voice Test - Robot Voice (903)**
- **Process Audio - Custom Parameters**
- **Process Live Audio**

---

## API Endpoints

### Authentication (No Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Voice Processing (No Auth Required)

| Method | Endpoint | Description | Code |
|--------|----------|-------------|------|
| POST | `/api/voiceTest` | Male to Female | 901 |
| POST | `/api/voiceTest` | Female to Male | 902 |
| POST | `/api/voiceTest` | Robot Voice | 903 |
| POST | `/api/process` | Custom parameters | - |
| POST | `/api/process-live` | Real-time processing | - |

---

## User Registration Flow

When a user registers:

1. **Database**: User record created with ROLE_USER
2. **FreeSWITCH**: XML config file created at `/usr/local/freeswitch/conf/directory/default/{username}.xml`
3. **FreeSWITCH**: Executes `fs_cli -x "reloadxml"` to reload configuration
4. **Response**: Returns JWT token and user information

### FreeSWITCH Extension Details
- **Extension ID**: Same as username
- **SIP Password**: `humayun200044` (configurable in application.properties)
- **Voicemail Password**: Same as username
- **Context**: default

---

## Sample Responses

### Signup Success
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "testuser123",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["ROLE_USER"],
  "createdAt": "2025-12-20T10:30:00",
  "message": "User registered successfully"
}
```

### Login Success
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "testuser123",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["ROLE_USER"],
  "createdAt": "2025-12-20T10:30:00",
  "message": "Login successful"
}
```

---

## Creating Admin Users

Admin users must be created manually (cannot register via API):

```sql
-- Create admin user (password: 'password123')
INSERT INTO users (username, password, first_name, last_name, enabled)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', 1);

-- Assign ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT id, 1 FROM users WHERE username = 'admin';
```

---

## Troubleshooting

### Database Connection Issues

Check credentials in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/magic_call
spring.datasource.username=tbuser
spring.datasource.password=Takay1takaane$
```

### FreeSWITCH Config Not Created

Check permissions:
```bash
ls -la /usr/local/freeswitch/conf/directory/default/
```

Ensure application has write access to the directory.

### JWT Token Expired

Login again to get a new token. Token expires after 24 hours (configurable in `application.properties`).

---

## Security Notes

1. Change default passwords in production
2. Use strong JWT secret key
3. Enable HTTPS for production
4. Regularly backup database
5. Monitor failed login attempts
6. Keep dependencies updated

---

## Support

For issues, check application logs:
```bash
tail -f logs/application.log
```
