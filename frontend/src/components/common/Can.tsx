'use client';

import { useSession } from 'next-auth/react';
import { hasPermission, hasAnyPermission, type Permission } from '@/lib/permissions';

interface CanProps {
  children: React.ReactNode;
  I: Permission;
  fallback?: React.ReactNode;
}

interface CanAnyProps {
  children: React.ReactNode;
  any: Permission[];
  fallback?: React.ReactNode;
}

export function Can({ children, I, fallback = null }: CanProps) {
  const { data: session } = useSession();
  const role = session?.user?.role;

  if (hasPermission(role, I)) {
    return <>{children}</>;
  }
  return <>{fallback}</>;
}

export function CanAny({ children, any, fallback = null }: CanAnyProps) {
  const { data: session } = useSession();
  const role = session?.user?.role;

  if (hasAnyPermission(role, any)) {
    return <>{children}</>;
  }
  return <>{fallback}</>;
}
