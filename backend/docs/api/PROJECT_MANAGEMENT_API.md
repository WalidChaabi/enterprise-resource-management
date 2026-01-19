# 🗂 Project Management API - Documentation

## 📋 Vue d'ensemble

API complète pour la gestion des projets avec :
- ✅ CRUD complet (Create, Read, Update, Archive)
- ✅ Gestion des membres de projet
- ✅ Rattachement à une organisation
- ✅ Règles métier strictes
- ✅ Sécurité RBAC
- ✅ Pagination, filtres et tri

---

## 🔌 Endpoints

| Method | Endpoint                                      | Description             | Permissions                |
|------  |-----------------------------------------------|-------------------------|----------------------------|
| POST   | `/api/v1/organizations/{orgId}/projects`      | Créer un projet         | ORG_ADMIN, PROJECT_MANAGER |
| GET    | `/api/v1/organizations/{orgId}/projects/{id}` | Obtenir un projet       | ORG_ADMIN, PROJECT_MANAGER |
| PUT    | `/api/v1/organizations/{orgId}/projects/{id}` | Mettre à jour un projet | ORG_ADMIN, PROJECT_MANAGER |
| GET    | `/api/v1/organizations/{orgId}/projects`      | Lister les projets      | ORG_ADMIN, PROJECT_MANAGER |
| DELETE | `/api/v1/organizations/{orgId}/projects/{id}` | Archiver un projet      | ORG_ADMIN                  |

---

## 📝 DTOs

### ProjectCreateDTO
```json
{
  "name": "ERP Migration",
  "description": "Migration vers ERP v2",
  "status": "PLANNED",
  "startDate": "2026-02-01",
  "endDate": "2026-06-30",
  "deadline": "2026-07-01",
  "budget": 150000,
  "managerId": "770e8400-e29b-41d4-a716-446655440003"
}
```

### ProjectUpdateDTO
```json
{
  "name": "ERP Migration Phase 1",
  "status": "IN_PROGRESS",
  "endDate": "2026-07-15"
}
```

### ProjectResponseDTO
```json
{
  "id": "990e8400-e29b-41d4-a716-446655440010",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "ERP Migration",
  "description": "Migration vers ERP v2",
  "status": "IN_PROGRESS",
  "startDate": "2026-02-01",
  "endDate": "2026-07-15",
  "deadline": "2026-07-01",
  "budget": 150000,
  "cost": 42000,
  "archived": false,
  "managerId": "770e8400-e29b-41d4-a716-446655440003",
  "managerName": "Admin User",
  "createdAt": "2026-01-10T09:00:00",
  "updatedAt": "2026-01-20T16:30:00"
}
```

---

## 🔒 Sécurité & Rôles

| Rôle            | Create | Read | Update | Archive |
|----             |------  |------|------  |-------- |
| ORG_ADMIN       | ✅     | ✅  | ✅     | ✅     |
| PROJECT_MANAGER | ✅     | ✅  | ✅     | ❌     |
| EMPLOYEE        | ❌     | ❌  | ❌     | ❌     |

---

## 🧪 Exemples de Requêtes

### Créer un Projet
```http
POST http://localhost:8080/api/v1/organizations/{orgId}/projects
Authorization: Bearer <token>
Content-Type: application/json
```

### Lister les Projets
```http
GET http://localhost:8080/api/v1/organizations/{orgId}/projects?page=0&size=20&status=IN_PROGRESS
```

---

## ⚠️ Erreurs Métier

| Code | Description |
|----|-------------|
| PROJECT_NOT_FOUND | Projet inexistant |
| PROJECT_CODE_ALREADY_EXISTS | Nom déjà utilisé |
| INVALID_DATES | Dates incohérentes |
| ACCESS_DENIED | Permissions insuffisantes |

---

## ✅ Checklist de Test

- [ ] Créer un projet valide
- [ ] Créer un projet avec dates invalides (fail)
- [ ] Lister les projets par statut
- [ ] Mettre à jour le manager
- [ ] Archiver un projet
- [ ] Vérifier l'isolation multi-tenant

---

**API prête pour Postman / Swagger / QA.**

