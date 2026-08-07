# Hirevo Web (Next.js 15)

HR backoffice + public marketing/careers page.

## Stack
- **Next.js 15** (App Router, RSC)
- **React 19**
- **TypeScript 5.6**
- **Tailwind CSS 4** — consumes CSS variables mapped from `design/tokens/*.json`
- **shadcn/ui** primitives (Radix UI + CVA)

## Getting Started

```bash
cd web
npm install --legacy-peer-deps    # React 19 RC needs peer flag
cp .env.example .env.local
npm run dev
```

Open <http://localhost:3000>.

By default runs in **mock mode** — no backend needed. All API calls return
fixtures from `src/lib/mocks.ts`.

To wire against the real backend:
```bash
# .env.local
NEXT_PUBLIC_MOCK_MODE=false
NEXT_PUBLIC_API_URL=http://localhost:8080/v1
NEXT_PUBLIC_DEFAULT_TENANT_SUBDOMAIN=acme
```
Then run the backend stack (`docker compose up -d` + `mvn spring-boot:run` on iam-service).

## Pages

| Route | Purpose |
|-------|---------|
| `/` | Landing/marketing page |
| `/login` | Login form (matches wireframe 01, MFA-ready) |
| `/signup` | Tenant self-signup |
| `/dashboard` | HR dashboard mock (matches wireframe 02) |
| `/wireframes` | Reviewer gallery — renders every SVG from `../design/wireframes/` inline |

## Deploy to Vercel

**One-click:**
```bash
npm install -g vercel
vercel
```

Or via the Vercel dashboard: import this repo, set project root to `web/`, framework `Next.js`. `vercel.json` already sets install-command with `--legacy-peer-deps` and mock mode `true` so the preview works standalone.

**Environment variables** to set in Vercel project settings:
- `NEXT_PUBLIC_MOCK_MODE=true` — default, safe for public preview.
- `NEXT_PUBLIC_API_URL` — leave unset until backend has a public URL.

The `/wireframes` route reads files from `../design/wireframes/` at build time. Since Vercel builds from repo root when the `web/` subdirectory is the project root, ensure Vercel is configured with **Root Directory = `web`** and **Include source files outside the Root Directory** enabled (Settings → General).

If that toggle isn't available on your Vercel plan, the wireframes page falls back to an empty state. In that case, copy the SVGs into `web/public/wireframes/` before deploy.

## Scripts

| Command | Purpose |
|---------|---------|
| `npm run dev` | Dev server with Turbopack |
| `npm run build` | Production build |
| `npm run start` | Production server |
| `npm run lint` | ESLint |
| `npm run typecheck` | Standalone tsc pass |

## Design tokens

Semantic tokens are defined in `src/app/globals.css` (mirror of `design/tokens/light.json` + `design/tokens/dark.json`). Tailwind config in `tailwind.config.ts` maps them to utility classes:

```tsx
<div className="bg-brand text-fg-inverse">…</div>
<div className="bg-surface border border-border-subtle text-fg-muted">…</div>
```

**Do not** hardcode hex colors in components — always go through tokens so tenant white-label (Enterprise plan) can override brand color by swapping `design/tokens/brand.json` + rebuilding CSS.

## Dark mode

- **System-based** by default (via `prefers-color-scheme`).
- Toggleable per-user by adding `.dark` class to `<html>` (theme switcher lives in `/dashboard/settings` — TODO).
