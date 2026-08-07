# iam-service

Authentication, MFA (TOTP), RBAC/permissions, tenant signup, device binding.

**Port:** 8081

## Endpoints

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | `/v1/tenants/signup` | Public | Create tenant + super_admin user |
| POST | `/v1/auth/login` | Public (needs `X-Tenant-Subdomain`) | Password login; returns tokens or MFA challenge |
| POST | `/v1/auth/mfa/verify` | Public | Complete MFA challenge |
| POST | `/v1/auth/refresh` | Public | Rotate refresh token |
| POST | `/v1/auth/logout` | Public | Revoke refresh token |
| POST | `/v1/mfa/enroll/totp/begin` | Bearer | Generate TOTP secret + otpauth URI |
| POST | `/v1/mfa/enroll/totp/confirm` | Bearer | Verify first TOTP code + activate |
| GET | `/v1/me` | Bearer | Current principal summary |

Full OpenAPI: `GET /v3/api-docs` · Swagger UI: `GET /swagger-ui.html`

## Running

```bash
# From repo root
docker compose up -d postgres redis kafka
cd backend && ./mvnw -pl services/iam-service -am spring-boot:run -Dspring-boot.run.profiles=dev
```

## Testing

```bash
cd backend && ./mvnw -pl services/iam-service test
```

Uses Testcontainers (Postgres + Redis) — Docker required.

## Key design notes

- **JWT** HS256 (dev) — swap to RS256 with KMS-loaded keypair in prod.
- **Refresh tokens** rotate on every use; presenting an already-rotated token invalidates the entire chain (OWASP reuse detection).
- **Password** Argon2id (m=64MiB, t=3, p=4).
- **Account lockout** after 5 failed logins for 15 min.
- **TOTP** secret stored AES-256-GCM encrypted; raw secret shown once at enrollment (QR).
- **RLS** enabled on all `iam.*` tables — connections must `SET LOCAL app.current_tenant_id`. See `TenantConnectionInterceptor` (in `hirevo-tenant` lib).
- **Audit** every state-changing endpoint via `@Audited` → Kafka `hirevo.audit.v1` topic.
