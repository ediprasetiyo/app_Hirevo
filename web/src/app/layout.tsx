import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-sans',
  display: 'swap',
});

export const metadata: Metadata = {
  title: {
    default: 'Hirevo HRIS — HR yang tidak bikin pusing',
    template: '%s · Hirevo HRIS',
  },
  description:
    'SaaS HRIS untuk UMKM hingga Enterprise di Indonesia. Payroll otomatis dengan PPh 21 TER 2024, attendance mobile dengan face recognition, BPJS auto-calc. Terdaftar PSE Kominfo.',
  metadataBase: new URL(process.env.NEXT_PUBLIC_SITE_URL ?? 'https://hirevo.id'),
  openGraph: {
    title: 'Hirevo HRIS',
    description: 'HR yang tidak bikin pusing — untuk UMKM hingga Enterprise.',
    locale: 'id_ID',
    type: 'website',
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="id" className={inter.variable}>
      <body>{children}</body>
    </html>
  );
}
