# Hirevo API

**Spec:** [`openapi.yaml`](openapi.yaml) — OpenAPI 3.1.0

## Highlights

- **90+ endpoints** across all 14 modules (auth, MFA, employee, attendance, leave, payroll, tax, bpjs, reimbursement, loan, workflow, notifications, documents, AI, reports, audit, webhooks).
- **Bearer JWT + subdomain-based tenant resolution** — every request routes through the API gateway, which resolves `acme.hirevo.id` → `X-Tenant-ID` header.
- **RFC 7807** error format (`Problem+JSON`).
- **Cursor pagination** on all list endpoints.
- **Idempotency-Key** required for sensitive POST (payroll approval, bank file generation).
- **4-eyes principle** enforced on payroll approval — reviewer ≠ approver.
- **Streaming** (SSE) for AI chat.
- **Signed URL** flow for document uploads (never proxied through app).

## Local preview

```bash
# With Redocly CLI
npx @redocly/cli preview-docs api/openapi.yaml

# With Swagger UI Docker
docker run -p 8090:8080 -v $PWD/api:/spec swaggerapi/swagger-ui \
  --env SWAGGER_JSON=/spec/openapi.yaml
```

## Generating clients

```bash
# TypeScript client for web frontend
npx openapi-typescript api/openapi.yaml -o web/src/lib/api/generated.ts

# Dart client for Flutter mobile
dart run openapi_generator generate -i api/openapi.yaml \
  -g dart-dio -o mobile/lib/core/network/generated

# Java (for internal service-to-service if needed — usually prefer Feign hand-written)
openapi-generator-cli generate -i api/openapi.yaml -g java -o /tmp/hirevo-java-client
```

## Contract testing

- **Provider side** (per service): springdoc-openapi generates `/v3/api-docs`. CI job diffs vs the checked-in `openapi.yaml` — any drift fails the build. Update the YAML *first*, then update code (spec-first).
- **Consumer side** (web/mobile): TypeScript / Dart types generated from the same YAML — compile errors surface breaking changes at build time.
- **Backward compatibility**: [Spectral](https://stoplight.io/open-source/spectral) rules in CI reject breaking changes on `main`; opt-in bump `/v2` for breaking releases.

## Versioning

- **URI versioning**: `/v1/*`, `/v2/*` for breaking changes.
- **Backward compatibility guarantee** within a major version: no field removal, no type narrowing, no required-field addition, no enum shrinking. Additive changes are always safe.
- **Deprecation**: mark with `deprecated: true` + `x-sunset` extension (RFC 8594) 6 months in advance.

## Security schemes

Currently only Bearer JWT is documented — SSO SAML/OIDC lands in Phase 3 and will add `openIdConnect` + `oauth2` security schemes.

## Webhook events

Subscribers verify signature with:
```
X-Hirevo-Signature: t=<unix_timestamp>,v1=<hex_hmac_sha256>
```
where `v1 = HMAC-SHA256(secret, timestamp + "." + raw_body)`.

Full webhook payload schema is embedded per-event under the `x-webhook-events` extension (add in future revision).
