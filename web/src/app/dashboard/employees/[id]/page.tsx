'use client';

import { use, useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { api, getErrorMessage, type EmployeeDetail } from '@/lib/api';
import { formatDate, formatIDR } from '@/lib/utils';

export default function EmployeeDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const [employee, setEmployee] = useState<EmployeeDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [resigning, setResigning] = useState(false);

  useEffect(() => {
    api.getEmployee(id)
      .then(setEmployee)
      .catch((err) => setError(getErrorMessage(err, 'Gagal memuat data karyawan')))
      .finally(() => setLoading(false));
  }, [id]);

  async function onResign() {
    if (!confirm(`Proses resign untuk ${employee?.fullName}?`)) return;
    setResigning(true);
    try {
      const updated = await api.resignEmployee(id, new Date().toISOString().slice(0, 10));
      setEmployee(updated);
    } catch (err) {
      alert(getErrorMessage(err, 'Gagal memproses resign'));
    } finally {
      setResigning(false);
    }
  }

  if (loading) return <div className="p-8 text-center text-fg-muted">Memuat…</div>;

  if (error || !employee) {
    return (
      <div>
        <Link href="/dashboard/employees" className="text-sm text-brand-fg">← Kembali</Link>
        <p className="mt-4 rounded-md border border-danger bg-danger-bg px-3 py-2 text-sm text-danger">
          {error ?? 'Karyawan tidak ditemukan'}
        </p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl">
      <Link href="/dashboard/employees" className="text-sm text-brand-fg">← Kembali ke daftar karyawan</Link>

      <div className="mt-2 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{employee.fullName}</h1>
          <p className="text-fg-muted">
            {employee.employeeNo} · {employee.activeContract?.contractType?.toUpperCase() ?? 'Belum ada kontrak'}
          </p>
        </div>
        {employee.status === 'active' && (
          <Button variant="danger" onClick={onResign} disabled={resigning}>
            {resigning ? 'Memproses…' : 'Proses Resign'}
          </Button>
        )}
      </div>

      <div className="mt-6 grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader><CardTitle>Data Pribadi</CardTitle></CardHeader>
          <CardContent className="space-y-3 text-sm">
            <Row label="NIK" value={employee.nikMasked ?? '—'} />
            <Row label="NPWP" value={employee.npwpMasked ?? '—'} />
            <Row label="Tanggal Lahir" value={employee.dateOfBirth ? formatDate(employee.dateOfBirth) : '—'} />
            <Row label="Jenis Kelamin" value={employee.gender === 'male' ? 'Laki-laki' : employee.gender === 'female' ? 'Perempuan' : '—'} />
            <Row label="Status Pernikahan" value={employee.maritalStatus ?? '—'} />
            <Row label="Email" value={employee.personalEmail ?? '—'} />
            <Row label="Telepon" value={employee.phone ?? '—'} />
            <Row label="Alamat" value={employee.address ?? '—'} />
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Kepegawaian</CardTitle></CardHeader>
          <CardContent className="space-y-3 text-sm">
            <Row label="Tanggal Masuk" value={formatDate(employee.hireDate)} />
            <Row label="Status" value={employee.status} />
            {employee.resignDate && <Row label="Tanggal Resign" value={formatDate(employee.resignDate)} />}
            {employee.activeContract && (
              <>
                <div className="h-px bg-border-subtle" />
                <Row label="Jenis Kontrak" value={employee.activeContract.contractType.toUpperCase()} />
                <Row label="Mulai Kontrak" value={formatDate(employee.activeContract.startDate)} />
                <Row label="Gaji Pokok" value={formatIDR(employee.activeContract.baseSalary)} />
                <Row label="Lokasi Kerja" value={employee.activeContract.workArrangement ?? '—'} />
              </>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4">
      <span className="text-fg-muted">{label}</span>
      <span className="text-right font-medium">{value}</span>
    </div>
  );
}
