# Department Management API - Guide de Test

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

## 2. Créer un département (CREATE)

**Endpoint:** `POST /api/v1/organizations/{organizationId}/departments`

**Permissions:** HR_ADMIN, ORG_ADMIN

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**URL:** `POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments`

**Body (sans parent):**
```json
{
  "name": "Research & Development",
  "code": "RND",
  "parentDepartmentId": null
}
```

**Body (avec parent):**
```json
{
  "name": "Frontend Team",
  "code": "FRONTEND",
  "parentDepartmentId": "880e8400-e29b-41d4-a716-446655440002"
}
```

**Response (201 Created):**
```json
{
  "id": "990e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "name": "Research & Development",
  "code": "RND",
  "parentDepartmentId": null,
  "parentDepartmentName": null,
  "parentDepartmentCode": null,
  "active": true,
  "createdAt": "2026-01-07T13:30:00",
  "updatedAt": "2026-01-07T13:30:00",
  "createdBy": null,
  "lastModifiedBy": null
}
```

---

## 3. Lister les départements (READ - List)

**Endpoint:** `GET /api/v1/organizations/{organizationId}/departments`

**Permissions:** HR_ADMIN, ORG_ADMIN, DEPARTMENT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URLs d'exemple:**

**Tous les départements:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments
```

**Avec pagination:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments?page=0&size=10
```

**Filtrer par parent:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments?parentId=880e8400-e29b-41d4-a716-446655440002
```

**Filtrer par statut actif:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments?active=true
```

**Combinaison de filtres:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments?active=true&parentId=880e8400-e29b-41d4-a716-446655440002&page=0&size=5
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "880e8400-e29b-41d4-a716-446655440001",
      "organizationId": "550e8400-e29b-41d4-a716-446655440000",
      "organizationName": "Demo Organization",
      "name": "Human Resources",
      "code": "HR",
      "parentDepartmentId": null,
      "parentDepartmentName": null,
      "parentDepartmentCode": null,
      "active": true,
      "createdAt": "2026-01-07T10:00:00",
      "updatedAt": "2026-01-07T10:00:00",
      "createdBy": null,
      "lastModifiedBy": null
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

## 4. Récupérer un département par ID (READ - Get)

**Endpoint:** `GET /api/v1/organizations/{organizationId}/departments/{departmentId}`

**Permissions:** HR_ADMIN, ORG_ADMIN, DEPARTMENT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments/880e8400-e29b-41d4-a716-446655440001`

**Response (200 OK):**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "name": "Human Resources",
  "code": "HR",
  "parentDepartmentId": null,
  "parentDepartmentName": null,
  "parentDepartmentCode": null,
  "active": true,
  "createdAt": "2026-01-07T10:00:00",
  "updatedAt": "2026-01-07T10:00:00",
  "createdBy": null,
  "lastModifiedBy": null
}
```

---

## 5. Mettre à jour un département (UPDATE)

**Endpoint:** `PUT /api/v1/organizations/{organizationId}/departments/{departmentId}`

**Permissions:** HR_ADMIN, ORG_ADMIN

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**URL:** `PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments/880e8400-e29b-41d4-a716-446655440001`

**Body (mise à jour partielle - tous les champs sont optionnels):**

**Changer le nom:**
```json
{
  "name": "Human Resources & Talent"
}
```

**Changer le code:**
```json
{
  "code": "HR_TALENT"
}
```

**Changer le parent:**
```json
{
  "parentDepartmentId": "880e8400-e29b-41d4-a716-446655440002"
}
```

**Désactiver:**
```json
{
  "active": false
}
```

**Mise à jour multiple:**
```json
{
  "name": "Human Resources & Talent",
  "code": "HR_TALENT",
  "active": true
}
```

**Retirer le parent (définir à null):**
```json
{
  "parentDepartmentId": null
}
```

**Response (200 OK):**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "name": "Human Resources & Talent",
  "code": "HR_TALENT",
  "parentDepartmentId": null,
  "parentDepartmentName": null,
  "parentDepartmentCode": null,
  "active": true,
  "createdAt": "2026-01-07T10:00:00",
  "updatedAt": "2026-01-07T13:45:00",
  "createdBy": null,
  "lastModifiedBy": null
}
```

---

## 6. Désactiver un département (DELETE - Soft Delete)

**Endpoint:** `DELETE /api/v1/organizations/{organizationId}/departments/{departmentId}`

**Permissions:** HR_ADMIN, ORG_ADMIN

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments/880e8400-e29b-41d4-a716-446655440001`

**Response (204 No Content):**
```
(No body)
```

**Note:** La désactivation échouera si:
- Le département a des sous-départements actifs
- Le département a des employés actifs

---

## Exemples d'erreurs

### 400 Bad Request - Validation Error
```json
{
  "code": "VALIDATION_ERROR",
  "details": [
    "code: Code must contain only uppercase letters, numbers, and underscores",
    "name: Name is required"
  ]
}
```

### 404 Not Found - Department Not Found
```json
{
  "code": "DEPARTMENT_NOT_FOUND",
  "details": [
    "Department with ID 00000000-0000-0000-0000-000000000000 not found"
  ]
}
```

### 409 Conflict - Code Already Exists
```json
{
  "code": "DEPARTMENT_ALREADY_EXISTS",
  "details": [
    "Department with code 'HR' already exists in organization"
  ]
}
```

### 403 Forbidden - Insufficient Permission
```json
{
  "code": "INSUFFICIENT_PERMISSION",
  "details": [
    "User does not have required role: HR_ADMIN, ORG_ADMIN"
  ]
}
```

### 409 Conflict - Cannot Deactivate (Has Active Children)
```json
{
  "code": "DEPARTMENT_HAS_ACTIVE_CHILDREN",
  "details": [
    "Cannot deactivate department with active sub-departments"
  ]
}
```

---

## IDs de référence (depuis la migration V3)

**Organizations:**
- Demo Organization: `550e8400-e29b-41d4-a716-446655440000`

**Departments:**
- Human Resources (HR): `880e8400-e29b-41d4-a716-446655440001`
- Engineering (ENG): `880e8400-e29b-41d4-a716-446655440002`
- Sales (SALES): `880e8400-e29b-41d4-a716-446655440003`
- Marketing (MKT): `880e8400-e29b-41d4-a716-446655440004`
- Finance (FIN): `880e8400-e29b-41d4-a716-446655440005`

---

## Commandes cURL pour tests rapides

**1. Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@demo.com","password":"password123"}'
```

**2. Créer un département:**
```bash
curl -X POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Research & Development","code":"RND","parentDepartmentId":null}'
```

**3. Lister les départements:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments?active=true&page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

**4. Récupérer un département:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments/880e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>"
```

**5. Mettre à jour un département:**
```bash
curl -X PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments/880e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Human Resources & Talent"}'
```

**6. Désactiver un département:**
```bash
curl -X DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments/880e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>"
```

