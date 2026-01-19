# Role Management API - Guide de Test

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
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": "770e8400-e29b-41d4-a716-446655440003",
  "email": "admin@demo.com",
  "firstName": "Admin",
  "lastName": "User",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["HR_ADMIN", "ORG_ADMIN"]
}
```

**Copier le `token` pour les requêtes suivantes.**

---

## 2. Créer un rôle (CREATE)

**Endpoint:** `POST /api/v1/organizations/{organizationId}/roles`

**Permissions:** ORG_ADMIN uniquement

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**URL:** `POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles`

**Body:**
```json
{
  "code": "PROJECT_MANAGER",
  "name": "Project Manager",
  "description": "Manages projects and teams",
  "scope": "ORG"
}
```

**Scopes disponibles:**
- `GLOBAL`: Rôle global (peut être utilisé par toutes les organisations)
- `ORG`: Rôle spécifique à l'organisation

**Response (201 Created):**
```json
{
  "id": "bb0e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "code": "PROJECT_MANAGER",
  "name": "Project Manager",
  "description": "Manages projects and teams",
  "scope": "ORG",
  "createdAt": "2026-01-07T15:00:00",
  "updatedAt": "2026-01-07T15:00:00"
}
```

---

## 3. Lister les rôles (READ - List)

**Endpoint:** `GET /api/v1/organizations/{organizationId}/roles`

**Permissions:** HR_ADMIN, ORG_ADMIN, DEPARTMENT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URLs d'exemple:**

**Tous les rôles:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles
```

**Avec pagination:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles?page=0&size=10
```

**Filtrer par scope:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles?scope=ORG
```

**Combinaison:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles?scope=ORG&page=0&size=5&sort=name,asc
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440001",
      "organizationId": "550e8400-e29b-41d4-a716-446655440000",
      "organizationName": "Demo Organization",
      "code": "ADMIN",
      "name": "Administrator",
      "description": "Full system access",
      "scope": "ORG",
      "createdAt": "2026-01-07T10:00:00",
      "updatedAt": "2026-01-07T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": false
    }
  },
  "totalElements": 5,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "first": true,
  "numberOfElements": 5,
  "empty": false
}
```

---

## 4. Récupérer un rôle par ID (READ - Get)

**Endpoint:** `GET /api/v1/organizations/{organizationId}/roles/{roleId}`

**Permissions:** HR_ADMIN, ORG_ADMIN, DEPARTMENT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles/660e8400-e29b-41d4-a716-446655440001`

**Response (200 OK):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "code": "ADMIN",
  "name": "Administrator",
  "description": "Full system access",
  "scope": "ORG",
  "createdAt": "2026-01-07T10:00:00",
  "updatedAt": "2026-01-07T10:00:00"
}
```

---

## 5. Mettre à jour un rôle (UPDATE)

**Endpoint:** `PUT /api/v1/organizations/{organizationId}/roles/{roleId}`

**Permissions:** ORG_ADMIN uniquement

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**URL:** `PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles/bb0e8400-e29b-41d4-a716-446655440001`

**Body (mise à jour partielle - tous les champs sont optionnels):**

**Changer le nom:**
```json
{
  "name": "Senior Project Manager"
}
```

**Changer la description:**
```json
{
  "description": "Manages complex projects and cross-functional teams"
}
```

**Changer le scope:**
```json
{
  "scope": "GLOBAL"
}
```

**Mise à jour multiple:**
```json
{
  "name": "Senior Project Manager",
  "description": "Manages complex projects and cross-functional teams",
  "scope": "ORG"
}
```

**Response (200 OK):**
```json
{
  "id": "bb0e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "code": "PROJECT_MANAGER",
  "name": "Senior Project Manager",
  "description": "Manages complex projects and cross-functional teams",
  "scope": "ORG",
  "createdAt": "2026-01-07T15:00:00",
  "updatedAt": "2026-01-07T15:30:00"
}
```

---

## 6. Supprimer un rôle (DELETE)

**Endpoint:** `DELETE /api/v1/organizations/{organizationId}/roles/{roleId}`

**Permissions:** ORG_ADMIN uniquement

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles/bb0e8400-e29b-41d4-a716-446655440001`

