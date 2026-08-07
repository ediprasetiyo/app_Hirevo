# API Specification — Hirevo HRIS

**Version:** v1
**Base URL:** `https://api.hirevo.id/v1`
**Format:** REST + JSON
**OpenAPI:** 3.1.0 (full spec di [`api/openapi.yaml`](../api/openapi.yaml) — outline di sini)

---

## 0. Cross-Cutting

### 0.1 Auth Header
```
Authorization: Bearer <JWT>
```

### 0.2 Tenant Resolution
- **Subdomain**: `acme.hirevo.id` → gateway sets `X-Tenant-ID` header.
- **Direct API user**: must pass `X-Tenant-ID: <uuid>` header (validated vs JWT.tenant_id).

### 0.3 Standard Headers (response)
```
X-Request-ID: <uuid>
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 87
X-RateLimit-Reset: 1750000000
```

### 0.4 Pagination (cursor-based)
**Request:**
```
GET /v1/employees?limit=20&cursor=eyJpZCI6IjEyMyJ9
```
**Response:**
```json
{
  "data": [...],
  "pagination": {
    "next_cursor": "eyJpZCI6IjE0MyJ9",
    "prev_cursor": null,
    "has_more": true,
    "total": 1024
  }
}
```

### 0.5 Filtering
```
GET /v1/employees?filter[status]=active&filter[department_id]=abc&filter[hire_date][gte]=2025-01-01
```

### 0.6 Sorting
```
GET /v1/employees?sort=-hire_date,full_name
```

### 0.7 Field Selection
```
GET /v1/employees?fields=id,full_name,email,position
```

### 0.8 Error Format (RFC 7807)
```json
{
  "type": "https://api.hirevo.id/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Email format invalid",
  "instance": "/v1/employees/123",
  "trace_id": "01HXYZ...",
  "errors": [
    { "field": "email", "code": "invalid_format", "message": "must be valid email" }
  ]
}
```

### 0.9 Idempotency
For POST that produce side effects:
```
Idempotency-Key: <client-generated-uuid>
```
Server caches result 24h, returns cached if same key.

### 0.10 Common HTTP Codes
| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 202 | Accepted (async) |
| 204 | No Content |
| 400 | Validation error |
| 401 | Unauthenticated |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict / Idempotency mismatch |
| 422 | Business rule violation |
| 429 | Rate limited |
| 500 | Server error |
| 503 | Service unavailable |

---

## 1. Authentication & MFA

### POST /auth/login
**Request:**
```json
{ "email": "edi@acme.com", "password": "S3cret!", "tenant_subdomain": "acme" }
```
**Response 200 (no MFA):**
```json
{
  "access_token": "eyJ...",
  "refresh_token": "rt_...",
  "expires_in": 900,
  "token_type": "Bearer",
  "user": { "id": "...", "email": "...", "roles": ["hr_admin"] }
}
```
**Response 202 (MFA required):**
```json
{
  "mfa_required": true,
  "challenge_id": "...",
  "available_methods": [
    { "id":"...", "type":"webauthn", "name":"YubiKey 5C" },
    { "id":"...", "type":"totp", "name":"Authy" }
  ]
}
```

### POST /auth/mfa/verify
```json
{ "challenge_id":"...", "method_id":"...", "code":"123456" }
// or for WebAuthn:
{ "challenge_id":"...", "method_id":"...", "assertion": { ...webauthn... } }
```

### POST /auth/mfa/enroll/totp
Returns secret + QR code.

### POST /auth/mfa/enroll/webauthn/begin
Returns WebAuthn registration options.

### POST /auth/mfa/enroll/webauthn/finish
Validates attestation, stores credential.

### POST /auth/refresh
```json
{ "refresh_token": "rt_..." }
```
**Response:** new access + rotated refresh token.

### POST /auth/logout
Revokes refresh token & blacklists current jti.

### POST /auth/password/reset/request
### POST /auth/password/reset/confirm
### GET /auth/devices
List user's trusted devices.

### DELETE /auth/devices/{id}
Revoke device.

---

## 2. Tenant & Workspace

### POST /tenants/signup
Public. Creates tenant + super_admin user.

### GET /me
Current user + tenant info + permissions.

### GET /tenants/{id}
Get tenant info (super_admin scope).

### PATCH /tenants/{id}
Update tenant settings (logo, custom_domain, locale).

