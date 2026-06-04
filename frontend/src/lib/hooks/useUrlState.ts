import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { useCallback, useMemo } from 'react';

export function useUrlState<T extends Record<string, string | undefined>>(defaults: T) {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  const state = useMemo(() => {
    const result = { ...defaults } as any;
    if (searchParams) {
      for (const key of Object.keys(defaults)) {
        const val = searchParams.get(key);
        if (val !== null) result[key] = val;
      }
    }
    return result as T;
  }, [searchParams, defaults]);

  const setState = useCallback((newState: Partial<T>) => {
    const params = new URLSearchParams(searchParams?.toString() ?? '');
    for (const [key, value] of Object.entries(newState)) {
      if (value === undefined || value === '' || value === defaults[key]) {
        params.delete(key);
      } else {
        params.set(key, String(value));
      }
    }
    const qs = params.toString();
    router.push(qs ? `${pathname}?${qs}` : pathname, { scroll: false });
  }, [searchParams, router, pathname, defaults]);

  return [state, setState] as const;
}
