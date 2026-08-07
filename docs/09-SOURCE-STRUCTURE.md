# Source Code Structure — Hirevo HRIS

**Monorepo strategy:** Single org repo `hirevo/hirevo` containing all backend services + frontend + mobile + infra-as-code.
**VCS:** Git + GitHub.
**Branch strategy:** Trunk-based (short-lived feature branches → PR → main → auto-deploy staging).

---

## 1. Top-Level Layout

```
hirevo/
├── README.md
├── LICENSE
├── CODEOWNERS
├── .github/
│   ├── workflows/             # CI: per-service matrix + frontend + mobile
│   ├── ISSUE_TEMPLATE/
│   └── PULL_REQUEST_TEMPLATE.md
├── docs/                      # ← THIS folder (PRD, architecture, ERD, etc.)
├── backend/                   # Java 21 + Spring Boot 3 multi-module Maven
├── web/                       # Next.js 15 — HR backoffice + career page
├── mobile/                    # Flutter 3 — Employee self-service
├── shared/                    # Cross-platform schemas
│   ├── openapi/               # Generated OpenAPI specs
│   └── proto/                 # (if gRPC internal) — TBD
├── infra/                     # Terraform + Helm + ArgoCD apps
├── ops/
│   ├── scripts/               # DB seed, data migration helpers
│   ├── runbooks/              # On-call SOPs
│   └── load-tests/            # k6 scripts
└── tools/                     # Code generators, IDE settings
```

---

## 2. Backend — Maven Multi-Module

### 2.1 Top-level `backend/pom.xml`

```xml
<project>
  <groupId>com.hirevo</groupId>
  <artifactId>hirevo-backend</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <modules>
    <!-- Shared libraries (BOM-managed) -->
    <module>libs/bom</module>
    <module>libs/core</module>
    <module>libs/security</module>
    <module>libs/tenant</module>
    <module>libs/audit</module>
    <module>libs/messaging</module>
    <module>libs/integration-bank</module>
    <module>libs/rule-engine</module>
    <module>libs/test-fixtures</module>

    <!-- Microservices -->
    <module>services/api-gateway</module>
    <module>services/iam-service</module>
    <module>services/tenant-service</module>
    <module>services/employee-service</module>
    <module>services/attendance-service</module>
    <module>services/leave-service</module>
    <module>services/payroll-service</module>
    <module>services/tax-service</module>
    <module>services/bpjs-service</module>
    <module>services/reimbursement-service</module>
    <module>services/loan-service</module>
    <module>services/recruitment-service</module>
    <module>services/performance-service</module>
    <module>services/asset-service</module>
    <module>services/workflow-service</module>
    <module>services/notification-service</module>
    <module>services/document-service</module>
    <module>services/ai-service</module>
    <module>services/audit-service</module>
    <module>services/integration-service</module>
    <module>services/reporting-service</module>
  </modules>

  <properties>
    <java.version>21</java.version>
    <spring-boot.version>3.3.4</spring-boot.version>
    <spring-cloud.version>2023.0.3</spring-cloud.version>
  </properties>
</project>
```

### 2.2 Per-service structure (example: `services/payroll-service`)