### GET /tenants/{id}/usage
Current usage vs plan limits (employees, storage, API calls, AI tokens).

### POST /tenants/{id}/upgrade
Upgrade plan.

---

## 3. RBAC

### GET /roles
### POST /roles
### GET /roles/{id}
### PATCH /roles/{id}
### DELETE /roles/{id}
### GET /permissions  (list catalog)
### POST /users/{id}/roles
Assign role to user (with optional scope_branch_id).
### DELETE /users/{id}/roles/{roleId}

---

## 4. Employee Management

### GET /employees
Filters: `status`, `branch_id`, `department_id`, `manager_id`, `search` (fuzzy name).

### POST /employees
```json
{
  "employee_no": "EMP001",
  "full_name": "Rudi Hartono",
  "nik": "3201...",
  "npwp": "12.345.678.9-...",
  "date_of_birth": "1995-05-15",
  "gender": "male",
  "marital_status": "married",
  "personal_email": "rudi@gmail.com",
  "phone": "081234567890",
  "address": "Jl. Sudirman ...",
  "hire_date": "2026-07-01",
  "contract": {
    "type": "pkwt",
    "start_date": "2026-07-01",
    "end_date": "2027-06-30",
    "position_id": "...",
    "branch_id": "...",
    "base_salary": 8000000
  },
  "tax_profile": { "ptkp_code": "K/1" },
  "bpjs_profile": { "is_kes_enrolled": true, "is_jht_enrolled": true }
}
```

### GET /employees/{id}
### PATCH /employees/{id}
### DELETE /employees/{id}  (soft delete)
### POST /employees/bulk-import  (multipart Excel)
### GET /employees/bulk-import/{jobId}  (status)

### POST /employees/{id}/resign
```json
{ "resign_date":"2026-12-31", "reason":"...", "exit_checklist":["..."] }
```

### GET /employees/{id}/documents
### POST /employees/{id}/documents  (multipart)
### DELETE /documents/{id}

### GET /employees/{id}/contracts
### POST /employees/{id}/contracts
### PATCH /contracts/{id}

### GET /orgchart
Returns hierarchical tree.

---

## 5. Organization

### GET/POST/PATCH/DELETE /companies
### GET/POST/PATCH/DELETE /branches
### GET/POST/PATCH/DELETE /departments
### GET/POST/PATCH/DELETE /positions
### GET/POST/PATCH/DELETE /job-levels

---

## 6. Attendance ⭐

### POST /attendance/face-enrollments
Multipart: 3 photos + employee_id.
Server: extract embeddings, store encrypted, set `face_enrollments.status='active'`.

### POST /attendance/clock-in
```json
{
  "device_id": "...",
  "timestamp_client": "2026-07-01T08:01:23+07:00",
  "latitude": -6.234567,
  "longitude": 106.823456,
  "accuracy_meters": 12,
  "is_mock_location": false,
  "is_root_jailbroken": false,
  "photo_base64": "<base64 jpeg>",
  "liveness_passed": true,
  "liveness_score": 0.94
}
```
**Response 201:**
```json
{
  "attendance_log_id": "...",
  "status": "late",
  "late_minutes": 1,
  "fraud_score": 5,
  "message": "Selamat datang Rudi"
}
```
**Response 422 (rejected):**
```json
{
  "type": ".../attendance-rejected",
  "title": "Attendance rejected",
  "errors": [{ "code":"face_mismatch", "message":"Wajah tidak cocok, coba lagi" }]
}
```

### POST /attendance/clock-out
Similar payload.

### GET /attendance/logs
Filter: `employee_id`, `date_from`, `date_to`, `status`, `fraud_only`.

### POST /attendance/corrections
Submit correction request.

### GET /attendance/anomalies
List `pending_review` records (HR-only).

### POST /attendance/anomalies/{id}/decide
Approve / reject anomaly.

### Work Locations
### GET/POST/PATCH/DELETE /work-locations

### Shifts
### GET/POST/PATCH/DELETE /shifts
### GET/POST/PATCH/DELETE /shift-assignments
### POST /shift-assignments/bulk

### Overtime
### POST /overtime-requests
### GET /overtime-requests
### POST /overtime-requests/{id}/approve
### POST /overtime-requests/{id}/reject

---

## 7. Leave

