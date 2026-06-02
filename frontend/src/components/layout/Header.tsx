'use client';

import { Layout, Avatar, Badge, Dropdown, Space, Typography, Button } from 'antd';
import { MenuOutlined, LogoutOutlined, UserOutlined, SettingOutlined } from '@ant-design/icons';
import { useSession, signOut } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import NotificationBadge from './NotificationBadge';

const { Header: AntHeader } = Layout;
const { Text } = Typography;

interface HeaderProps {
  collapsed?: boolean;
  onToggle?: () => void;
  isMobile?: boolean;
}

export default function Header({ onToggle, isMobile }: HeaderProps) {
  const { data: session } = useSession();
  const router = useRouter();

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Profile',
      onClick: () => router.push('/settings'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: 'Settings',
      onClick: () => router.push('/settings'),
    },
    { type: 'divider' as const },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      danger: true,
      onClick: () => signOut({ callbackUrl: '/auth/login' }),
    },
  ];

  return (
    <AntHeader
      style={{
        background: '#fff',
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: isMobile ? 'space-between' : 'flex-end',
        borderBottom: '1px solid #f0f0f0',
        height: 64,
        position: 'sticky',
        top: 0,
        zIndex: 99,
      }}
    >
      {isMobile && (
        <Button
          type="text"
          icon={<MenuOutlined style={{ fontSize: 20 }} />}
          onClick={onToggle}
          style={{ marginLeft: -8 }}
        />
      )}
      <Space size={24}>
        <NotificationBadge />
        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
          <Space style={{ cursor: 'pointer' }}>
            <Avatar icon={<UserOutlined />} style={{ backgroundColor: '#1677ff' }} />
            <Text strong>{session?.user?.name || 'User'}</Text>
          </Space>
        </Dropdown>
      </Space>
    </AntHeader>
  );
}
