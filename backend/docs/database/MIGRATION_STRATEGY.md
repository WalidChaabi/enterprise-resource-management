# Database Migration Strategy

## Overview

This project uses **Flyway** for database schema versioning and migrations. All migrations are located in `src/main/resources/db/migration/`.

## Migration Files

Migrations follow the naming convention: `V{version}__{description}.sql`

Example:
- `V1__init.sql` - Initial schema creation
- `V2__core_schema.sql` - Core tables
- `V3__seed_test_data.sql` - Test data

## Critical Rules

### ⚠️ **NEVER Modify Existing Migrations**

**Once a migration has been applied to any environment (especially production), it must NEVER be modified.**

**Why?**
- Flyway tracks checksums of applied migrations
- Modifying an existing migration causes checksum mismatches
- This breaks the migration history and can cause deployment failures

**What to do instead?**
- Create a **new migration** file with the next version number
- Example: If V3 needs changes, create `V9__fix_previous_issue.sql`

## Handling Checksum Mismatches

### In Development

If you encounter a checksum mismatch error:

```
Migration checksum mismatch for migration version 3
-> Applied to database : -131759667
-> Resolved locally    : -94227383
```

**Option 1: Repair (Quick Fix - Dev Only)**
```bash
# Set environment variable
export FLYWAY_REPAIR_ON_MIGRATE=true

# Or in docker-compose.dev.yml
environment:
  FLYWAY_REPAIR_ON_MIGRATE: "true"
```

**Option 2: Create New Migration (Recommended)**
1. Identify what changed in the modified migration
2. Create a new migration file (e.g., `V9__fix_previous_changes.sql`)
3. Apply the changes in the new migration
4. Revert the original migration file to its original state (from git)

**Option 3: Reset Database (Dev Only)**
```bash
# Drop and recreate the database
docker compose -f docker/docker-compose.dev.yml down -v
docker compose -f docker/docker-compose.dev.yml up --build
```

### In Production

**NEVER repair checksums in production!**

If a checksum mismatch occurs in production:
1. **DO NOT** enable `FLYWAY_REPAIR_ON_MIGRATE=true`
2. **DO NOT** modify existing migration files
3. **DO** create a new migration to fix the issue
4. **DO** investigate why the mismatch occurred (security concern)

## Migration Best Practices

### 1. **Idempotent Migrations**

Use `IF NOT EXISTS` and `ON CONFLICT DO NOTHING` where appropriate:

```sql
CREATE TABLE IF NOT EXISTS erm.example_table (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO erm.example_table (id, name)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Example')
ON CONFLICT (id) DO NOTHING;
```

### 2. **Backward Compatible Changes**

- Add columns as nullable first, then populate, then make NOT NULL
- Add indexes concurrently to avoid locking
- Use transactions for data migrations

### 3. **Test Migrations Locally**

Always test migrations in development before deploying:

```bash
# Test migration locally
docker compose -f docker/docker-compose.dev.yml up --build

# Verify schema
docker compose -f docker/docker-compose.dev.yml exec db psql -U erm_user -d erm_db -c "\dt erm.*"
```

### 4. **Review Before Merging**

- Review migration SQL carefully
- Ensure migrations are backward compatible
- Test rollback scenarios (if needed)

## Migration Workflow

### Creating a New Migration

1. **Identify the change needed**
   ```sql
   -- Example: Add a new column
   ALTER TABLE erm.users ADD COLUMN phone_number VARCHAR(20);
   ```

2. **Create migration file**
   - Find the highest version number: `V8__create_leave_requests_schema.sql`
   - Create: `V9__add_user_phone_number.sql`

3. **Write the migration**
   ```sql
   -- V9__add_user_phone_number.sql
   ALTER TABLE erm.users 
   ADD COLUMN phone_number VARCHAR(20);
   
   -- Add index if needed
   CREATE INDEX idx_users_phone_number 
   ON erm.users(phone_number);
   ```

4. **Test locally**
   ```bash
   docker compose -f docker/docker-compose.dev.yml down -v
   docker compose -f docker/docker-compose.dev.yml up --build
   ```

5. **Commit and deploy**
   - Commit the migration file
   - Deploy to staging first
   - Verify in staging
   - Deploy to production

## Troubleshooting

### Migration Fails to Apply

**Error:** `Migration checksum mismatch`

**Solution:**
- In DEV: Enable `FLYWAY_REPAIR_ON_MIGRATE=true` (one-time fix)
- In PROD: Create new migration, never repair

### Migration Takes Too Long

**Problem:** Large data migrations can lock tables

**Solution:**
- Use `CREATE INDEX CONCURRENTLY` for indexes
- Batch updates in smaller transactions
- Consider maintenance windows for large changes

### Rollback Needed

**Flyway doesn't support automatic rollbacks.**

**Manual Rollback:**
1. Create a new migration that reverses the changes
2. Example: If V9 added a column, V10 removes it
3. Apply the rollback migration

**Better Approach:**
- Test migrations thoroughly in dev/staging
- Use feature flags for risky changes
- Plan rollback strategy before deploying

## Current Migration Status

To check which migrations have been applied:

```sql
SELECT * FROM erm.flyway_schema_history 
ORDER BY installed_rank DESC;
```

## References

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway Best Practices](https://flywaydb.org/documentation/learnmore/repeatable)
- [PostgreSQL Migration Guide](https://www.postgresql.org/docs/current/ddl-alter.html)