### GET/POST/PATCH/DELETE /leave-types
### GET /leave-balances?employee_id=...&year=2026
### POST /leave-requests
### GET /leave-requests
### GET /leave-requests/{id}
### POST /leave-requests/{id}/cancel
### POST /leave-requests/{id}/approve  (workflow-driven)
### POST /leave-requests/{id}/reject
### GET /leave-calendar?team_id=...&month=2026-07
### GET/POST/PATCH/DELETE /holidays
### POST /leaves/year-end-carryover  (admin job)

---

## 8. Payroll

### GET/POST/PATCH/DELETE /payroll/components
### GET/POST/PATCH /payroll/employee-structures
### POST /payroll/employee-structures/bulk-adjust
```json
{
  "scope": { "department_id": "...", "branch_id": "..." },
  "adjustment": { "type": "percentage", "value": 10 },
  "component_codes": ["BASE_SALARY"],
  "effective_from": "2026-08-01",
  "reason": "Annual raise 2026"
}
```

### Period & Run
### GET/POST /payroll/periods
### POST /payroll/runs
```json
{ "period_id":"...", "company_id":"...", "branch_id": null, "scope": "all" }
```
**Response 202:** `{ "run_id":"...", "status":"draft" }`

### POST /payroll/runs/{id}/calculate
**202 Accepted** — async. Track via:

### GET /payroll/runs/{id}
Returns status + summary.

### GET /payroll/runs/{id}/payslips?cursor=...

### POST /payroll/runs/{id}/recalculate-employee/{employeeId}

### POST /payroll/runs/{id}/review
Mark reviewed.

### POST /payroll/runs/{id}/approve
**Idempotency-Key required.** 4-eyes principle enforced (cannot be same user as reviewer).

### POST /payroll/runs/{id}/disburse
Marks paid.

### POST /payroll/runs/{id}/reopen
Super-admin only, audit-trailed.

### GET /payroll/payslips/{id}
### GET /payroll/payslips/{id}/pdf  (signed URL)

### Bank Files
### POST /payroll/runs/{id}/bank-files?bank=BCA
### GET /payroll/runs/{id}/bank-files

### THR
### POST /payroll/runs/thr  (special run)

---

## 9. Tax (PPh 21)

### GET/PATCH /tax/profiles/{employeeId}
### GET /tax/calculations?employee_id=...&year=2026
### POST /tax/bukti-potong/generate
```json
{ "tax_year": 2026, "company_id": "...", "scope": "all" }
```
### GET /tax/bukti-potong?tax_year=2026&employee_id=...
### GET /tax/bukti-potong/{id}/pdf
### GET /tax/bukti-potong/{id}/ebupot-xml

### GET /tax/brackets/ter?effective_date=2026-07-01  (read-only)
### GET /tax/ptkp-statuses

---

## 10. BPJS

### GET/PATCH /bpjs/profiles/{employeeId}
### GET /bpjs/calculations?employee_id=...&period=2026-07
### POST /bpjs/exports
```json
{ "payroll_run_id":"...", "type":"sipp" }  // or "edabu"
```
### GET /bpjs/exports/{id}

---

## 11. Reimbursement

### GET/POST/PATCH/DELETE /reimbursement/categories
### POST /reimbursement/requests
```json
{
  "title": "Transport client visit",
  "category_id": "...",
  "items": [
    { "transaction_date":"2026-07-10", "amount":150000, "description":"Grab to client",
      "ocr_job_id":"...", "receipt_url":"..." }
  ]
}
```
### GET /reimbursement/requests
### POST /reimbursement/requests/{id}/submit
### POST /reimbursement/requests/{id}/approve
### POST /reimbursement/requests/{id}/reject
### POST /reimbursement/ocr/jobs  (multipart receipt photo)
### GET /reimbursement/ocr/jobs/{id}

### Cash Advance
### POST/GET /reimbursement/cash-advances
### POST /reimbursement/cash-advances/{id}/settle

---

## 12. Loan

### GET /loan/types
### POST /loan/loans
### GET /loan/loans (filter by employee, status)
### GET /loan/loans/{id}/schedule
### POST /loan/loans/{id}/approve
### POST /loan/loans/{id}/disburse
### POST /loan/loans/{id}/early-settle
### GET /loan/eligibility?employee_id=...&amount=...&tenor=12
### GET /loan/dashboard  (exposure, aging, top borrowers)

---