```
services/payroll-service/
├── Dockerfile
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/hirevo/payroll/
│   │   │   ├── PayrollServiceApplication.java
│   │   │   ├── config/                    # Bean configs, OpenAPI, Security overrides
│   │   │   │   ├── PayrollConfig.java
│   │   │   │   ├── KafkaConfig.java
│   │   │   │   └── RedisConfig.java
│   │   │   ├── api/                        # REST controllers (thin)
│   │   │   │   ├── v1/
│   │   │   │   │   ├── PayrollRunController.java
│   │   │   │   │   ├── PayslipController.java
│   │   │   │   │   ├── SalaryComponentController.java
│   │   │   │   │   └── BankFileController.java
│   │   │   │   └── dto/
│   │   │   │       ├── request/
│   │   │   │       └── response/
│   │   │   ├── application/                # Use-case layer (orchestration)
│   │   │   │   ├── command/                # Commands + handlers (CQRS-lite)
│   │   │   │   │   ├── CreatePayrollRunHandler.java
│   │   │   │   │   ├── CalculatePayrollHandler.java
│   │   │   │   │   ├── ApprovePayrollHandler.java
│   │   │   │   │   └── GenerateBankFileHandler.java
│   │   │   │   ├── query/
│   │   │   │   └── service/
│   │   │   │       ├── PayrollOrchestrationService.java
│   │   │   │       └── SlipPdfService.java
│   │   │   ├── domain/                     # Business entities + invariants
│   │   │   │   ├── model/
│   │   │   │   │   ├── PayrollRun.java
│   │   │   │   │   ├── PayrollRunStatus.java
│   │   │   │   │   ├── Payslip.java
│   │   │   │   │   ├── PayslipLine.java
│   │   │   │   │   ├── SalaryComponent.java
│   │   │   │   │   └── PayrollPeriod.java
│   │   │   │   ├── event/                  # Domain events
│   │   │   │   │   ├── PayrollApprovedEvent.java
│   │   │   │   │   ├── PayslipGeneratedEvent.java
│   │   │   │   │   └── PayrollCalculationRequestedEvent.java
│   │   │   │   ├── repository/             # Port interfaces
│   │   │   │   │   ├── PayrollRunRepository.java
│   │   │   │   │   └── PayslipRepository.java
│   │   │   │   └── exception/
│   │   │   ├── infrastructure/
│   │   │   │   ├── persistence/jpa/
│   │   │   │   │   ├── PayrollRunEntity.java
│   │   │   │   │   ├── PayrollRunJpaRepository.java
│   │   │   │   │   └── PayrollRunRepositoryAdapter.java
│   │   │   │   ├── messaging/
│   │   │   │   │   ├── outbox/
│   │   │   │   │   │   ├── OutboxEventEntity.java
│   │   │   │   │   │   └── OutboxPublisher.java
│   │   │   │   │   ├── KafkaPayrollEventPublisher.java
│   │   │   │   │   └── BpjsServiceClient.java        # REST client to bpjs-service
│   │   │   │   ├── pdf/
│   │   │   │   │   └── OpenHtmlPdfGenerator.java
│   │   │   │   ├── bank/                              # Strategy per bank
│   │   │   │   │   ├── BankFileStrategy.java
│   │   │   │   │   ├── BcaPayrollFileStrategy.java
│   │   │   │   │   ├── MandiriPayrollFileStrategy.java
│   │   │   │   │   ├── BriPayrollFileStrategy.java
│   │   │   │   │   └── BniPayrollFileStrategy.java
│   │   │   │   └── feign/
│   │   │   │       ├── TaxServiceClient.java
│   │   │   │       └── EmployeeServiceClient.java
│   │   │   └── worker/
│   │   │       ├── PayrollCalculationWorker.java     # Kafka consumer
│   │   │       └── PayrollDigestEmailScheduler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/changelog/                          # Liquibase
│   │       │   ├── master.xml
│   │       │   ├── 001-payroll-base.xml
│   │       │   ├── 002-payslip-lines.xml
│   │       │   ├── 003-bank-files.xml
│   │       │   └── 004-outbox.xml
│   │       ├── templates/                             # PDF templates (Pebble)
│   │       │   └── payslip.peb
│   │       └── i18n/
│   └── test/
│       ├── java/com/hirevo/payroll/
│       │   ├── unit/
│       │   │   ├── domain/                            # Pure domain tests
│       │   │   └── application/
│       │   ├── integration/                           # Testcontainers Postgres+Redis+Kafka
│       │   │   ├── PayrollRunIntegrationTest.java
│       │   │   └── BankFileIntegrationTest.java
│       │   ├── architecture/
│       │   │   └── HexagonalArchTest.java             # ArchUnit
│       │   └── golden/                                # Payroll calc golden file tests
│       │       ├── PayrollGoldenTest.java
│       │       └── fixtures/                          # 200+ JSON scenarios
│       └── resources/
└── target/
```

