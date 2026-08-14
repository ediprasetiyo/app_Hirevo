'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import type { ReactNode } from 'react';
import { authStore } from '@/lib/api';

const nav = [
  { href: '/dashboard', label: 'Dashboard', icon: '🏠' },
  { href: '/dashboard/employees', label: 'Karyawan', icon: '👥' },
  { href: '/dashboard/attendance', label: 'Attendance', icon: '⏰' },
  { href: '/dashboard/leaves', label: 'Cuti', icon: '🌴' },
  { href: '/dashboard/payroll', label: 'Payroll', icon: '💰' },
  { href: '/dashboard/reimbursements', label: 'Reimbursement', icon: '🧾' },
  { href: '/dashboard/loans', label: 'Pinjaman', icon: '💳' },
  { href: '/dashboard/reports', label: 'Laporan', icon: '📊' },
  { href: '/dashboard/settings', label: 'Setelan', icon: '⚙️' },
];

export default function DashboardLayout({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  function isActive(href: string) {
    return href === '/dashboard' ? pathname === href : pathname.startsWith(href);
  }

  function onLogout() {
    authStore.clear();
    router.push('/login');
  }

  return (
    <div className="flex min-h-screen bg-canvas">
      <aside className="hidden w-60 flex-col border-r border-border-subtle bg-surface md:flex">
        <div className="p-6">
          <Link href="/" className="text-xl font-bold text-brand">
            Hirevo
          </Link>
          <p className="mt-1 text-xs text-fg-subtle">acme.hirevo.id</p>
        </div>
        <nav className="flex-1 px-3">
          {nav.map((n) => (
            <Link
              key={n.href}
              href={n.href}
              className={`mb-1 flex items-center gap-3 rounded-md px-3 py-2 text-sm ${
                isActive(n.href) ? 'bg-brand-subtle font-semibold text-brand-fg' : 'text-fg hover:bg-sunken'
              }`}
            >
              <span>{n.icon}</span>
              <span>{n.label}</span>
            </Link>
          ))}
        </nav>
        <div className="border-t border-border-subtle p-3">
          <Link href="/wireframes" className="block rounded-md px-3 py-2 text-xs text-fg-muted hover:bg-sunken">
            🎨 Preview Wireframes
          </Link>
          <button
            onClick={onLogout}
            className="block w-full rounded-md px-3 py-2 text-left text-xs text-fg-muted hover:bg-sunken"
          >
            → Keluar
          </button>
        </div>
      </aside>

      <div className="flex-1">
        <header className="sticky top-0 z-10 flex h-16 items-center justify-between border-b border-border-subtle bg-surface px-6">
          <input
            type="search"
            placeholder="🔍 Cari karyawan, cuti, slip… (⌘K)"
            className="h-10 w-96 rounded-full bg-sunken px-4 text-sm placeholder:text-fg-subtle focus:outline-none focus-visible:ring-2 focus-visible:ring-brand"
          />
          <div className="flex items-center gap-4">
            <button className="relative text-xl">
              🔔
              <span className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-danger text-[10px] font-bold text-white">
                3
              </span>
            </button>
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-brand text-xs font-bold text-white">
              ED
            </div>
          </div>
        </header>
        <main className="p-6">{children}</main>
      </div>
    </div>
  );
}