## 13. Recruitment (Phase 2)

### GET/POST/PATCH/DELETE /recruitment/jobs
### POST /recruitment/jobs/{id}/publish
### GET /recruitment/jobs/{id}/applications
### POST /careers/jobs/{slug}/apply  (public, no auth)
### GET/POST /recruitment/candidates
### POST /recruitment/applications/{id}/move-stage
### POST /recruitment/applications/{id}/reject
### POST /recruitment/interviews
### POST /recruitment/interviews/{id}/feedback
### POST /recruitment/offers
### POST /recruitment/offers/{id}/send
### POST /recruitment/applications/{id}/hire  (→ creates employee)

---

## 14. Performance (Phase 2)

### GET/POST/PATCH /performance/cycles
### GET/POST/PATCH /performance/objectives
### GET/POST/PATCH /performance/key-results
### POST /performance/key-results/{id}/check-in
### GET/POST /performance/reviews
### POST /performance/reviews/{id}/submit
### POST /performance/feedback-360
### GET/POST /performance/one-on-ones

---

## 15. Asset (Phase 2)

### GET/POST/PATCH/DELETE /assets/categories
### GET/POST/PATCH/DELETE /assets
### GET /assets/{id}/qr  (image)
### POST /assets/{id}/assign
### POST /assets/{id}/acknowledge  (employee mobile)
### POST /assets/{id}/return
### POST /assets/{id}/maintenance
### GET /assets/maintenance/upcoming

---

## 16. Workflow

### GET/POST/PATCH/DELETE /workflows
### GET/POST/PATCH/DELETE /workflows/{id}/steps
### GET /workflows/instances  (cross-module pending approvals)
### POST /workflows/instances/{id}/act
```json
{ "action":"approve", "comment":"Looks good" }
```
### POST /workflows/delegate
```json
{ "delegate_user_id":"...", "from":"2026-07-15", "to":"2026-07-22", "modules":["leave","reimbursement"] }
```
### GET /approval-inbox  (current user's pending approvals across modules)

---

## 17. Notifications

### GET /notifications  (in-app inbox)
### POST /notifications/{id}/read
### POST /notifications/read-all
### GET /notifications/preferences
### PATCH /notifications/preferences
### POST /notifications/devices/register  (mobile, FCM token)

---

## 18. Documents

### POST /documents/upload-url
Returns presigned S3 PUT URL.
```json
{ "module":"employee", "owner_id":"...", "filename":"ktp.jpg", "size":124000, "mime":"image/jpeg" }
```
→
```json
{ "upload_url":"https://s3...", "document_id":"...", "expires_at":"..." }
```
### POST /documents  (confirm uploaded)
### GET /documents/{id}/download  (returns signed URL, 5min)
### DELETE /documents/{id}

---

## 19. AI

### POST /ai/chat/sessions
```json
{ "context_type":"hr_chatbot", "title":"Cuti April" }
```
### POST /ai/chat/sessions/{id}/messages
```json
{ "content":"Berapa sisa cuti saya?" }
```
**Streaming response (SSE):**
```
data: {"delta":"Sisa cuti tahunan Anda "}
data: {"delta":"adalah 7 hari."}
data: {"citations":[{"doc_id":"...","title":"Leave Policy"}]}
data: [DONE]
```
### POST /ai/chat/messages/{id}/feedback
```json
{ "feedback":"helpful" }
```

### POST /ai/ocr/extract  (multipart image, OPTIONAL document_type for prompt)
```json
{ "document_type":"ktp" }  // or "npwp","receipt","ijazah"
```
**Response:**
```json
{
  "fields": {
    "nik": "32...",
    "full_name": "...",
    "date_of_birth": "1990-01-15",
    "address": "..."
  },
  "confidence": { "nik": 0.98, "full_name": 0.92 }
}
```

### POST /ai/fraud/score
Internal endpoint, used by services after entity created.
```json
{ "entity_type":"reimbursement", "entity_id":"..." }
```

### POST /ai/recruitment/screen-cv
```json
{ "candidate_id":"...", "job_posting_id":"..." }
```

### POST /ai/knowledge/documents  (upload policy doc)
### DELETE /ai/knowledge/documents/{id}

---

## 20. Reports & Dashboard

### GET /reports/dashboard/hr
Query: `period=2026-07&branch_id=...`
Returns aggregated widgets data (cached 5 min).

