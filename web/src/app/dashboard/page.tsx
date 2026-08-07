import { Card } from '@/components/ui/card';
import { formatIDR } from '@/lib/utils';

// Server component — reads mock data at build/request time (works on Vercel).
// When live backend is wired, swap the `mocks` call for a fetch to /reports/dashboard/hr.
import { mocks } from '@/lib/mocks';

export default function DashboardPage() {
  const data = mocks['/reports/dashboard/hr']();

  return (
    <div>
      <h1 className="text-2xl font-bold">Halo Edi 👋</h1>
      <p className="mt-1 text-fg-muted">
        Acme Corp · Periode {data.period} · {data.headcount.total} karyawan aktif
      </p>

      {/* Setup checklist banner */}
      <div className="mt-6 flex items-center justify-between rounded-lg border border-brand bg-brand-subtle p-4">
        <div>
          <p className="text-sm font-semibold text-brand-fg">🎯 Setup checklist: 85% selesai</p>
          <p className="mt-1 text-xs text-brand-fg/80">
            Lanjut: Enroll wajah karyawan (32 tersisa) · Konfigurasi shift · Undang finance team
          </p>
        </div>
        <div className="hidden w-48 md:block">
          <div className="h-2 rounded-full bg-white/40">
            <div className="h-2 rounded-full bg-brand" style={{ width: '85%' }} />
          </div>
        </div>
      </div>

      {/* KPI cards */}
      <div className="mt-6 grid gap-4 md:grid-cols-4">
        <Kpi
          label="Headcount"
          value={data.headcount.total.toString()}
          delta={`▲ ${data.headcount.total - 244} karyawan bulan ini`}
          deltaKind="success"
        />
        <Kpi
          label="Attendance Hari Ini"
          value={`${Math.round(data.attendance.presentRate * 100)}%`}
          delta={`${data.attendance.lateCount} terlambat · ${data.attendance.onLeaveCount} cuti`}
          deltaKind="danger"
        />
        <Kpi
          label="Payroll Bulan Lalu"
          value={formatIDR(data.payroll.lastPeriodNet)}
          delta={`▼ ${Math.abs(data.payroll.momPercent * 100).toFixed(1)}% MoM`}
          deltaKind="success"
        />
        <Kpi
          label="⚠ Fraud Alert"
          value={String(data.fraudAlerts.attendance + data.fraudAlerts.reimbursement)}
          delta={`${data.fraudAlerts.attendance} attendance · ${data.fraudAlerts.reimbursement} reimburse`}
          deltaKind="warning"
          featured
        />
      </div>

      {/* Chart + activity */}
      <div className="mt-6 grid gap-4 md:grid-cols-3">
        <Card className="p-6 md:col-span-2">
          <h3 className="text-lg font-semibold">Payroll Cost 6 Bulan Terakhir</h3>
          <div className="mt-6 flex h-56 items-end gap-3">
            {[65, 68, 72, 78, 74, 76].map((h, i) => (
              <div key={i} className="flex flex-1 flex-col items-center gap-2">
                <div
                  className="w-full rounded-t-md bg-brand transition-all hover:bg-brand/80"
                  style={{ height: `${h}%` }}
                />
                <span className="text-xs text-fg-subtle">
                  {['Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul'][i]}
                </span>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-6">
          <h3 className="text-lg font-semibold">Aktivitas Terbaru</h3>
          <ul className="mt-4 space-y-4">
            {data.recentActivity.map((a, i) => (
              <li key={i} className="text-sm">
                <p className="text-fg">{iconOf(a.kind)} {a.text}</p>
                <p className="text-xs text-fg-subtle">{a.at}</p>
              </li>
            ))}
          </ul>
        </Card>
      </div>

      {/* Upcoming */}
      <Card className="mt-6 p-6">
        <h3 className="font-semibold">Akan Datang</h3>
        <p className="mt-2 text-sm text-fg-muted">
          🎂 {data.upcoming.birthdays} ulang tahun minggu ini · 📅 {data.upcoming.contractsExpiring} kontrak habis dalam 30 hari
          · 🏖 {data.upcoming.onLeaveTomorrow} karyawan cuti besok
        </p>
      </Card>
    </div>
  );
}

function Kpi({
  label, value, delta, deltaKind, featured,
}: { label: string; value: string; delta: string; deltaKind: 'success' | 'danger' | 'warning'; featured?: boolean }) {
  const deltaClass = deltaKind === 'success' ? 'text-success' : deltaKind === 'danger' ? 'text-danger' : 'text-warning';
  return (
    <Card className={`p-5 ${featured ? 'border-warning bg-warning-bg' : ''}`}>
      <p className="text-xs font-semibold uppercase tracking-wide text-fg-muted">{label}</p>
      <p className="mt-2 text-3xl font-bold">{value}</p>
      <p className={`mt-2 text-xs ${deltaClass}`}>{delta}</p>
    </Card>
  );
}

function iconOf(kind: string): string {
  switch (kind) {
    case 'success': return '✅';
    case 'warning': return '🕐';
    case 'info':    return '📝';
    default:        return '•';
  }
}
