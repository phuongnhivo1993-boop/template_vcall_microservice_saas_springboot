'use client';

import { useState } from 'react';
import { Layout, Menu } from 'antd';
import { usePathname, useRouter } from 'next/navigation';
import { useSession } from 'next-auth/react';
import { hasPermission } from '@/lib/permissions';
import { useTheme } from '@/lib/theme';
import { menuItems as allMenuItems, type MenuItem } from './menuConfig';

const { Sider } = Layout;

function filterMenuItems(items: MenuItem[], role: string | undefined): MenuItem[] {
  return items.filter((item) => {
    if (item.permission && !hasPermission(role, item.permission as any)) {
      return false;
    }
    if (item.children) {
      item.children = filterMenuItems(item.children, role);
      return item.children.length > 0;
    }
    return true;
  });
}

interface SidebarProps {
  collapsed?: boolean;
  onCollapse?: (collapsed: boolean) => void;
  inDrawer?: boolean;
  onItemClick?: () => void;
}

export default function Sidebar({ collapsed, onCollapse, inDrawer, onItemClick }: SidebarProps) {
  const [localCollapsed, setLocalCollapsed] = useState(false);
  const pathname = usePathname();
  const router = useRouter();
  const { data: session } = useSession();
  const { mode } = useTheme();

  const isCollapsed = collapsed ?? localCollapsed;
  const handleCollapse = onCollapse || setLocalCollapsed;

  const role = session?.user?.role;
  const visibleItems = filterMenuItems(allMenuItems, role);

  const selectedKey = pathname.split('/')[1] || 'dashboard';

  const menuElement = (
    <>
      <div
        style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderBottom: `1px solid ${mode === 'dark' ? '#303030' : '#f0f0f0'}`,
        }}
      >
        {isCollapsed ? (
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
        items={visibleItems.map((item) => ({
          key: item.key,
          icon: item.icon,
          label: item.label,
        }))}
        onClick={({ key }) => {
          const menuItem = allMenuItems.find((m) => m.key === key);
          if (menuItem?.path) router.push(menuItem.path);
          onItemClick?.();
        }}
        style={{ borderRight: 0, marginTop: 8 }}
      />
    </>
  );

  if (inDrawer) {
    return <>{menuElement}</>;
  }

  return (
    <Sider
      collapsible
      collapsed={isCollapsed}
      onCollapse={handleCollapse}
      theme={mode === 'dark' ? 'dark' : 'light'}
      width={240}
      style={{
        borderRight: `1px solid ${mode === 'dark' ? '#303030' : '#f0f0f0'}`,
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
        zIndex: 100,
      }}
    >
      {menuElement}
    </Sider>
  );
}
