# Prochaines Étapes - ERM Backend

## État Actuel ✅

- ✅ Architecture de base (packages, sécurité, logging)
- ✅ Authentification JWT
- ✅ Employee Management (CRUD complet)
- ✅ Department Management (CRUD complet)
- ✅ RBAC basique (rôles hardcodés: HR_ADMIN, ORG_ADMIN, DEPARTMENT_MANAGER)
- ✅ Audit logging
- ✅ Multi-tenancy

---

## Option 1: Role Management API (RECOMMANDÉ) 🎯

### Pourquoi maintenant ?
- Complète le système RBAC existant
- Permet la gestion dynamique des rôles (au lieu de hardcoder)
- Nécessaire avant d'autres fonctionnalités qui dépendent des permissions
- Complète naturellement Employee Management

### Fonctionnalités à implémenter:

**1. Role CRUD:**
- `POST /api/v1/organizations/{orgId}/roles` - Créer un rôle
- `GET /api/v1/organizations/{orgId}/roles` - Lister les rôles
- `GET /api/v1/organizations/{orgId}/roles/{roleId}` - Récupérer un rôle
- `PUT /api/v1/organizations/{orgId}/roles/{roleId}` - Mettre à jour un rôle
- `DELETE /api/v1/organizations/{orgId}/roles/{roleId}` - Désactiver un rôle

**2. User Role Assignment:**
- `POST /api/v1/organizations/{orgId}/employees/{employeeId}/roles` - Assigner un rôle
- `DELETE /api/v1/organizations/{orgId}/employees/{employeeId}/roles/{roleId}` - Retirer un rôle
- `GET /api/v1/organizations/{orgId}/employees/{employeeId}/roles` - Lister les rôles d'un employé

**3. Business Rules:**
- Validation: code unique par organisation
- Scope: GLOBAL vs ORG
- Permissions: Seuls ORG_ADMIN peuvent gérer les rôles
- Audit logging pour toutes les opérations

**Complexité:** Moyenne (3-4 heures)
**Impact:** Élevé (fondation pour autres fonctionnalités)

---

## Option 2: Project Management API 🚀

### Pourquoi maintenant ?
- Cas d'usage central pour un ERM
- Permet de gérer les projets et leurs membres
- Base pour Task Management et Time Tracking

### Fonctionnalités à implémenter:

**1. Project CRUD:**
- `POST /api/v1/organizations/{orgId}/projects` - Créer un projet
- `GET /api/v1/organizations/{orgId}/projects` - Lister les projets
- `GET /api/v1/organizations/{orgId}/projects/{projectId}` - Récupérer un projet
- `PUT /api/v1/organizations/{orgId}/projects/{projectId}` - Mettre à jour un projet
- `DELETE /api/v1/organizations/{orgId}/projects/{projectId}` - Archiver un projet

**2. Project Members:**
- `POST /api/v1/organizations/{orgId}/projects/{projectId}/members` - Ajouter un membre
- `DELETE /api/v1/organizations/{orgId}/projects/{projectId}/members/{employeeId}` - Retirer un membre
- `GET /api/v1/organizations/{orgId}/projects/{projectId}/members` - Lister les membres

**3. Business Rules:**
- Statuts: PLANNING, ACTIVE, ON_HOLD, COMPLETED, CANCELLED
- Dates: startDate, endDate, deadline
- Budget et coûts
- Assignation de manager de projet
- Multi-département

**Complexité:** Élevée (5-6 heures)
**Impact:** Très élevé (fonctionnalité métier principale)

---

## Option 3: Task Management API 📋

### Pourquoi maintenant ?
- Complément naturel de Project Management
- Gestion des tâches au niveau projet/employé
- Suivi de progression

### Fonctionnalités à implémenter:

