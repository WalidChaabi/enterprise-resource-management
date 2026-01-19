# Project Management API - Guide de Test

## Informations de base

**Organization ID:** `550e8400-e29b-41d4-a716-446655440000` (Demo Organization)

**Base URL:** `http://localhost:8080`

**Authentification:** Tous les endpoints nécessitent un JWT token dans le header `Authorization: Bearer <token>`

---

## 1. Obtenir un token JWT (Login)

**Endpoint:** `POST /auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "email": "admin@demo.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "id": "770e8400-e29b-41d4-a716-446655440003",
  "email": "admin@demo.com",
  "firstName": "Admin",
  "lastName": "User",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["ORG_ADMIN", "HR_ADMIN"]
}
```

**Copier le `accessToken` pour les requêtes suivantes.**

---

## 2. Créer un projet (CREATE)

**Endpoint:** `POST /api/v1/organizations/{organizationId}/projects`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**URL:** `POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects`

**Body (projet simple):**
```json
{
  "name": "ERP Migration",
  "description": "Migration vers ERP v2",
  "startDate": "2026-02-01",
  "deadline": "2026-07-01",
  "budget": 150000.00,
  "managerId": null
}
```

**Body (avec manager):**
```json
{
  "name": "Website Redesign",
  "description": "Complete website redesign project",
  "startDate": "2026-03-01",
  "deadline": "2026-06-30",
  "budget": 50000.00,
  "managerId": "770e8400-e29b-41d4-a716-446655440003"
}
```

**Body (avec membres):**
```json
{
  "name": "Mobile App Development",
  "description": "Development of mobile application",
  "startDate": "2026-04-01",
  "deadline": "2026-09-30",
  "budget": 200000.00,
  "managerId": "770e8400-e29b-41d4-a716-446655440003",
  "memberIds": [
    "770e8400-e29b-41d4-a716-446655440004",
    "770e8400-e29b-41d4-a716-446655440005"
  ]
}
```

**Response (201 Created):**
```json
{
  "id": "e71442ce-daa3-43bc-a4e9-6a6150608626",
  "name": "ERP Migration",
  "status": "PLANNING",
  "startDate": "2026-02-01",
  "deadline": "2026-07-01",
  "managerName": null,
  "membersCount": 0
}
```

**Note:** Le statut par défaut est `PLANNING` lors de la création.

---

## 3. Lister les projets (READ - List)

**Endpoint:** `GET /api/v1/organizations/{organizationId}/projects`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects`

**Response (200 OK):**
```json
[
  {
    "id": "e71442ce-daa3-43bc-a4e9-6a6150608626",
    "name": "ERP Migration",
    "status": "PLANNING",
    "startDate": "2026-02-01",
    "deadline": "2026-07-01",
    "managerName": "Admin User",
    "membersCount": 3
  },
  {
    "id": "f82553df-ebb4-54cd-b5fa-7b7261719737",
    "name": "Website Redesign",
    "status": "ACTIVE",
    "startDate": "2026-03-01",
    "deadline": "2026-06-30",
    "managerName": "Project Manager",
    "membersCount": 5
  }
]
```

**Note:** Seuls les projets non archivés sont retournés.

---

## 4. Obtenir un projet par ID (READ - Get One)

**Endpoint:** `GET /api/v1/organizations/{organizationId}/projects/{projectId}`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId}`

**Response (200 OK):**
```json
{
  "id": "e71442ce-daa3-43bc-a4e9-6a6150608626",
  "name": "ERP Migration",
  "status": "PLANNING",
  "startDate": "2026-02-01",
  "deadline": "2026-07-01",
  "managerName": "Admin User",
  "membersCount": 3
}
```

**Note:** Retourne les détails d'un projet spécifique, même s'il est archivé.

---

## 5. Mettre à jour un projet (UPDATE)

**Endpoint:** `PUT /api/v1/organizations/{organizationId}/projects/{projectId}`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**URL:** `PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId}`

**Body:**
```json
{
  "name": "ERP Migration v2",
  "description": "Updated description for ERP migration",
  "status": "ACTIVE",
  "endDate": "2026-08-01",
  "cost": 120000.00
}
```

**Note:** Tous les champs sont optionnels. Seuls les champs fournis seront mis à jour.

**Response (200 OK):**
```json
{
  "id": "e71442ce-daa3-43bc-a4e9-6a6150608626",
  "name": "ERP Migration v2",
  "status": "ACTIVE",
  "startDate": "2026-02-01",
  "deadline": "2026-07-01",
  "managerName": "Admin User",
  "membersCount": 3
}
```

