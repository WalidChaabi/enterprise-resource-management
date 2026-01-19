# Angular Quick Start - Copy & Paste Commands

## Complete Setup in One Go

```bash
# 1. Install Angular CLI
npm install -g @angular/cli

# 2. Create project
ng new enterprise-resource-management-frontend --routing=true --style=scss
cd enterprise-resource-management-frontend

# 3. Install dependencies
npm install @angular/material@^17.0.0 @angular/cdk@^17.0.0
npm install @auth0/angular-jwt
npm install date-fns
npm install uuid
npm install --save-dev @types/uuid

# 4. Generate core structure
mkdir -p src/app/core/{models,services,guards,interceptors,utils}
mkdir -p src/app/features/{auth/login,dashboard,employees,projects,departments,reporting}
mkdir -p src/app/shared/{components,pipes}

# 5. Generate components
ng generate component features/auth/login/login --skip-tests
ng generate component features/dashboard/dashboard --skip-tests
ng generate component shared/components/loading-spinner --skip-tests

# 6. Generate services
ng generate service core/services/auth --skip-tests
ng generate service core/services/api --skip-tests
ng generate service core/services/project --skip-tests
ng generate service core/services/employee --skip-tests

# 7. Generate guards
ng generate guard core/guards/auth --skip-tests
ng generate guard core/guards/role --skip-tests

# 8. Start development server
ng serve
```

---

## Environment Setup

Create `src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  apiVersion: 'v1'
};
```

---

## Test Login

1. Open `http://localhost:4200/login`
2. Email: `admin@demo.com`
3. Password: `password123`
4. Should redirect to dashboard

---

**Ready to code!** 🎉
