param(
    [string]$Domain = "localhost"
)

$certDir = Resolve-Path (Join-Path $PSScriptRoot "..\certs")
New-Item -ItemType Directory -Force -Path $certDir | Out-Null

$certPath = Join-Path $certDir "tls.crt"
$keyPath = Join-Path $certDir "tls.key"

$openssl = Get-Command openssl -ErrorAction SilentlyContinue
if ($openssl) {
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 `
        -keyout $keyPath `
        -out $certPath `
        -subj "/CN=$Domain/O=ERM Staging/C=US"
} else {
    Write-Host "openssl not found locally; generating certs via Docker..."
    docker run --rm -v "${certDir}:/certs" alpine/openssl req -x509 -nodes -days 365 -newkey rsa:2048 `
        -keyout /certs/tls.key `
        -out /certs/tls.crt `
        -subj "/CN=$Domain/O=ERM Staging/C=US"
}

if (-not (Test-Path $certPath)) {
    Write-Error "Failed to create certificates in $certDir"
    exit 1
}

Write-Host "Created $certPath and $keyPath for CN=$Domain"
Write-Host "Set CORS_ALLOWED_ORIGINS=https://$Domain in docker/.env"
