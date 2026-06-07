import { Suspense } from 'react';
import AppLayout from '@/components/layout/AppLayout';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <AppLayout><Suspense>{children}</Suspense></AppLayout>;
}
