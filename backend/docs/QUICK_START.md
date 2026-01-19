# Quick Start Guide

## First Time Setup

### 1. Set Up Environment Variables

Copy the example environment file and configure it:

```bash
# Linux/Mac
cp env.example .env

# Windows PowerShell
Copy-Item env.example .env
```

Edit `.env` and set the required variables:

```bash
# REQUIRED: Generate a secure JWT secret (minimum 32 characters)
# Generate with: openssl rand -base64 32
JWT_SECRET=your-generated-secret-here-minimum-32-characters

# Database configuration (defaults are fine for local dev)
DB_URL=jdbc:postgresql://db:5432/erm_db
DB_USER=erm_user
DB_PASS=erm_pass
DB_SCHEMA=erm
```

**Important:** Never commit `.env` to version control!

### 2. Fix Flyway Checksum Issues (If Needed)

If you encounter checksum mismatch errors when starting the application, fix them using one of these methods:

#### Option A: Using the Repair Script (Recommended)

**Linux/Mac:**
```bash
chmod +x scripts/fix-flyway-checksums.sh
./scripts/fix-flyway-checksums.sh
```

**Windows PowerShell:**
```powershell
.\scripts\fix-flyway-checksums.ps1
```

#### Option B: Manual Repair via Docker Compose

```bash
# Set environment variable and start
export FLYWAY_REPAIR_ON_MIGRATE=true  # Linux/Mac
# or
$env:FLYWAY_REPAIR_ON_MIGRATE="true"  # Windows PowerShell

docker compose -f docker/docker-compose.dev.yml up --build
```

After successful repair, **remove** `FLYWAY_REPAIR_ON_MIGRATE=true` from future runs.

#### Option C: Reset Database (Dev Only - Loses All Data)

```bash
# Drop and recreate database
docker compose -f docker/docker-compose.dev.yml down -v
docker compose -f docker/docker-compose.dev.yml up --build
```

### 3. Start the Application

```bash
# Make sure JWT_SECRET is set
export JWT_SECRET=$(openssl rand -base64 32)  # Linux/Mac
# or use the value from your .env file

# Start all services
docker compose -f docker/docker-compose.dev.yml up --build
```

The application will be available at:
- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **pgAdmin:** http://localhost:5050

### 4. Verify Installation

1. **Check application logs:**
   ```bash
   docker compose -f docker/docker-compose.dev.yml logs app
   ```

2. **Verify health:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **Test authentication:**
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@demo.com","password":"password"}'
   ```

## Default Test Credentials

After running migrations, you can use:

- **Email:** admin@demo.com
- **Password:** password

## Troubleshooting

### Application Won't Start - JWT_SECRET Error

**Error:** `JWT secret is required`

**Solution:**
```bash
# Generate and set JWT_SECRET
export JWT_SECRET=$(openssl rand -base64 32)
# Or add it to your .env file
```

### Flyway Checksum Mismatch

**Error:** `Migration checksum mismatch for migration version X`

**Solution:** See "Fix Flyway Checksum Issues" section above.

### Database Connection Failed

**Error:** `Connection refused` or `Database not found`

**Solution:**
```bash
# Check if database container is running
docker compose -f docker/docker-compose.dev.yml ps

# Restart database
docker compose -f docker/docker-compose.dev.yml restart db

# Check database logs
docker compose -f docker/docker-compose.dev.yml logs db
```

## Next Steps

- Read [MIGRATION_STRATEGY.md](database/MIGRATION_STRATEGY.md) for database migration guidelines
- Read [SECURITY_FIXES.md](SECURITY_FIXES.md) for security configuration details
- Review [ARCHITECTURAL_REVIEW.md](architecture/ARCHITECTURAL_REVIEW.md) for project overview
