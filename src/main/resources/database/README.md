# Magic Call Database & API Documentation

This directory contains database schemas, backup files, and API testing resources for the Magic Call voice changer system.

## Files

### Database Files
- **magic_call_backup.sql** - Complete database schema with all tables and sample data
- **DATABASE_SETUP.md** - Detailed database setup instructions (if exists)

### API Testing
- **Magic_Call_API.postman_collection.json** - Postman collection with all API endpoints for testing

---

## Quick Database Setup

### 1. Create Database and User

Run as MySQL root user:
```bash
sudo mysql
```

```sql
CREATE DATABASE IF NOT EXISTS magic_call CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tbuser'@'localhost' IDENTIFIED BY 'Takay1takaane$';
GRANT ALL PRIVILEGES ON magic_call.* TO 'tbuser'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Import Database Schema

```bash
mysql -u tbuser -p'Takay1takaane$' magic_call < src/main/resources/database/magic_call_backup.sql
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
| balances             |
| call_history         |
| roles                |
| transactions         |
| user_details         |
| user_roles           |
| users                |
| voice_purchases      |
| voice_types          |
| voice_user_mapping   |
+----------------------+
```

---

## Complete Database Schema

### Core User Tables

#### users
**Purpose**: Store user authentication and basic profile information

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| username | VARCHAR(50) | Unique username (also SIP extension ID) |
| password | VARCHAR(255) | BCrypt encrypted password |
| first_name | VARCHAR(100) | User's first name |
| last_name | VARCHAR(100) | User's last name |
| created_at | TIMESTAMP | Registration timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| enabled | TINYINT(1) | Account status (1=active, 0=disabled) |

**Indexes**: username, created_at

#### roles
**Purpose**: Define available system roles

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| name | VARCHAR(50) | Role name (ROLE_ADMIN, ROLE_USER) |

**Default Roles**:
- `ROLE_ADMIN` - Full system access
- `ROLE_USER` - User-level access

#### user_roles
**Purpose**: Junction table for many-to-many user-role relationship

| Field | Type | Description |
|-------|------|-------------|
| user_id | BIGINT | Foreign key to users.id |
| role_id | BIGINT | Foreign key to roles.id |

**Primary Key**: (user_id, role_id)

#### user_details
**Purpose**: Extended user profile information

| Field | Type | Description |
|-------|------|-------------|
| id_user_details | BIGINT | Primary key |
| id_user | BIGINT | Foreign key to users.id (unique) |
| date_of_birth | DATE | User's birth date |
| gender | VARCHAR(10) | User's gender |
| address | VARCHAR(500) | Physical address |
| email | VARCHAR(100) | Email address |
| profile_photo | VARCHAR(500) | Profile photo URL/path |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

**Indexes**: email

### Voice Management Tables

#### voice_types
**Purpose**: Define available voice transformation types

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| voice_name | VARCHAR(100) | Display name (e.g., "Male Voice") |
| code | VARCHAR(10) | FreeSWITCH code (e.g., "901") |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

**Default Voice Types**:
1. Male Voice (code: 901)
2. Female Voice (code: 902)
3. Child Voice (code: 903) - 3-day trial on signup
4. Robot Voice (code: 904) - Free permanent access on signup

**Indexes**: code

#### voice_user_mapping
**Purpose**: Map users to their assigned/purchased voice types

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| id_user | BIGINT | Foreign key to users.id |
| id_voice_type | BIGINT | Foreign key to voice_types.id |
| is_purchased | BIT(1) | false=free/trial, true=purchased |
| assigned_at | TIMESTAMP | When voice type was assigned |
| trial_expiry_date | DATETIME(6) | Trial expiration (null=no trial) |
| expiry_date | DATETIME(6) | Subscription expiration (null=permanent) |

**Unique Key**: (id_user, id_voice_type)
**Indexes**: id_user, id_voice_type, trial_expiry_date, expiry_date

**Note**:
- `trial_expiry_date` - Used for free trial access (e.g., 3-day trial)
- `expiry_date` - Used for subscription-based access (monthly/yearly)

#### voice_purchases
**Purpose**: Voice type purchase requests with admin approval workflow

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| id_user | BIGINT | Foreign key to users.id |
| id_voice_type | BIGINT | Foreign key to voice_types.id |
| id_transaction | BIGINT | Reference to transaction (optional) |
| transaction_method | VARCHAR(50) | Payment method (bkash/nagad/rocket) |
| tnx_id | VARCHAR(100) | Payment transaction ID |
| subscription_type | VARCHAR(20) | "monthly" or "yearly" |
| amount | DECIMAL(10,2) | Purchase amount |
| purchase_date | TIMESTAMP | Purchase request date |
| expiry_date | DATETIME(6) | When subscription expires |
| status | VARCHAR(20) | pending/approved/rejected |
| updated_at | TIMESTAMP | Last update timestamp |

**Indexes**: id_user, id_voice_type, status, purchase_date, tnx_id

**Status Flow**:
- `pending` - Awaiting admin approval
- `approved` - Approved, user granted access
- `rejected` - Rejected by admin

### Balance & Transaction Tables

#### balances
**Purpose**: Track user call time balance (in seconds)

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| id_user | BIGINT | Foreign key to users.id (unique) |
| purchase_amount | BIGINT | Total purchased duration (seconds) |
| last_used_amount | BIGINT | Last call duration (seconds) |
| total_used_amount | BIGINT | Total used duration (seconds) |
| remain_amount | BIGINT | Remaining duration (seconds) |

**Note**: All amounts are stored in **seconds**, not minutes or currency

#### transactions
**Purpose**: Top-up balance transactions with admin approval

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| id_user | BIGINT | Foreign key to users.id |
| transaction_method | VARCHAR(50) | Payment method (bKash/Nagad/Rocket) |
| amount | DECIMAL(10,2) | Transaction amount in BDT |
| tnx_id | VARCHAR(100) | Unique transaction ID from provider |
| date | TIMESTAMP | Transaction creation date |
| status | VARCHAR(20) | PENDING/SUCCESS/FAILED |
| updated_at | TIMESTAMP | Last update timestamp |

**Unique Key**: tnx_id
**Indexes**: tnx_id, date, status, id_user

### Call Tracking Tables

#### call_history
**Purpose**: Store CDR (Call Detail Records) from FreeSWITCH

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT | Primary key |
| aparty | VARCHAR(50) | Calling party (username) |
| bparty | VARCHAR(50) | Called party |
| uuid | VARCHAR(100) | FreeSWITCH call UUID (unique) |
| source_ip | VARCHAR(50) | Source IP address |
| create_time | TIMESTAMP | Call creation time |
| start_time | TIMESTAMP | Call answer time (null if not answered) |
| end_time | TIMESTAMP | Call end time (null if ongoing) |
| duration | BIGINT | Call duration in seconds |
| status | VARCHAR(20) | RESERVED/ANSWERED/COMPLETED/REJECTED/FAILED |
| hangup_cause | VARCHAR(50) | Hangup cause from FreeSWITCH |
| codec | VARCHAR(50) | Audio codec used |
| id_user | BIGINT | Foreign key to users.id (nullable) |

**Unique Key**: uuid
**Indexes**: aparty, uuid, create_time, status, id_user

**Call Status Flow**:
1. `RESERVED` - Call initiated, ringing
2. `ANSWERED` - Call answered
3. `COMPLETED` - Call ended normally
4. `REJECTED` - Call rejected by callee
5. `FAILED` - Call failed to connect

---

## Postman Collection Setup

### Import Collection

1. Open Postman
2. Click **Import** button
3. Select file: `Magic_Call_API.postman_collection.json`
4. Click **Import**

### Configure Environment Variables

Create a new environment with these variables:
- `base_url` - `http://localhost:8080`
- `jwt_token` - Auto-populated after login
- `username` - Auto-populated after login

