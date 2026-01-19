# PowerShell script to fix Flyway checksum mismatches in development
# Usage: .\scripts\fix-flyway-checksums.ps1

Write-Host "🔧 Fixing Flyway checksum mismatches..." -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will repair Flyway checksums for migrations that have been modified." -ForegroundColor Yellow
Write-Host "⚠️  WARNING: This should ONLY be used in development environments!" -ForegroundColor Yellow
Write-Host ""

# Check if we're in dev mode
if (-not $env:SPRING_PROFILES_ACTIVE) {
    $env:SPRING_PROFILES_ACTIVE = "dev"
}

if ($env:SPRING_PROFILES_ACTIVE -notlike "*dev*") {
    Write-Host "❌ ERROR: This script should only be run in development mode!" -ForegroundColor Red
    Write-Host "   Current profile: $env:SPRING_PROFILES_ACTIVE" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Development mode detected. Proceeding with repair..." -ForegroundColor Green
Write-Host ""

# Stop existing containers
Write-Host "Stopping existing containers..." -ForegroundColor Cyan
docker compose -f docker/docker-compose.dev.yml down

Write-Host ""
Write-Host "Starting with repair enabled..." -ForegroundColor Cyan
$env:FLYWAY_REPAIR_ON_MIGRATE = "true"
docker compose -f docker/docker-compose.dev.yml up --build -d

Write-Host ""
Write-Host "✅ Flyway repair completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Check logs: docker compose -f docker/docker-compose.dev.yml logs app | Select-String -Pattern 'flyway'"
Write-Host "2. Verify application started: docker compose -f docker/docker-compose.dev.yml ps"
Write-Host "3. After successful repair, you can remove FLYWAY_REPAIR_ON_MIGRATE=true from future runs"
Write-Host ""
