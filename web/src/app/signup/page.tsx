'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { PasswordInput } from '@/components/ui/password-input';
import { api, getErrorMessage, isMockMode, type TenantSignupResponse } from '@/lib/api';

export default function SignupPage() {
  const [form, setForm] = useState({
    companyName: '',
    subdomain: '',
    adminEmail: '',
    adminFullName: '',
    adminPassword: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<TenantSignupResponse | null>(null);

  function update<K extends keyof typeof form>(key: K, value: string) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await api.signupTenant(form);
      setResult(res);
    } catch (err) {
      setError(getErrorMessage(err, 'Pendaftaran gagal — coba lagi'));
    } finally {
      setLoading(false);
    }
  }

  if (result) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-canvas p-8">
        <div className="max-w-md rounded-lg border border-border-subtle bg-surface p-8 text-center shadow-md">
          <div className="text-5xl">🎉</div>
          <h1 className="mt-4 text-2xl font-bold">Workspace Anda siap</h1>
          <p className="mt-2 text-fg-muted">
            <span className="font-semibold text-fg">{result.tenantUrl}</span> sudah aktif.
            Belum ada verifikasi email di tahap ini — Anda bisa langsung masuk.
          </p>
          {isMockMode && (
            <div className="mt-4 rounded-md border border-info bg-info-bg px-3 py-2 text-xs text-info">
              🧪 Mode preview — tenant di atas adalah data mock, tidak benar-benar dibuat.
            </div>
          )}
          <Button asChild size="lg" className="mt-6 w-full">
            <Link href="/login">Masuk sekarang →</Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-canvas p-8">
      <div className="w-full max-w-md">
        <Link href="/" className="mb-6 block text-2xl font-bold text-brand">
          Hirevo
        </Link>
        <div className="rounded-xl border border-border-subtle bg-surface p-8 shadow-md">
          <h1 className="text-2xl font-bold">Buat workspace baru</h1>
          <p className="mt-2 text-sm text-fg-muted">Trial 14 hari · Free untuk ≤ 5 karyawan · No kartu kredit</p>

          <form onSubmit={onSubmit} className="mt-6 space-y-4">
            <Field
              label="Nama Perusahaan"
              name="companyName"
              placeholder="PT Contoh Sejahtera"
              required
              value={form.companyName}
              onChange={(e) => update('companyName', e.target.value)}
            />
            <div>
              <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-fg-muted">Subdomain</label>
              <div className="flex items-center rounded-md border border-border bg-sunken pr-3">
                <input
                  className="h-11 flex-1 bg-transparent px-3 text-sm placeholder:text-fg-subtle focus:outline-none"
                  placeholder="acme"
                  pattern="^[a-z0-9](?:[a-z0-9-]{1,61}[a-z0-9])?$"
                  required
                  value={form.subdomain}
                  onChange={(e) => update('subdomain', e.target.value.toLowerCase())}
                />
                <span className="text-sm text-fg-subtle">.hirevo.id</span>
              </div>
            </div>
            <Field
              label="Email Admin"
              name="adminEmail"
              type="email"
              placeholder="anda@perusahaan.com"
              required
              value={form.adminEmail}
              onChange={(e) => update('adminEmail', e.target.value)}
            />
            <Field
              label="Nama Lengkap Admin"
              name="adminFullName"
              placeholder="Nama Anda"
              required
              value={form.adminFullName}
              onChange={(e) => update('adminFullName', e.target.value)}
            />
            <div>
              <label htmlFor="adminPassword" className="mb-1 block text-xs font-semibold uppercase tracking-wide text-fg-muted">
                Kata Sandi
              </label>
              <PasswordInput
                id="adminPassword"
                placeholder="Min. 8 karakter"
                required
                minLength={8}
                value={form.adminPassword}
                onChange={(e) => update('adminPassword', e.target.value)}
              />
            </div>

            {error && (
              <p className="rounded-md border border-danger bg-danger-bg px-3 py-2 text-sm text-danger">
                {error}
              </p>
            )}

            <Button type="submit" size="lg" className="w-full" disabled={loading}>
              {loading ? 'Memproses…' : 'Daftar & Buat Workspace'}
            </Button>

            <p className="text-center text-xs text-fg-subtle">
              Dengan mendaftar Anda setuju dengan{' '}
              <a href="#" className="underline">Syarat &amp; Ketentuan</a> dan{' '}
              <a href="#" className="underline">Kebijakan Privasi</a> (UU PDP 27/2022).
            </p>
          </form>
        </div>

        <p className="mt-4 text-center text-sm text-fg-muted">
          Sudah punya akun?{' '}
          <Link href="/login" className="font-semibold text-brand-fg">Masuk →</Link>
        </p>
      </div>
    </div>
  );
}

function Field(
  props: React.InputHTMLAttributes<HTMLInputElement> & { label: string; name: string }
) {
  const { label, ...rest } = props;
  return (
    <div>
      <label htmlFor={rest.name} className="mb-1 block text-xs font-semibold uppercase tracking-wide text-fg-muted">
        {label}
      </label>
      <Input id={rest.name} {...rest} />
    </div>
  );
}