### Authentication Flow

1. **Signup** - Register new user (auto-assigns default voice types)
2. **Login** - Get JWT token (saved automatically)
3. All subsequent requests use the token automatically

---

## API Endpoints by Category

### Authentication (No Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register new user + create FreeSWITCH extension |
| POST | `/api/auth/login` | Login and get JWT token |

### User Management

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users` | Admin | Get all users |
| GET | `/api/users/{id}` | Admin | Get user by ID |
| PUT | `/api/users/{id}` | Admin | Update user |
| DELETE | `/api/users/{id}` | Admin | Delete user |
| GET | `/api/user-details/my` | User | Get my details |
| PUT | `/api/user-details/my` | User | Update my details |

### Voice Types

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/voice-types` | Any | Get all voice types |
| POST | `/api/voice-types` | Admin | Create voice type |
| PUT | `/api/voice-types/{id}` | Admin | Update voice type |
| DELETE | `/api/voice-types/{id}` | Admin | Delete voice type |

### Voice Purchases

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/voice-purchase` | User | Request voice purchase (monthly/yearly) |
| GET | `/api/voice-purchase/my` | User | Get my purchase history |
| GET | `/api/voice-purchase` | Admin | Get all purchases |
| GET | `/api/voice-purchase/pending` | Admin | Get pending purchases |
| PUT | `/api/voice-purchase/{id}/approve` | Admin | Approve purchase |
| PUT | `/api/voice-purchase/{id}/reject` | Admin | Reject purchase |

### Voice Access

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/voice-purchase/my-voices` | User | Get all my voice types |
| GET | `/api/voice-purchase/my-active-voices` | User | Get active (non-expired) voices |
| GET | `/api/voice-purchase/available` | User | Get available voices for purchase |

