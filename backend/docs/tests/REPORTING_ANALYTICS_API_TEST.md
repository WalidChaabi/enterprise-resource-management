# Reporting & Analytics API - Guide de Test

## Informations de base

**Organization ID:** `550e8400-e29b-41d4-a716-446655440000` (Demo Organization)

**Base URL:** `http://localhost:8080`

**Authentification:** Tous les endpoints nécessitent un JWT token dans le header `Authorization: Bearer <token>`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN

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

## 2. Dashboard - Vue d'ensemble

**Endpoint:** `GET /api/v1/organizations/{organizationId}/reports/dashboard`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/dashboard`

**Response (200 OK):**
```json
{
  "totalProjects": 15,
  "activeProjects": 8,
  "completedProjects": 5,
  "projectsOnHold": 2,
  "totalHoursThisMonth": 1240.50,
  "totalHoursThisWeek": 320.25,
  "pendingTimeEntries": 12,
  "approvedTimeEntries": 450,
  "pendingLeaveRequests": 5,
  "approvedLeaveRequests": 120,
  "upcomingLeaveRequests": 8,
  "totalEmployees": 45,
  "activeEmployees": 42,
  "totalBudget": 2500000.00,
  "totalCost": 1850000.00,
  "budgetUtilization": 74.00
}
```

**Description des champs:**
- **Projects:** Statistiques sur les projets (total, actifs, complétés, en pause)
- **Time Tracking:** Heures totales (mois/semaine), entrées en attente/approuvées
- **Leave Management:** Demandes de congé (en attente, approuvées, à venir dans 30 jours)
- **Employees:** Nombre total et actifs d'employés
- **Budget:** Budget total, coût total, et pourcentage d'utilisation

---

## 3. Statistiques d'un projet

**Endpoint:** `GET /api/v1/organizations/{organizationId}/reports/projects/{projectId}/stats`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/projects/{projectId}/stats`

**Response (200 OK):**
```json
{
  "projectId": "e71442ce-daa3-43bc-a4e9-6a6150608626",
  "projectName": "ERP Migration",
  "status": "ACTIVE",
  "startDate": "2026-02-01",
  "deadline": "2026-07-01",
  "endDate": null,
  "budget": 150000.00,
  "cost": 112500.00,
  "budgetUtilization": 75.00,
  "membersCount": 5,
  "tasksCount": 24,
  "totalHoursLogged": 1250.75,
  "completionPercentage": 62
}
```

**Description des champs:**
- **budgetUtilization:** Pourcentage d'utilisation du budget (cost / budget * 100)
- **totalHoursLogged:** Total des heures approuvées enregistrées pour ce projet
- **completionPercentage:** Pourcentage de tâches complétées (tâches DONE / total tâches * 100)

---

## 4. Rapport de temps par projet

**Endpoint:** `GET /api/v1/organizations/{organizationId}/reports/time/by-project`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/time/by-project`

**Avec filtres de date:**
`GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/time/by-project?startDate=2026-01-01&endDate=2026-01-31`

**Response (200 OK):**
```json
[
  {
    "id": "e71442ce-daa3-43bc-a4e9-6a6150608626",
    "name": "ERP Migration",
    "totalHours": 1250.75,
    "entriesCount": 145,
    "averageHoursPerEntry": 8.63
  },
  {
    "id": "f82553df-ebb4-54cd-b5fa-7b7261719737",
    "name": "Website Redesign",
    "totalHours": 890.50,
    "entriesCount": 98,
    "averageHoursPerEntry": 9.09
  }
]
```

**Note:** 
- Seuls les projets avec des heures enregistrées sont retournés
- Seules les entrées de temps **APPROVED** sont comptabilisées
- Les filtres de date sont optionnels

---

## 5. Rapport de temps par employé

**Endpoint:** `GET /api/v1/organizations/{organizationId}/reports/time/by-employee`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/time/by-employee`

**Avec filtres de date:**
`GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/time/by-employee?startDate=2026-01-01&endDate=2026-01-31`

