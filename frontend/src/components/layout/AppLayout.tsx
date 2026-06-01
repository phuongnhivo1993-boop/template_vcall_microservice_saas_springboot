'use client';

import { Layout } from 'antd';
import Sidebar from './Sidebar';
import Header from './Header';
import { ReactNode } from 'react';

const { Content } = Layout;

export default function AppLayout({ children }: { children: ReactNode }) {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sidebar />
      <Layout style={{ marginLeft: 240 }}>
        <Header />
        <Content style={{ margin: 24, minHeight: 280 }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
