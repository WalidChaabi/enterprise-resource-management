# Search & Filtering API - Guide de Test

## Informations de base

**Organization ID:** `550e8400-e29b-41d4-a716-446655440000` (Demo Organization)

**Base URL:** `http://localhost:8080`

**Authentification:** Tous les endpoints nécessitent un JWT token dans le header `Authorization: Bearer <token>`

**Permissions:** Tous les utilisateurs authentifiés (avec restrictions par entité)

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

## 2. Recherche globale

**Endpoint:** `GET /api/v1/organizations/{organizationId}/search`

**Permissions:** Tous les utilisateurs authentifiés

**Headers:**
```
Authorization: Bearer <token>
```

**Paramètres de requête:**
- `query` (requis): Terme de recherche
- `entityTypes` (optionnel): Types d'entités à rechercher, séparés par des virgules (PROJECT, EMPLOYEE, TASK, DEPARTMENT, TIME_ENTRY, LEAVE_REQUEST)
- `page` (optionnel, défaut: 0): Numéro de page
- `size` (optionnel, défaut: 20): Taille de la page
- `sortBy` (optionnel, défaut: relevance): Champ de tri (relevance, name, date)
- `sortDirection` (optionnel, défaut: desc): Direction du tri (asc, desc)

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=ERP`

**Recherche dans tous les types d'entités:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=ERP&page=0&size=20&sortBy=relevance&sortDirection=desc
```

**Recherche uniquement dans les projets:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=ERP&entityTypes=PROJECT
```

**Recherche dans plusieurs types:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=admin&entityTypes=EMPLOYEE,PROJECT
```

**Response (200 OK):**
```json
{
  "query": "ERP",
  "totalResults": 15,
  "page": 0,
  "size": 20,
  "totalPages": 1,
  "resultsByType": {
    "PROJECT": {
      "entityType": "PROJECT",
      "count": 3,
      "items": [
        {
          "entityType": "PROJECT",
          "id": "e71442ce-daa3-43bc-a4e9-6a6150608626",
          "title": "ERP Migration",
          "description": "Migration vers ERP v2",
          "highlight": "**ERP** Migration",
          "date": "2026-02-01",
          "createdAt": "2026-01-15T10:30:00",
          "metadata": {
            "status": "ACTIVE",
            "manager": "John Doe",
            "membersCount": 5
          }
        }
      ]
    },
    "TASK": {
      "entityType": "TASK",
      "count": 5,
      "items": [
        {
          "entityType": "TASK",
          "id": "f82553df-ebb4-54cd-b5fa-7b7261719737",
          "title": "Setup ERP Database",
          "description": "Configure database for ERP system",
          "highlight": "Setup **ERP** Database",
          "date": "2026-02-15",
          "createdAt": "2026-01-20T14:00:00",
          "metadata": {
            "status": "IN_PROGRESS",
            "priority": "HIGH",
            "project": "ERP Migration",
            "assignedTo": "Jane Smith"
          }
        }
      ]
    },
    "EMPLOYEE": {
      "entityType": "EMPLOYEE",
      "count": 2,
      "items": [
        {
          "entityType": "EMPLOYEE",
          "id": "770e8400-e29b-41d4-a716-446655440003",
          "title": "Admin User",
          "description": "admin@demo.com",
          "highlight": "**Admin** User",
          "date": null,
          "createdAt": "2026-01-01T08:00:00",
          "metadata": {
            "email": "admin@demo.com",
            "department": "IT",
            "active": true
          }
        }
      ]
    }
  }
}
```

**Description des champs:**
- **query:** Terme de recherche utilisé
- **totalResults:** Nombre total de résultats trouvés
- **page:** Page actuelle (0-indexed)
- **size:** Taille de la page
- **totalPages:** Nombre total de pages
- **resultsByType:** Résultats groupés par type d'entité
- **items:** Liste des résultats pour chaque type
- **highlight:** Texte avec le terme de recherche mis en évidence (format: `**terme**`)
- **metadata:** Informations supplémentaires spécifiques à chaque type d'entité

---

## 3. Types d'entités disponibles

### PROJECT
- **Recherche dans:** nom, description
- **Metadata:** status, manager, membersCount

### EMPLOYEE
- **Recherche dans:** prénom, nom, email
- **Metadata:** email, department, active

### TASK
- **Recherche dans:** titre, description, nom du projet
- **Metadata:** status, priority, project, assignedTo

### DEPARTMENT
- **Recherche dans:** nom
- **Metadata:** active, parent, code

### TIME_ENTRY
- **Recherche dans:** description, nom du projet, titre de la tâche
- **Metadata:** status, durationHours, project, task, employee

### LEAVE_REQUEST
- **Recherche dans:** raison, nom de l'employé
- **Metadata:** status, leaveType, totalDays, employee

---

## 4. Options de tri

### Par pertinence (défaut)
- Les résultats avec le terme de recherche dans le titre apparaissent en premier
- `sortBy=relevance`

