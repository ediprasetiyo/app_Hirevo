# Hirevo HRIS — Documentation Index

**Project:** Hirevo HRIS (SaaS Multi-Tenant untuk Indonesia)
**Stack:** Java 21 + Spring Boot 3 (BE) · Next.js 15 + React + TS + shadcn (Web) · Flutter (Mobile) · PostgreSQL + Redis · Kubernetes (AWS/GCP)
**Status:** Design Phase v2.0
**Tanggal:** 2026-06-16
**Owner:** Edi Prasetiyo

---

## Dokumen

| # | File | Deskripsi |
|---|------|-----------|
| 01 | [PRD v2](01-PRD-v2.md) | Product Requirement Document — visi, persona, scope, 14 modul |
| 02 | [Architecture](02-ARCHITECTURE.md) | High-level + microservice + API + security + multi-tenant + deployment |
| 03 | [User Stories](03-USER-STORIES.md) | Epic & user stories per modul (INVEST + acceptance criteria) |
| 04 | [Use Cases & Activity Diagrams](04-USE-CASES.md) | Use case + activity diagram (Mermaid) per flow utama |
| 05 | [Database Schema v2](05-DATABASE-SCHEMA-v2.md) | DDL Postgres lengkap — extends [ERD.md](../ERD.md) |
| 06 | [API Specification](06-API-SPEC.md) | OpenAPI 3.1 outline per modul + konvensi REST |
| 07 | [UI Wireframes](07-UI-WIREFRAMES.md) | Deskripsi wireframe per screen (web + mobile) |
| 08 | [Sprint Backlog](08-SPRINT-BACKLOG.md) | 12 sprint × 2 minggu untuk MVP |
| 09 | [Source Code Structure](09-SOURCE-STRUCTURE.md) | Layout Maven multi-module + Next.js + Flutter |
| 10 | [Development Roadmap](10-ROADMAP.md) | Phased delivery Q3 2026 – Q4 2027 |

## Referensi Sebelumnya
- [PRD v1](../PRD.md) — versi awal (NestJS stack, di-supersede oleh v2)
- [ERD v1](../ERD.md) — ERD detail per domain (masih relevan, di-extend di doc 05)

## Konvensi Membaca
- Diagram pakai **Mermaid** — bisa di-render di GitHub, GitLab, Obsidian, VS Code (extension `Markdown Preview Mermaid`).
- Code block SQL = PostgreSQL 16.
- Code block Java = Java 21 + Spring Boot 3.
- API path = REST + JSON.
- Bahasa: dokumen utama Bahasa Indonesia, kode/identifier English.

## Kontak
- Product Owner: Edi Prasetiyo (`edi.prasetiyo1994@gmail.com`)
