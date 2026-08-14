'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { api, getErrorMessage, type EmployeePage } from '@/lib/api';
import { formatDate } from '@/lib/utils';

export default function EmployeesPage() {
  const [data, setData] = useState<EmployeePage | null>(null);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function load(searchTerm: string) {
    setLoading(true);
    setError(null);
    try {
      const res = await api.listEmployees({ search: searchTerm || undefined, size: 50 });
      setData(res);
    } catch (err) {
      setError(getErrorMessage(err, 'Gagal memuat data karyawan'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load('');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function onSearchSubmit(e: React.FormEvent) {
    e.preventDefault();
    load(search);
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Karyawan</h1>
          <p className="mt-1 text-fg-muted">
            {data ? `${data.pagination.totalElements} karyawan terdaftar` : 'Memuat…'}
          </p>
        </div>
        <Button asChild>
          <Link href="/dashboard/employees/new">+ Tambah Karyawan</Link>
        </Button>
      </div>

      <form onSubmit={onSearchSubmit} className="mt-6 flex gap-2">
        <Input
          placeholder="Cari nama atau nomor karyawan…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-sm"
        />
        <Button type="submit" variant="secondary">Cari</Button>
      </form>

      {error && (
        <p className="mt-4 rounded-md border border-danger bg-danger-bg px-3 py-2 text-sm text-danger">
          {error}
        </p>
      )}

      <Card className="mt-6 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-fg-muted">Memuat…</div>
        ) : !data || data.data.length === 0 ? (
          <div className="p-12 text-center">
            <p className="text-fg-muted">Belum ada karyawan.</p>
            <Button asChild className="mt-4">
              <Link href="/dashboard/employees/new">+ Tambah Karyawan Pertama</Link>
            </Button>
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="border-b border-border-subtle bg-sunken text-left text-xs uppercase text-fg-muted">
              <tr>
                <th className="px-4 py-3">No. Karyawan</th>
                <th className="px-4 py-3">Nama</th>
                <th className="px-4 py-3">Kontrak</th>
                <th className="px-4 py-3">Tgl. Masuk</th>
                <th className="px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody>
              {data.data.map((emp) => (
                <tr key={emp.id} className="border-b border-border-subtle last:border-0 hover:bg-sunken">
                  <td className="px-4 py-3">
                    <Link href={`/dashboard/employees/${emp.id}`} className="font-mono text-brand-fg">
                      {emp.employeeNo}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    <Link href={`/dashboard/employees/${emp.id}`} className="font-medium hover:underline">
                      {emp.fullName}
                    </Link>
                    <p className="text-xs text-fg-subtle">{emp.personalEmail}</p>
                  </td>
                  <td className="px-4 py-3 uppercase text-fg-muted">{emp.contractType ?? '—'}</td>
                  <td className="px-4 py-3 text-fg-muted">{formatDate(emp.hireDate)}</td>
                  <td className="px-4 py-3">
                    <StatusBadge status={emp.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}

function StatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    active: 'bg-success-bg text-success',
    probation: 'bg-warning-bg text-warning',
    resigned: 'bg-sunken text-fg-subtle',
    terminated: 'bg-danger-bg text-danger',
  };
  const labels: Record<string, string> = {
    active: 'Aktif',
    probation: 'Probation',
    resigned: 'Resign',
    terminated: 'Diberhentikan',
  };
  return (
    <span className={`rounded-full px-2 py-1 text-xs font-semibold ${styles[status] ?? 'bg-sunken text-fg-muted'}`}>
      {labels[status] ?? status}
    </span>
  );
}
