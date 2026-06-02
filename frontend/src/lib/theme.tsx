'use client';

import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { ConfigProvider, theme as antTheme } from 'antd';

type ThemeMode = 'light' | 'dark';

interface ThemeContextType {
  mode: ThemeMode;
  toggleTheme: () => void;
  setTheme: (mode: ThemeMode) => void;
}

const ThemeContext = createContext<ThemeContextType>({
  mode: 'light',
  toggleTheme: () => {},
  setTheme: () => {},
});

export function useTheme() {
  return useContext(ThemeContext);
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<ThemeMode>('light');
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const saved = localStorage.getItem('vcall-theme') as ThemeMode | null;
    if (saved) {
      setMode(saved);
    } else if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      setMode('dark');
    }
  }, []);

  useEffect(() => {
    if (mounted) {
      document.documentElement.setAttribute('data-theme', mode);
    }
  }, [mode, mounted]);

  const toggleTheme = () => {
    const newMode = mode === 'light' ? 'dark' : 'light';
    setMode(newMode);
    localStorage.setItem('vcall-theme', newMode);
  };

  const setTheme = (newMode: ThemeMode) => {
    setMode(newMode);
    localStorage.setItem('vcall-theme', newMode);
  };

  if (!mounted) {
    return <>{children}</>;
  }

  return (
    <ThemeContext.Provider value={{ mode, toggleTheme, setTheme }}>
      <ConfigProvider
        theme={{
          algorithm: mode === 'dark' ? antTheme.darkAlgorithm : antTheme.defaultAlgorithm,
          token: {
            colorPrimary: '#1890ff',
            borderRadius: 6,
          },
        }}
      >
        {children}
      </ConfigProvider>
    </ThemeContext.Provider>
  );
}
