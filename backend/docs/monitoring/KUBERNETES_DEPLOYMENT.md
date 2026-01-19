# Kubernetes Deployment Guide

## Health Checks Configuration

This application provides health check endpoints suitable for Kubernetes liveness and readiness probes.

### Liveness Probe

The liveness probe determines if the container is running. If it fails, Kubernetes will restart the container.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: erm-backend
spec:
  template:
    spec:
      containers:
      - name: erm-backend
        image: erm-backend:latest
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
          successThreshold: 1
```

### Readiness Probe

The readiness probe determines if the container is ready to accept traffic. If it fails, Kubernetes will remove the pod from service endpoints.

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
  successThreshold: 1
```

### Startup Probe

For applications with slow startup, use a startup probe to give the application more time to start.

```yaml
startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 0
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 30  # Allow up to 5 minutes for startup
  successThreshold: 1
```

## Complete Deployment Example

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: erm-backend
  labels:
    app: erm-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: erm-backend
  template:
    metadata:
      labels:
        app: erm-backend
    spec:
      containers:
      - name: erm-backend
        image: erm-backend:latest
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: erm-secrets
              key: jwt-secret
        - name: DB_URL
          valueFrom:
            configMapKeyRef:
              name: erm-config
              key: db-url
        - name: DB_USER
          valueFrom:
            secretKeyRef:
              name: erm-secrets
              key: db-user
        - name: DB_PASS
          valueFrom:
            secretKeyRef:
              name: erm-secrets
              key: db-password
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 0
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 30
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
---
apiVersion: v1
kind: Service
metadata:
  name: erm-backend
spec:
  selector:
    app: erm-backend
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
  type: ClusterIP
```

## Health Check Endpoints

### Liveness Endpoint

- **Path:** `/actuator/health/liveness`
- **Purpose:** Indicates if the application is alive
- **Returns:** `UP` or `DOWN`
- **Use Case:** Kubernetes will restart the container if this fails

### Readiness Endpoint

- **Path:** `/actuator/health/readiness`
- **Purpose:** Indicates if the application is ready to serve traffic
- **Returns:** `UP` or `DOWN`
- **Use Case:** Kubernetes will remove the pod from service if this fails

### Full Health Endpoint

- **Path:** `/actuator/health`
- **Purpose:** Complete health status with component details
- **Returns:** Detailed health information
- **Use Case:** Monitoring and debugging

## Monitoring Integration

### Prometheus ServiceMonitor

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: erm-backend
  labels:
    app: erm-backend
spec:
  selector:
    matchLabels:
      app: erm-backend
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
    bearerTokenSecret:
      name: prometheus-token
      key: token
```

### Grafana Dashboard

Import metrics from Prometheus:
- HTTP request metrics
- JVM memory usage
- Database connection pool
- CPU and memory usage

## Best Practices

1. **Liveness Probe:**
   - Use `/actuator/health/liveness`
   - Set appropriate `initialDelaySeconds` (60s recommended)
   - Don't make it too aggressive (periodSeconds: 10s)

2. **Readiness Probe:**
   - Use `/actuator/health/readiness`
   - Check database connectivity
   - Set shorter intervals (periodSeconds: 5s)

3. **Startup Probe:**
   - Use for slow-starting applications
   - Allow sufficient time for startup (failureThreshold: 30)
   - Prevents premature restarts

4. **Resource Limits:**
   - Set appropriate memory and CPU limits
   - Monitor actual usage and adjust

5. **Environment Variables:**
   - Use Secrets for sensitive data (JWT_SECRET, DB_PASS)
   - Use ConfigMaps for non-sensitive configuration

## Troubleshooting

### Pod Keeps Restarting

**Check liveness probe:**
```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

**Verify health endpoint:**
```bash
kubectl exec -it <pod-name> -- curl http://localhost:8080/actuator/health/liveness
```

### Pod Not Receiving Traffic

**Check readiness probe:**
```bash
kubectl get endpoints erm-backend
kubectl describe pod <pod-name>
```

**Verify readiness endpoint:**
```bash
kubectl exec -it <pod-name> -- curl http://localhost:8080/actuator/health/readiness
```

### Health Endpoint Returns 503

**Possible causes:**
- Database connection failed
- Disk space below threshold
- Application startup incomplete

**Debug:**
```bash
# Check full health status
kubectl exec -it <pod-name> -- curl http://localhost:8080/actuator/health

# Check application logs
kubectl logs <pod-name> | grep -i health
```

## References

- [Kubernetes Probes Documentation](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/)
- [Spring Boot Actuator Health](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health)
