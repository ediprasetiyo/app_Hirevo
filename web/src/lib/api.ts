/**
 * Thin API client. In mock mode (Vercel preview default), returns fixtures
 * from `mocks.ts`. In live mode, hits per-service backend URLs directly
 * (api-gateway isn't running yet in local dev, so each service is called on
 * its own port — see SERVICE_URLS below).
 */
import { mocks } from './mocks';

/**
 * Per-service base URLs. Extend this map as each backend service comes
 * online — 'iam' covers auth/tenants/me, 'employee' covers /employees, etc.
 * Falls back to NEXT_PUBLIC_API_URL (legacy single-gateway env var) so
 * existing deployments that only set that one still work.
 */
const SERVICE_URLS: Record<string, string> = {
  iam: process.env.NEXT_PUBLIC_IAM_API_URL ?? process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8081/v1',
  employee: process.env.NEXT_PUBLIC_EMPLOYEE_API_URL ?? 'http://localhost:8082/v1',
  attendance: process.env.NEXT_PUBLIC_ATTENDANCE_API_URL ?? 'http://localhost:8083/v1',
};

const TENANT_SUBDOMAIN = process.env.NEXT_PUBLIC_DEFAULT_TENANT_SUBDOMAIN ?? 'acme';
const MOCK_MODE = process.env.NEXT_PUBLIC_MOCK_MODE === 'true';
const TOKEN_STORAGE_KEY = 'hirevo_access_token';

export interface ApiError {
  code: string;
  status: number;
  title: string;
  detail?: string;
}

/**
 * Access token persistence. localStorage (not httpOnly cookie) is a known
 * simplification for local dev/testing — iam-service doesn't set a cookie,
 * it returns the token in the JSON body, so the browser has to store it
 * itself. Production should move this to an httpOnly cookie set server-side
 * to reduce XSS exposure; tracked as follow-up, not done here.
 */
export const authStore = {
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return window.localStorage.getItem(TOKEN_STORAGE_KEY);
  },
  setToken(token: string) {
    if (typeof window === 'undefined') return;
    window.localStorage.setItem(TOKEN_STORAGE_KEY, token);
  },
  clear() {
    if (typeof window === 'undefined') return;
    window.localStorage.removeItem(TOKEN_STORAGE_KEY);
  },
};

