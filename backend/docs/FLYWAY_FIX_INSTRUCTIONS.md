# Fixing Flyway Checksum Mismatches

## Current Issue

You're encountering checksum mismatches for migrations V2, V3, and V4. This happens when migration files are modified after they've been applied to the database.

## Quick Fix (Development Only)

### Option 1: Use the Repair Script (Recommended)

**Linux/Mac:**
```bash
chmod +x scripts/fix-flyway-checksums.sh
./scripts/fix-flyway-checksums.sh
```

**Windows PowerShell:**
```powershell
.\scripts\fix-flyway-checksums.ps1
```

### Option 2: Manual Repair via Environment Variable

```bash
# Linux/Mac
export FLYWAY_REPAIR_ON_MIGRATE=true
docker compose -f docker/docker-compose.dev.yml up --build

# Windows PowerShell
$env:FLYWAY_REPAIR_ON_MIGRATE="true"
docker compose -f docker/docker-compose.dev.yml up --build
```

**Important:** After successful repair, remove `FLYWAY_REPAIR_ON_MIGRATE=true` or set it back to `false` for future runs.

### Option 3: Reset Database (Loses All Data - Dev Only)

If you don't need to preserve data:

```bash
docker compose -f docker/docker-compose.dev.yml down -v
docker compose -f docker/docker-compose.dev.yml up --build
```

## Verification

After running the repair, verify it worked:

```bash
# Check application logs
docker compose -f docker/docker-compose.dev.yml logs app | grep -i flyway

# You should see:
# ✅ Flyway checksums repaired successfully.
# ✅ Migrating schema "erm" to version X
```

## What Happens During Repair

1. Flyway detects checksum mismatches
2. With `FLYWAY_REPAIR_ON_MIGRATE=true`, it updates the checksums in the `flyway_schema_history` table
3. Migrations proceed normally after repair

## Prevention

To prevent this in the future:

1. **Never modify existing migration files** after they've been applied
2. **Create new migrations** for any schema changes
3. **Test migrations locally** before committing
4. **Review migration files** before merging PRs

See [MIGRATION_STRATEGY.md](database/MIGRATION_STRATEGY.md) for complete guidelines.

## Production Warning

⚠️ **NEVER use repair in production!**

If checksum mismatches occur in production:
1. **DO NOT** enable `FLYWAY_REPAIR_ON_MIGRATE=true`
2. **DO NOT** modify existing migration files
3. **DO** create a new migration to fix the issue
4. **DO** investigate why the mismatch occurred (security concern)
