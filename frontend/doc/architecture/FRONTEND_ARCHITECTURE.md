# Frontend Architecture - ERM Angular Application

## Stack Recommendation

**Framework:** Angular 17+ (Standalone Components)
**State Management:** Angular Signals (built-in, no external library needed)
**HTTP Client:** Angular HttpClient with typed interceptors
**Forms:** Angular Reactive Forms
**UI Library:** Angular Material (recommended) or PrimeNG
**Date Handling:** date-fns
**Type Safety:** Full TypeScript strict mode

**Why this stack:**
- Angular Signals provide reactive state without external dependencies
- Standalone components reduce boilerplate
- TypeScript ensures type safety matching backend DTOs
- Angular Material provides production-ready components
- No over-engineering - uses Angular's built-in features

---

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/                    # Core functionality (singletons)
│   │   │   ├── models/             # TypeScript interfaces matching backend DTOs
│   │   │   ├── services/           # API services, auth service
│   │   │   ├── guards/             # Route guards
│   │   │   ├── interceptors/       # HTTP interceptors
│   │   │   ├── utils/              # Constants, helpers
│   │   │   └── config/             # App configuration
│   │   │
│   │   ├── features/               # Feature modules (lazy-loaded)
│   │   │   ├── auth/
│   │   │   ├── dashboard/
│   │   │   ├── employees/
│   │   │   ├── projects/
│   │   │   ├── departments/
│   │   │   ├── tasks/
│   │   │   ├── time-entries/
│   │   │   ├── leave-requests/
│   │   │   ├── reporting/
│   │   │   └── search/
│   │   │
│   │   ├── shared/                 # Shared components, pipes, directives
│   │   │   ├── components/
│   │   │   ├── pipes/
│   │   │   └── directives/
│   │   │
│   │   ├── layout/                 # Layout components
│   │   │   ├── main-layout/
│   │   │   └── auth-layout/
│   │   │
│   │   └── app.config.ts           # App configuration
│   │   └── app.routes.ts           # Route definitions
│   │
│   ├── assets/                     # Static assets
│   ├── environments/               # Environment configs
│   └── styles/                     # Global styles
│
├── angular.json
├── package.json
└── tsconfig.json
```

---

## Key Design Decisions

1. **Standalone Components:** Modern Angular approach, no NgModules
2. **Signals for State:** Reactive state without external libraries
3. **Typed API Layer:** All API calls are fully typed
4. **Centralized Error Handling:** Global error interceptor
5. **JWT in Memory + LocalStorage:** Token stored securely
6. **Role-based Guards:** Route protection based on user roles
7. **Lazy Loading:** All feature modules are lazy-loaded
8. **Environment-based Config:** Separate configs for dev/prod

---

## Authentication Flow

1. User submits login form
2. POST /auth/login with email/password
3. Backend returns AuthResponse with JWT token
4. Token stored in localStorage + memory
5. Token added to all subsequent requests via interceptor
6. Guards check authentication before route access
7. On 401, redirect to login

---

## API Client Pattern

- Base `ApiService` handles all HTTP calls
- Feature-specific services extend base patterns
- All responses typed with interfaces matching backend DTOs
- Error handling centralized in interceptor

---

## Next Steps After Scaffolding

1. Implement remaining feature modules
2. Add UI components (tables, forms, modals)
3. Implement search functionality
4. Add reporting dashboards with charts
5. Add file upload (if needed)
6. Implement real-time updates (WebSocket, optional)
7. Add E2E tests with Cypress/Playwright