### Balance Management

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/balance/my` | User | Get my balance |
| POST | `/api/balance/topup` | User | Request balance top-up |
| GET | `/api/balance/topup/pending` | Admin | Get pending top-up requests |
| PUT | `/api/balance/topup/{id}/approve` | Admin | Approve top-up |
| PUT | `/api/balance/topup/{id}/reject` | Admin | Reject top-up |

### Call History

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/call-history` | Admin | Get all call history |
| GET | `/api/call-history/my` | User | Get my call history |
| GET | `/api/call-history/user/{userId}` | Admin | Get call history by user |

### Transactions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/transactions` | Admin | Get all transactions |
| GET | `/api/transactions/my` | User | Get my transactions |
| PUT | `/api/transactions/{id}/status` | Admin | Update transaction status |

---

## Sample API Requests

### 1. User Registration

**Request:**
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john_doe",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["ROLE_USER"],
  "createdAt": "2025-12-21T10:30:00",
  "message": "User registered successfully"
}
```

**What Happens:**
1. User record created in database
2. Default voice types assigned (Child Voice - 3 day trial, Robot Voice - permanent)
3. FreeSWITCH SIP extension created at `/usr/local/freeswitch/conf/directory/default/john_doe.xml`
4. Balance record initialized
5. JWT token returned

### 2. Purchase Voice Type (Monthly Subscription)

**Request:**
```http
POST /api/voice-purchase
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "idVoiceType": 1,
  "transactionMethod": "bkash",
  "tnxId": "TXN123456789",
  "subscriptionType": "monthly"
}
```

**Response:**
```json
{
  "id": 1,
  "idUser": 2,
  "idVoiceType": 1,
  "transactionMethod": "bkash",
  "tnxId": "TXN123456789",
  "subscriptionType": "monthly",
  "amount": 50.00,
  "purchaseDate": "2025-12-21T10:30:00",
  "expiryDate": "2026-01-21T10:30:00",
  "status": "pending",
  "voiceType": {
    "id": 1,
    "voiceName": "Male Voice",
    "code": "901"
  }
}
```

**Subscription Types:**
- `monthly` - Access for 1 month (expiryDate = purchaseDate + 1 month)
- `yearly` - Access for 1 year (expiryDate = purchaseDate + 1 year)

### 3. Admin Approves Purchase

**Request:**
```http
PUT /api/voice-purchase/1/approve
Authorization: Bearer {admin_jwt_token}
```

**Response:**
```json
{
  "id": 1,
  "status": "approved",
  "updatedAt": "2025-12-21T11:00:00",
  ...
}
```

**What Happens:**
1. Purchase status changed to "approved"
2. `voice_user_mapping` record created/updated with:
   - `is_purchased` = true
   - `expiry_date` = purchase.expiryDate
   - `trial_expiry_date` = null
3. User can now use the voice type until expiry_date

### 4. Request Balance Top-Up

**Request:**
```http
POST /api/balance/topup
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "amount": 100.00,
  "transactionMethod": "nagad",
  "tnxId": "NAG987654321"
}
```

**Response:**
```json
{
  "id": 5,
  "idUser": 2,
  "amount": 100.00,
  "transactionMethod": "nagad",
  "tnxId": "NAG987654321",
  "status": "PENDING",
  "date": "2025-12-21T10:30:00"
}
```

---

## Default Accounts

### Admin Account
- **Username**: `admin`
- **Password**: `admin123`
- **Roles**: ROLE_ADMIN, ROLE_USER
- **Use**: Full system access, approve transactions, manage users

### Test User Account
- **Username**: `testuser`
- **Password**: `password123`
- **Roles**: ROLE_USER
- **Use**: Testing user-level features

**⚠️ SECURITY WARNING**: Change these passwords immediately in production!

---

## User Registration Flow

When a user registers (`POST /api/auth/signup`):

1. **Database Operations**:
   - User record created with ROLE_USER
   - Balance record initialized (all amounts = 0)
   - Voice Type 3 (Child Voice) assigned with 3-day trial
   - Voice Type 4 (Robot Voice) assigned permanently

2. **FreeSWITCH Integration**:
   - XML config file created: `/usr/local/freeswitch/conf/directory/default/{username}.xml`
   - SIP extension ID: same as username
   - SIP password: `humayun200044` (configurable)
   - Voicemail password: same as username
   - FreeSWITCH reloadxml executed

3. **Response**:
   - JWT token generated
   - User details returned
   - Token saved automatically (Postman)

---

## Voice Purchase & Subscription Lifecycle

### Purchase Flow
```
1. User: POST /api/voice-purchase (status: pending)
   ↓
