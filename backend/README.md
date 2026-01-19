# Enterprise Resource Management – Backend

Backend Spring Boot pour la gestion des ressources d’entreprise (organisations, départements, employés, rôles), conçu pour un usage **professionnel**, **dockerisé**, avec **hot reload en développement** et **build optimisé en production**.

---

## 🧱 Stack technique

- Java 17  
- Spring Boot  
- Spring Security (JWT / RBAC)  
- PostgreSQL 16  
- Docker / Docker Compose  
- Maven  
- pgAdmin  

---

## 📁 Structure Docker

```text
backend/
├── docker/
│   ├── Dockerfile.dev
│   ├── Dockerfile.prod
│   ├── docker-compose.dev.yml
│   └── docker-compose.prod.yml
├── src/
├── pom.xml
└── README.md
```

---

## 🔥 Mode Développement (Hot Reload)

### Objectif
- Modifier le code sans rebuild
- Redémarrage automatique de Spring Boot
- PostgreSQL et pgAdmin inclus

### Lancer le projet en DEV

**Important:** Définissez `JWT_SECRET` avant de lancer l'application :

```bash
# Option 1: Variable d'environnement
export JWT_SECRET=$(openssl rand -base64 32)
docker compose -f docker/docker-compose.dev.yml up --build

# Option 2: Fichier .env (recommandé)
cp env.example .env
# Éditez .env et définissez JWT_SECRET
docker compose -f docker/docker-compose.dev.yml up --build
```

**Note:** En développement, un secret par défaut est fourni dans `docker-compose.dev.yml`, mais il est recommandé d'utiliser votre propre secret.

**Si vous rencontrez des erreurs de checksum Flyway:**
```bash
# Option A: Utiliser le script de réparation (recommandé)
./scripts/fix-flyway-checksums.sh  # Linux/Mac
.\scripts\fix-flyway-checksums.ps1  # Windows

# Option B: Réparation manuelle
export FLYWAY_REPAIR_ON_MIGRATE=true
docker compose -f docker/docker-compose.dev.yml up --build
```

Voir [QUICK_START.md](docs/QUICK_START.md) pour plus de détails.

### Services exposés

| Service    | URL                   |
|------------|-----------------------|
| API        | http://localhost:8080 |
| pgAdmin    | http://localhost:5050 |
| PostgreSQL | localhost:5432        |

### Accès pgAdmin
- Email : admin@local.dev  
- Password : admin  

Configuration du serveur pgAdmin :

- Host     : db  
- Port     : 5432  
- User     : erm_user  
- Password : erm_pass  
- Database : erm_db  

### Hot Reload
- Code monté via volume Docker
- Commande `mvn spring-boot:run`
- Spring DevTools actif

Aucun rebuild Docker n’est nécessaire lors d’un changement de code.

---

## 🚀 Mode Production

### Objectif
- Image légère
- Build immuable
- Pas de Maven en runtime

### Lancer en PROD

```bash
docker compose -f docker/docker-compose.prod.yml up --build
```

### Détails
- Build multi-stage
- JAR généré par Maven
- Runtime basé sur `eclipse-temurin:17-jre-alpine`

---

## 🔐 Variables d’environnement

### Variables requises

**JWT_SECRET** (REQUIRED) - Clé secrète pour la signature JWT
- Minimum 32 caractères (256 bits) pour la sécurité
- Générer avec : `openssl rand -base64 32`
- **NE JAMAIS** utiliser la valeur par défaut en production
- Voir `env.example` pour un exemple complet

```env
# Base de données
DB_URL=jdbc:postgresql://db:5432/erm_db
DB_USER=erm_user
DB_PASS=erm_pass
DB_SCHEMA=erm

# JWT (REQUIRED)
JWT_SECRET=your-secret-key-minimum-32-characters-required
JWT_EXPIRATION=86400000  # 24 heures (optionnel)

# Application
SPRING_PROFILES_ACTIVE=dev|prod
SERVER_PORT=8080
```

**Note:** Copiez `env.example` vers `.env` et remplissez les valeurs. Voir `env.example` pour la liste complète des variables.

---

## 🧪 Sécurité & API

- Authentification JWT
- RBAC :
  - ORG_ADMIN
  - HR_ADMIN
  - DEPARTMENT_MANAGER
- Isolation multi-tenant par organizationId

Endpoints versionnés :
```text
/api/v1/organizations/{organizationId}/...
```

## 📊 Monitoring & Health Checks

- **Spring Boot Actuator** pour le monitoring
- **Health endpoints** publics pour Kubernetes probes
- **Metrics** disponibles via `/actuator/metrics`
- **Prometheus** export disponible

**Endpoints disponibles:**
- `/actuator/health` - Statut de santé (public)
- `/actuator/info` - Informations de l'application (public)
- `/actuator/metrics` - Métriques (authentification requise)
- `/actuator/prometheus` - Export Prometheus (authentification requise)

Voir [ACTUATOR_ENDPOINTS.md](docs/monitoring/ACTUATOR_ENDPOINTS.md) pour plus de détails.

---

## 🧠 Bonnes pratiques

- Séparation DEV / PROD
- Dockerfiles dédiés
- Images reproductibles
- Gestion globale des exceptions
- DTOs et mapping stricts
- Sécurité par annotations

## 🧪 Tests

- Tests unitaires pour les services critiques (AuthService, EmployeeService)
- Tests d'intégration pour les contrôleurs (AuthController, EmployeeController)
- Couverture de code avec JaCoCo (cible: 30%+ pour les chemins critiques)
- Base de données H2 en mémoire pour les tests

**Exécuter les tests:**
```bash
# Tous les tests
mvn test

# Avec rapport de couverture
mvn clean test jacoco:report
# Rapport: target/site/jacoco/index.html
```

Voir [TESTING_GUIDE.md](docs/testing/TESTING_GUIDE.md) pour plus de détails.

---

## 🧭 Commandes utiles

```bash
docker compose logs -f app
docker compose down
docker compose down -v
```

---

## 📌 Notes

- Dockerfile.dev uniquement en DEV
- Dockerfile.prod uniquement en PROD
- PostgreSQL persistant via volume Docker

---

## 👨‍💻 Auteur

Backend conçu avec une approche **enterprise-grade**, prêt pour CI/CD et déploiement cloud.
