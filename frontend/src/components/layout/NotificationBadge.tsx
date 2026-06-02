'use client';

import { useEffect, useState } from 'react';
import { Badge, Popover, List, Typography, Spin, Empty } from 'antd';
import { BellOutlined } from '@ant-design/icons';
import { useSession } from 'next-auth/react';
import { getSocket } from '@/lib/socket';
import { notificationsApi } from '@/lib/api';

export default function NotificationBadge() {
  const { data: session } = useSession();
  const [count, setCount] = useState(0);
  const [notifications, setNotifications] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (!session?.accessToken) return;
    fetchNotifications();
    try {
      const socket = getSocket();
      socket.auth = { token: session.accessToken };
      if (!socket.connected) {
        socket.connect();
      }
      socket.on('notification', (data: any) => {
        setCount(prev => prev + 1);
        setNotifications(prev => [data, ...prev].slice(0, 50));
      });
      return () => {
        socket.off('notification');
      };
    } catch (e) {
      console.warn('WebSocket connection failed, using polling');
    }
  }, [session]);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const res = await notificationsApi.getStats();
      setCount(res.data?.data?.unreadCount || 0);
    } catch {} finally { setLoading(false); }
  };

  const fetchList = async () => {
    setLoading(true);
    try {
      const res = await notificationsApi.list({ page: 0, size: 10 });
      setNotifications(res.data?.data?.content || []);
    } catch {} finally { setLoading(false); }
  };

  const handleOpenChange = (newOpen: boolean) => {
    setOpen(newOpen);
    if (newOpen) fetchList();
  };

  const content = (
    <div style={{ width: 360, maxHeight: 400 }}>
      {loading ? (
        <div style={{ textAlign: 'center', padding: 20 }}>
          <Spin />
        </div>
      ) : notifications.length === 0 ? (
        <Empty description="No notifications" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      ) : (
        <List
          dataSource={notifications}
          renderItem={(item: any) => (
            <List.Item style={{ cursor: 'pointer' }}>
              <List.Item.Meta
                title={
                  <Typography.Text strong={!item.readAt}>
                    {item.title || 'Notification'}
                  </Typography.Text>
                }
                description={
                  <Typography.Paragraph ellipsis={{ rows: 2 }} style={{ margin: 0 }}>
                    {item.body || item.message}
                  </Typography.Paragraph>
                }
              />
            </List.Item>
          )}
        />
      )}
    </div>
  );

  return (
    <Popover content={content} title="Notifications" trigger="click" open={open} onOpenChange={handleOpenChange}>
      <Badge count={count} size="small" style={{ cursor: 'pointer' }}>
        <BellOutlined style={{ fontSize: 18, cursor: 'pointer' }} />
      </Badge>
    </Popover>
  );
}
