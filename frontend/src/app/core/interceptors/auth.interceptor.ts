import { HttpInterceptorFn } from '@angular/common/http';

const REQUEST_ID_HEADER = 'X-Request-Id';

function createRequestId(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

/**
 * Sends credentials (httpOnly auth cookies) and a correlation ID on API requests.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const headers = req.headers.has(REQUEST_ID_HEADER)
    ? req.headers
    : req.headers.set(REQUEST_ID_HEADER, createRequestId());

  return next(req.clone({ withCredentials: true, headers }));
};
