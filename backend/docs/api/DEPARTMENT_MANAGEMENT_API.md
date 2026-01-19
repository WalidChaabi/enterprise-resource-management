# 🏢 Department Management API - Documentation

## 📋 Vue d'ensemble

API complète pour la gestion des départements avec :
- ✅ CRUD complet (Create, Read, Update, Delete)
- ✅ Hiérarchie de départements (parent/child)
- ✅ Validation des règles métier
- ✅ Sécurité RBAC
- ✅ Audit logging
- ✅ Pagination et filtres

---

## 🔌 Endpoints

| Method | Endpoint                                         | Description                  | Permissions                       |
|--------|--------------------------------------------------|------------------------------|-----------------------------------|
| POST   | `/api/v1/organizations/{orgId}/departments`      | Créer un département         | HR_ADMIN, ORG_ADMIN               |
| GET    | `/api/v1/organizations/{orgId}/departments/{id}` | Obtenir un département       | HR_ADMIN, ORG_ADMIN, DEPT_MANAGER |
| PUT    | `/api/v1/organizations/{orgId}/departments/{id}` | Mettre à jour un département | HR_ADMIN, ORG_ADMIN               |
| GET    | `/api/v1/organizations/{orgId}/departments`      | Lister les départements      | HR_ADMIN, ORG_ADMIN, DEPT_MANAGER |
| DELETE | `/api/v1/organizations/{orgId}/departments/{id}` | Désactiver un département    | HR_ADMIN, ORG_ADMIN               |

---

## 📝 DTOs

### DepartmentCreateDTO
```json
{
  "name": "Research & Development",
  "code": "RND",
  "parentDepartmentId": "880e8400-e29b-41d4-a716-446655440002" // Optional
}
```

**Validation:**
- `name`: Requis, max 255 caractères
- `code`: Requis, max 100 caractères, uniquement majuscules, chiffres et underscores
- `parentDepartmentId`: Optionnel, doit exister et être actif

### DepartmentUpdateDTO
```json
{
  "name": "Research & Development",
  "code": "RND",
  "parentDepartmentId": "880e8400-e29b-41d4-a716-446655440002",
  "active": true
}
```

**Tous les champs sont optionnels** - seuls les champs fournis seront mis à jour.

### DepartmentResponseDTO
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440006",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "name": "Research & Development",
  "code": "RND",
  "parentDepartmentId": "880e8400-e29b-41d4-a716-446655440002",
  "parentDepartmentName": "Engineering",
  "parentDepartmentCode": "ENG",
  "active": true,
  "createdAt": "2026-01-07T10:00:00",
  "updatedAt": "2026-01-07T10:00:00",
  "createdBy": null,
  "lastModifiedBy": null
}
```

---

## 🔒 Règles de Sécurité

### Permissions par Rôle

| Rôle                   | Create | Read | Update | Delete |
|------                  |--------|------|--------|--------|
| **HR_ADMIN**           | ✅     | ✅  | ✅    | ✅     |
| **ORG_ADMIN**          | ✅     | ✅  | ✅    | ✅     |
| **DEPARTMENT_MANAGER** | ❌     | ✅  | ❌    | ❌     |
| **EMPLOYEE**           | ❌     | ❌  | ❌    | ❌     |

### Contraintes Multi-Tenant
- Tous les départements sont isolés par organisation
- Impossible d'accéder aux départements d'une autre organisation

---

## ✅ Règles Métier

### Création
1. **Code unique** : Le code doit être unique dans l'organisation
2. **Parent valide** : Si un parent est spécifié, il doit exister et être actif
3. **Pas de référence circulaire** : Un département ne peut pas être son propre parent

### Mise à jour
1. **Code unique** : Si le code change, il doit rester unique
2. **Parent valide** : Le nouveau parent doit exister et être actif
3. **Pas de référence circulaire** : Vérification des références circulaires
4. **Pas de self-reference** : Un département ne peut pas être son propre parent

### Désactivation
1. **Pas d'enfants actifs** : Impossible de désactiver un département avec des départements enfants actifs
2. **Pas d'employés actifs** : (À implémenter) Impossible de désactiver un département avec des employés actifs

---

## 🧪 Exemples de Requêtes

### 1. Créer un Département (sans parent)

```http
POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Research & Development",
  "code": "RND"
}
```

**Réponse:** `201 Created`
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440006",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "organizationName": "Demo Organization",
  "name": "Research & Development",
  "code": "RND",
  "parentDepartmentId": null,
  "parentDepartmentName": null,
  "parentDepartmentCode": null,
  "active": true,
  "createdAt": "2026-01-07T10:00:00",
  "updatedAt": "2026-01-07T10:00:00"
}
```

---

### 2. Créer un Département avec Parent

```http
POST http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Backend Development",
  "code": "BACKEND",
  "parentDepartmentId": "880e8400-e29b-41d4-a716-446655440002"
}
```

---

### 3. Lister les Départements

```http
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments?active=true&page=0&size=20&sort=name,asc
Authorization: Bearer <token>
```

