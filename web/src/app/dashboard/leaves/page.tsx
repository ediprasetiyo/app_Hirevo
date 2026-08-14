'use client';

import { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  api, getErrorMessage,
  type EmployeeSummary, type LeaveBalance, type LeaveRequestEntry, type LeaveType,
} from '@/lib/api';
import { formatDate } from '@/lib/utils';

export default function LeavesPage() {
  const [employees, setEmployees] = useState<EmployeeSummary[]>([]);
  const [selectedEmployee, setSelectedEmployee] = useState('');
  const [types, setTypes] = useState<LeaveType[]>([]);
  const [balances, setBalances] = useState<LeaveBalance[]>([]);
  const [requests, setRequests] = useState<LeaveRequestEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const [emps, allTypes, allRequests] = await Promise.all([
          api.listEmployees({ status: 'active', size: 100 }),
          api.listLeaveTypes(),
          api.listLeaveRequests(),
        ]);
        setEmployees(emps.data);
        setTypes(allTypes);
        setRequests(allRequests);
        if (emps.data[0]) setSelectedEmployee(emps.data[0].id);
      } catch (err) {
        setError(getErrorMessage(err, 'Gagal memuat data cuti'));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  useEffect(() => {
    if (!selectedEmployee) return;
    api.listLeaveBalances(selectedEmployee).then(setBalances).catch(() => setBalances([]));
  }, [selectedEmployee]);

  async function refreshRequests() {
    const all = await api.listLeaveRequests();
    setRequests(all);
    if (selectedEmployee) {
      api.listLeaveBalances(selectedEmployee).then(setBalances).catch(() => {});
    }
  }

  async function onDecide(id: string, action: 'approve' | 'reject') {
    try {
      if (action === 'approve') await api.approveLeaveRequest(id);
      else await api.rejectLeaveRequest(id);
      await refreshRequests();
    } catch (err) {
      alert(getErrorMessage(err, 'Gagal memproses persetujuan'));
    }
  }

  if (loading) return <div className="p-8 text-center text-fg-muted">Memuat…</div>;

  return (
    <div>
      <h1 className="text-2xl font-bold">Cuti</h1>
      <p className="mt-1 text-fg-muted">Saldo cuti, pengajuan, dan persetujuan.</p>

      {error && (
        <p className="mt-4 rounded-md border border-danger bg-danger-bg px-3 py-2 text-sm text-danger">{error}</p>
      )}

      <div className="mt-6 grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader className="flex-row items-center justify-between space-y-0">
              <CardTitle>Saldo Cuti</CardTitle>
              <select
                className="h-9 rounded-md border border-border bg-sunken px-2 text-sm"
                value={selectedEmployee}
                onChange={(e) => setSelectedEmployee(e.target.value)}
              >
                {employees.map((e) => (
                  <option key={e.id} value={e.id}>{e.fullName}</option>
                ))}
              </select>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
                {balances.filter((b) => b.initialBalance > 0).map((b) => (
                  <div key={b.leaveTypeId} className="rounded-md bg-sunken p-3">
                    <p className="text-xs text-fg-muted">{b.leaveTypeName}</p>
                    <p className="text-xl font-bold">{b.remaining}</p>
                    <p className="text-xs text-fg-subtle">dari {b.initialBalance} hari</p>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle>Semua Pengajuan</CardTitle></CardHeader>
            <CardContent className="p-0">
              {requests.length === 0 ? (
                <div className="p-8 text-center text-fg-muted">Belum ada pengajuan cuti.</div>
              ) : (
                <table className="w-full text-sm">
                  <thead className="border-b border-border-subtle bg-sunken text-left text-xs uppercase text-fg-muted">
                    <tr>
                      <th className="px-4 py-3">Karyawan</th>
                      <th className="px-4 py-3">Jenis</th>
                      <th className="px-4 py-3">Tanggal</th>
                      <th className="px-4 py-3">Hari</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3"></th>
                    </tr>
                  </thead>
                  <tbody>
                    {requests.map((r) => {
                      const emp = employees.find((e) => e.id === r.employeeId);
                      return (
                        <tr key={r.id} className="border-b border-border-subtle last:border-0">
                          <td className="px-4 py-3 font-medium">{emp?.fullName ?? r.employeeId.slice(0, 8)}</td>
                          <td className="px-4 py-3">{r.leaveTypeName}</td>
                          <td className="px-4 py-3 text-fg-muted">{formatDate(r.startDate)} – {formatDate(r.endDate)}</td>
                          <td className="px-4 py-3">{r.totalDays}</td>
                          <td className="px-4 py-3"><StatusBadge status={r.status} /></td>
                          <td className="px-4 py-3">
                            {r.status === 'pending' && (
                              <div className="flex gap-1">
                                <Button size="sm" onClick={() => onDecide(r.id, 'approve')}>Setujui</Button>
                                <Button size="sm" variant="secondary" onClick={() => onDecide(r.id, 'reject')}>Tolak</Button>
                              </div>
                            )}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </CardContent>
          </Card>
        </div>

        <SubmitLeaveCard
          employees={employees}
          types={types}
          defaultEmployee={selectedEmployee}
          onSubmitted={refreshRequests}
        />
      </div>
    </div>
  );
}

function SubmitLeaveCard({
  employees, types, defaultEmployee, onSubmitted,
}: {
  employees: EmployeeSummary[]; types: LeaveType[]; defaultEmployee: string; onSubmitted: () => void;
}) {
  const [employeeId, setEmployeeId] = useState(defaultEmployee);
  const [leaveTypeId, setLeaveTypeId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [reason, setReason] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => setEmployeeId(defaultEmployee), [defaultEmployee]);
  useEffect(() => { if (types[0] && !leaveTypeId) setLeaveTypeId(types[0].id); }, [types, leaveTypeId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setSuccess(false);
    try {
      await api.submitLeaveRequest({ employeeId, leaveTypeId, startDate, endDate, reason: reason || undefined });
      setSuccess(true);
      setStartDate(''); setEndDate(''); setReason('');
      onSubmitted();
    } catch (err) {
      setError(getErrorMessage(err, 'Gagal mengajukan cuti'));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card>
      <CardHeader><CardTitle>Ajukan Cuti</CardTitle></CardHeader>
      <CardContent>
        <form onSubmit={onSubmit} className="space-y-3">
          <select
            className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm"
            value={employeeId}
            onChange={(e) => setEmployeeId(e.target.value)}
            required
          >
            <option value="">Pilih karyawan…</option>
            {employees.map((e) => <option key={e.id} value={e.id}>{e.fullName}</option>)}
          </select>
          <select
            className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm"
            value={leaveTypeId}
            onChange={(e) => setLeaveTypeId(e.target.value)}
            required
          >
            {types.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
          </select>
          <div className="grid grid-cols-2 gap-2">
            <input type="date" required value={startDate} onChange={(e) => setStartDate(e.target.value)}
              className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm" />
            <input type="date" required value={endDate} onChange={(e) => setEndDate(e.target.value)}
              className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm" />
          </div>
          <textarea
            placeholder="Alasan (opsional)"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            className="h-20 w-full rounded-md border border-border bg-sunken px-3 py-2 text-sm"
          />
          {error && <p className="text-xs text-danger">{error}</p>}
          {success && <p className="text-xs text-success">Pengajuan berhasil dikirim.</p>}
          <Button type="submit" className="w-full" disabled={busy}>
            {busy ? 'Mengirim…' : 'Ajukan Cuti'}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

function StatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    pending: 'bg-warning-bg text-warning',
    approved: 'bg-success-bg text-success',
    rejected: 'bg-danger-bg text-danger',
    cancelled: 'bg-sunken text-fg-subtle',
  };
  const labels: Record<string, string> = {
    pending: 'Menunggu', approved: 'Disetujui', rejected: 'Ditolak', cancelled: 'Dibatalkan',
  };
  return (
    <span className={`inline-block rounded-full px-2 py-1 text-xs font-semibold ${styles[status] ?? 'bg-sunken text-fg-muted'}`}>
      {labels[status] ?? status}
    </span>
  );
}
