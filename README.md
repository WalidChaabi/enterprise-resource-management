# Enterprise Resource Management (ERM)

[![CI](https://github.com/WalidChaabi/enterprise-resource-management/actions/workflows/ci.yml/badge.svg)](https://github.com/WalidChaabi/enterprise-resource-management/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Multi-tenant business management platform covering employees, projects, time tracking, leave and reporting.

**Author:** [Walid Chaabi](https://github.com/WalidChaabi)  
**Stack:** Spring Boot 4.0.1 · Java 17 · Angular 19 · PostgreSQL · Docker

> Portfolio project with frontend hardening in progress. See the [implementation roadmap](docs/PORTFOLIO_IMPLEMENTATION_ROADMAP.md) for release scope and remaining work.

## Project focus

- Organization-scoped authorization using JWT and cross-tenant security tests.
- Employee, project, time-tracking and leave-management workflows.
- Authentication features including MFA and SSO.
- Reporting dashboards, audit events and webhooks.
- Angular frontend with shared list/table components and English/French support.

## Technical decisions

- Separate the Spring Boot API and Angular client so authorization and business rules are enforced in the backend.
- Use Flyway migrations to version database changes.
- Include cross-tenant tests to exercise organization boundaries.
- Use Docker Compose and health checks to coordinate service startup.
- Generate coverage with JaCoCo rather than present a manually maintained coverage percentage. The Maven configuration defines a 35% line-coverage check with explicit exclusions; run verify to check the current result.

## Repository structure

```text
enterprise-resource-management/
├── backend/          # Spring Boot API
├── frontend/         # Angular client
├── docker/           # Unified Docker configuration
├── docs/             # Project documentation
└── shared/           # Optional shared resources
```

## Prerequisites

- Java 17 and Maven 3.8+ for the backend.
- Node.js 20+ and npm 9+ for the frontend.
- PostgreSQL, or Docker for the development stack.

Framework versions above match backend/pom.xml and frontend/package.json.

## Quick start

```bash
git clone https://github.com/WalidChaabi/enterprise-resource-management.git
cd enterprise-resource-management/docker
docker compose -f docker-compose.dev.yml up --build
```

Review the [Docker guide](docker/README.md) for environment configuration.

| Service | Local URL |
|---------|-----------|
| Frontend | http://localhost:4200 |
| Backend | http://localhost:8080 |
| Backend health | http://localhost:8080/actuator/health |

### Run services separately

Configure the database and environment using the module documentation first.

Backend:

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

Frontend, in a separate terminal:

```bash
cd frontend
npm install
npm start
```

## Tests and coverage

Backend tests:

```bash
cd backend
./mvnw test
```

Coverage report: backend/target/site/jacoco/index.html after tests. To enforce the configured coverage check:

```bash
./mvnw verify
```

Frontend tests:

```bash
cd frontend
npm test
```

Inspect the [CI workflow](.github/workflows/ci.yml) and its run results for build verification. Feature descriptions are not a claim that every workflow is production-ready.

## Deployment configuration

```bash
cd docker
cp env.example .env
# Configure POSTGRES_PASSWORD, JWT_SECRET and CORS_ALLOWED_ORIGINS.
docker compose up --build
```

Compose waits for PostgreSQL and backend health checks before starting dependent services.

```bash
curl http://localhost:8080/actuator/health
```

For HTTPS configuration, see the [Docker guide](docker/README.md):

```bash
cd docker/scripts
./generate-staging-certs.sh localhost
cd ..
# Set CORS_ALLOWED_ORIGINS=https://localhost in .env.
docker compose -f docker-compose.yml -f docker-compose.tls.yml up --build
```

Use generate-staging-certs.ps1 on Windows.

### Demo data

Use demo data only in a local development environment. ERM_DEMO_DATA_ENABLED=false is the production default in docker/env.example. Configure credentials locally and do not reuse development accounts in production.

## Documentation

- [Monorepo guide](MONOREPO_GUIDE.md)
- [Backend architecture](backend/docs/architecture/)
- [Frontend architecture](frontend/doc/architecture/)
- [API documentation](backend/docs/api/)
- [Implementation roadmap](docs/PORTFOLIO_IMPLEMENTATION_ROADMAP.md)

## Contributing

See the module documentation for development guidelines. For questions or bugs, open a GitHub issue.

## License

MIT — see [LICENSE](LICENSE).