**Champs disponibles pour la mise à jour:**
- `name` - Nom du projet
- `description` - Description du projet
- `status` - Statut du projet (valeurs valides: `PLANNING`, `ACTIVE`, `ON_HOLD`, `COMPLETED`, `CANCELLED`)
  - ⚠️ **Important:** Utilisez `PLANNING` (pas `PLANNED`)
- `endDate` - Date de fin réelle du projet
- `cost` - Coût réel du projet

---

## 6. Archiver un projet (DELETE - Soft Delete)

**Endpoint:** `DELETE /api/v1/organizations/{organizationId}/projects/{projectId}`

**Permissions:** ORG_ADMIN uniquement

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId}`

**Response (200 OK):**
```
(No body)
```

**Note:** 
- Seuls les ORG_ADMIN peuvent archiver des projets
- Les projets archivés ne sont plus visibles dans la liste
- L'archivage est un soft delete (le projet n'est pas supprimé de la base de données)

---

## 7. Ajouter un membre à un projet

**Endpoint:** `POST /api/v1/organizations/{organizationId}/projects/{projectId}/members/{employeeId}`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId}/members/{employeeId}`

**Response (200 OK):**
```json
{
  "id": "e71442ce-daa3-43bc-a4e9-6a6150608626",
  "name": "ERP Migration",
  "status": "PLANNING",
  "startDate": "2026-02-01",
  "deadline": "2026-07-01",
  "managerName": "Admin User",
  "membersCount": 4
}
```

**Note:** 
- L'employé doit appartenir à la même organisation que le projet
- Si l'employé est déjà membre, la requête réussit sans erreur (idempotent)

---

## 8. Retirer un membre d'un projet

**Endpoint:** `DELETE /api/v1/organizations/{organizationId}/projects/{projectId}/members/{employeeId}`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId}/members/{employeeId}`

**Response (200 OK):**
```json
{
  "id": "e71442ce-daa3-43bc-a4e9-6a6150608626",
  "name": "ERP Migration",
  "status": "PLANNING",
  "startDate": "2026-02-01",
  "deadline": "2026-07-01",
  "managerName": "Admin User",
  "membersCount": 3
}
```

**Note:** 
- Si l'employé n'est pas membre du projet, une erreur 400 sera retournée

---

## 9. Supprimer définitivement un projet (DELETE - Hard Delete)

**Endpoint:** `DELETE /api/v1/organizations/{organizationId}/projects/{projectId}/hard`

**Permissions:** ORG_ADMIN uniquement

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId}/hard`

**Response (200 OK):**
```
(No body)
```

**Note:** 
- Seuls les ORG_ADMIN peuvent supprimer définitivement des projets
- Cette opération est irréversible
- Le projet et toutes ses données associées seront supprimés de la base de données
- ⚠️ **Attention:** Utilisez avec précaution. Préférez l'archivage dans la plupart des cas.

---

## Exemples d'erreurs

### 400 Bad Request - Validation Error

**Scénario:** Dates invalides (deadline avant startDate)

**Request:**
```json
{
  "name": "Invalid Project",
  "description": "Project with invalid dates",
  "startDate": "2026-02-01",
  "deadline": "2026-01-01",
  "budget": 10000.00
}
```

**Response (400 Bad Request):**
```json
{
  "code": "VALIDATION_ERROR",
  "details": [
    "Deadline must be after start date"
  ]
}
```

---

### 400 Bad Request - Missing Required Fields

**Request:**
```json
{
  "name": ""
}
```

**Response (400 Bad Request):**
```json
{
  "code": "VALIDATION_ERROR",
  "details": [
    "name: Name is required",
    "startDate: Start date is required",
    "deadline: Deadline is required"
  ]
}
```

---

### 403 Forbidden - Insufficient Permission

**Scénario:** Un utilisateur avec le rôle EMPLOYEE essaie de créer un projet

**Response (403 Forbidden):**
```json
{
  "code": "INSUFFICIENT_PERMISSION",
  "details": [
    "User does not have required role: ORG_ADMIN, PROJECT_MANAGER"
  ]
}
```

---

### 403 Forbidden - Cannot Archive (PROJECT_MANAGER)

**Scénario:** Un PROJECT_MANAGER essaie d'archiver un projet

