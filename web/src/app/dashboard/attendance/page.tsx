'use client';

import { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import {
  api, getErrorMessage,
  type AttendanceLogEntry, type EmployeeSummary, type WorkLocation,
} from '@/lib/api';

export default function AttendancePage() {
  const [locations, setLocations] = useState<WorkLocation[]>([]);
  const [employees, setEmployees] = useState<EmployeeSummary[]>([]);
  const [logs, setLogs] = useState<AttendanceLogEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadAll() {
    setLoading(true);
    setError(null);
    try {
      const [locs, emps, logsRes] = await Promise.all([
        api.listWorkLocations(),
        // Unfiltered by status: logs can belong to now-resigned employees
        // (e.g. someone flagged for fraud right before leaving), and the
        // name lookup below needs to resolve those too, not just active staff.
        api.listEmployees({ size: 100 }),
        api.listAttendanceLogs({}),
      ]);
      setLocations(locs);
      setEmployees(emps.data);
      setLogs(logsRes);
    } catch (err) {
      setError(getErrorMessage(err, 'Gagal memuat data attendance'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAll();
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold">Attendance</h1>
      <p className="mt-1 text-fg-muted">Geofence, simulasi absen, dan log kehadiran.</p>

      {error && (
        <p className="mt-4 rounded-md border border-danger bg-danger-bg px-3 py-2 text-sm text-danger">{error}</p>
      )}

      <div className="mt-6 grid gap-6 md:grid-cols-2">
        <WorkLocationsCard locations={locations} onCreated={loadAll} />
        <ClockSimulatorCard employees={employees} locations={locations} onDone={loadAll} />
      </div>

      <Card className="mt-6">
        <CardHeader><CardTitle>Log Kehadiran (30 hari terakhir)</CardTitle></CardHeader>
        <CardContent className="p-0">
          {loading ? (
            <div className="p-8 text-center text-fg-muted">Memuat…</div>
          ) : logs.length === 0 ? (
            <div className="p-8 text-center text-fg-muted">Belum ada data kehadiran.</div>
          ) : (
            <table className="w-full text-sm">
              <thead className="border-b border-border-subtle bg-sunken text-left text-xs uppercase text-fg-muted">
                <tr>
                  <th className="px-4 py-3">Tanggal</th>
                  <th className="px-4 py-3">Karyawan</th>
                  <th className="px-4 py-3">Masuk</th>
                  <th className="px-4 py-3">Keluar</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Fraud Score</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => {
                  const emp = employees.find((e) => e.id === log.employeeId);
                  return (
                    <tr key={log.id} className="border-b border-border-subtle last:border-0">
                      <td className="px-4 py-3">{log.workDate}</td>
                      <td className="px-4 py-3 font-medium">{emp?.fullName ?? log.employeeId.slice(0, 8)}</td>
                      <td className="px-4 py-3 text-fg-muted">{log.clockInAt ? new Date(log.clockInAt).toLocaleTimeString('id-ID') : '—'}</td>
                      <td className="px-4 py-3 text-fg-muted">{log.clockOutAt ? new Date(log.clockOutAt).toLocaleTimeString('id-ID') : '—'}</td>
                      <td className="px-4 py-3"><StatusBadge status={log.status} /></td>
                      <td className="px-4 py-3">
                        <FraudBadge score={log.fraudScore} reason={log.anomalyReason} />
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
  );
}

function WorkLocationsCard({ locations, onCreated }: { locations: WorkLocation[]; onCreated: () => void }) {
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [lat, setLat] = useState('');
  const [lng, setLng] = useState('');
  const [radius, setRadius] = useState('100');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await api.createWorkLocation({
        name, latitude: Number(lat), longitude: Number(lng), radiusMeters: Number(radius),
      });
      setShowForm(false);
      setName(''); setLat(''); setLng('');
      onCreated();
    } catch (err) {
      setError(getErrorMessage(err, 'Gagal menyimpan lokasi'));
    } finally {
      setSaving(false);
    }
  }

  return (
    <Card>
      <CardHeader className="flex-row items-center justify-between space-y-0">
        <CardTitle>Lokasi Kerja</CardTitle>
        <Button size="sm" variant="secondary" onClick={() => setShowForm((s) => !s)}>
          {showForm ? 'Batal' : '+ Tambah'}
        </Button>
      </CardHeader>
      <CardContent>
        {showForm && (
          <form onSubmit={onSubmit} className="mb-4 space-y-3 rounded-md border border-border-subtle p-3">
            <Input placeholder="Nama lokasi (Kantor Pusat)" required value={name} onChange={(e) => setName(e.target.value)} />
            <div className="grid grid-cols-3 gap-2">
              <Input placeholder="Latitude" required type="number" step="any" value={lat} onChange={(e) => setLat(e.target.value)} />
              <Input placeholder="Longitude" required type="number" step="any" value={lng} onChange={(e) => setLng(e.target.value)} />
              <Input placeholder="Radius (m)" required type="number" value={radius} onChange={(e) => setRadius(e.target.value)} />
            </div>
            {error && <p className="text-xs text-danger">{error}</p>}
            <Button type="submit" size="sm" disabled={saving}>{saving ? 'Menyimpan…' : 'Simpan Lokasi'}</Button>
          </form>
        )}
        {locations.length === 0 ? (
          <p className="text-sm text-fg-muted">Belum ada lokasi kerja terdaftar.</p>
        ) : (
          <ul className="space-y-2">
            {locations.map((loc) => (
              <li key={loc.id} className="rounded-md bg-sunken px-3 py-2 text-sm">
                <p className="font-medium">{loc.name}</p>
                <p className="text-xs text-fg-subtle">
                  {loc.latitude.toFixed(4)}, {loc.longitude.toFixed(4)} · radius {loc.radiusMeters}m
                </p>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}

function ClockSimulatorCard({
  employees, locations, onDone,
}: { employees: EmployeeSummary[]; locations: WorkLocation[]; onDone: () => void }) {
  const [employeeId, setEmployeeId] = useState('');
  const [lat, setLat] = useState('');
  const [lng, setLng] = useState('');
  const [mockGps, setMockGps] = useState(false);
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState<AttendanceLogEntry | null>(null);
  const [error, setError] = useState<string | null>(null);

  function useOfficeLocation() {
    if (locations[0]) {
      setLat(String(locations[0].latitude));
      setLng(String(locations[0].longitude));
    }
  }

  function useBrowserGeolocation() {
    if (!navigator.geolocation) {
      setError('Browser tidak mendukung geolocation');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLat(String(pos.coords.latitude));
        setLng(String(pos.coords.longitude));
      },
      (err) => setError(`Geolocation gagal: ${err.message}`)
    );
  }

  async function act(kind: 'in' | 'out') {
    if (!employeeId || !lat || !lng) {
      setError('Pilih karyawan dan lokasi terlebih dahulu');
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const payload = {
        employeeId, latitude: Number(lat), longitude: Number(lng),
        accuracyMeters: 10, isMockLocation: mockGps, source: 'web',
      };
      const res = kind === 'in' ? await api.clockIn(payload) : await api.clockOut(payload);
      setResult(res);
      onDone();
    } catch (err) {
      setError(getErrorMessage(err, 'Gagal memproses absensi'));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card>
      <CardHeader><CardTitle>Simulasi Absen</CardTitle></CardHeader>
      <CardContent className="space-y-3">
        <p className="text-xs text-fg-subtle">
          Tidak ada perangkat GPS/kamera asli di lingkungan ini — form ini memanggil API clock-in/out
          yang sama seperti aplikasi mobile akan gunakan, dengan koordinat manual atau geolocation browser.
        </p>
        <select
          className="h-11 w-full rounded-md border border-border bg-sunken px-3 text-sm"
          value={employeeId}
          onChange={(e) => setEmployeeId(e.target.value)}
        >
          <option value="">Pilih karyawan…</option>
          {employees.map((e) => (
            <option key={e.id} value={e.id}>{e.fullName} ({e.employeeNo})</option>
          ))}
        </select>
        <div className="grid grid-cols-2 gap-2">
          <Input placeholder="Latitude" type="number" step="any" value={lat} onChange={(e) => setLat(e.target.value)} />
          <Input placeholder="Longitude" type="number" step="any" value={lng} onChange={(e) => setLng(e.target.value)} />
        </div>
        <div className="flex gap-2">
          <Button type="button" size="sm" variant="secondary" onClick={useBrowserGeolocation}>📍 Lokasi Saya</Button>
          {locations[0] && (
            <Button type="button" size="sm" variant="secondary" onClick={useOfficeLocation}>🏢 Lokasi Kantor</Button>
          )}
        </div>
        <label className="flex items-center gap-2 text-sm text-fg-muted">
          <input type="checkbox" checked={mockGps} onChange={(e) => setMockGps(e.target.checked)} />
          Simulasikan Mock GPS (uji deteksi fraud)
        </label>

        {error && <p className="text-xs text-danger">{error}</p>}

        <div className="flex gap-2">
          <Button type="button" onClick={() => act('in')} disabled={busy}>Clock In</Button>
          <Button type="button" variant="secondary" onClick={() => act('out')} disabled={busy}>Clock Out</Button>
        </div>

        {result && (
          <div className="rounded-md bg-sunken p-3 text-sm">
            <p>Status: <StatusBadge status={result.status} /></p>
            <p className="mt-1">Fraud Score: <FraudBadge score={result.fraudScore} reason={result.anomalyReason} /></p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function StatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    present: 'bg-success-bg text-success',
    late: 'bg-warning-bg text-warning',
    pending_review: 'bg-danger-bg text-danger',
    absent: 'bg-sunken text-fg-subtle',
  };
  const labels: Record<string, string> = {
    present: 'Hadir', late: 'Terlambat', pending_review: 'Perlu Review', absent: 'Absen',
  };
  return (
    <span className={`inline-block rounded-full px-2 py-1 text-xs font-semibold ${styles[status] ?? 'bg-sunken text-fg-muted'}`}>
      {labels[status] ?? status}
    </span>
  );
}

function FraudBadge({ score, reason }: { score: number; reason: string | null }) {
  const color = score >= 70 ? 'text-danger' : score >= 40 ? 'text-warning' : 'text-success';
  return (
    <span className={`font-semibold ${color}`} title={reason ?? undefined}>
      {score}/100
    </span>
  );
}
