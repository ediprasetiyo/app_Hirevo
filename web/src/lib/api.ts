/**
 * Thin API client. In mock mode (Vercel preview default), returns fixtures
 * from `mocks.ts`. In live mode, hits NEXT_PUBLIC_API_URL through the gateway.
 */
import { mocks } from './mocks';

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080/v1';
const TENANT_SUBDOMAIN = process.env.NEXT_PUBLIC_DEFAULT_TENANT_SUBDOMAIN ?? 'acme';
const MOCK_MODE = process.env.NEXT_PUBLIC_MOCK_MODE === 'true';

export interface ApiError {
  code: string;
  status: number;
  title: string;
  detail?: string;
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  if (MOCK_MODE) {
    const mockFn = mocks[path as keyof typeof mocks] as (() => T) | undefined;
    if (mockFn) return mockFn();
    throw new Error(`Mock not defined for ${path}`);
  }

  const res = await fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      'X-Tenant-Subdomain': TENANT_SUBDOMAIN,
      ...(init?.headers ?? {}),
    },
    credentials: 'include',
  });

  if (!res.ok) {
    let problem: ApiError = { code: 'unknown', status: res.status, title: res.statusText };
    try {
      const body = await res.json();
      problem = { ...problem, ...body };
    } catch {
      /* body was not JSON */
    }
    throw problem;
  }

  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export const api = {
  login: (email: string, password: string) =>
    request<{ accessToken: string; refreshToken: string; user: unknown }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  me: () => request<{ user_id: string; tenant_id: string; roles: string[] }>('/me'),

  hrDashboard: () => request<unknown>('/reports/dashboard/hr'),
};

export const isMockMode = MOCK_MODE;

/**
 * `request()` throws a plain ApiError object (RFC 7807 problem+json shape) on
 * HTTP failure, not an `Error` instance — a naive `err instanceof Error` check
 * in a catch block will always miss it and fall through to a generic message,
 * silently discarding the backend's actual `detail` (e.g. "Invalid
 * credentials"). Use this helper in every catch block that surfaces API
 * errors to the user instead of re-deriving the same instanceof check.
 */
export function getErrorMessage(err: unknown, fallback: string): string {
  if (err && typeof err === 'object' && 'detail' in err && typeof err.detail === 'string') {
    return err.detail;
  }
  if (err && typeof err === 'object' && 'title' in err && typeof err.title === 'string') {
    return err.title;
  }
  if (err instanceof Error) {
    return err.message;
  }
  return fallback;
}