2. Admin: PUT /api/voice-purchase/{id}/approve
   ↓
3. System: Creates voice_user_mapping with expiry_date
   ↓
4. User: Can use voice type until expiry_date
   ↓
5. After expiry: User needs to purchase again (renewal)
```

### Expiry Date Calculation
- **Monthly**: `expiryDate = purchaseDate + 1 month`
- **Yearly**: `expiryDate = purchaseDate + 1 year`

### Access Types

| Type | Field | Description | Example |
|------|-------|-------------|---------|
| Free Permanent | `trial_expiry_date=null`, `expiry_date=null` | Permanent free access | Robot Voice (904) |
| Trial | `trial_expiry_date!=null` | Time-limited trial | Child Voice 3-day trial |
| Subscription | `expiry_date!=null`, `is_purchased=true` | Paid subscription | Monthly/Yearly purchase |

---

## Payment Methods

| Method | Code | Description |
|--------|------|-------------|
| bKash | `bkash` | Mobile financial service (Bangladesh) |
| Nagad | `nagad` | Digital payment platform (Bangladesh) |
| Rocket | `rocket` | Mobile banking service (Bangladesh) |

---

## Troubleshooting

### Database Connection Failed

**Symptoms**: Application won't start, database errors in logs

**Solutions**:
1. Verify MySQL is running:
   ```bash
   sudo systemctl status mysql
   ```

2. Check database exists:
   ```bash
   mysql -u tbuser -p'Takay1takaane$' -e "SHOW DATABASES;"
   ```

3. Verify credentials in `application.properties`

4. Test connection:
   ```bash
   mysql -u tbuser -p'Takay1takaane$' magic_call -e "SHOW TABLES;"
   ```

### Tables Not Created

**Symptoms**: Empty database after import

**Solutions**:
1. Re-import schema:
   ```bash
   mysql -u tbuser -p'Takay1takaane$' magic_call < src/main/resources/database/magic_call_backup.sql
   ```

2. Check for errors during import

3. Verify user has permissions:
   ```sql
   SHOW GRANTS FOR 'tbuser'@'localhost';
   ```

### JWT Token Expired

**Symptoms**: 401 Unauthorized errors

**Solutions**:
1. Login again to get new token
2. Token expires after 24 hours (configurable in `application.properties`)
3. Check token expiration setting: `jwt.expiration=86400000` (milliseconds)

### FreeSWITCH Extension Not Created

**Symptoms**: User can't make SIP calls

**Solutions**:
1. Check directory permissions:
   ```bash
   ls -la /usr/local/freeswitch/conf/directory/default/
   ```

2. Ensure application has write access

3. Manually reload FreeSWITCH:
   ```bash
   fs_cli -x "reloadxml"
   ```

---

## Security Best Practices

1. **Change Default Passwords**
   - Update admin password immediately
   - Use strong passwords (12+ characters, mixed case, numbers, symbols)

2. **Secure JWT Secret**
   - Use long, random secret key
   - Never commit to version control
   - Rotate periodically

3. **Enable HTTPS**
   - Use SSL/TLS in production
   - Configure reverse proxy (nginx/Apache)

4. **Database Security**
   - Use strong database passwords
   - Limit network access to localhost or specific IPs
   - Regular backups

5. **Monitor Activity**
   - Review failed login attempts
   - Monitor admin actions
   - Track unusual transaction patterns

6. **Keep Updated**
   - Update dependencies regularly
   - Apply security patches
   - Monitor CVE databases

---

## Database Backup & Restore

### Create Backup
```bash
mysqldump -u tbuser -p'Takay1takaane$' magic_call > backup_$(date +%Y%m%d).sql
```

### Restore Backup
```bash
mysql -u tbuser -p'Takay1takaane$' magic_call < backup_20251221.sql
```

### Automated Backup (cron)
```bash
# Add to crontab (daily at 2 AM)
0 2 * * * mysqldump -u tbuser -p'Takay1takaane$' magic_call > /backups/magic_call_$(date +\%Y\%m\%d).sql
```

---

## Support

For database issues or API questions:
- Check application logs: `tail -f logs/application.log`
- Review this documentation
- Contact: **Humayun Ahmed**

---

## Additional Resources

- **Main README**: `/README.md` - Project overview and setup
- **Postman Collection**: Test all API endpoints
- **Application Properties**: Configuration reference
- **FreeSWITCH Docs**: Voice processing setup

---

Last Updated: 2025-12-21
