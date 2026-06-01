'use client';

import { Layout, Avatar, Badge, Dropdown, Space, Typography } from 'antd';
import { BellOutlined, LogoutOutlined, UserOutlined, SettingOutlined } from '@ant-design/icons';
import { useSession, signOut } from 'next-auth/react';
import { useRouter } from 'next/navigation';

const { Header: AntHeader } = Layout;
const { Text } = Typography;

export default function Header() {
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
        justifyContent: 'flex-end',
        borderBottom: '1px solid #f0f0f0',
        height: 64,
        position: 'sticky',
        top: 0,
        zIndex: 99,
      }}
    >
      <Space size={24}>
        <Badge count={5} size="small">
          <BellOutlined style={{ fontSize: 20, cursor: 'pointer', color: '#666' }} />
        </Badge>
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
