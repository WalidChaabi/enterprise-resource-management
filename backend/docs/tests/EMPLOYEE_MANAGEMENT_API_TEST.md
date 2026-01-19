# Employee Management API - Guide de Test

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

## 2. Créer un employé (CREATE)

**Endpoint:** `POST /api/v1/organizations/{organizationId}/employees`

**Permissions:** HR_ADMIN, ORG_ADMIN

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**URL:** `POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees`

**Body:**
```json
{
  "email": "john.doe@demo.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "departmentId": "880e8400-e29b-41d4-a716-446655440002",
  "defaultRoleId": "660e8400-e29b-41d4-a716-446655440002"
}
```

**Règles de validation du mot de passe:**
- Minimum 8 caractères, maximum 100
- Au moins une majuscule (A-Z)
- Au moins une minuscule (a-z)
- Au moins un chiffre (0-9)
- Au moins un caractère spécial (@$!%*?&)

**Exemples de mots de passe valides:**
- `SecurePass123!`
- `MyP@ssw0rd`
- `Test1234$`

**Response (201 Created):**
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "departmentId": "880e8400-e29b-41d4-a716-446655440002",
  "departmentName": "Engineering",
  "departmentCode": "ENG",
  "email": "john.doe@demo.com",
  "firstName": "John",
  "lastName": "Doe",
  "active": true,
  "roles": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "code": "USER",
      "name": "Standard user",
      "scope": "ORG"
    }
  ],
  "createdAt": "2026-01-07T14:00:00",
  "updatedAt": "2026-01-07T14:00:00",
  "createdBy": null,
  "lastModifiedBy": null
}
```

---

## 3. Lister les employés (READ - List)

**Endpoint:** `GET /api/v1/organizations/{organizationId}/employees`

**Permissions:** 
- HR_ADMIN, ORG_ADMIN: tous les employés de l'organisation
- DEPARTMENT_MANAGER: uniquement les employés de son département

**Headers:**
```
Authorization: Bearer <token>
```

**URLs d'exemple:**

**Tous les employés:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees
```

**Avec pagination:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees?page=0&size=10
```

**Filtrer par département:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees?departmentId=880e8400-e29b-41d4-a716-446655440002
```

**Filtrer par statut actif:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees?active=true
```

**Combinaison de filtres avec pagination:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees?departmentId=880e8400-e29b-41d4-a716-446655440002&active=true&page=0&size=5&sort=lastName,asc
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "aa0e8400-e29b-41d4-a716-446655440001",
      "organizationId": "550e8400-e29b-41d4-a716-446655440000",
      "organizationName": "Demo Organization",
      "departmentId": "880e8400-e29b-41d4-a716-446655440002",
      "departmentName": "Engineering",
      "departmentCode": "ENG",
      "email": "john.doe@demo.com",
      "firstName": "John",
      "lastName": "Doe",
      "active": true,
      "roles": [
        {
          "id": "660e8400-e29b-41d4-a716-446655440002",
          "code": "USER",
          "name": "Standard user",
          "scope": "ORG"
        }
      ],
      "createdAt": "2026-01-07T14:00:00",
      "updatedAt": "2026-01-07T14:00:00",
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
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "first": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

## 4. Récupérer un employé par ID (READ - Get)

**Endpoint:** `GET /api/v1/organizations/{organizationId}/employees/{employeeId}`

**Permissions:**
- HR_ADMIN, ORG_ADMIN: n'importe quel employé de l'organisation
- DEPARTMENT_MANAGER: uniquement les employés de son département

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001`

