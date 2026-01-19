#!/bin/bash
# Script to fix Flyway checksum mismatches in development
# Usage: ./scripts/fix-flyway-checksums.sh

set -e

echo "🔧 Fixing Flyway checksum mismatches..."
echo ""
echo "This script will repair Flyway checksums for migrations that have been modified."
echo "⚠️  WARNING: This should ONLY be used in development environments!"
echo ""

# Check if we're in dev mode
if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
    export SPRING_PROFILES_ACTIVE=dev
fi

if [[ "$SPRING_PROFILES_ACTIVE" != *"dev"* ]]; then
    echo "❌ ERROR: This script should only be run in development mode!"
    echo "   Current profile: $SPRING_PROFILES_ACTIVE"
    exit 1
fi

echo "✅ Development mode detected. Proceeding with repair..."
echo ""

# Option 1: Use Docker Compose with repair enabled
echo "Option 1: Using Docker Compose with FLYWAY_REPAIR_ON_MIGRATE=true"
echo "Running: docker compose -f docker/docker-compose.dev.yml down"
docker compose -f docker/docker-compose.dev.yml down

echo ""
echo "Starting with repair enabled..."
FLYWAY_REPAIR_ON_MIGRATE=true docker compose -f docker/docker-compose.dev.yml up --build -d

echo ""
echo "✅ Flyway repair completed!"
echo ""
echo "Next steps:"
echo "1. Check logs: docker compose -f docker/docker-compose.dev.yml logs app | grep -i flyway"
echo "2. Verify application started: docker compose -f docker/docker-compose.dev.yml ps"
echo "3. After successful repair, you can remove FLYWAY_REPAIR_ON_MIGRATE=true from future runs"
echo ""