**Response (200 OK):**
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440003",
    "name": "Admin User",
    "totalHours": 160.00,
    "entriesCount": 20,
    "averageHoursPerEntry": 8.00
  },
  {
    "id": "770e8400-e29b-41d4-a716-446655440004",
    "name": "Project Manager",
    "totalHours": 145.50,
    "entriesCount": 18,
    "averageHoursPerEntry": 8.08
  }
]
```

**Note:** 
- Seuls les employés actifs avec des heures enregistrées sont retournés
- Seules les entrées de temps **APPROVED** sont comptabilisées

---

## 6. Résumé du temps de travail

**Endpoint:** `GET /api/v1/organizations/{organizationId}/reports/time/summary`

**Permissions:** ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/time/summary`

**Avec filtres de date:**
`GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/time/summary?startDate=2026-01-01&endDate=2026-01-31`

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Total",
  "totalHours": 5240.25,
  "entriesCount": 650,
  "averageHoursPerEntry": 8.06
}
```

**Note:** Retourne les statistiques globales pour toute l'organisation

---

## 7. Rapport de solde de congés

**Endpoint:** `GET /api/v1/organizations/{organizationId}/reports/leave/balance`

**Permissions:** ORG_ADMIN, HR_ADMIN

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/leave/balance`

**Response (200 OK):**
```json
[
  {
    "employeeId": "770e8400-e29b-41d4-a716-446655440003",
    "employeeName": "Admin User",
    "employeeEmail": "admin@demo.com",
    "totalDaysUsed": 15,
    "totalDaysPending": 3,
    "totalDaysApproved": 15,
    "totalDaysThisYear": 12
  },
  {
    "employeeId": "770e8400-e29b-41d4-a716-446655440004",
    "employeeName": "Project Manager",
    "employeeEmail": "pm@demo.com",
    "totalDaysUsed": 10,
    "totalDaysPending": 0,
    "totalDaysApproved": 10,
    "totalDaysThisYear": 8
  }
]
```

**Description des champs:**
- **totalDaysUsed:** Total des jours de congé approuvés (toutes années confondues)
- **totalDaysPending:** Total des jours en attente d'approbation
- **totalDaysApproved:** Total des jours approuvés (toutes années confondues)
- **totalDaysThisYear:** Total des jours approuvés pour l'année en cours

---

## 8. Demandes de congé à venir

**Endpoint:** `GET /api/v1/organizations/{organizationId}/reports/leave/upcoming`

**Permissions:** ORG_ADMIN, HR_ADMIN

**Headers:**
```
Authorization: Bearer <token>
```

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/leave/upcoming`

**Response (200 OK):**
```json
[
  {
    "employeeId": "770e8400-e29b-41d4-a716-446655440003",
    "employeeName": "Admin User",
    "employeeEmail": "admin@demo.com",
    "leaveRequestId": "880e8400-e29b-41d4-a716-446655440010",
    "leaveType": "VACATION",
    "startDate": "2026-01-20",
    "endDate": "2026-01-25",
    "totalDays": 6,
    "status": "APPROVED"
  },
  {
    "employeeId": "770e8400-e29b-41d4-a716-446655440004",
    "employeeName": "Project Manager",
    "employeeEmail": "pm@demo.com",
    "leaveRequestId": "990e8400-e29b-41d4-a716-446655440011",
    "leaveType": "SICK_LEAVE",
    "startDate": "2026-01-28",
    "endDate": "2026-01-29",
    "totalDays": 2,
    "status": "APPROVED"
  }
]
```

**Note:** 
- Retourne uniquement les demandes **APPROVED** dont la date de début est dans les 30 prochains jours
- Utile pour la planification et l'anticipation des absences

---

## Exemples d'erreurs

### 404 Not Found - Project Not Found

**Scénario:** Obtenir les statistiques d'un projet inexistant

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

### 403 Forbidden - Insufficient Permission

**Scénario:** Un utilisateur avec le rôle EMPLOYEE essaie d'accéder aux rapports

**Response (403 Forbidden):**
```json
{
  "code": "INSUFFICIENT_PERMISSION",
  "details": [
    "User does not have required role: ORG_ADMIN, PROJECT_MANAGER, HR_ADMIN"
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
    "Authentication required"
  ]
}
```

---

## Commandes cURL pour tests rapides

**1. Dashboard:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/dashboard \
  -H "Authorization: Bearer <TOKEN>"
```