### GET /reports/dashboard/executive
Director-level high-altitude view.

### GET /reports/payroll/summary?from=2026-01&to=2026-07
### GET /reports/attendance/summary?period=2026-07
### GET /reports/turnover?year=2026
### GET /reports/headcount?as_of=2026-07-31

### POST /reports/custom  (Phase 2)
```json
{
  "entity":"employee",
  "filters":[{"field":"department_id","op":"eq","value":"..."}],
  "fields":["full_name","position","base_salary"],
  "group_by":["department_id"]
}
```

### POST /reports/export
```json
{ "report_id":"...", "format":"xlsx" }
```
→ returns async job, then signed URL.

---

## 21. Audit

### GET /audit/logs
Filter: `entity_type`, `entity_id`, `user_id`, `action`, `from`, `to`.
### GET /audit/logs/{id}
### POST /audit/export  (zip, with hash chain proof)
### POST /audit/verify-chain  (admin, runs verification job)

---

## 22. Webhooks (B2B integrations)

### GET/POST/PATCH/DELETE /webhooks/subscriptions
### GET /webhooks/deliveries?subscription_id=...
### POST /webhooks/deliveries/{id}/retry

**Event types:**
- `employee.created`, `employee.updated`, `employee.resigned`
- `attendance.clocked_in`, `attendance.anomaly_detected`
- `leave.requested`, `leave.approved`
- `payroll.run_approved`, `payroll.disbursed`
- `reimbursement.submitted`, `reimbursement.approved`
- `recruitment.candidate_hired`

**Payload format:**
```json
{
  "id": "evt_...",
  "type": "payroll.run_approved",
  "tenant_id": "...",
  "occurred_at": "2026-07-25T10:00:00Z",
  "data": { "payroll_run_id":"...", "period":"2026-07", "total_net":123456789 }
}
```
**Signature header:** `X-Hirevo-Signature: t=1750000000,v1=<hex>` (HMAC-SHA256 of timestamp+body).

---

## 23. Integration

### POST /integration/accounting/sync  (Accurate/Jurnal)
### GET /integration/accounting/status
### POST /integration/djp/ebupot/submit
### POST /integration/bpjs/sipp/submit

---

## 24. Health & Operational

### GET /actuator/health  (per service, internal)
### GET /actuator/info
### GET /actuator/metrics  (Prometheus scrape)

---

## 25. OpenAPI Spec Skeleton

```yaml
openapi: 3.1.0
info:
  title: Hirevo HRIS API
  version: 1.0.0
  description: Multi-tenant HRIS for Indonesia
  contact: { email: api@hirevo.id }
servers:
  - url: https://api.hirevo.id/v1
  - url: https://api.staging.hirevo.id/v1
security:
  - bearerAuth: []
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
  parameters:
    Cursor:
      name: cursor
      in: query
      schema: { type: string }
    Limit:
      name: limit
      in: query
      schema: { type: integer, default: 20, maximum: 100 }
  schemas:
    Problem:
      type: object
      properties:
        type: { type: string }
        title: { type: string }
        status: { type: integer }
        detail: { type: string }
        instance: { type: string }
        trace_id: { type: string }
        errors:
          type: array
          items: { $ref: '#/components/schemas/FieldError' }
    FieldError:
      type: object
      properties:
        field: { type: string }
        code: { type: string }
        message: { type: string }
    Employee:
      type: object
      required: [employee_no, full_name, hire_date]
      properties:
        id: { type: string, format: uuid }
        employee_no: { type: string }
        full_name: { type: string }
        nik: { type: string, writeOnly: true }
        npwp: { type: string, writeOnly: true }
        # ... etc
paths:
  /employees:
    get:
      tags: [Employees]
      summary: List employees
      parameters:
        - $ref: '#/components/parameters/Cursor'
        - $ref: '#/components/parameters/Limit'
        - name: filter[status]
          in: query
          schema: { enum: [active, probation, resigned, terminated] }
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items: { $ref: '#/components/schemas/Employee' }
                  pagination: { $ref: '#/components/schemas/Pagination' }
        '401': { $ref: '#/components/responses/Unauthorized' }
    post: { ... }
  /employees/{id}: { ... }
  # ... ~200 endpoints
```

Full OpenAPI YAML to be generated from springdoc-openapi annotations on Spring Boot controllers, then exported & versioned.
