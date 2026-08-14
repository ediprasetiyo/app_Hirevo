'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { api, getErrorMessage, type CreateEmployeeRequest } from '@/lib/api';

const initialForm: CreateEmployeeRequest = {
  employeeNo: '',
  fullName: '',
  nik: '',
  dateOfBirth: '',
  gender: 'male',
  maritalStatus: 'single',
  personalEmail: '',
  phone: '',
  hireDate: new Date().toISOString().slice(0, 10),
  contract: {
    contractType: 'pkwt',
    startDate: new Date().toISOString().slice(0, 10),
    baseSalary: 0,
    workArrangement: 'onsite',
  },
};

export default function NewEmployeePage() {
  const router = useRouter();
  const [form, setForm] = useState<CreateEmployeeRequest>(initialForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function update<K extends keyof CreateEmployeeRequest>(key: K, value: CreateEmployeeRequest[K]) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  function updateContract<K extends keyof CreateEmployeeRequest['contract']>(
    key: K,
    value: CreateEmployeeRequest['contract'][K]
  ) {
    setForm((f) => ({ ...f, contract: { ...f.contract, [key]: value } }));
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const created = await api.createEmployee({
        ...form,
        nik: form.nik || undefined,
        personalEmail: form.personalEmail || undefined,
        phone: form.phone || undefined,
        dateOfBirth: form.dateOfBirth || undefined,
      });
      router.push(`/dashboard/employees/${created.id}`);
    } catch (err) {
      setError(getErrorMessage(err, 'Gagal menyimpan karyawan'));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-2xl">
      <Link href="/dashboard/employees" className="text-sm text-brand-fg">
        ← Kembali ke daftar karyawan
      </Link>
      <h1 className="mt-2 text-2xl font-bold">Tambah Karyawan Baru</h1>

      <form onSubmit={onSubmit} className="mt-6 space-y-6">
        <Card>
          <CardHeader><CardTitle>Data Pribadi</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Field label="Nomor Karyawan" required>
                <Input required value={form.employeeNo} onChange={(e) => update('employeeNo', e.target.value)} placeholder="EMP001" />
              </Field>
              <Field label="Nama Lengkap" required>
                <Input required value={form.fullName} onChange={(e) => update('fullName', e.target.value)} />
              </Field>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Field label="NIK (16 digit)">
                <Input
                  value={form.nik}
                  onChange={(e) => update('nik', e.target.value)}
                  pattern="^$|^[0-9]{16}$"
                  title="NIK harus 16 digit angka"
                  placeholder="3201234567890001"
                />
              </Field>
              <Field label="Tanggal Lahir">
                <Input type="date" value={form.dateOfBirth} onChange={(e) => update('dateOfBirth', e.target.value)} />
              </Field>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Jenis Kelamin">
                <select
                  className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm"
                  value={form.gender}
                  onChange={(e) => update('gender', e.target.value)}
                >
                  <option value="male">Laki-laki</option>
                  <option value="female">Perempuan</option>
                </select>
              </Field>
              <Field label="Status Pernikahan">
                <select
                  className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm"
                  value={form.maritalStatus}
                  onChange={(e) => update('maritalStatus', e.target.value)}
                >
                  <option value="single">Belum Menikah</option>
                  <option value="married">Menikah</option>
                  <option value="divorced">Cerai</option>
                  <option value="widowed">Janda/Duda</option>
                </select>
              </Field>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Email Pribadi">
                <Input type="email" value={form.personalEmail} onChange={(e) => update('personalEmail', e.target.value)} />
              </Field>
              <Field label="No. Telepon">
                <Input value={form.phone} onChange={(e) => update('phone', e.target.value)} placeholder="0812xxxxxxxx" />
              </Field>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Kontrak Kerja</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Field label="Tanggal Masuk" required>
                <Input type="date" required value={form.hireDate} onChange={(e) => update('hireDate', e.target.value)} />
              </Field>
              <Field label="Jenis Kontrak">
                <select
                  className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm"
                  value={form.contract.contractType}
                  onChange={(e) => updateContract('contractType', e.target.value)}
                >
                  <option value="pkwt">PKWT (Kontrak)</option>
                  <option value="pkwtt">PKWTT (Tetap)</option>
                  <option value="magang">Magang</option>
                  <option value="harian_lepas">Harian Lepas</option>
                  <option value="outsource">Outsource</option>
                  <option value="part_time">Part Time</option>
                </select>
              </Field>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Gaji Pokok (Rp)" required>
                <Input
                  type="number"
                  required
                  min={0}
                  value={form.contract.baseSalary || ''}
                  onChange={(e) => updateContract('baseSalary', Number(e.target.value))}
                />
              </Field>
              <Field label="Lokasi Kerja">
                <select
                  className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm"
                  value={form.contract.workArrangement}
                  onChange={(e) => updateContract('workArrangement', e.target.value)}
                >
                  <option value="onsite">Onsite</option>
                  <option value="remote">Remote</option>
                  <option value="hybrid">Hybrid</option>
                </select>
              </Field>
            </div>
          </CardContent>
        </Card>

        {error && (
          <p className="rounded-md border border-danger bg-danger-bg px-3 py-2 text-sm text-danger">{error}</p>
        )}

        <div className="flex gap-3">
          <Button type="submit" size="lg" disabled={loading}>
            {loading ? 'Menyimpan…' : 'Simpan Karyawan'}
          </Button>
          <Button asChild type="button" variant="secondary" size="lg">
            <Link href="/dashboard/employees">Batal</Link>
          </Button>
        </div>
      </form>
    </div>
  );
}

function Field({ label, required, children }: { label: string; required?: boolean; children: React.ReactNode }) {
  return (
    <div>
      <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-fg-muted">
        {label}{required && <span className="text-danger"> *</span>}
      </label>
      {children}
    </div>
  );
}