### 2.3 Architecture Pattern: Hexagonal (Ports & Adapters)
- **api/** + **infrastructure/** = adapters.
- **application/** = use cases (port consumers).
- **domain/** = pure business logic, no Spring imports.
- Repository: port interface in `domain/repository/`, JPA adapter in `infrastructure/persistence/jpa/`.

**Enforced via ArchUnit:**
```java
@Test
void domainShouldNotDependOnSpring() {
  noClasses().that().resideInAPackage("..domain..")
    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
    .check(importedClasses);
}
```

### 2.4 Shared Libraries

```
libs/core/
├── exception/                          # HirevoException, BusinessException
├── pagination/                          # CursorPagination
├── result/                             # Result<T, E> for non-exception flow
├── time/                               # DateUtils, JakartaClock
└── validation/

libs/security/
├── jwt/                                # JwtService, JwtAuthFilter
├── permission/                          # PermissionEvaluator, @RequirePermission
├── webauthn/
├── totp/
└── audit/                              # @Audited annotation

libs/tenant/
├── TenantContext.java                  # ThreadLocal
├── TenantContextFilter.java
├── DataSourceTenantInterceptor.java    # SET LOCAL app.current_tenant_id
└── MultiTenantProperties.java

libs/audit/
├── AuditedAspect.java                  # AOP for @Audited
├── HibernateRevisionEntity.java
├── AuditEventProducer.java             # Kafka producer
└── AuditMaskAnnotation.java

libs/messaging/
├── outbox/
├── KafkaTopicNames.java                # Type-safe topic names
└── EventEnvelope.java                  # Standard wrapper

libs/integration-bank/
├── BankFileStrategy.java
├── BcaStrategy.java
├── MandiriStrategy.java
└── ...

libs/rule-engine/
├── tax/
│   ├── PphTerEngine.java
│   ├── PphAnnualEngine.java
│   └── RulePack.java                   # versioned tariff
├── bpjs/
│   ├── BpjsCalculator.java
│   └── BpjsRate.java
└── overtime/
    └── Pp35OvertimeCalculator.java

libs/test-fixtures/
├── EmployeeFixture.java
├── PayrollFixture.java
└── TestcontainersConfig.java
```

### 2.5 API Gateway (`services/api-gateway`)
- **Spring Cloud Gateway** (reactive).
- Routes loaded from `application.yml` or Kubernetes ConfigMap.
- Per-route: JWT validate, tenant resolve, rate limit, request log, circuit breaker.
- WebSocket support for AI chat streaming + notifications.

```yaml
# Excerpt application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: payroll-svc
          uri: lb://payroll-service
          predicates:
            - Path=/v1/payroll/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
            - name: CircuitBreaker
              args:
                name: payrollCB
                fallbackUri: forward:/fallback/payroll
```

### 2.6 Dockerfile (per service, multi-stage)

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml ./
COPY ../../libs ./libs
RUN ./mvnw -B -ntp dependency:go-offline
COPY src ./src
RUN ./mvnw -B -ntp package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-XX:+UseZGC","-XX:MaxRAMPercentage=75","-jar","app.jar"]
```

---

## 3. Web Frontend (Next.js 15)

```
web/
├── package.json
├── tsconfig.json
├── next.config.mjs
├── tailwind.config.ts
├── components.json                  # shadcn/ui config
├── public/
├── src/
│   ├── app/                          # App Router
│   │   ├── (auth)/
│   │   │   ├── login/
│   │   │   ├── signup/
│   │   │   ├── mfa/
│   │   │   └── layout.tsx
│   │   ├── (dashboard)/
│   │   │   ├── layout.tsx           # Sidebar + topbar
│   │   │   ├── page.tsx             # HR Dashboard
│   │   │   ├── employees/
│   │   │   │   ├── page.tsx
│   │   │   │   ├── [id]/page.tsx
│   │   │   │   └── new/page.tsx
│   │   │   ├── attendance/
│   │   │   ├── leaves/
│   │   │   ├── payroll/
│   │   │   │   ├── runs/
│   │   │   │   └── periods/
│   │   │   ├── reimbursements/
│   │   │   ├── loans/
│   │   │   ├── recruitments/
│   │   │   ├── performance/
│   │   │   ├── assets/
│   │   │   ├── reports/
│   │   │   ├── settings/
│   │   │   └── workflows/
│   │   ├── careers/
│   │   │   └── [tenant]/
│   │   │       └── [slug]/page.tsx  # Public career page
│   │   ├── api/                      # BFF (proxy + auth refresh)
│   │   │   └── proxy/[...path]/route.ts
│   │   ├── layout.tsx
│   │   └── globals.css
│   ├── components/
│   │   ├── ui/                       # shadcn primitives
│   │   ├── auth/
│   │   ├── employee/
│   │   ├── attendance/
│   │   ├── payroll/
│   │   ├── charts/
│   │   ├── data-table/
│   │   ├── form/
│   │   └── layout/
│   ├── lib/
│   │   ├── api/                      # API client (TanStack Query hooks)
│   │   │   ├── http.ts               # axios + interceptor
│   │   │   ├── auth.ts
│   │   │   ├── employees.ts
│   │   │   ├── payroll.ts
│   │   │   └── generated/            # openapi-typescript outputs
│   │   ├── auth/
│   │   ├── i18n/
│   │   ├── utils/
│   │   └── hooks/
│   ├── store/                        # Zustand
│   │   ├── auth.store.ts
│   │   ├── tenant.store.ts
│   │   └── ui.store.ts
│   └── styles/
├── e2e/                              # Playwright
│   ├── login.spec.ts
│   ├── payroll-run.spec.ts
│   └── fixtures/
└── messages/                          # i18n bundles
    ├── id.json
    └── en.json
```

**Key conventions:**
- Server Components by default; `'use client'` only when needed.
- API calls via Server Actions for mutations; TanStack Query for cache/refetch.
- Auth: refresh token httpOnly cookie + access token in memory.
- shadcn components customized via `components/ui/` (own copies).
- Forms: react-hook-form + zod resolver.

---

## 4. Mobile (Flutter 3)

```
mobile/
├── pubspec.yaml
├── analysis_options.yaml
├── android/
├── ios/
├── lib/
│   ├── main.dart
│   ├── app/
│   │   ├── app.dart
│   │   ├── router.dart                # go_router
│   │   └── theme.dart
│   ├── core/
│   │   ├── env.dart                   # flavor configs
│   │   ├── di/                        # get_it
│   │   ├── network/
│   │   │   ├── dio_client.dart
│   │   │   ├── auth_interceptor.dart
│   │   │   ├── retry_interceptor.dart
│   │   │   └── error_handler.dart
│   │   ├── storage/
│   │   │   ├── secure_storage.dart    # tokens
│   │   │   └── hive_boxes.dart        # offline cache
│   │   ├── notifications/
│   │   │   └── fcm_service.dart
│   │   ├── analytics/
│   │   └── biometric/
│   ├── features/
│   │   ├── auth/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   └── presentation/
│   │   │       ├── pages/
│   │   │       └── widgets/
│   │   ├── home/
│   │   ├── attendance/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   ├── ml/                    # TFLite liveness
│   │   │   │   ├── liveness_detector.dart
│   │   │   │   └── face_detector.dart
│   │   │   └── presentation/
│   │   ├── leaves/
│   │   ├── payslip/
│   │   ├── reimbursement/
│   │   ├── loan/
│   │   ├── directory/
│   │   ├── notifications/
│   │   ├── profile/
│   │   └── ai_chat/
│   ├── shared/
│   │   ├── widgets/
│   │   ├── extensions/
│   │   └── utils/
│   └── l10n/
│       ├── intl_id.arb
│       └── intl_en.arb
├── assets/
│   ├── fonts/
│   ├── images/
│   └── ml_models/                     # arcface_lite.tflite (small inference)
├── test/
├── integration_test/                  # Patrol
└── flavors/
    ├── dev/
    ├── staging/
    └── prod/
```

**Conventions:**
- State: **Riverpod 2.x** (no setState in features).
- Routing: **go_router** with type-safe routes.
- Network: **Dio** + interceptors; offline = Hive cache + queue.
- Sensitive data: **flutter_secure_storage** + biometric unlock.
- Camera + ML: `camera` + `google_ml_kit` for face detection; custom TFLite for liveness.
- GPS: `geolocator` + `flutter_background_geolocation` for tracking.
- Build flavors via `--flavor` and `dart-define`.

---

## 5. Infrastructure (`infra/`)

```
infra/
├── terraform/
│   ├── modules/
│   │   ├── eks/
│   │   ├── rds-aurora/
│   │   ├── redis-elasticache/
│   │   ├── msk-kafka/
│   │   ├── opensearch/
│   │   ├── s3-tenant-bucket/
│   │   ├── kms-tenant-key/
│   │   ├── vpc/
│   │   └── waf/
│   ├── envs/
│   │   ├── dev/
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   └── terraform.tfvars
│   │   ├── staging/
│   │   └── prod/
│   └── backend.tf                     # S3 + DynamoDB lock
├── helm/                              # Helm charts per service
│   ├── hirevo-payroll/
│   ├── hirevo-iam/
│   └── ...
├── argocd/
│   ├── applications/                  # ApplicationSet
│   │   ├── apps-dev.yaml
│   │   ├── apps-staging.yaml
│   │   └── apps-prod.yaml
│   └── projects/
├── kustomize/                         # Per-env overlays
│   ├── base/
│   └── overlays/
│       ├── dev/
│       ├── staging/
│       └── prod/
└── observability/
    ├── prometheus/
    ├── grafana-dashboards/
    └── alert-rules/
```

---

## 6. CI/CD Workflows

### `.github/workflows/ci.yml`
```yaml
name: CI
on:
  pull_request:
    paths: ['backend/**', 'web/**', 'mobile/**']
jobs:
  backend:
    if: contains(github.event.pull_request.changed_files, 'backend/')
    strategy:
      matrix:
        service: [iam, employee, attendance, leave, payroll, tax, bpjs, ...]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: 21, distribution: temurin, cache: maven }
      - name: Build & Test
        run: cd backend/services/${{ matrix.service }}-service && ./mvnw verify
      - name: Sonar
        run: ./mvnw sonar:sonar -Dsonar.token=${{ secrets.SONAR_TOKEN }}
      - name: Snyk
        run: snyk test --severity-threshold=high
      - name: Build Docker
        if: github.event_name == 'push' && github.ref == 'refs/heads/main'
        run: |
          docker build -t ghcr.io/hirevo/${{ matrix.service }}-service:${{ github.sha }} .
          cosign sign --yes ghcr.io/hirevo/${{ matrix.service }}-service:${{ github.sha }}
          docker push ghcr.io/hirevo/${{ matrix.service }}-service:${{ github.sha }}
  web:
    ...
  mobile:
    ...
```

### `.github/workflows/cd-staging.yml`
- On merge to main → bump image tag in `infra/argocd/applications/apps-staging.yaml` → ArgoCD sync.

---

## 7. Tooling

| Tool | Purpose |
|------|---------|
| **SonarQube** | Code quality + coverage |
| **Snyk / Dependabot** | Dependency vuln |
| **Trivy** | Container scan |
| **Cosign** | Image signing (sigstore) |
| **gitleaks** | Secret detection (pre-commit + CI) |
| **pre-commit hooks** | Format (spotless, prettier, dart format) |
| **Renovate** | Auto-update dependencies |
| **Spectral** | OpenAPI linting |
| **k6** | Load testing |
| **Patrol** | Flutter E2E |
| **Playwright** | Web E2E |

---

## 8. Dev Environment (`docker-compose.yml` at repo root)

```yaml
version: '3.9'
services:
  postgres:
    image: postgres:16
    environment: { POSTGRES_PASSWORD: dev, POSTGRES_DB: hirevo }
    ports: ["5432:5432"]
    volumes: [pgdata:/var/lib/postgresql/data]
  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
  kafka:
    image: confluentinc/cp-kafka:7.6.0
    ...
  opensearch:
    image: opensearchproject/opensearch:2.13.0
    ...
  minio:
    image: minio/minio
    command: server /data
    ports: ["9000:9000", "9001:9001"]
  mailhog:
    image: mailhog/mailhog
    ports: ["8025:8025"]
  jaeger:
    image: jaegertracing/all-in-one
    ports: ["16686:16686"]
volumes:
  pgdata:
```

Single command to start: `docker compose up -d`.
Each service: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.

---

## 9. Code Quality Gates (enforced in PR)

- ✅ Coverage line ≥ 80%, branch ≥ 70%.
- ✅ Mutation score (Pitest) ≥ 60% for `/domain/` packages.
- ✅ SonarQube quality gate pass (no new code smells, bugs, vulns).
- ✅ Snyk: no high/critical vulns in deps.
- ✅ Trivy: no high/critical in container.
- ✅ ArchUnit: hexagonal architecture invariants pass.
- ✅ OpenAPI: backward-compatible (Spectral diff vs main).
- ✅ Liquibase: changeset present if entity changed.
- ✅ Conventional commits.

---

## 10. Repository Hygiene

- **CODEOWNERS** per top-level folder.
- **DCO** (Developer Certificate of Origin) sign-off required.
- **Branch protection**: 1 review + all checks pass + linear history.
- **No force push** to main.
- **Pre-commit hook**: lint + secret scan.
