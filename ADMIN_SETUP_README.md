# Admin Setup Guide

## Problem
If you're getting "Invalid username or password" when trying to login as admin, use this guide to fix it.

## Solution 1: Use the Setup Web Page (Easiest)

1. Start your Spring Boot application:
   ```bash
   cd /home/prototype/Downloads/VoicechnagerBackend
   mvn spring-boot:run
   ```

2. Open your browser and go to:
   ```
   http://192.168.0.103:8080/setup.html
   ```

3. Click "Check if Admin Exists" to see if admin user is already created

4. Fill in the form (defaults are already provided):
   - Username: `admin`
   - Password: `admin123`
   - First Name: `Admin`
   - Last Name: `User`

5. Click "Create/Update Admin User"

6. Once successful, go to `http://192.168.0.103:8080/index.html` and login with:
   - Username: `admin`
   - Password: `admin123`

---

## Solution 2: Use curl Commands

### Check if Admin Exists
```bash
curl -X GET http://192.168.0.103:8080/api/setup/test-admin
```

### Create/Update Admin User
```bash
curl -X POST http://192.168.0.103:8080/api/setup/create-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "firstName": "Admin",
    "lastName": "User"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Admin user created/updated successfully",
  "username": "admin",
  "password": "admin123",
  "roles": ["ROLE_ADMIN", "ROLE_USER"],
  "passwordHash": "$2a$10$..."
}
```

### Test Login After Creating Admin
```bash
curl -X POST http://192.168.0.103:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "firstName": "Admin",
  "lastName": "User",
  "roles": ["ROLE_ADMIN", "ROLE_USER"],
  "createdAt": "2025-12-21T00:00:00",
  "message": "Login successful"
}
```

---

## Solution 3: Direct Database Update

If the API is not working, you can update the database directly:

```bash
mysql -u tbuser -p'Takay1takaane$' magic_call -e "
UPDATE users
SET password = '\$2a\$10\$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7q5kQjL86'
WHERE username = 'admin';
"
```

Then restart your Spring Boot application.

---

## Solution 4: Restore Database from Backup

The database backup file now includes the correct admin user. To restore:

```bash
mysql -u tbuser -p'Takay1takaane$' < src/main/resources/database/magic_call_backup.sql
```

This will create:
- **Admin user**: username=`admin`, password=`admin123`
- **Test user**: username=`testuser`, password=`password123`

---

## Admin Panel Features

Once logged in, you can access:

- **Dashboard**: http://192.168.0.103:8080/admin/dashboard.html
- **Package Management**: http://192.168.0.103:8080/admin/packages.html
- **Voice Types**: http://192.168.0.103:8080/admin/voice-types.html
- **Purchase History**: http://192.168.0.103:8080/admin/purchases.html
- **User Management**: http://192.168.0.103:8080/admin/users.html

---

## Troubleshooting

### Issue: Cannot access setup page
**Solution**: Make sure the application is running and accessible at port 8080

### Issue: Still getting invalid credentials
**Solution**:
1. Use the setup page to recreate the admin user
2. Clear your browser cache
3. Try logging in again

### Issue: Database connection error
**Solution**:
1. Check MySQL is running: `systemctl status mysql`
2. Verify credentials in `src/main/resources/application.properties`
3. Test connection: `mysql -u tbuser -p'Takay1takaane$' magic_call`

---

## Default Credentials

After setup, these are the default credentials:

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin123 | ROLE_ADMIN, ROLE_USER |
| testuser | password123 | ROLE_USER |

**⚠️ Security Warning**: Change these passwords in production!
