# 🏗️ Guide: Organisation Monorepo - Best Practices

## ✅ Recommandation: **Un seul repo (Monorepo)**

Pour un projet ERP full-stack comme le vôtre, un **monorepo unique** est la meilleure approche.

## 📊 Comparaison des approches

### Option 1: Monorepo (✅ Recommandé)
```
enterprise-resource-management/
├── backend/
├── frontend/
├── docker/
├── docs/
├── .github/
│   └── workflows/
├── README.md
└── .gitignore
```

**Avantages:**
- ✅ **Cohérence des versions** : Backend et frontend évoluent ensemble
- ✅ **Atomic commits** : Un changement API + UI dans un seul commit
- ✅ **CI/CD simplifié** : Un seul pipeline qui teste/build les deux
- ✅ **Facilité de développement** : Clone une fois, tout est là
- ✅ **Partage de code** : Types TypeScript partagés, constants, etc.
- ✅ **Historique unifié** : Voir l'évolution complète du projet
- ✅ **Déploiement coordonné** : Versionner les releases ensemble

**Inconvénients:**
- ⚠️ Repo plus volumineux (mais Git gère bien)
- ⚠️ Nécessite une bonne organisation

### Option 2: Multi-repo (❌ Non recommandé pour votre cas)
```
enterprise-resource-management-backend/
enterprise-resource-management-frontend/
```

**Avantages:**
- ✅ Séparation claire
- ✅ Permissions différentes par repo

**Inconvénients:**
- ❌ Synchronisation des versions complexe
- ❌ Duplication de code (types, constants)
- ❌ CI/CD plus complexe
- ❌ Développement moins fluide
- ❌ Risque de désynchronisation

## 🎯 Structure recommandée

```
enterprise-resource-management/
├── .github/
│   └── workflows/
│       ├── backend-ci.yml
│       ├── frontend-ci.yml
│       └── docker-build.yml
├── backend/
│   ├── src/
│   ├── pom.xml (ou build.gradle)
│   ├── Dockerfile
│   └── README.md
├── frontend/
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── README.md
├── docker/
│   ├── docker-compose.yml
│   └── docker-compose.dev.yml
├── docs/
│   ├── architecture/
│   ├── api/
│   └── deployment/
├── shared/                    # Optionnel : code partagé
│   └── types/                 # Types TypeScript partagés
├── .gitignore
├── .gitattributes
├── README.md                  # README principal
└── LICENSE
```

## 📝 Fichiers essentiels à la racine

### `.gitignore` (racine)
```gitignore
# Backend
backend/target/
backend/.gradle/
backend/.idea/
backend/*.iml

# Frontend
frontend/node_modules/
frontend/dist/
frontend/.angular/

# IDEs
.idea/
.vscode/
*.swp
*.swo

# OS
.DS_Store
Thumbs.db

# Logs
*.log
logs/

# Environment
.env
.env.local
```

### `README.md` (racine)
```markdown
# Enterprise Resource Management

Système de gestion des ressources d'entreprise (ERP) full-stack.

## Structure

- `backend/` - API Spring Boot
- `frontend/` - Application Angular
- `docker/` - Configuration Docker
- `docs/` - Documentation

## Quick Start

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm start
```

## Documentation

Voir `docs/` pour plus de détails.
```

## 🔧 Configuration Git

### `.gitattributes` (optionnel mais recommandé)
```gitattributes
# Normaliser les fins de ligne
* text=auto

# Backend Java
*.java text eol=lf
*.xml text eol=lf
*.properties text eol=lf

# Frontend
*.ts text eol=lf
*.js text eol=lf
*.json text eol=lf
*.html text eol=lf
*.css text eol=lf
*.scss text eol=lf

# Binaires
*.jar binary
*.war binary
*.png binary
*.jpg binary
```

## 🚀 CI/CD avec GitHub Actions

### `.github/workflows/ci.yml`
```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  backend:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: ./backend
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build
        run: ./mvnw clean package -DskipTests
      - name: Test
        run: ./mvnw test

  frontend:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: ./frontend
    steps:
      - uses: actions/checkout@v3
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '20'
      - name: Install
        run: npm ci
      - name: Lint
        run: npm run lint
      - name: Build
        run: npm run build
      - name: Test
        run: npm test
```

## 📦 Partage de code (Optionnel mais recommandé)

### Créer `shared/types/` pour les types TypeScript

```typescript
// shared/types/api.types.ts
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
}

export interface Project {
  id: string;
  name: string;
  // ...
}
```

**Backend:** Générer ces types depuis les entités Java (via openapi-generator)

**Frontend:** Importer depuis `shared/types/`

## 🎯 Workflow de développement

1. **Feature branch:** `feature/user-management`
   - Modifier backend + frontend dans le même commit si nécessaire
   - Tests unitaires pour les deux

2. **Commit message:**
   ```
   feat: add user management
   
   - Backend: Add UserController and UserService
   - Frontend: Add user list and form components
   ```

3. **Pull Request:**
   - CI vérifie backend ET frontend
   - Review couvre les deux parties

## 🔐 Gestion des secrets

Utiliser GitHub Secrets pour:
- Variables d'environnement
- Clés API
- Credentials Docker

## 📈 Quand considérer un multi-repo?

Seulement si:
- ❌ Projets complètement indépendants
- ❌ Équipes séparées sans communication
- ❌ Déploiements à des rythmes très différents
- ❌ Codebases très volumineux (>100k lignes chacun)

**Pour votre cas ERP:** Monorepo est clairement la meilleure option.

## ✅ Checklist de migration

- [ ] Créer repo `enterprise-resource-management`
- [ ] Déplacer `backend/` et `frontend/` à la racine
- [ ] Créer `.gitignore` racine
- [ ] Créer `README.md` principal
- [ ] Configurer GitHub Actions
- [ ] Mettre à jour les chemins dans Docker
- [ ] Documenter la structure dans `docs/`
