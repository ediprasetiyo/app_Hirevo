import Link from 'next/link';
import { Button } from '@/components/ui/button';

export default function LandingPage() {
  return (
    <main className="min-h-screen">
      {/* Header */}
      <header className="sticky top-0 z-10 border-b border-border-subtle bg-surface/90 backdrop-blur">
        <div className="container flex h-16 items-center justify-between">
          <Link href="/" className="text-xl font-bold text-brand">
            Hirevo
          </Link>
          <nav className="flex items-center gap-6 text-sm">
            <Link href="/wireframes" className="text-fg-muted hover:text-fg">
              Preview UI
            </Link>
            <Link href="/login" className="text-fg-muted hover:text-fg">
              Masuk
            </Link>
            <Button asChild size="sm">
              <Link href="/signup">Daftar Gratis</Link>
            </Button>
          </nav>
        </div>
      </header>

      {/* Hero */}
      <section className="relative overflow-hidden bg-gradient-to-br from-brand to-brand-fg py-24 text-white">
        <div className="container grid gap-12 md:grid-cols-2 md:items-center">
          <div>
            <p className="mb-4 inline-block rounded-full bg-white/10 px-3 py-1 text-xs font-semibold text-white/90">
              🇮🇩 Terdaftar PSE Kominfo · Data di Indonesia
            </p>
            <h1 className="text-4xl font-bold leading-tight md:text-5xl">
              HR yang tidak bikin pusing.
            </h1>
            <p className="mt-6 text-lg text-white/80">
              SaaS HRIS untuk UMKM hingga Enterprise di Indonesia. Payroll otomatis dengan PPh 21
              TER 2024 &amp; BPJS. Absensi mobile dengan face recognition &amp; anti mock GPS.
            </p>
            <div className="mt-8 flex gap-4">
              <Button asChild size="lg" variant="secondary" className="!bg-white !text-brand">
                <Link href="/signup">Mulai Gratis (14 hari)</Link>
              </Button>
              <Button
                asChild
                size="lg"
                variant="ghost"
                className="!border !border-white/30 !text-white hover:!bg-white/10"
              >
                <Link href="/wireframes">Lihat Demo →</Link>
              </Button>
            </div>
            <p className="mt-4 text-sm text-white/70">Free untuk UMKM ≤ 5 karyawan · No kartu kredit.</p>
          </div>

          <div className="hidden md:block">
            <div className="rounded-2xl border border-white/20 bg-white/5 p-6 backdrop-blur">
              <p className="text-sm font-semibold text-white/90">Ringkasan Payroll Juli 2026</p>
              <div className="mt-4 space-y-3">
                <Row label="Total Gross" value="Rp 2.450.000.000" />
                <Row label="PPh 21 (TER)" value="Rp 95.250.000" muted />
                <Row label="BPJS" value="Rp 178.500.000" muted />
                <div className="h-px bg-white/20" />
                <Row label="Net Dibayarkan" value="Rp 2.163.750.000" bold />
              </div>
              <p className="mt-4 text-xs text-white/60">248 karyawan · Rule pack v2024.01</p>
            </div>
          </div>
        </div>
      </section>

      {/* Modules */}
      <section className="container py-20">
        <h2 className="text-center text-3xl font-bold">14 modul, satu platform</h2>
        <p className="mt-2 text-center text-fg-muted">
          Semua yang dibutuhkan HR modern — dan tidak lebih.
        </p>

        <div className="mt-12 grid gap-4 md:grid-cols-3 lg:grid-cols-4">
          {modules.map((m) => (
            <div key={m.name} className="rounded-lg border border-border-subtle bg-surface p-5">
              <div className="text-2xl">{m.icon}</div>
              <p className="mt-3 font-semibold">{m.name}</p>
              <p className="mt-1 text-sm text-fg-muted">{m.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Pricing teaser */}
      <section className="bg-sunken py-20">
        <div className="container text-center">
          <h2 className="text-3xl font-bold">Harga sederhana</h2>
          <p className="mt-2 text-fg-muted">Transparan. Skala sesuai pertumbuhan.</p>
          <div className="mt-10 grid gap-6 md:grid-cols-4">
            <PriceCard tier="Free" price="Rp 0" seats="≤ 5 karyawan" cta="Mulai" />
            <PriceCard tier="Starter" price="Rp 15rb" seats="/kary/bulan" cta="Coba" featured />
            <PriceCard tier="Growth" price="Rp 35rb" seats="/kary/bulan" cta="Coba" />
            <PriceCard tier="Enterprise" price="Custom" seats="500+ karyawan" cta="Kontak Sales" />
          </div>
        </div>
      </section>

      <footer className="border-t border-border-subtle bg-surface py-10 text-center text-sm text-fg-muted">
        © 2026 PT Hirevo Indonesia · Terdaftar PSE Kominfo
      </footer>
    </main>
  );
}

const modules = [
  { icon: '👥', name: 'Employee Management', desc: 'Data karyawan lengkap + audit trail' },
  { icon: '⏰', name: 'Attendance', desc: 'Face + GPS + anti mock location' },
  { icon: '🌴', name: 'Leave Management', desc: 'Preset UU 13/2003' },
  { icon: '💰', name: 'Payroll', desc: 'PPh 21 TER + BPJS + lembur PP 35' },
  { icon: '🧾', name: 'Reimbursement', desc: 'OCR struk + deteksi duplikat' },
  { icon: '💳', name: 'Employee Loan', desc: 'Auto-deduct dari payroll' },
  { icon: '🎯', name: 'Recruitment ATS', desc: 'AI screening CV' },
  { icon: '📊', name: 'Performance', desc: 'OKR + review 360' },
  { icon: '📦', name: 'Asset Management', desc: 'QR code + maintenance' },
  { icon: '📱', name: 'Self-Service Mobile', desc: 'Flutter, offline-capable' },
  { icon: '🤖', name: 'AI Assistant', desc: 'Chatbot + fraud detection' },
  { icon: '📈', name: 'HR Dashboard', desc: 'Real-time + custom reports' },
];

function Row({ label, value, bold, muted }: { label: string; value: string; bold?: boolean; muted?: boolean }) {
  return (
    <div className="flex justify-between">
      <span className={`text-sm ${muted ? 'text-white/60' : 'text-white/80'}`}>{label}</span>
      <span className={`text-sm ${bold ? 'font-bold' : ''} ${muted ? 'text-white/60' : ''}`}>{value}</span>
    </div>
  );
}

function PriceCard({
  tier, price, seats, cta, featured,
}: { tier: string; price: string; seats: string; cta: string; featured?: boolean }) {
  return (
    <div
      className={`rounded-xl border p-6 ${
        featured ? 'border-brand bg-surface shadow-lg ring-2 ring-brand' : 'border-border-subtle bg-surface'
      }`}
    >
      <p className="text-sm font-semibold text-fg-muted">{tier}</p>
      <p className="mt-2 text-3xl font-bold">{price}</p>
      <p className="mt-1 text-xs text-fg-subtle">{seats}</p>
      <Button className="mt-6 w-full" variant={featured ? 'primary' : 'secondary'}>
        {cta}
      </Button>
    </div>
  );
}