**Response (403 Forbidden):**
```json
{
  "code": "INSUFFICIENT_PERMISSION",
  "details": [
    "Only ORG_ADMIN can archive projects"
  ]
}
```

---

### 404 Not Found - Project Not Found

**Scénario:** Tentative d'archiver un projet inexistant

**Response (404 Not Found):**
```json
{
  "code": "PROJECT_NOT_FOUND",
  "details": [
    "Project not found"
  ]
}
```

---

### 404 Not Found - Manager Not Found

**Scénario:** Créer un projet avec un managerId inexistant

**Request:**
```json
{
  "name": "Test Project",
  "description": "Test",
  "startDate": "2026-02-01",
  "deadline": "2026-07-01",
  "budget": 10000.00,
  "managerId": "00000000-0000-0000-0000-000000000000"
}
```

**Response (404 Not Found):**
```json
{
  "code": "MANAGER_NOT_FOUND",
  "details": [
    "Manager not found"
  ]
}
```

---

### 400 Bad Request - Employee Not Found or Wrong Organization

**Scénario:** Ajouter un membre qui n'existe pas ou appartient à une autre organisation

**Response (400 Bad Request):**
```json
{
  "code": "VALIDATION_ERROR",
  "details": [
    "One or more employees not found or do not belong to the organization"
  ]
}
```

---

### 400 Bad Request - Employee Not a Member

**Scénario:** Retirer un membre qui n'est pas dans le projet

**Response (400 Bad Request):**
```json
{
  "code": "VALIDATION_ERROR",
  "details": [
    "Employee is not a member of this project"
  ]
}
```

---

### 401 Unauthorized - No Authentication

**Scénario:** Requête sans token JWT

**Response (401 Unauthorized):**
```json
{
  "code": "UNAUTHORIZED",
  "details": [
    "Full authentication is required to access this resource"
  ]
}
```

---

## Statuts de projet

Les projets peuvent avoir les statuts suivants:

- **PLANNING** - Projet en phase de planification (statut par défaut)
  - ⚠️ **Note:** Utilisez `PLANNING` (pas `PLANNED`)
- **ACTIVE** - Projet actif en cours
- **ON_HOLD** - Projet mis en pause
- **COMPLETED** - Projet terminé
- **CANCELLED** - Projet annulé

**Valeurs valides exactes:** `PLANNING`, `ACTIVE`, `ON_HOLD`, `COMPLETED`, `CANCELLED`

---

## IDs de référence (depuis la migration V3)

**Organizations:**
- Demo Organization: `550e8400-e29b-41d4-a716-446655440000`

**Users (Managers potentiels):**
- Admin User: `770e8400-e29b-41d4-a716-446655440003`
- Project Manager: `770e8400-e29b-41d4-a716-446655440004`
- Employee: `770e8400-e29b-41d4-a716-446655440005`

---

## Commandes cURL pour tests rapides

**1. Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"password123"}'
```

**2. Créer un projet:**
```bash
curl -X POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ERP Migration",
    "description": "Migration vers ERP v2",
    "startDate": "2026-02-01",
    "deadline": "2026-07-01",
    "budget": 150000.00
  }'
```

**3. Lister les projets:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects \
  -H "Authorization: Bearer <TOKEN>"
```

**4. Obtenir un projet par ID:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId} \
  -H "Authorization: Bearer <TOKEN>"
```

**5. Mettre à jour un projet:**
```bash
curl -X PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId} \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ERP Migration v2",
    "status": "ACTIVE",
    "endDate": "2026-08-01",
    "cost": 120000.00
  }'
```

**6. Archiver un projet:**
```bash
curl -X DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId} \
  -H "Authorization: Bearer <TOKEN>"
```

**7. Ajouter un membre à un projet:**
```bash
curl -X POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId}/members/{employeeId} \
  -H "Authorization: Bearer <TOKEN>"
```

**8. Retirer un membre d'un projet:**
```bash
curl -X DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId}/members/{employeeId} \
  -H "Authorization: Bearer <TOKEN>"
```

**9. Supprimer définitivement un projet:**
```bash
curl -X DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/projects/{projectId}/hard \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Scénarios de test recommandés

### Test 1: Créer un projet simple
1. Login avec un utilisateur ORG_ADMIN
2. Créer un projet sans manager
3. Vérifier que le statut est `PLANNING`
4. Vérifier que le projet apparaît dans la liste

