export const environment = {
  production: true,
  // Empty: API paths in constants already start with /api/...; login uses /auth/login.
  // nginx proxies /api/ and /auth/ to the backend without stripping prefixes.
  apiUrl: '',
  apiVersion: 'v1',
  showDemoAccess: false
};
