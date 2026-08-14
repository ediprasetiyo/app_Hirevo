'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { api, getErrorMessage, isMockMode } from '@/lib/api';

export default function LoginPage() {
  const router = useRouter();
  // Pre-filled to match the tenant in .env.local's NEXT_PUBLIC_DEFAULT_TENANT_SUBDOMAIN
  // (testco) so this page works out of the box in local live-mode testing.
  const [email, setEmail] = useState('test@testco.com');
  const [password, setPassword] = useState('TestPass123!');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await api.login(email, password);
      router.push('/dashboard');
    } catch (err) {
      setError(getErrorMessage(err, 'Login gagal — coba lagi'));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid min-h-screen md:grid-cols-2">
      {/* Left hero */}
      <div className="hidden bg-gradient-to-br from-brand to-brand-fg p-12 text-white md:flex md:flex-col md:justify-between">
        <Link href="/" className="text-2xl font-bold">
          Hirevo
        </Link>
        <div>
          <h2 className="text-3xl font-bold">HR yang tidak bikin pusing</h2>
          <ul className="mt-8 space-y-3 text-white/90">
            <li>✓ Payroll otomatis dengan PPh 21 TER 2024</li>
            <li>✓ Attendance mobile dengan Face + Anti Mock GPS</li>
            <li>✓ Free untuk UMKM ≤ 5 karyawan</li>
            <li>✓ BPJS auto-calculate + file ekspor SIPP/EDABU</li>
          </ul>
        </div>
        <p className="text-sm text-white/70">✓ Terdaftar PSE Kominfo · Data di Indonesia</p>
      </div>

      {/* Right form */}
      <div className="flex items-center justify-center bg-canvas p-8">
        <div className="w-full max-w-sm">
          <div className="md:hidden mb-8 text-center">
            <Link href="/" className="text-xl font-bold text-brand">
              Hirevo
            </Link>
          </div>
          <h1 className="text-2xl font-bold">Masuk ke akun Anda</h1>
          <p className="mt-2 text-sm text-fg-muted">
            Belum punya akun?{' '}
            <Link href="/signup" className="font-semibold text-brand-fg">
              Daftar gratis
            </Link>
          </p>

          {isMockMode && (
            <div className="mt-4 rounded-md border border-info bg-info-bg px-3 py-2 text-xs text-info">
              🧪 Mode preview — form pre-filled dengan kredensial demo, semua request dijawab data mock.
            </div>
          )}

          <form onSubmit={onSubmit} className="mt-6 space-y-4">
            <div>
              <label htmlFor="email" className="mb-1 block text-xs font-semibold uppercase tracking-wide text-fg-muted">
                Email
              </label>
              <Input
                id="email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
              />
            </div>
            <div>
              <div className="flex items-center justify-between">
                <label htmlFor="password" className="mb-1 block text-xs font-semibold uppercase tracking-wide text-fg-muted">
                  Kata Sandi
                </label>
                <Link href="/forgot" className="text-xs font-semibold text-brand-fg">
                  Lupa?
                </Link>
              </div>
              <Input
                id="password"
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </div>

            <label className="flex items-center gap-2 text-sm text-fg-muted">
              <input type="checkbox" className="rounded border-border" defaultChecked />
              Percayai perangkat ini 30 hari
            </label>

            {error && (
              <p className="rounded-md border border-danger bg-danger-bg px-3 py-2 text-sm text-danger">
                {error}
              </p>
            )}

            <Button type="submit" size="lg" className="w-full" disabled={loading}>
              {loading ? 'Memproses…' : 'Masuk'}
            </Button>
          </form>

          <div className="relative my-6 text-center text-xs text-fg-subtle">
            <span className="bg-canvas px-2">atau</span>
            <div className="absolute inset-x-0 top-1/2 -z-10 h-px bg-border-subtle" />
          </div>

          <Button variant="secondary" className="w-full" size="lg">
            🔑 Masuk dengan Passkey (WebAuthn)
          </Button>
        </div>
      </div>
    </div>
  );
}
