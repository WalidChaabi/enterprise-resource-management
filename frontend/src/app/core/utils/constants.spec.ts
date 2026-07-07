import { API_ENDPOINTS, ROLES } from './constants';

describe('constants', () => {
  it('builds organization-scoped API endpoints', () => {
    expect(API_ENDPOINTS.EMPLOYEES('org-1')).toBe('/api/v1/organizations/org-1/employees');
    expect(API_ENDPOINTS.TASKS('org-1', 'project-1')).toBe(
      '/api/v1/organizations/org-1/projects/project-1/tasks',
    );
  });

  it('exposes canonical role codes', () => {
    expect(ROLES.ORG_ADMIN).toBe('ORG_ADMIN');
    expect(ROLES.EMPLOYEE).toBe('EMPLOYEE');
  });
});
