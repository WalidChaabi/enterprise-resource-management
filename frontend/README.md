# Enterprise Resource Management - Frontend

Angular 17+ frontend application for the ERM system.

## Quick Start

```bash
# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build
```

## Development

The app will be available at `http://localhost:4200`

### Default Login Credentials
- Email: `admin@demo.com`
- Password: `password123`

## Project Structure

```
src/app/
├── core/              # Core functionality (services, guards, interceptors)
├── features/          # Feature modules (lazy-loaded)
├── layout/            # Layout components
└── shared/            # Shared components and utilities
```

## Key Features

- ✅ JWT Authentication
- ✅ Role-based route guards
- ✅ Typed API client
- ✅ Global error handling
- ✅ Reactive state with Signals
- ✅ Standalone components

## Next Steps

1. Implement feature components (Project List, Employee List, etc.)
2. Add forms for create/edit operations
3. Implement search functionality
4. Add reporting dashboards
5. Style with Angular Material components
