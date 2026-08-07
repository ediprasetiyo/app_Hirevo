/**
 * Fixtures used when NEXT_PUBLIC_MOCK_MODE=true — powers the Vercel preview
 * so reviewers can click through the whole UI without a running backend.
 */

export const mocks = {
  '/auth/login': () => ({
    accessToken: 'mock.jwt.token',
    refreshToken: 'mock.refresh.token',
    expiresIn: 900,
    tokenType: 'Bearer',
    user: {
      id: '11111111-1111-1111-1111-000000000042',
      email: 'edi@acme.hirevo.id',
      fullName: 'Edi Prasetiyo',
      roles: ['super_admin'],
    },
  }),

  '/me': () => ({
    user_id: '11111111-1111-1111-1111-000000000042',
    tenant_id: '22222222-2222-2222-2222-000000000001',
    fullName: 'Edi Prasetiyo',
    roles: ['super_admin'],
    permissions: ['payroll.run', 'payroll.approve', 'employee.write', 'audit.read'],
  }),

  '/reports/dashboard/hr': () => ({
    period: '2026-07',
    headcount: { total: 248, active: 236, probation: 12 },
    attendance: { presentRate: 0.95, lateCount: 5, onLeaveCount: 12 },
    payroll: { lastPeriodNet: 2241250000, momPercent: -0.012 },
    fraudAlerts: { attendance: 3, reimbursement: 1 },
    recentActivity: [
      { at: '08:15', text: 'Rudi H. clock-in terlambat', kind: 'warning' },
      { at: '2h', text: 'Payroll Juni disetujui Sari', kind: 'success' },
      { at: '3h', text: '3 reimbursement menunggu approval', kind: 'info' },
      { at: '1d', text: 'Karyawan baru: Ahmad — SE', kind: 'success' },
    ],
    upcoming: {
      birthdays: 3,
      contractsExpiring: 5,
      onLeaveTomorrow: 12,
    },
  }),
};