**Response (204 No Content):**
```
(No body)
```

**Note:** La suppression échouera si:
- Le rôle est assigné à des utilisateurs (doit retirer les assignations d'abord)
- Le rôle est un rôle système protégé (ADMIN, USER, HR_ADMIN, ORG_ADMIN, DEPARTMENT_MANAGER)

---

## 7. Assigner un rôle à un employé (User Role Assignment)

**Endpoint:** `POST /api/v1/organizations/{organizationId}/employees/{employeeId}/roles`

**Permissions:** ORG_ADMIN, HR_ADMIN

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**URL:** `POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001/roles`

**Body:**
```json
{
  "roleId": "bb0e8400-e29b-41d4-a716-446655440001"
}
```

**Response (201 Created):**
```json
{
  "id": "bb0e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "code": "PROJECT_MANAGER",
  "name": "Project Manager",
  "description": "Manages projects and teams",
  "scope": "ORG",
  "createdAt": "2026-01-07T15:00:00",
  "updatedAt": "2026-01-07T15:00:00"
}
```

---

## 8. Retirer un rôle d'un employé (User Role Removal)

**Endpoint:** `DELETE /api/v1/organizations/{organizationId}/employees/{employeeId}/roles/{roleId}`

**Permissions:** ORG_ADMIN, HR_ADMIN

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001/roles/bb0e8400-e29b-41d4-a716-446655440001`

**Response (204 No Content):**
```
(No body)
```

**Note:** La suppression échouera si:
- C'est le dernier rôle de l'utilisateur (un utilisateur doit avoir au moins un rôle)
- Le rôle est un rôle système protégé

---

## 9. Lister les rôles d'un employé (Get User Roles)

**Endpoint:** `GET /api/v1/organizations/{organizationId}/employees/{employeeId}/roles`

**Permissions:** HR_ADMIN, ORG_ADMIN, DEPARTMENT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001/roles`

**Response (200 OK):**
```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440002",
    "organizationId": "550e8400-e29b-41d4-a716-446655440000",
    "organizationName": "Demo Organization",
    "code": "USER",
    "name": "Standard user",
    "description": "Standard user",
    "scope": "ORG",
    "createdAt": "2026-01-07T10:00:00",
    "updatedAt": "2026-01-07T10:00:00"
  },
  {
    "id": "bb0e8400-e29b-41d4-a716-446655440001",
    "organizationId": "550e8400-e29b-41d4-a716-446655440000",
    "organizationName": "Demo Organization",
    "code": "PROJECT_MANAGER",
    "name": "Project Manager",
    "description": "Manages projects and teams",
    "scope": "ORG",
    "createdAt": "2026-01-07T15:00:00",
    "updatedAt": "2026-01-07T15:00:00"
  }
]
```

---

## Exemples d'erreurs

### 400 Bad Request - Validation Error
```json
{
  "code": "VALIDATION_ERROR",
  "details": [
    "code: Code must contain only uppercase letters, numbers, and underscores",
    "scope: Scope must be either GLOBAL or ORG",
    "name: Name is required"
  ]
}
```

### 404 Not Found - Role Not Found
```json
{
  "code": "ROLE_NOT_FOUND",
  "details": [
    "Role with ID 00000000-0000-0000-0000-000000000000 not found"
  ]
}
```

### 409 Conflict - Role Already Exists
```json
{
  "code": "ROLE_ALREADY_EXISTS",
  "details": [
    "Role with code 'PROJECT_MANAGER' already exists in organization"
  ]
}
```

### 409 Conflict - Role Already Assigned
```json
{
  "code": "ROLE_ALREADY_ASSIGNED",
  "details": [
    "Role is already assigned to this user"
  ]
}
```

### 403 Forbidden - Insufficient Permission
```json
{
  "code": "INSUFFICIENT_PERMISSION",
  "details": [
    "User does not have required role: ORG_ADMIN"
  ]
}
```

### 409 Conflict - Role In Use
```json
{
  "code": "ROLE_IN_USE",
  "details": [
    "Cannot delete role that is assigned to 5 user(s). Remove assignments first."
  ]
}
```

### 403 Forbidden - System Role Protected
```json
{
  "code": "SYSTEM_ROLE_PROTECTED",
  "details": [
    "Cannot delete system role: ADMIN"
  ]
}
```

### 400 Bad Request - Cannot Remove Last Role
```json
{
  "code": "CANNOT_REMOVE_LAST_ROLE",
  "details": [
    "Cannot remove the last role from a user. User must have at least one role."
  ]
}
```

---

## IDs de référence (depuis la migration V3)

**Organizations:**
- Demo Organization: `550e8400-e29b-41d4-a716-446655440000`

**Roles (existants):**
- ADMIN: `660e8400-e29b-41d4-a716-446655440001`
- USER: `660e8400-e29b-41d4-a716-446655440002`
- HR_ADMIN: `660e8400-e29b-41d4-a716-446655440003`
- ORG_ADMIN: `660e8400-e29b-41d4-a716-446655440004`
- DEPARTMENT_MANAGER: `660e8400-e29b-41d4-a716-446655440005`

---

## Commandes cURL pour tests rapides

**1. Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"password123"}'
```

**2. Créer un rôle:**
```bash
curl -X POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "PROJECT_MANAGER",
    "name": "Project Manager",
    "description": "Manages projects and teams",
    "scope": "ORG"
  }'
