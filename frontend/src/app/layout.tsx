import type { Metadata } from 'next';
import Providers from '@/providers/Providers';
import './globals.css';

export const metadata: Metadata = {
  title: 'VCall Contact Center',
  description: 'Enterprise Contact Center Platform for Healthcare',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