**Paramètres de requête:**
- `parentId` (optionnel) : Filtrer par département parent
- `active` (optionnel) : Filtrer par statut actif/inactif
- `page` : Numéro de page (défaut: 0)
- `size` : Taille de page (défaut: 20)
- `sort` : Tri (ex: `name,asc` ou `code,desc`)

**Réponse:** `200 OK`
```json
{
  "content": [
    {
      "id": "...",
      "name": "Engineering",
      "code": "ENG",
      ...
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

---

### 4. Obtenir un Département

```http
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments/880e8400-e29b-41d4-a716-446655440002
Authorization: Bearer <token>
```

---

### 5. Mettre à Jour un Département

```http
PUT http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments/880e8400-e29b-41d4-a716-446655440002
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Software Engineering",
  "code": "SWE"
}
```

---

### 6. Désactiver un Département

```http
DELETE http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/departments/880e8400-e29b-41d4-a716-446655440002
Authorization: Bearer <token>
```

**Réponse:** `204 No Content`

---

## ⚠️ Edge Cases & Erreurs

### 1. Code Déjà Existant

**Scénario:** Créer un département avec un code qui existe déjà

**Réponse:** `409 Conflict`
```json
{
  "code": "DEPARTMENT_CODE_ALREADY_EXISTS",
  "details": ["Department code already exists in organization"]
}
```

---

### 2. Parent Inexistant

**Scénario:** Créer un département avec un parent qui n'existe pas

**Réponse:** `404 Not Found`
```json
{
  "code": "PARENT_DEPARTMENT_NOT_FOUND",
  "details": ["Parent department not found"]
}
```

---

### 3. Désactiver avec Enfants Actifs

**Scénario:** Désactiver un département qui a des départements enfants actifs

**Réponse:** `409 Conflict`
```json
{
  "code": "DEPARTMENT_HAS_ACTIVE_CHILDREN",
  "details": ["Cannot deactivate department with active child departments"]
}
```

---

### 4. Référence Circulaire

**Scénario:** Définir un département comme parent de son propre parent

**Réponse:** `400 Bad Request`
```json
{
  "code": "VALIDATION_ERROR",
  "details": ["Circular reference detected"]
}
```

---

### 5. Self-Reference

**Scénario:** Définir un département comme son propre parent

**Réponse:** `400 Bad Request`
```json
{
  "code": "VALIDATION_ERROR",
  "details": ["Department cannot be its own parent"]
}
```

---

## 📊 Audit Logging

Toutes les opérations sont loggées avec :
- Type d'événement (DEPARTMENT_CREATED, DEPARTMENT_UPDATED, DEPARTMENT_DEACTIVATED)
- Organisation ID
- Département ID
- Utilisateur qui a effectué l'action
- Détails des changements (pour les mises à jour)
- Timestamp

**Exemple de log:**
```json
{
  "eventType": "DEPARTMENT_UPDATED",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "entityType": "DEPARTMENT",
  "entityId": "880e8400-e29b-41d4-a716-446655440002",
  "actor": "admin@demo.com",
  "timestamp": "2026-01-07T10:00:00",
  "details": {
    "name": {
      "old": "Engineering",
      "new": "Software Engineering"
    },
    "code": {
      "old": "ENG",
      "new": "SWE"
    }
  }
}
```

---

## 🎯 Cas d'Usage Typiques

### 1. Créer une Hiérarchie de Départements

```
Engineering (ENG)
  ├── Backend Development (BACKEND)
  ├── Frontend Development (FRONTEND)
  └── DevOps (DEVOPS)
```

**Étapes:**
1. Créer "Engineering" (sans parent)
2. Créer "Backend Development" avec parent = Engineering
3. Créer "Frontend Development" avec parent = Engineering
4. Créer "DevOps" avec parent = Engineering

---

### 2. Réorganiser un Département

**Scénario:** Déplacer "Backend Development" sous "Infrastructure"

**Action:**
```http
PUT /api/v1/organizations/{orgId}/departments/{backendDeptId}
{
  "parentDepartmentId": "<infrastructureDeptId>"
}
```

---

### 3. Renommer un Département

**Action:**
```http
PUT /api/v1/organizations/{orgId}/departments/{deptId}
{
  "name": "New Department Name",
  "code": "NEW_CODE"
}
```

---

## ✅ Checklist de Test

- [ ] Créer un département sans parent
- [ ] Créer un département avec parent
- [ ] Créer un département avec code dupliqué (doit échouer)
- [ ] Lister tous les départements
- [ ] Lister les départements actifs uniquement
- [ ] Lister les départements d'un parent spécifique
- [ ] Obtenir un département par ID
- [ ] Mettre à jour le nom d'un département
- [ ] Mettre à jour le code d'un département
- [ ] Changer le parent d'un département
- [ ] Désactiver un département sans enfants
- [ ] Désactiver un département avec enfants actifs (doit échouer)
- [ ] Tester les permissions (DEPARTMENT_MANAGER ne peut pas créer/modifier)
- [ ] Tester l'isolation multi-tenant (accès cross-organization doit échouer)

---

**L'API est prête à être testée !** 🚀

