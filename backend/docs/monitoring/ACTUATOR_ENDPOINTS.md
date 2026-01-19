# Spring Boot Actuator Endpoints

## Overview

Spring Boot Actuator provides production-ready features to help you monitor and manage your application. This document describes the available endpoints and how to use them.

## Available Endpoints

### Health Endpoint (`/actuator/health`)

**Access:** Public (no authentication required)  
**Purpose:** Application health status for Kubernetes liveness/readiness probes

**Response Examples:**

**Healthy:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500107862016,
        "free": 450107862016,
        "threshold": 10485760,
        "exists": true
      }
    },
    "application": {
      "status": "UP",
      "details": {
        "status": "Application is running"
      }
    }
  }
}
```

**Unhealthy:**
```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "Connection refused"
      }
    }
  }
}
```

**Kubernetes Probes:**
- **Liveness:** `/actuator/health/liveness`
- **Readiness:** `/actuator/health/readiness`

### Info Endpoint (`/actuator/info`)

**Access:** Public (no authentication required)  
**Purpose:** Application information

**Response:**
```json
{
  "app": {
    "name": "erm-backend",
    "version": "0.0.1-SNAPSHOT"
  }
}
```

**Configuration:** Add `info.*` properties to `application.properties` to customize:
```properties
info.app.name=ERM Backend
info.app.version=0.0.1-SNAPSHOT
info.app.description=Enterprise Resource Management Backend
```

### Metrics Endpoint (`/actuator/metrics`)

**Access:** Requires authentication  
**Purpose:** Application metrics

**Available Metrics:**
- `http.server.requests` - HTTP request metrics
- `jvm.memory.used` - JVM memory usage
- `jvm.gc.pause` - Garbage collection metrics
- `process.cpu.usage` - CPU usage
- `hikaricp.connections.active` - Database connection pool metrics

**Example:**
```bash
# List all metrics
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/metrics

# Get specific metric
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/metrics/http.server.requests
```

### Prometheus Endpoint (`/actuator/prometheus`)

**Access:** Requires authentication  
**Purpose:** Prometheus-compatible metrics export

**Example:**
```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/prometheus
```

**Response Format:**
```
# HELP http_server_requests_seconds HTTP server request duration
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",status="200",uri="/api/v1/..."} 42.0
http_server_requests_seconds_sum{method="GET",status="200",uri="/api/v1/..."} 1.234
```

## Configuration

### Endpoint Exposure

Configured in `application.properties`:

```properties
# Expose specific endpoints
management.endpoints.web.exposure.include=health,info,metrics,prometheus

# Health endpoint details (when-authorized shows details only to authenticated users)
management.endpoint.health.show-details=when-authorized

# Enable Kubernetes probes
management.endpoint.health.probes.enabled=true
```

### Security

- **Public Endpoints:** `/actuator/health`, `/actuator/health/**`, `/actuator/info`
- **Protected Endpoints:** `/actuator/metrics`, `/actuator/prometheus`, and all other endpoints

Protected endpoints require JWT authentication.

## Usage Examples

### Health Check (Public)

```bash
# Basic health check
curl http://localhost:8080/actuator/health

# Liveness probe (Kubernetes)
curl http://localhost:8080/actuator/health/liveness

# Readiness probe (Kubernetes)
curl http://localhost:8080/actuator/health/readiness
```

### Metrics (Authenticated)

```bash
# Get JWT token first
TOKEN=$(curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"password"}' \
  | jq -r '.accessToken')

# Get all metrics
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/actuator/metrics

# Get HTTP request metrics
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/actuator/metrics/http.server.requests

# Get JVM memory metrics
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/actuator/metrics/jvm.memory.used
```

### Prometheus Export (Authenticated)

```bash
# Export metrics for Prometheus
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/actuator/prometheus > metrics.txt
```

## Kubernetes Integration

### Liveness Probe

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Readiness Probe

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

## Monitoring Setup

### Prometheus Configuration

Add to `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'erm-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
    bearer_token: '<your-jwt-token>'
    # Or use basic auth if configured
    # basic_auth:
    #   username: 'admin'
    #   password: 'password'
```

### Grafana Dashboard

Import the following metrics:
- `http_server_requests_seconds` - Request duration
- `jvm_memory_used_bytes` - Memory usage
- `hikaricp_connections_active` - Database connections
- `process_cpu_usage` - CPU usage

## Health Indicators

### Built-in Indicators

1. **Database Health** (`db`)
   - Checks database connectivity
   - Status: UP if connection successful

2. **Disk Space Health** (`diskSpace`)
   - Checks available disk space
   - Threshold: 10GB (configurable)
   - Status: UP if free space > threshold

3. **Application Health** (`application`)
   - Custom application health
   - Can be extended for custom checks

### Custom Health Indicators

To add custom health indicators, implement `HealthIndicator`:

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check external service, cache, etc.
        if (isServiceAvailable()) {
            return Health.up()
                    .withDetail("service", "available")
                    .build();
        }
        return Health.down()
                .withDetail("service", "unavailable")
                .build();
    }
}
```

## Troubleshooting

### Health Endpoint Returns 503

**Possible Causes:**
1. Database connection failed
2. Disk space below threshold
3. Custom health indicator returned DOWN

**Solution:**
```bash
# Check detailed health status
curl http://localhost:8080/actuator/health

# Check application logs
docker compose -f docker/docker-compose.dev.yml logs app | grep -i health
```

### Metrics Endpoint Returns 401

**Cause:** Endpoint requires authentication

**Solution:**
```bash
# Include JWT token in request
curl -H "Authorization: Bearer <token>" http://localhost:8080/actuator/metrics
```

### Prometheus Can't Scrape Metrics

**Cause:** Authentication required

**Solution:**
- Configure Prometheus with bearer token
- Or create a dedicated service account with read-only access
- Or expose metrics endpoint with different security configuration

## Best Practices

1. **Health Endpoints:** Keep public for Kubernetes probes
2. **Metrics Endpoints:** Require authentication
3. **Details:** Show details only to authorized users
4. **Monitoring:** Set up alerts based on health status
5. **Metrics:** Export to Prometheus for long-term storage
6. **Custom Indicators:** Add indicators for critical dependencies

## References

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