### Test 2: Créer un projet avec manager
1. Login avec un utilisateur ORG_ADMIN
2. Créer un projet avec un managerId valide
3. Vérifier que le managerName est retourné dans la réponse

### Test 3: Tester les permissions
1. Login avec un utilisateur EMPLOYEE
2. Tenter de créer un projet → doit retourner 403
3. Login avec un utilisateur PROJECT_MANAGER
4. Créer un projet → doit réussir
5. Tenter d'archiver le projet → doit retourner 403

### Test 4: Validation des dates
1. Créer un projet avec deadline avant startDate → doit retourner 400
2. Créer un projet avec dates valides → doit réussir

### Test 5: Isolation multi-tenant
1. Créer un projet dans l'organisation A
2. Lister les projets de l'organisation B → ne doit pas voir le projet de A

### Test 6: Archivage
1. Créer un projet
2. Archiver le projet (en tant qu'ORG_ADMIN)
3. Lister les projets → le projet archivé ne doit pas apparaître

### Test 7: Manager inexistant
1. Créer un projet avec un managerId inexistant → doit retourner 404

### Test 8: Obtenir un projet par ID
1. Créer un projet
2. Obtenir le projet par son ID → doit retourner les détails complets
3. Obtenir un projet inexistant → doit retourner 404

### Test 9: Mettre à jour un projet
1. Créer un projet
2. Mettre à jour le nom et le statut → doit réussir
3. Vérifier que seuls les champs fournis ont été mis à jour
4. Mettre à jour un projet inexistant → doit retourner 404

### Test 10: Gestion des membres de projet
1. Créer un projet
2. Ajouter un membre au projet → doit réussir
3. Vérifier que membersCount a augmenté
4. Ajouter le même membre à nouveau → doit réussir (idempotent)
5. Retirer un membre du projet → doit réussir
6. Vérifier que membersCount a diminué
7. Retirer un membre qui n'est pas dans le projet → doit retourner 400
8. Ajouter un employé d'une autre organisation → doit retourner 400

### Test 11: Suppression définitive
1. Créer un projet
2. Supprimer définitivement le projet (en tant qu'ORG_ADMIN) → doit réussir
3. Tenter d'obtenir le projet → doit retourner 404
4. Tenter de supprimer un projet inexistant → doit retourner 404

---

## Checklist de test complète

- [ ] Créer un projet simple (sans manager)
- [ ] Créer un projet avec manager
- [ ] Créer un projet avec membres
- [ ] Créer un projet avec dates invalides (doit échouer)
- [ ] Créer un projet sans authentification (doit échouer)
- [ ] Créer un projet avec rôle EMPLOYEE (doit échouer)
- [ ] Créer un projet avec rôle PROJECT_MANAGER (doit réussir)
- [ ] Créer un projet avec rôle ORG_ADMIN (doit réussir)
- [ ] Lister les projets (doit retourner uniquement les non-archivés)
- [ ] Lister les projets avec aucun projet (doit retourner liste vide)
- [ ] Obtenir un projet par ID (doit réussir)
- [ ] Obtenir un projet inexistant (doit retourner 404)
- [ ] Mettre à jour un projet (en tant qu'ORG_ADMIN)
- [ ] Mettre à jour un projet (en tant que PROJECT_MANAGER)
- [ ] Mettre à jour un projet inexistant (doit retourner 404)
- [ ] Mettre à jour un projet avec champs partiels (seuls les champs fournis doivent être mis à jour)
- [ ] Archiver un projet (en tant qu'ORG_ADMIN)
- [ ] Archiver un projet (en tant que PROJECT_MANAGER - doit échouer)
- [ ] Archiver un projet inexistant (doit retourner 404)
- [ ] Ajouter un membre à un projet
- [ ] Ajouter un membre déjà présent (doit réussir - idempotent)
- [ ] Retirer un membre d'un projet
- [ ] Retirer un membre qui n'est pas dans le projet (doit retourner 400)
- [ ] Ajouter un employé d'une autre organisation (doit retourner 400)
- [ ] Vérifier que membersCount est mis à jour correctement
- [ ] Supprimer définitivement un projet (en tant qu'ORG_ADMIN)
- [ ] Supprimer définitivement un projet (en tant que PROJECT_MANAGER - doit échouer)
- [ ] Supprimer définitivement un projet inexistant (doit retourner 404)
- [ ] Vérifier l'isolation multi-tenant
- [ ] Tester avec manager inexistant (doit retourner 404)

---

**L'API est prête à être testée !** 🚀