**1. Task CRUD:**
- `POST /api/v1/organizations/{orgId}/projects/{projectId}/tasks` - Créer une tâche
- `GET /api/v1/organizations/{orgId}/projects/{projectId}/tasks` - Lister les tâches
- `PUT /api/v1/organizations/{orgId}/tasks/{taskId}` - Mettre à jour une tâche
- `DELETE /api/v1/organizations/{orgId}/tasks/{taskId}` - Supprimer une tâche

**2. Task Assignment:**
- Assigner des tâches aux employés
- Priorités: LOW, MEDIUM, HIGH, URGENT
- Statuts: TODO, IN_PROGRESS, REVIEW, DONE, BLOCKED

**Complexité:** Moyenne (4-5 heures)
**Impact:** Moyen (dépend de Project Management)

---

## Option 4: Time Tracking API ⏱️

### Pourquoi maintenant ?
- Suivi du temps de travail
- Lié aux projets et tâches
- Reporting et facturation

### Fonctionnalités à implémenter:

**1. Time Entry CRUD:**
- `POST /api/v1/organizations/{orgId}/time-entries` - Enregistrer du temps
- `GET /api/v1/organizations/{orgId}/time-entries` - Lister les entrées
- `PUT /api/v1/organizations/{orgId}/time-entries/{entryId}` - Modifier une entrée
- `DELETE /api/v1/organizations/{orgId}/time-entries/{entryId}` - Supprimer une entrée

**2. Business Rules:**
- Validation: dates, heures, projet/tâche
- Approubation par managers
- Reporting par projet/département/employé

**Complexité:** Moyenne (4-5 heures)
**Impact:** Moyen (utile pour facturation)

---

## Option 5: Leave Management API 🏖️

### Pourquoi maintenant ?
- Gestion des congés et absences
- Workflow d'approbation
- Intégration avec les départements

### Fonctionnalités à implémenter:

**1. Leave Request CRUD:**
- `POST /api/v1/organizations/{orgId}/leave-requests` - Créer une demande
- `GET /api/v1/organizations/{orgId}/leave-requests` - Lister les demandes
- `PUT /api/v1/organizations/{orgId}/leave-requests/{requestId}` - Mettre à jour
- `POST /api/v1/organizations/{orgId}/leave-requests/{requestId}/approve` - Approuver
- `POST /api/v1/organizations/{orgId}/leave-requests/{requestId}/reject` - Rejeter

**2. Business Rules:**
- Types: VACATION, SICK_LEAVE, PERSONAL, MATERNITY, etc.
- Statuts: PENDING, APPROVED, REJECTED, CANCELLED
- Workflow d'approbation par managers
- Calcul des jours restants

**Complexité:** Élevée (5-6 heures)
**Impact:** Élevé (fonctionnalité HR importante)

---

## Recommandation Finale 🎯

**Commencer par: Role Management API**

**Raisons:**
1. ✅ Complète le système RBAC existant
2. ✅ Permet la gestion dynamique des rôles
3. ✅ Nécessaire avant d'autres fonctionnalités
4. ✅ Complexité modérée (3-4h)
5. ✅ Impact élevé sur l'architecture

**Ensuite:**
- Project Management (cas d'usage central)
- Task Management (complément de Project)
- Time Tracking (suivi du temps)
- Leave Management (gestion HR)

---

## Structure proposée pour Role Management

```
src/main/java/com/company/erm/
├── dto/
│   ├── RoleCreateDTO.java
│   ├── RoleUpdateDTO.java
│   ├── RoleResponseDTO.java
│   └── UserRoleAssignmentDTO.java
├── controller/
│   ├── RoleController.java
│   └── UserRoleController.java
├── service/
│   ├── RoleService.java
│   ├── RoleDomainService.java
│   └── UserRoleService.java
├── mapper/
│   └── RoleMapper.java
└── exception/
    ├── RoleNotFoundException.java
    └── RoleAlreadyExistsException.java
```

---

**Quelle option préfères-tu ?** Je recommande **Role Management API** pour compléter le système RBAC.

