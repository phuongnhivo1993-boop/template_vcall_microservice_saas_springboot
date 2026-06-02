'use client';

import { useState } from 'react';
import { Layout, Drawer, Grid } from 'antd';
import Sidebar from './Sidebar';
import Header from './Header';
import { ReactNode } from 'react';

const { Content } = Layout;
const { useBreakpoint } = Grid;

export default function AppLayout({ children }: { children: ReactNode }) {
  const screens = useBreakpoint();
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [mobileDrawerOpen, setMobileDrawerOpen] = useState(false);
  const isMobile = !screens.md;

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {isMobile ? (
        <Drawer
          placement="left"
          open={mobileDrawerOpen}
          onClose={() => setMobileDrawerOpen(false)}
          width={260}
          styles={{ body: { padding: 0 } }}
        >
          <Sidebar inDrawer onItemClick={() => setMobileDrawerOpen(false)} />
        </Drawer>
      ) : (
        <Sidebar collapsed={sidebarCollapsed} onCollapse={setSidebarCollapsed} />
      )}
      <Layout style={{
        marginLeft: isMobile ? 0 : (sidebarCollapsed ? 80 : 240),
        transition: 'margin-left 0.2s',
      }}>
        <Header
          collapsed={sidebarCollapsed}
          onToggle={() => isMobile ? setMobileDrawerOpen(!mobileDrawerOpen) : setSidebarCollapsed(!sidebarCollapsed)}
          isMobile={isMobile}
        />
        <Content style={{ margin: 24, minHeight: 280 }}>
          {children}
        </Content>
      </Layout>
    </Layout>
  );
}