**2. Statistiques d'un projet:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/projects/{projectId}/stats \
  -H "Authorization: Bearer <TOKEN>"
```

**3. Rapport de temps par projet:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/time/by-project?startDate=2026-01-01&endDate=2026-01-31" \
  -H "Authorization: Bearer <TOKEN>"
```

**4. Rapport de temps par employé:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/time/by-employee?startDate=2026-01-01&endDate=2026-01-31" \
  -H "Authorization: Bearer <TOKEN>"
```

**5. Résumé du temps:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/time/summary?startDate=2026-01-01&endDate=2026-01-31" \
  -H "Authorization: Bearer <TOKEN>"
```

**6. Solde de congés:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/leave/balance \
  -H "Authorization: Bearer <TOKEN>"
```

**7. Congés à venir:**
```bash
curl -X GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/reports/leave/upcoming \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Scénarios de test recommandés

### Test 1: Dashboard complet
1. Login avec un utilisateur ORG_ADMIN
2. Obtenir le dashboard → doit retourner toutes les statistiques
3. Vérifier que les chiffres sont cohérents avec les données réelles

### Test 2: Statistiques de projet
1. Créer un projet avec des tâches et des entrées de temps
2. Obtenir les statistiques du projet → doit retourner les bonnes métriques
3. Vérifier le calcul du pourcentage de complétion
4. Vérifier le calcul de l'utilisation du budget

### Test 3: Rapports de temps
1. Créer plusieurs entrées de temps pour différents projets
2. Obtenir le rapport par projet → doit grouper correctement
3. Obtenir le rapport par employé → doit grouper correctement
4. Tester avec des filtres de date → doit filtrer correctement

### Test 4: Solde de congés
1. Créer plusieurs demandes de congé pour différents employés
2. Approuver certaines demandes
3. Obtenir le solde de congés → doit calculer correctement les totaux

### Test 5: Congés à venir
1. Créer des demandes de congé approuvées avec des dates futures
2. Obtenir les congés à venir → doit retourner uniquement ceux dans les 30 prochains jours
3. Vérifier que les congés passés ne sont pas inclus

### Test 6: Permissions
1. Login avec un utilisateur EMPLOYEE
2. Tenter d'accéder aux rapports → doit retourner 403
3. Login avec un utilisateur PROJECT_MANAGER
4. Accéder aux rapports de temps → doit réussir
5. Accéder aux rapports de congés → doit retourner 403 (HR_ADMIN uniquement)

---

## Checklist de test complète

- [ ] Obtenir le dashboard (doit retourner toutes les statistiques)
- [ ] Obtenir les statistiques d'un projet existant
- [ ] Obtenir les statistiques d'un projet inexistant (doit retourner 404)
- [ ] Rapport de temps par projet (sans filtres)
- [ ] Rapport de temps par projet (avec filtres de date)
- [ ] Rapport de temps par employé (sans filtres)
- [ ] Rapport de temps par employé (avec filtres de date)
- [ ] Résumé du temps (sans filtres)
- [ ] Résumé du temps (avec filtres de date)
- [ ] Solde de congés (doit retourner tous les employés)
- [ ] Congés à venir (doit retourner uniquement les 30 prochains jours)
- [ ] Tester avec un utilisateur EMPLOYEE (doit retourner 403)
- [ ] Tester avec un utilisateur PROJECT_MANAGER (doit réussir pour les rapports de temps)
- [ ] Tester avec un utilisateur HR_ADMIN (doit réussir pour tous les rapports)
- [ ] Vérifier que seules les entrées APPROVED sont comptabilisées
- [ ] Vérifier que seules les demandes APPROVED sont incluses dans les congés à venir

---

**L'API de reporting est prête à être testée !** 📊