### Par nom/titre
- Tri alphabétique par nom ou titre
- `sortBy=name` ou `sortBy=title`
- `sortDirection=asc` (croissant) ou `desc` (décroissant)

### Par date
- Tri par date (date de début, date d'échéance, date de création selon l'entité)
- `sortBy=date`
- `sortDirection=asc` ou `desc`

**Exemple:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=project&sortBy=date&sortDirection=desc
```

---

## 5. Pagination

La pagination permet de naviguer dans les résultats de recherche.

**Paramètres:**
- `page`: Numéro de page (commence à 0)
- `size`: Nombre d'éléments par page (défaut: 20, max recommandé: 100)

**Exemple:**
```
GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=test&page=1&size=10
```

**Response avec pagination:**
```json
{
  "query": "test",
  "totalResults": 45,
  "page": 1,
  "size": 10,
  "totalPages": 5,
  "resultsByType": {
    ...
  }
}
```

---

## Exemples d'erreurs

### 400 Bad Request - Query manquant

**Scénario:** Requête sans paramètre `query`

**Response (400 Bad Request):**
```json
{
  "code": "BAD_REQUEST",
  "details": [
    "Required parameter 'query' is missing"
  ]
}
```

---

### 400 Bad Request - Type d'entité invalide

**Scénario:** Type d'entité non reconnu dans `entityTypes`

**Response (400 Bad Request):**
```json
{
  "code": "BAD_REQUEST",
  "details": [
    "Invalid entity type: INVALID_TYPE"
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

**1. Recherche globale simple:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=ERP" \
  -H "Authorization: Bearer <TOKEN>"
```

**2. Recherche uniquement dans les projets:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=ERP&entityTypes=PROJECT" \
  -H "Authorization: Bearer <TOKEN>"
```

**3. Recherche avec pagination:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=test&page=0&size=10" \
  -H "Authorization: Bearer <TOKEN>"
```

**4. Recherche triée par date:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=project&sortBy=date&sortDirection=desc" \
  -H "Authorization: Bearer <TOKEN>"
```

**5. Recherche dans plusieurs types:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search?query=admin&entityTypes=EMPLOYEE,PROJECT,TASK" \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Scénarios de test recommandés

### Test 1: Recherche globale basique
1. Login avec un utilisateur authentifié
2. Effectuer une recherche avec un terme commun (ex: "ERP")
3. Vérifier que les résultats sont retournés pour plusieurs types d'entités
4. Vérifier que le highlight fonctionne correctement

### Test 2: Recherche par type d'entité
1. Effectuer une recherche limitée aux projets uniquement
2. Vérifier que seuls les projets sont retournés
3. Tester avec plusieurs types d'entités

### Test 3: Pagination
1. Effectuer une recherche qui retourne plus de 20 résultats
2. Naviguer vers la page 2
3. Vérifier que les résultats sont correctement paginés
4. Vérifier que `totalPages` est calculé correctement

### Test 4: Tri
1. Effectuer une recherche et trier par nom (asc)
2. Vérifier que les résultats sont triés alphabétiquement
3. Tester le tri par date (desc)
4. Vérifier que le tri par pertinence fonctionne (termes dans le titre en premier)

### Test 5: Recherche sans résultats
1. Effectuer une recherche avec un terme qui n'existe pas
2. Vérifier que `totalResults` est 0
3. Vérifier que `resultsByType` est vide

### Test 6: Recherche avec caractères spéciaux
1. Effectuer une recherche avec des caractères spéciaux (ex: "test@demo")
2. Vérifier que la recherche fonctionne correctement

### Test 7: Recherche case-insensitive
1. Effectuer une recherche en majuscules (ex: "ERP")
2. Effectuer la même recherche en minuscules (ex: "erp")
3. Vérifier que les résultats sont identiques

---

## Checklist de test complète

- [ ] Recherche globale simple (tous les types)
- [ ] Recherche limitée à un type d'entité
- [ ] Recherche limitée à plusieurs types d'entités
- [ ] Pagination (page 0, page 1, dernière page)
- [ ] Tri par pertinence (défaut)
- [ ] Tri par nom/titre (asc, desc)
- [ ] Tri par date (asc, desc)
- [ ] Recherche sans résultats
- [ ] Recherche avec query vide (doit retourner résultats vides)
- [ ] Recherche case-insensitive
- [ ] Vérifier que le highlight fonctionne
- [ ] Vérifier que les metadata sont correctement remplis pour chaque type
- [ ] Tester avec différents utilisateurs (permissions)
- [ ] Vérifier que seules les entités de l'organisation sont retournées

---

## Notes importantes

1. **Sécurité:** Les résultats de recherche respectent les permissions de l'utilisateur. Par exemple, un EMPLOYEE ne verra que ses propres time entries et leave requests.

2. **Performance:** Pour de grandes organisations, il est recommandé de limiter le nombre de types d'entités recherchés ou d'utiliser la pagination.

3. **Highlighting:** Le highlighting simple utilise `**terme**` pour mettre en évidence. Pour une mise en évidence plus avancée (HTML), une bibliothèque dédiée serait nécessaire.

4. **Relevance:** Le tri par pertinence place les résultats avec le terme de recherche dans le titre en premier, puis les autres résultats.

5. **Metadata:** Les metadata varient selon le type d'entité et peuvent inclure des informations supplémentaires utiles pour l'affichage.

---

## 9. Export des résultats de recherche

### Export CSV

**Endpoint:** `GET /api/v1/organizations/{organizationId}/search/export/csv`

**Permissions:** Tous les utilisateurs authentifiés

**Headers:**
```
Authorization: Bearer <token>
```

**Paramètres de requête:**
- `query` (requis): Terme de recherche
- `entityTypes` (optionnel): Types d'entités à rechercher, séparés par des virgules
- `sortBy` (optionnel, défaut: relevance): Champ de tri
- `sortDirection` (optionnel, défaut: desc): Direction du tri

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search/export/csv?query=ERP`

**Response (200 OK):**
- Content-Type: `application/octet-stream`
- Content-Disposition: `attachment; filename="search_results_<timestamp>.csv"`
- Body: Fichier CSV avec les résultats de recherche

**Format CSV:**
- En-têtes: Entity Type, ID, Title, Description, Date, Created At, Additional Info
- Commentaires avec les informations de recherche
- Résultats groupés par type d'entité
- BOM UTF-8 pour compatibilité Excel

---

### Export PDF

**Endpoint:** `GET /api/v1/organizations/{organizationId}/search/export/pdf`

**Permissions:** Tous les utilisateurs authentifiés

**Headers:**
```
Authorization: Bearer <token>
```

**Paramètres de requête:**
- `query` (requis): Terme de recherche
- `entityTypes` (optionnel): Types d'entités à rechercher, séparés par des virgules
- `sortBy` (optionnel, défaut: relevance): Champ de tri
- `sortDirection` (optionnel, défaut: desc): Direction du tri

**URL:** `GET http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search/export/pdf?query=ERP`

**Response (200 OK):**
- Content-Type: `application/pdf`
- Content-Disposition: `attachment; filename="search_results_<timestamp>.pdf"`
- Body: Fichier PDF avec les résultats de recherche formatés

**Format PDF:**
- En-tête avec titre et informations de recherche
- Résultats groupés par type d'entité
- Formatage avec polices et espacements appropriés
- Pagination automatique pour les longs résultats

---

## Commandes cURL pour export

**1. Export CSV:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search/export/csv?query=ERP" \
  -H "Authorization: Bearer <TOKEN>" \
  -o search_results.csv
```

**2. Export PDF:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search/export/pdf?query=ERP" \
  -H "Authorization: Bearer <TOKEN>" \
  -o search_results.pdf
```

**3. Export CSV avec filtres:**
```bash
curl -X GET "http://localhost:8080/api/v1/organizations/550e8400-e29b-41d4-a716-446655440000/search/export/csv?query=test&entityTypes=PROJECT,EMPLOYEE&sortBy=date&sortDirection=desc" \
  -H "Authorization: Bearer <TOKEN>" \
  -o search_results.csv
```

---

## Scénarios de test pour export

### Test 1: Export CSV basique
1. Effectuer une recherche
2. Exporter les résultats en CSV
3. Ouvrir le fichier CSV dans Excel ou un éditeur de texte
4. Vérifier que toutes les colonnes sont présentes
5. Vérifier que les données sont correctement formatées

### Test 2: Export PDF basique
1. Effectuer une recherche
2. Exporter les résultats en PDF
3. Ouvrir le fichier PDF
4. Vérifier que le formatage est correct
5. Vérifier que la pagination fonctionne pour les longs résultats

### Test 3: Export avec filtres
1. Effectuer une recherche avec des filtres (entityTypes, sortBy)
2. Exporter les résultats
3. Vérifier que seuls les résultats filtrés sont inclus

### Test 4: Export de grandes quantités de données
1. Effectuer une recherche qui retourne beaucoup de résultats
2. Exporter en CSV et PDF
3. Vérifier que tous les résultats sont inclus
4. Vérifier que les fichiers sont générés correctement

---

## Checklist de test pour export

- [ ] Export CSV avec recherche simple
- [ ] Export CSV avec filtres
- [ ] Export PDF avec recherche simple
- [ ] Export PDF avec filtres
- [ ] Vérifier que le CSV s'ouvre correctement dans Excel
- [ ] Vérifier que le PDF s'affiche correctement
- [ ] Vérifier que tous les résultats sont inclus (pas de pagination)
- [ ] Vérifier que les métadonnées sont correctement formatées
- [ ] Tester avec différents types d'entités
- [ ] Tester avec des recherches vides (doit retourner un fichier vide)

---

**L'API de recherche et filtrage avec export est prête à être testée !** 🔍📄