```

**3. Lister les rôles:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles?scope=ORG&page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

**4. Récupérer un rôle:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles/660e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>"
```

**5. Mettre à jour un rôle:**
```bash
curl -X PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles/bb0e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Senior Project Manager"}'
```

**6. Supprimer un rôle:**
```bash
curl -X DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/roles/bb0e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>"
```

**7. Assigner un rôle à un employé:**
```bash
curl -X POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001/roles \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"roleId":"bb0e8400-e29b-41d4-a716-446655440001"}'
```

**8. Retirer un rôle d'un employé:**
```bash
curl -X DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001/roles/bb0e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>"
```

**9. Lister les rôles d'un employé:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001/roles \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Scénarios de test recommandés

### Scénario 1: Création et gestion complète d'un rôle
1. Login avec ORG_ADMIN
2. Créer un nouveau rôle (ex: PROJECT_MANAGER)
3. Lister les rôles pour vérifier
4. Mettre à jour le nom/description du rôle
5. Assigner le rôle à un employé
6. Vérifier les rôles de l'employé
7. Retirer le rôle de l'employé
8. Supprimer le rôle

### Scénario 2: Validation des permissions
1. Login avec HR_ADMIN
2. Essayer de créer un rôle → doit échouer (403)
3. Essayer de supprimer un rôle → doit échouer (403)
4. Essayer d'assigner un rôle → doit réussir (201)
5. Login avec DEPARTMENT_MANAGER
6. Essayer de créer un rôle → doit échouer (403)
7. Essayer de lister les rôles → doit réussir (200)

### Scénario 3: Protection des rôles système
1. Login avec ORG_ADMIN
2. Essayer de supprimer ADMIN → doit échouer (403 - système protégé)
3. Essayer de retirer ADMIN d'un utilisateur → doit échouer (403 - système protégé)

### Scénario 4: Gestion des erreurs
1. Créer un rôle avec un code déjà existant → 409
2. Assigner un rôle déjà assigné → 409
3. Retirer le dernier rôle d'un utilisateur → 400
4. Supprimer un rôle assigné à des utilisateurs → 409

