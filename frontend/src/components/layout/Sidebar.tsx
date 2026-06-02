'use client';

import { useState } from 'react';
import { Layout, Menu } from 'antd';
import { usePathname, useRouter } from 'next/navigation';
import {
  DashboardOutlined,
  PhoneOutlined,
  TeamOutlined,
  UsergroupAddOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  BulbOutlined,
  DollarOutlined,
  BarChartOutlined,
  SettingOutlined,
  AuditOutlined,
  BellOutlined,
} from '@ant-design/icons';

const { Sider } = Layout;

const menuItems = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/calls', icon: <PhoneOutlined />, label: 'Calls' },
  { key: '/agents', icon: <TeamOutlined />, label: 'Agents' },
  { key: '/customers', icon: <UsergroupAddOutlined />, label: 'Customers' },
  { key: '/crm', icon: <AppstoreOutlined />, label: 'CRM' },
  { key: '/tickets', icon: <FileTextOutlined />, label: 'Tickets' },
  { key: '/campaigns', icon: <BulbOutlined />, label: 'Campaigns' },
  { key: '/billing', icon: <DollarOutlined />, label: 'Billing' },
  { key: '/reports', icon: <BarChartOutlined />, label: 'Reports' },
  { key: '/notifications', icon: <BellOutlined />, label: 'Notifications' },
  { key: '/settings', icon: <SettingOutlined />, label: 'Settings' },
  { key: '/audit', icon: <AuditOutlined />, label: 'Audit' },
];

export default function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const pathname = usePathname();
  const router = useRouter();

  const selectedKey = '/' + pathname.split('/')[1];

  return (
    <Sider
      collapsible
      collapsed={collapsed}
      onCollapse={setCollapsed}
      theme="light"
      width={240}
      style={{
        borderRight: '1px solid #f0f0f0',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
        zIndex: 100,
      }}
    >
      <div
        style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderBottom: '1px solid #f0f0f0',
        }}
      >
        {collapsed ? (
          <img src="/logo.svg" alt="VCall" style={{ height: 32 }} />
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <img src="/logo.svg" alt="VCall" style={{ height: 32 }} />
            <span style={{ fontSize: 18, fontWeight: 700, color: '#1677ff' }}>VCall</span>
          </div>
        )}
      </div>
      <Menu
        mode="inline"
        selectedKeys={[selectedKey]}
        items={menuItems}
        onClick={({ key }) => router.push(key)}
        style={{ borderRight: 0, marginTop: 8 }}
      />
    </Sider>
  );
}
