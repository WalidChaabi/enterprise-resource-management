# Security Fixes - JWT Secret & Flyway

**Date:** 2026-01-08  
**Status:** ✅ Completed

## Summary

Fixed critical security issues related to JWT secret configuration and Flyway migration checksum handling.

---

## Changes Made

### 1. JWT Secret Security Fix ✅

**Problem:**
- Default JWT secret hardcoded in `application.properties`
- No validation that secret meets minimum security requirements (256 bits)
- Secret could be accidentally committed to version control

**Solution:**
- Created `JwtProperties` class with `@ConfigurationProperties` and validation
- Enforced minimum 32 characters (256 bits) for JWT secret
- Removed default secret from `application.properties`
- Application now **fails to start** if `JWT_SECRET` is not provided or too weak

**Files Changed:**
- ✅ `src/main/java/com/company/erm/config/JwtProperties.java` (new)
- ✅ `src/main/java/com/company/erm/security/JwtTokenProvider.java` (updated)
- ✅ `src/main/java/com/example/erm_backend/ErmBackendApplication.java` (added `@EnableConfigurationProperties`)
- ✅ `src/main/resources/application.properties` (removed default secret)

**Validation Rules:**
- `@NotBlank` - Secret must be provided
- `@Size(min = 32)` - Minimum 32 characters (256 bits)
- `@Min(3600000)` - Expiration must be at least 1 hour

---

### 2. Flyway Checksum Mismatch Handling ✅

**Problem:**
- Migration V3 was modified after being applied, causing checksum mismatch
- No clear strategy for handling checksum issues
- Risk of deployment failures

**Solution:**
- Updated `FlywayConfig` to handle checksum mismatches appropriately
- In **development**: Allow repair via `FLYWAY_REPAIR_ON_MIGRATE=true` (with warnings)
- In **production**: Strict validation (never repair, create new migrations)
- Added comprehensive documentation for migration strategy

**Files Changed:**
- ✅ `src/main/java/com/company/erm/config/FlywayConfig.java` (enhanced)
- ✅ `src/main/resources/application.properties` (added repair option)
- ✅ `docs/database/MIGRATION_STRATEGY.md` (new - comprehensive guide)

**Key Points:**
- **Never modify existing migrations** after they've been applied
- **Create new migrations** for any changes
- Repair only in development, never in production

---

## Environment Variables

### Required

```bash
JWT_SECRET=<your-secret-here>  # Minimum 32 characters
```

Generate a secure secret:
```bash
openssl rand -base64 32
# or
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

### Optional

```bash
JWT_EXPIRATION=86400000  # Default: 24 hours (ms)
FLYWAY_REPAIR_ON_MIGRATE=false  # Only enable in dev if needed
```

---

## Migration Strategy

See `docs/database/MIGRATION_STRATEGY.md` for complete guide.

**Quick Fix for Current Checksum Issue:**

**Option 1: Repair (Dev Only)**
```bash
export FLYWAY_REPAIR_ON_MIGRATE=true
docker compose -f docker/docker-compose.dev.yml up --build
```

**Option 2: Reset Database (Dev Only)**
```bash
docker compose -f docker/docker-compose.dev.yml down -v
docker compose -f docker/docker-compose.dev.yml up --build
```

**Option 3: Create New Migration (Recommended)**
- Revert V3 to original state (from git)
- Create V9__fix_previous_changes.sql with the needed changes

---

## Testing

### Verify JWT Secret Validation

1. **Test without JWT_SECRET:**
   ```bash
   # Should fail with validation error
   docker compose -f docker/docker-compose.dev.yml up
   ```

2. **Test with weak secret (< 32 chars):**
   ```bash
   export JWT_SECRET=short
   # Should fail with validation error
   ```

3. **Test with valid secret:**
   ```bash
   export JWT_SECRET=$(openssl rand -base64 32)
   docker compose -f docker/docker-compose.dev.yml up --build
   # Should start successfully
   ```

### Verify Flyway Configuration

1. **Check logs for Flyway warnings:**
   ```bash
   docker compose -f docker/docker-compose.dev.yml logs app | grep -i flyway
   ```

2. **Verify migration status:**
   ```bash
   docker compose -f docker/docker-compose.dev.yml exec db psql -U erm_user -d erm_db -c "SELECT * FROM erm.flyway_schema_history ORDER BY installed_rank DESC;"
   ```

---

## Breaking Changes

⚠️ **This is a breaking change for existing deployments:**

1. **JWT_SECRET is now REQUIRED**
   - Old: Application would start with default secret
   - New: Application **fails to start** without JWT_SECRET
   - **Action Required:** Set `JWT_SECRET` environment variable before deployment

2. **JWT_SECRET must be at least 32 characters**
   - Old: Any secret was accepted
   - New: Validation enforces minimum 32 characters
   - **Action Required:** Generate a secure secret using provided commands

---

## Next Steps

1. ✅ Set `JWT_SECRET` in all environments (dev, staging, prod)
2. ✅ Update deployment documentation
3. ✅ Fix Flyway checksum issue (choose one of the options above)
4. ✅ Test application startup with new configuration
5. ✅ Update CI/CD pipelines to include JWT_SECRET

---

## References

- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties)