async function request<T>(
  service: keyof typeof SERVICE_URLS,
  path: string,
  init?: RequestInit
): Promise<T> {
  const mockKey = path as keyof typeof mocks;
  if (MOCK_MODE) {
    const mockFn = mocks[mockKey] as (() => T) | undefined;
    if (mockFn) return mockFn();
    throw new Error(`Mock not defined for ${path}`);
  }

  const token = authStore.getToken();
  const res = await fetch(`${SERVICE_URLS[service]}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      'X-Tenant-Subdomain': TENANT_SUBDOMAIN,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
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

export interface TenantSignupRequest {
  companyName: string;
  subdomain: string;
  adminEmail: string;
  adminPassword: string;
  adminFullName: string;
}

export interface TenantSignupResponse {
  tenantId: string;
  subdomain: string;
  adminUserId: string;
  tenantUrl: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: { id: string; email: string; fullName: string; roles: string[] };
}

export interface EmployeeSummary {
  id: string;
  employeeNo: string;
  fullName: string;
  status: string;
  hireDate: string;
  contractType: string | null;
  personalEmail: string | null;
  phone: string | null;
}

export interface EmployeeDetail extends EmployeeSummary {
  nikMasked: string | null;
  npwpMasked: string | null;
  dateOfBirth: string | null;
  gender: string | null;
  maritalStatus: string | null;
  address: string | null;
  resignDate: string | null;
  activeContract: {
    id: string;
    contractType: string;
    startDate: string;
    endDate: string | null;
    baseSalary: number;
    workArrangement: string | null;
    status: string;
  } | null;
}

export interface EmployeePage {
  data: EmployeeSummary[];
  pagination: { page: number; size: number; totalElements: number; totalPages: number };
}

export interface CreateEmployeeRequest {
  employeeNo: string;
  fullName: string;
  nik?: string;
  npwp?: string;
  dateOfBirth?: string;
  gender?: string;
  maritalStatus?: string;
  personalEmail?: string;
  phone?: string;
  address?: string;
  hireDate: string;
  contract: {
    contractType: string;
    startDate: string;
    endDate?: string;
    baseSalary: number;
    workArrangement?: string;
  };
}

export interface WorkLocation {
  id: string;
  name: string;
  address: string | null;
  latitude: number;
  longitude: number;
  radiusMeters: number;
  active: boolean;
}

export interface CreateWorkLocationRequest {
  name: string;
  address?: string;
  latitude: number;
  longitude: number;
  radiusMeters?: number;
}

export interface ClockRequest {
  employeeId: string;
  latitude: number;
  longitude: number;
  accuracyMeters?: number;
  isMockLocation: boolean;
  source?: string;
}

export interface AttendanceLogEntry {
  id: string;
  employeeId: string;
  workDate: string;
  clockInAt: string | null;
  clockOutAt: string | null;
  status: string;
  lateMinutes: number | null;
  workedMinutes: number | null;
  fraudScore: number;
  anomaly: boolean;
  anomalyReason: string | null;
}

export const api = {
  login: (email: string, password: string) =>
    request<LoginResponse>('iam', '/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  me: () => request<{ user_id: string; tenant_id: string; roles: string[] }>('iam', '/me'),

  hrDashboard: () => request<unknown>('iam', '/reports/dashboard/hr'),

  // Note: unlike other calls, tenant signup is tenant-less by definition — it
  // does not send X-Tenant-Subdomain (there is no tenant yet). The request()
  // helper sends it unconditionally today, which iam-service happens to
  // ignore for this specific public endpoint, but be aware if that changes.
  signupTenant: (payload: TenantSignupRequest) =>
    request<TenantSignupResponse>('iam', '/tenants/signup', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  listEmployees: (params: { search?: string; status?: string; page?: number; size?: number } = {}) => {
    const qs = new URLSearchParams();
    if (params.search) qs.set('search', params.search);
    if (params.status) qs.set('status', params.status);
    qs.set('page', String(params.page ?? 0));
    qs.set('size', String(params.size ?? 20));
    return request<EmployeePage>('employee', `/employees?${qs.toString()}`);
  },

  getEmployee: (id: string) => request<EmployeeDetail>('employee', `/employees/${id}`),

  createEmployee: (payload: CreateEmployeeRequest) =>
    request<EmployeeDetail>('employee', '/employees', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  resignEmployee: (id: string, resignDate: string, reason?: string) =>
    request<EmployeeDetail>('employee', `/employees/${id}/resign`, {
      method: 'POST',
      body: JSON.stringify({ resignDate, reason }),
    }),

  listWorkLocations: () => request<WorkLocation[]>('attendance', '/work-locations'),

  createWorkLocation: (payload: CreateWorkLocationRequest) =>
    request<WorkLocation>('attendance', '/work-locations', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  clockIn: (payload: ClockRequest) =>
    request<AttendanceLogEntry>('attendance', '/attendance/clock-in', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  clockOut: (payload: ClockRequest) =>
    request<AttendanceLogEntry>('attendance', '/attendance/clock-out', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  listAttendanceLogs: (params: { employeeId?: string; from?: string; to?: string } = {}) => {
    const qs = new URLSearchParams();
    if (params.employeeId) qs.set('employeeId', params.employeeId);
    if (params.from) qs.set('from', params.from);
    if (params.to) qs.set('to', params.to);
    return request<AttendanceLogEntry[]>('attendance', `/attendance/logs?${qs.toString()}`);
  },
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
