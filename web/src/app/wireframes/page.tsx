import Link from 'next/link';
import fs from 'fs/promises';
import path from 'path';

/**
 * Wireframe gallery — reads all SVGs from ../../design/wireframes/ at build time
 * and renders them inline so reviewers can click through the design without
 * needing Figma access.
 */
export default async function WireframesPage() {
  const dir = path.join(process.cwd(), '..', 'design', 'wireframes');
  let items: { name: string; svg: string }[] = [];
  try {
    const files = (await fs.readdir(dir)).filter((f) => f.endsWith('.svg')).sort();
    items = await Promise.all(
      files.map(async (f) => ({
        name: f.replace(/\.svg$/, '').replace(/^\d+-/, '').replace(/-/g, ' '),
        svg: await fs.readFile(path.join(dir, f), 'utf8'),
      })),
    );
  } catch {
    // Directory not found in this environment — gallery renders empty state.
  }

  return (
    <main className="min-h-screen bg-canvas p-8">
      <div className="mx-auto max-w-6xl">
        <div className="mb-8">
          <Link href="/" className="text-sm text-brand-fg">
            ← Home
          </Link>
          <h1 className="mt-2 text-3xl font-bold">Wireframe Preview</h1>
          <p className="mt-2 text-fg-muted">
            Low-fidelity mockups for the primary Hirevo screens. Final high-fidelity designs live in Figma.
          </p>
        </div>

        {items.length === 0 ? (
          <div className="rounded-lg border border-border-subtle bg-surface p-12 text-center">
            <p className="text-fg-muted">No wireframes found in design/wireframes/.</p>
          </div>
        ) : (
          <div className="space-y-12">
            {items.map((item) => (
              <section key={item.name} className="rounded-lg border border-border-subtle bg-surface p-6 shadow-sm">
                <h2 className="mb-4 text-lg font-semibold capitalize">{item.name}</h2>
                <div
                  className="overflow-hidden rounded-md bg-sunken [&_svg]:h-auto [&_svg]:w-full"
                  dangerouslySetInnerHTML={{ __html: item.svg }}
                />
              </section>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

// Force static rendering — SVGs baked in at build time, works on Vercel edge.
export const dynamic = 'force-static';
