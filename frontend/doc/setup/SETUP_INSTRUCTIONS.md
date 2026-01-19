# Angular Frontend Setup Instructions

## Step 1: Install Angular CLI

```bash
npm install -g @angular/cli@latest
```

## Step 2: Install Dependencies

```bash
cd frontend
npm install
```

## Step 3: Start Development Server

```bash
npm start
```

The application will be available at `http://localhost:4200`

## Step 4: Test Login

1. Navigate to `http://localhost:4200/login`
2. Use credentials:
   - Email: `admin@demo.com`
   - Password: `password`
3. You should be redirected to the dashboard

## Project Structure Created

✅ Core models (Auth, Project, Employee)
✅ Services (Auth, API, Project)
✅ Guards (Auth, Role)
✅ Interceptors (Auth, Error)
✅ Login component
✅ Dashboard component
✅ Main layout component
✅ Routing configuration

## Next Steps

1. **Generate remaining components:**
   ```bash
   ng generate component features/projects/project-list
   ng generate component features/projects/project-detail
   ng generate component features/employees/employee-list
   ```

2. **Implement services:**
   - EmployeeService
   - DepartmentService
   - ReportingService
   - SearchService

3. **Add UI components:**
   - Tables for listing
   - Forms for create/edit
   - Modals for confirmations

4. **Style with Angular Material:**
   ```bash
   ng add @angular/material
   ```

## Troubleshooting

**CORS Errors:**
- Ensure backend CORS is configured for `http://localhost:4200`
- Check backend is running on `http://localhost:8080`

**401 Unauthorized:**
- Check token is being stored correctly
- Verify backend JWT_SECRET is set

**Module not found:**
- Run `npm install` again
- Check all imports are correct
