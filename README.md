# 🏢 Enterprise Resource Management (ERM)

Système de gestion des ressources d'entreprise full-stack développé avec Spring Boot et Angular.

## 📋 Table des matières

- [Structure du projet](#structure-du-projet)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Développement](#développement)
- [Déploiement](#déploiement)
- [Documentation](#documentation)

## 🏗️ Structure du projet

```
enterprise-resource-management/
├── backend/          # API Spring Boot (Java 17+)
├── frontend/         # Application Angular 17+
├── docker/           # Configuration Docker (stack unifiée)
├── docs/             # Documentation
└── shared/            # Code partagé (optionnel)
```

## 📦 Prérequis

### Backend
- Java 17 ou supérieur
- Maven 3.8+
- Docker (optionnel)

### Frontend
- Node.js 20+
- npm 9+

## 🚀 Installation

### 1. Cloner le repository

```bash
git clone https://github.com/votre-org/enterprise-resource-management.git
cd enterprise-resource-management
```

### 2. Backend

```bash
cd backend
./mvnw clean install
```

### 3. Frontend

```bash
cd frontend
npm install
```

## 💻 Développement

### Démarrer le backend

```bash
cd backend
./mvnw spring-boot:run
```

Le backend sera disponible sur `http://localhost:8080`

### Démarrer le frontend

```bash
cd frontend
npm start
```

Le frontend sera disponible sur `http://localhost:4200`

### Développement avec Docker

```bash
# Stack complète (recommandé) — projet Docker: erm-platform
cd docker
docker compose -f docker-compose.dev.yml up --build

# Backend seul
cd backend/docker
docker compose -f docker-compose.dev.yml up --build

# Frontend seul
cd frontend/docker
docker compose -f docker-compose.dev.yml up --build
```

**Convention de nommage (kebab-case):**

| Ressource | Nom |
|-----------|-----|
| Projet Compose | `erm-platform` |
| Conteneurs | `erm-front`, `erm-backend`, `erm-db`, `erm-pgadmin` |
| Images | `erm-front:latest`, `erm-backend:latest` |
| Réseau | `erm-platform-network` |
| Volumes | `erm-platform-pgdata`, `erm-platform-pgadmin-data` |

## 🧪 Tests

### Backend

```bash
cd backend
./mvnw test
```

### Frontend

```bash
cd frontend
npm test
```

## 🐳 Déploiement

### Production avec Docker

```bash
cd docker
cp env.example .env
# Edit .env: set POSTGRES_PASSWORD, JWT_SECRET, CORS_ALLOWED_ORIGINS
docker compose up --build
```

Compose waits for PostgreSQL and the backend health checks before starting dependent services. Verify backend health:

```bash
curl http://localhost:8080/actuator/health
```

### HTTPS (staging / production)

See [docker/README.md](./docker/README.md) for TLS setup with `docker-compose.tls.yml`:

```bash
cd docker/scripts
./generate-staging-certs.sh localhost   # or generate-staging-certs.ps1 on Windows
cd ..
# Set CORS_ALLOWED_ORIGINS=https://localhost in .env
docker compose -f docker-compose.yml -f docker-compose.tls.yml up --build
```

## 📚 Documentation

- [Guide Monorepo](./MONOREPO_GUIDE.md)
- [Architecture Backend](./backend/docs/architecture/)
- [Architecture Frontend](./frontend/doc/architecture/)
- [API Documentation](./backend/docs/api/)

## 🔐 Identifiants par défaut (développement uniquement)

Ces identifiants proviennent des données de démo Flyway (profil `dev` uniquement) :

- **Email:** `admin@demo.com`
- **Password:** `password123`

⚠️ **Production:** `ERM_DEMO_DATA_ENABLED=false` par défaut dans `docker/env.example`. Ne réutilisez jamais ces identifiants en production.

## 🛠️ Technologies

### Backend
- Spring Boot 3.x
- Spring Security (JWT)
- PostgreSQL
- Maven

### Frontend
- Angular 17+
- TailwindCSS
- RxJS
- Chart.js

## 📝 License

[Votre licence ici]

## 👥 Contribution

Voir les guidelines dans la documentation respective de chaque module.

## 📞 Support

Pour toute question, ouvrez une issue sur GitHub.
