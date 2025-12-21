# Database Automation Setup

## Automatic Database Table Creation

The Magic Call application is configured to **automatically create all required database tables** when you run the application for the first time.

### How It Works

1. **Automatic Table Creation**:
   - Spring Boot JPA/Hibernate automatically creates missing tables based on your entity classes
   - Configured with `spring.jpa.hibernate.ddl-auto=update`
   - Updates existing tables with new columns when entities are modified
   - **Never drops existing tables or data**

2. **Automatic Data Initialization**:
   - Default roles and voice types are automatically inserted on startup
   - Uses `data.sql` script with `INSERT IGNORE` to prevent duplicates
   - Runs only once after table creation

### Configuration

Located in `src/main/resources/application.properties`:

```properties
# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Database Initialization
spring.sql.init.mode=always
spring.sql.init.continue-on-error=true
spring.jpa.defer-datasource-initialization=true
```

### Default Data Inserted

The application automatically inserts:

**Roles:**
- ROLE_ADMIN (id: 1)
- ROLE_USER (id: 2)

**Voice Types:**
- Male Voice (code: 901)
- Female Voice (code: 902)
- Child Voice (code: 903)

### Database Tables Created

The following tables are automatically created:

1. **users** - User accounts
2. **user_details** - Extended user profile information
3. **user_roles** - User-Role mapping (junction table)
4. **roles** - User roles (ADMIN, USER)
5. **voice_types** - Available voice transformation types
6. **balances** - User balance tracking (duration in seconds)
7. **transactions** - Top-up transaction records
8. **call_history** - Call detail records (CDR) from FreeSWITCH

### First-Time Setup Instructions

1. **Create the database** (one-time manual step):
   ```sql
   CREATE DATABASE IF NOT EXISTS magic_call
   DEFAULT CHARACTER SET utf8mb4
   COLLATE utf8mb4_unicode_ci;
   ```

2. **Configure database credentials** in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/magic_call
   spring.datasource.username=tbuser
   spring.datasource.password=Takay1takaane$
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Tables and default data are automatically created!**

### Creating Admin User

After the application starts, create an admin user via the setup endpoint:

```bash
curl -X POST http://localhost:8080/api/setup/create-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "firstName": "Admin",
    "lastName": "User"
  }'
```

Or access: `http://localhost:8080/api/setup/create-admin` in your browser.

### Verification

Check if tables were created:

```sql
USE magic_call;
SHOW TABLES;

-- Check default data
SELECT * FROM roles;
SELECT * FROM voice_types;
```

### Adding New Tables

To add new tables:

1. Create a new entity class in `com.example.voicechanger.entity`
2. Restart the application
3. Hibernate will automatically create the new table

### Manual SQL Backup

A manual SQL backup file is available at:
- `src/main/resources/database/magic_call_backup.sql`

This can be used for:
- Setting up a new database manually
- Understanding the complete schema
- Disaster recovery

### Troubleshooting

**Tables not created?**
- Check database connection in application.properties
- Verify MySQL service is running
- Check logs for Hibernate errors

**Data not inserted?**
- Check `data.sql` file exists in `src/main/resources/`
- Verify `spring.sql.init.mode=always` is set
- Check logs for SQL execution errors

**Permission errors?**
- Ensure database user has CREATE, INSERT, UPDATE privileges
- Grant permissions: `GRANT ALL PRIVILEGES ON magic_call.* TO 'tbuser'@'localhost';`

### Production Recommendations

For production environments, consider:

1. **Change to validate mode**:
   ```properties
   spring.jpa.hibernate.ddl-auto=validate
   ```

2. **Use migration tools** like Flyway or Liquibase for controlled schema changes

3. **Disable SQL initialization**:
   ```properties
   spring.sql.init.mode=never
   ```

4. **Use proper backup and recovery strategies**

### Database Schema Updates

When you update entity classes:
- **Adding columns**: Automatically added on next startup
- **Renaming columns**: Manual migration required
- **Removing columns**: Not automatically removed (safety feature)
- **Changing column types**: May require manual migration

The `update` mode is safe for development and handles most schema changes automatically.
