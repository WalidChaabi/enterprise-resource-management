# 🧪 Guide de Test - Employee Management

## ✅ Données de Test Disponibles

### Organisation
- **ID:** `550e8400-e29b-41d4-a716-446655440000`
- **Nom:** Demo Organization
- **Code:** DEMO

### Départements Disponibles
| ID | Nom | Code |
|---|---|---|
| `880e8400-e29b-41d4-a716-446655440001` | Human Resources | HR |
| `880e8400-e29b-41d4-a716-446655440002` | Engineering | ENG |
| `880e8400-e29b-41d4-a716-446655440003` | Sales | SALES |
| `880e8400-e29b-41d4-a716-446655440004` | Marketing | MKT |
| `880e8400-e29b-41d4-a716-446655440005` | Finance | FIN |

### Rôles Disponibles
| ID | Code | Nom |
|---|---|---|
| `660e8400-e29b-41d4-a716-446655440001` | ADMIN | Administrator |
| `660e8400-e29b-41d4-a716-446655440002` | USER | User |
| `660e8400-e29b-41d4-a716-446655440003` | HR_ADMIN | HR Administrator |
| `660e8400-e29b-41d4-a716-446655440004` | ORG_ADMIN | Organization Administrator |
| `660e8400-e29b-41d4-a716-446655440005` | DEPARTMENT_MANAGER | Department Manager |

### Utilisateur Admin
- **Email:** `admin@demo.com`
- **Password:** `password123`
- **Rôles:** HR_ADMIN, ORG_ADMIN

---

## 🚀 Étapes de Test

### 1. Obtenir un Token JWT

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "admin@demo.com",
  "password": "password123"
}
```

**Réponse attendue:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": "770e8400-e29b-41d4-a716-446655440003",
  "email": "admin@demo.com",
  "firstName": "Admin",
  "lastName": "User",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["HR_ADMIN", "ORG_ADMIN"]
}
```

---

### 2. Créer un Employé

```http
POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees
Authorization: Bearer <token_from_step_1>
Content-Type: application/json

{
  "email": "john.doe@company.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "departmentId": "880e8400-e29b-41d4-a716-446655440002",
  "defaultRoleId": "660e8400-e29b-41d4-a716-446655440002"
}
```

**Réponse attendue:** `201 Created`
```json
{
  "id": "...",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "departmentId": "880e8400-e29b-41d4-a716-446655440002",
  "departmentName": "Engineering",
  "departmentCode": "ENG",
  "email": "john.doe@company.com",
  "firstName": "John",
  "lastName": "Doe",
  "active": true,
  "roles": [
    {
      "id": "660e8400-e29b-41d4-a716-446655440002",
      "code": "USER",
      "name": "User",
      "scope": "ORG"
    }
  ],
  "createdAt": "2026-01-07T10:00:00",
  "updatedAt": "2026-01-07T10:00:00"
}
```

---

### 3. Lister les Employés

```http
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees?page=0&size=20
Authorization: Bearer <token>
```

**Réponse attendue:** `200 OK` avec pagination

---

### 4. Obtenir un Employé par ID

```http
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/{employeeId}
Authorization: Bearer <token>
```

---

### 5. Mettre à Jour un Employé

```http
PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/employees/{employeeId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "Jonathan",
  "departmentId": "880e8400-e29b-41d4-a716-446655440001"
}
```

---

## 📋 IDs de Référence Rapide

**Organization ID:**
```
550e8400-e29b-41d4-a716-446655440000
```

**Department IDs:**
- HR: `880e8400-e29b-41d4-a716-446655440001`
- Engineering: `880e8400-e29b-41d4-a716-446655440002`
- Sales: `880e8400-e29b-41d4-a716-446655440003`
- Marketing: `880e8400-e29b-41d4-a716-446655440004`
- Finance: `880e8400-e29b-41d4-a716-446655440005`

**Role IDs:**
- ADMIN: `660e8400-e29b-41d4-a716-446655440001`
- USER: `660e8400-e29b-41d4-a716-446655440002`
- HR_ADMIN: `660e8400-e29b-41d4-a716-446655440003`
- ORG_ADMIN: `660e8400-e29b-41d4-a716-446655440004`
- DEPARTMENT_MANAGER: `660e8400-e29b-41d4-a716-446655440005`

---

## ✅ Vérifications

1. ✅ Application démarrée sur `http://localhost:8080`
2. ✅ Base de données connectée
3. ✅ 5 départements créés
4. ✅ 5 rôles créés
5. ✅ Utilisateur admin avec rôles HR_ADMIN et ORG_ADMIN
6. ✅ Endpoints Employee Management disponibles

---

**Prêt à tester !** 🚀

