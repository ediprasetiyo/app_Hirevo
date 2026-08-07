# Backend Services

Each service owns a PostgreSQL **schema** (not database) and its own Liquibase migration set.

| Service | Schema | Port | Status |
|---------|--------|------|--------|
| iam-service | `iam`, `tenant` | 8081 | ✅ Scaffolded (Sprint 1) |
| api-gateway | (routing only) | 8080 | ✅ Scaffolded |
| employee-service | `employee` | 8082 | 🚧 Liquibase only |
| attendance-service | `attendance` | 8083 | 🚧 Liquibase only |
| leave-service | `leave_mgmt` | 8084 | 🚧 Liquibase only |
| payroll-service | `payroll` | 8086 | 🚧 Liquibase only |
| tax-service | `tax` | 8087 | 🚧 Liquibase only (PPh 21 TER seeded) |
| bpjs-service | `bpjs` | 8088 | 🚧 Liquibase only (rates seeded) |
| reimbursement-service | `reimburse` | 8089 | 🚧 Liquibase only |
| loan-service | `loan` | 8090 | 🚧 Liquibase only |
| workflow-service | `workflow` | 8091 | 🚧 Liquibase only |
| audit-service | `audit` | 8092 | 🚧 Liquibase only (partitioned + hash chain) |
| notification-service | `notif` | 8093 | 🚧 Liquibase only (partitioned) |

## Schema seed data included

- **iam**: 8 system roles + 22 permissions
- **employee**: 12 major Indonesian banks + national holidays 2026
- **leave_mgmt**: 11 leave types (UU 13/2003 + UU Cipta Kerja)
- **tax**: 12 PTKP statuses (2016+) + PPh 21 TER brackets 2024 (kategori A/B/C) + progressive annual brackets (UU HPP 2021)
- **bpjs**: 5 programs (KES/JHT/JP/JKK/JKM) + rates (JP cap Rp 10.042.300 per 2026, KES cap Rp 12jt, JKK 5-tier risk)

## Multi-tenancy

All tenant-scoped tables have RLS enabled with the same policy:
```sql
CREATE POLICY tenant_isolation ON <schema>.<table>
USING (tenant_id = COALESCE(
  NULLIF(current_setting('app.current_tenant_id', true), ''),
  '00000000-0000-0000-0000-000000000000'
)::uuid);
```
The zero-UUID fallback means **missing tenant context = zero rows visible** (defense in depth).

Connection setup handled by `hirevo-tenant` lib's `TenantConnectionInterceptor` — sets `SET LOCAL app.current_tenant_id` on every JDBC acquire.

## Partitioning strategy

| Table | Partition | Retention |
|-------|-----------|-----------|
| `attendance.attendance_logs` | monthly by `work_date` | 3 years |
| `attendance.face_match_logs` | monthly by `created_at` | 90 days |
| `payroll.payslip_lines` | yearly by `created_at` | 10 years |
| `audit.audit_logs` | monthly by `created_at` | 5–10 years |
| `notif.notifications` | monthly by `created_at` | 6 months |

`pg_partman` recommended for production auto-creation; sample partitions for 2026 seeded in migrations.

## Rule pack versioning (tax + bpjs)

Regulatory rates are **data**, not code. Update PPh 21 TER or BPJS cap = insert new rows with `effective_from` — no application redeploy. Each `tax_calculations` / `bpjs_calculations` row records the `rule_pack_version` used, for auditability.