**Response (200 OK):**
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "departmentId": "880e8400-e29b-41d4-a716-446655440002",
  "departmentName": "Engineering",
  "departmentCode": "ENG",
  "email": "john.doe@demo.com",
  "firstName": "John",
  "lastName": "Doe",
  "active": true,
  "roles": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "code": "USER",
      "name": "Standard user",
      "scope": "ORG"
    }
  ],
  "createdAt": "2026-01-07T14:00:00",
  "updatedAt": "2026-01-07T14:00:00",
  "createdBy": null,
  "lastModifiedBy": null
}
```

---

## 5. Mettre à jour un employé (UPDATE)

**Endpoint:** `PUT /api/v1/organizations/{organizationId}/employees/{employeeId}`

**Permissions:**
- HR_ADMIN, ORG_ADMIN: tous les champs, tous les employés
- DEPARTMENT_MANAGER: champs limités, uniquement employés de son département (ne peut pas changer le département)

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**URL:** `PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001`

**Body (mise à jour partielle - tous les champs sont optionnels):**

**Changer l'email:**
```json
{
  "email": "john.doe.new@demo.com"
}
```

**Changer le prénom et nom:**
```json
{
  "firstName": "Jonathan",
  "lastName": "Doe-Smith"
}
```

**Changer le département (HR_ADMIN/ORG_ADMIN uniquement):**
```json
{
  "departmentId": "880e8400-e29b-41d4-a716-446655440001"
}
```

**Désactiver l'employé:**
```json
{
  "active": false
}
```

**Mise à jour multiple:**
```json
{
  "firstName": "Jonathan",
  "lastName": "Doe-Smith",
  "email": "jonathan.doe@demo.com",
  "active": true
}
```

**Note:** Les DEPARTMENT_MANAGER ne peuvent pas modifier le `departmentId`. Si tenté, erreur 403.

**Response (200 OK):**
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440001",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "departmentId": "880e8400-e29b-41d4-a716-446655440001",
  "departmentName": "Human Resources",
  "departmentCode": "HR",
  "email": "jonathan.doe@demo.com",
  "firstName": "Jonathan",
  "lastName": "Doe-Smith",
  "active": true,
  "roles": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "code": "USER",
      "name": "Standard user",
      "scope": "ORG"
    }
  ],
  "createdAt": "2026-01-07T14:00:00",
  "updatedAt": "2026-01-07T14:30:00",
  "createdBy": null,
  "lastModifiedBy": null
}
```

---

## 6. Désactiver un employé (DELETE - Soft Delete)

**Endpoint:** `DELETE /api/v1/organizations/{organizationId}/employees/{employeeId}`

**Permissions:** HR_ADMIN, ORG_ADMIN uniquement

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001`

**Response (204 No Content):**
```
(No body)
```

**Note:** La désactivation met `active = false`. L'employé ne pourra plus se connecter.

---

## Exemples d'erreurs

### 400 Bad Request - Validation Error
```json
{
  "code": "VALIDATION_ERROR",
  "details": [
    "email: Email must be valid",
    "password: Password must contain at least one uppercase, lowercase, digit, and special character",
    "firstName: First name is required"
  ]
}
```

### 404 Not Found - Employee Not Found
```json
{
  "code": "EMPLOYEE_NOT_FOUND",
  "details": [
    "Employee with ID 00000000-0000-0000-0000-000000000000 not found"
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

### 409 Conflict - Email Already Exists
```json
{
  "code": "EMAIL_ALREADY_EXISTS",
  "details": [
    "Email already exists in this organization"
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

### 403 Forbidden - Department Manager Cannot Change Department
```json
{
  "code": "INSUFFICIENT_PERMISSION",
  "details": [
    "Department managers cannot change employee department"
  ]
}
```

### 400 Bad Request - Department Inactive
```json
{
  "code": "DEPARTMENT_INACTIVE",
  "details": [
    "Cannot assign employee to an inactive department"
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

**Roles:**
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

**2. Créer un employé:**
```bash
curl -X POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@demo.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "departmentId": "880e8400-e29b-41d4-a716-446655440002",
    "defaultRoleId": "660e8400-e29b-41d4-a716-446655440002"
  }'
```

**3. Lister les employés:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees?active=true&page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

**4. Récupérer un employé:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>"
```

**5. Mettre à jour un employé:**
```bash
curl -X PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jonathan","lastName":"Doe-Smith"}'
```

**6. Désactiver un employé:**
```bash
curl -X DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/aa0e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Scénarios de test recommandés

### Scénario 1: Création complète d'un employé
1. Login avec HR_ADMIN
2. Créer un employé avec tous les champs requis
3. Vérifier la réponse 201 avec les détails complets
4. Récupérer l'employé par ID pour vérifier

### Scénario 2: Validation des permissions
1. Login avec DEPARTMENT_MANAGER
2. Essayer de créer un employé → doit échouer (403)
3. Essayer de modifier le département d'un employé → doit échouer (403)
4. Essayer de désactiver un employé → doit échouer (403)

### Scénario 3: Filtrage et pagination
1. Créer plusieurs employés dans différents départements
2. Lister tous les employés
3. Filtrer par département
4. Filtrer par statut actif
5. Tester la pagination avec différents `page` et `size`

### Scénario 4: Gestion des erreurs
1. Créer un employé avec un email déjà existant → 409
2. Créer un employé avec un mot de passe invalide → 400
3. Créer un employé avec un département inexistant → 404
4. Mettre à jour un employé inexistant → 404

