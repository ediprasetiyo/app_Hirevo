# Hirevo HRIS

Enterprise SaaS HRIS multi-tenant untuk Indonesia (UMKM → Enterprise).

**Stack:** Java 21 · Spring Boot 3 · PostgreSQL · Redis · Kafka · Next.js 15 · Flutter · Kubernetes (EKS Jakarta)

## Documentation

Lihat [`docs/00-INDEX.md`](docs/00-INDEX.md) untuk navigasi lengkap.

## Quick Start (Local Dev)

**Prerequisites:**
- Java 21 (Temurin)
- Maven 3.9+
- Docker Desktop / Docker Engine
- Node 20+ (untuk frontend, optional)
- Flutter 3.27+ (untuk mobile, optional)

### 1. Start dependencies

```bash
docker compose up -d
```

Services started:
- PostgreSQL 16 (port 5432, user `dev` / pass `dev`)
- Redis 7 (port 6379)
- Kafka (port 9092, KRaft mode)
- OpenSearch (port 9200)
- MinIO (port 9000 + console 9001, user `minioadmin` / pass `minioadmin`)
- MailHog (SMTP 1025, UI 8025)
- Jaeger (UI 16686)

### 2. Build everything

```bash
cd backend
./mvnw -B -ntp clean install -DskipTests
```

### 3. Run iam-service

```bash
cd services/iam-service
../../mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Hit `http://localhost:8081/actuator/health` → `{"status":"UP"}`

### 4. Sign up tenant

```bash
curl -X POST http://localhost:8081/v1/tenants/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "company_name": "Acme Corp",
    "subdomain": "acme",
    "admin_email": "edi@acme.com",
    "admin_password": "SecurePass123!",
    "admin_full_name": "Edi Prasetiyo"
  }'
```

### 5. Login

```bash
curl -X POST http://localhost:8081/v1/auth/login \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Subdomain: acme' \
  -d '{ "email": "edi@acme.com", "password": "SecurePass123!" }'
```

## Repository Layout

```
hirevo/
├── docs/                  # Architecture & PRD docs
├── backend/               # Java microservices (Maven)
│   ├── libs/              # Shared libraries
│   └── services/          # Microservices
├── web/                   # Next.js 15 (HR backoffice + careers)
├── mobile/                # Flutter (employee self-service)
├── infra/                 # Terraform + Helm + ArgoCD
└── docker-compose.yml     # Local dev dependencies
```

## License

Proprietary © 2026 PT Hirevo Indonesia.
