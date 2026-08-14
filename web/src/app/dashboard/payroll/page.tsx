'use client';

import { Fragment, useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  api, getErrorMessage,
  type EmployeeSummary, type PayrollPeriod, type PayrollRun, type Payslip,
} from '@/lib/api';
import { formatIDR } from '@/lib/utils';

export default function PayrollPage() {
  const [employees, setEmployees] = useState<EmployeeSummary[]>([]);
  const [periods, setPeriods] = useState<PayrollPeriod[]>([]);
  const [runs, setRuns] = useState<PayrollRun[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedRun, setExpandedRun] = useState<string | null>(null);
  const [payslips, setPayslips] = useState<Payslip[]>([]);
  const [busyRunId, setBusyRunId] = useState<string | null>(null);

  async function loadAll() {
    try {
      const [emps, allPeriods, allRuns] = await Promise.all([
        api.listEmployees({ status: 'active', size: 100 }),
        api.listPayrollPeriods(),
        api.listPayrollRuns(),
      ]);
      setEmployees(emps.data);
      setPeriods(allPeriods);
      setRuns(allRuns);
    } catch (err) {
      setError(getErrorMessage(err, 'Gagal memuat data payroll'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadAll(); }, []);

  async function toggleExpand(run: PayrollRun) {
    if (expandedRun === run.id) {
      setExpandedRun(null);
      return;
    }
    setExpandedRun(run.id);
    try {
      const list = await api.listRunPayslips(run.id);
      setPayslips(list);
    } catch {
      setPayslips([]);
    }
  }

  async function onCalculate(id: string) {
    setBusyRunId(id);
    try {
      await api.calculatePayrollRun(id);
      await loadAll();
      if (expandedRun === id) {
        setPayslips(await api.listRunPayslips(id));
      }
    } catch (err) {
      alert(getErrorMessage(err, 'Gagal menghitung payroll'));
    } finally {
      setBusyRunId(null);
    }
  }

  async function onApprove(id: string) {
    setBusyRunId(id);
    try {
      await api.approvePayrollRun(id);
      await loadAll();
    } catch (err) {
      alert(getErrorMessage(err, 'Gagal menyetujui payroll'));
    } finally {
      setBusyRunId(null);
    }
  }

  async function onMarkPaid(id: string) {
    setBusyRunId(id);
    try {
      await api.markPayrollRunPaid(id);
      await loadAll();
    } catch (err) {
      alert(getErrorMessage(err, 'Gagal menandai payroll sebagai dibayar'));
    } finally {
      setBusyRunId(null);
    }
  }

  if (loading) return <div className="p-8 text-center text-fg-muted">Memuat…</div>;

  return (
    <div>
      <h1 className="text-2xl font-bold">Payroll</h1>
      <p className="mt-1 text-fg-muted">Periode, proses hitung gaji, dan slip gaji karyawan.</p>

      {error && (
        <p className="mt-4 rounded-md border border-danger bg-danger-bg px-3 py-2 text-sm text-danger">{error}</p>
      )}

      <div className="mt-6 grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2 space-y-6">
          <Card>
            <CardHeader><CardTitle>Payroll Runs</CardTitle></CardHeader>
            <CardContent className="p-0">
              {runs.length === 0 ? (
                <div className="p-8 text-center text-fg-muted">Belum ada payroll run.</div>
              ) : (
                <table className="w-full text-sm">
                  <thead className="border-b border-border-subtle bg-sunken text-left text-xs uppercase text-fg-muted">
                    <tr>
                      <th className="px-4 py-3">Periode</th>
                      <th className="px-4 py-3">Karyawan</th>
                      <th className="px-4 py-3">Total Net</th>
                      <th className="px-4 py-3">Status</th>
                      <th className="px-4 py-3"></th>
                    </tr>
                  </thead>
                  <tbody>
                    {runs.map((r) => (
                      <Fragment key={r.id}>
                        <tr className="border-b border-border-subtle last:border-0">
                          <td className="px-4 py-3 font-medium">{r.periodName ?? r.payrollPeriodId.slice(0, 8)}</td>
                          <td className="px-4 py-3">{r.totalEmployees ?? '—'}</td>
                          <td className="px-4 py-3">{r.totalNet != null ? formatIDR(r.totalNet) : '—'}</td>
                          <td className="px-4 py-3"><StatusBadge status={r.status} /></td>
                          <td className="px-4 py-3">
                            <div className="flex flex-wrap gap-1">
                              {r.status === 'draft' && (
                                <Button size="sm" disabled={busyRunId === r.id} onClick={() => onCalculate(r.id)}>
                                  Hitung
                                </Button>
                              )}
                              {r.status === 'calculated' && (
                                <>
                                  <Button size="sm" disabled={busyRunId === r.id} onClick={() => onCalculate(r.id)} variant="secondary">
                                    Hitung Ulang
                                  </Button>
                                  <Button size="sm" disabled={busyRunId === r.id} onClick={() => onApprove(r.id)}>
                                    Setujui
                                  </Button>
                                </>
                              )}
                              {r.status === 'approved' && (
                                <Button size="sm" disabled={busyRunId === r.id} onClick={() => onMarkPaid(r.id)}>
                                  Tandai Dibayar
                                </Button>
                              )}
                              <Button size="sm" variant="secondary" onClick={() => toggleExpand(r)}>
                                {expandedRun === r.id ? 'Tutup' : 'Detail'}
                              </Button>
                            </div>
                          </td>
                        </tr>
                        {expandedRun === r.id && (
                          <tr className="border-b border-border-subtle bg-sunken">
                            <td colSpan={5} className="px-4 py-3">
                              {payslips.length === 0 ? (
                                <p className="text-fg-muted text-xs">Belum ada slip gaji — klik Hitung terlebih dahulu.</p>
                              ) : (
                                <table className="w-full text-xs">
                                  <thead className="text-left uppercase text-fg-subtle">
                                    <tr>
                                      <th className="py-1 pr-3">Karyawan</th>
                                      <th className="py-1 pr-3">Gross</th>
                                      <th className="py-1 pr-3">PPh21</th>
                                      <th className="py-1 pr-3">BPJS (Emp)</th>
                                      <th className="py-1 pr-3">Net</th>
                                    </tr>
                                  </thead>
                                  <tbody>
                                    {payslips.map((p) => {
                                      const emp = employees.find((e) => e.id === p.employeeId);
                                      return (
                                        <tr key={p.id} className="border-t border-border-subtle">
                                          <td className="py-1.5 pr-3 font-medium">{emp?.fullName ?? p.employeeId.slice(0, 8)}</td>
                                          <td className="py-1.5 pr-3">{formatIDR(p.grossAmount)}</td>
                                          <td className="py-1.5 pr-3">{formatIDR(p.pph21Amount)}</td>
                                          <td className="py-1.5 pr-3">{formatIDR(p.bpjsEmployee)}</td>
                                          <td className="py-1.5 pr-3 font-semibold">{formatIDR(p.netAmount)}</td>
                                        </tr>
                                      );
                                    })}
                                  </tbody>
                                </table>
                              )}
                            </td>
                          </tr>
                        )}
                      </Fragment>
                    ))}
                  </tbody>
                </table>
              )}
            </CardContent>
          </Card>
        </div>

        <div className="space-y-6">
          <CreatePeriodCard onCreated={loadAll} />
          <CreateRunCard periods={periods} onCreated={loadAll} />
        </div>
      </div>
    </div>
  );
}

function CreatePeriodCard({ onCreated }: { onCreated: () => void }) {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.createPayrollPeriod(year, month);
      onCreated();
    } catch (err) {
      setError(getErrorMessage(err, 'Gagal membuat periode'));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card>
      <CardHeader><CardTitle>Buat Periode</CardTitle></CardHeader>
      <CardContent>
        <form onSubmit={onSubmit} className="space-y-3">
          <div className="grid grid-cols-2 gap-2">
            <input type="number" required value={year} onChange={(e) => setYear(Number(e.target.value))}
              className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm" placeholder="Tahun" />
            <select value={month} onChange={(e) => setMonth(Number(e.target.value))}
              className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm">
              {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                <option key={m} value={m}>Bulan {m}</option>
              ))}
            </select>
          </div>
          {error && <p className="text-xs text-danger">{error}</p>}
          <Button type="submit" className="w-full" disabled={busy}>
            {busy ? 'Membuat…' : 'Buat Periode'}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

function CreateRunCard({ periods, onCreated }: { periods: PayrollPeriod[]; onCreated: () => void }) {
  const [periodId, setPeriodId] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => { if (periods[0] && !periodId) setPeriodId(periods[0].id); }, [periods, periodId]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!periodId) return;
    setBusy(true);
    setError(null);
    try {
      await api.createPayrollRun(periodId);
      onCreated();
    } catch (err) {
      setError(getErrorMessage(err, 'Gagal membuat payroll run'));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card>
      <CardHeader><CardTitle>Buat Payroll Run</CardTitle></CardHeader>
      <CardContent>
        <form onSubmit={onSubmit} className="space-y-3">
          <select
            className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm"
            value={periodId}
            onChange={(e) => setPeriodId(e.target.value)}
            required
          >
            <option value="">Pilih periode…</option>
            {periods.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
          {error && <p className="text-xs text-danger">{error}</p>}
          <Button type="submit" className="w-full" disabled={busy || !periodId}>
            {busy ? 'Membuat…' : 'Buat Run'}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

function StatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    draft: 'bg-sunken text-fg-subtle',
    calculating: 'bg-warning-bg text-warning',
    calculated: 'bg-info-bg text-info',
    approved: 'bg-success-bg text-success',
    paid: 'bg-success-bg text-success',
    cancelled: 'bg-danger-bg text-danger',
    failed: 'bg-danger-bg text-danger',
  };
  const labels: Record<string, string> = {
    draft: 'Draft', calculating: 'Menghitung', calculated: 'Terhitung',
    approved: 'Disetujui', paid: 'Dibayar', cancelled: 'Dibatalkan', failed: 'Gagal',
  };
  return (
    <span className={`inline-block rounded-full px-2 py-1 text-xs font-semibold ${styles[status] ?? 'bg-sunken text-fg-muted'}`}>
      {labels[status] ?? status}
    </span>
  );
}
