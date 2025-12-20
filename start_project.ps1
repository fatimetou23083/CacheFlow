# Startup Script for CacheFlow

Write-Host "Checking Docker status..."
$dockerStatus = docker info 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Docker is not running! Please start Docker Desktop and try again."
    exit 1
}
Write-Host "Docker is up."

Write-Host "Starting Database Services (Redis & MongoDB)..."
docker compose up -d
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to start Docker Compose services."
    exit 1
}

Write-Host "Waiting for services to initialize..."
Start-Sleep -Seconds 5

Write-Host "Starting Backend..."
cd backend
./mvnw spring-boot:run
