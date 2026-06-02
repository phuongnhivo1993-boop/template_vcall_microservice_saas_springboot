'use client';

import { Timeline, Typography, Tag, Empty, Spin } from 'antd';
import { PhoneOutlined, MessageOutlined, MailOutlined, FileTextOutlined, CustomerServiceOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface TimelineEvent {
  id: string;
  type: 'call' | 'chat' | 'email' | 'ticket' | 'note' | 'system';
  title: string;
  description?: string;
  timestamp: string;
  status?: string;
  agent?: string;
}

interface CustomerTimelineProps {
  events: TimelineEvent[];
  loading?: boolean;
  height?: number;
}

const eventConfig: Record<string, { color: string; icon: React.ReactNode }> = {
  call: { color: '#1890ff', icon: <PhoneOutlined /> },
  chat: { color: '#52c41a', icon: <MessageOutlined /> },
  email: { color: '#722ed1', icon: <MailOutlined /> },
  ticket: { color: '#faad14', icon: <FileTextOutlined /> },
  note: { color: '#13c2c2', icon: <CustomerServiceOutlined /> },
  system: { color: '#888', icon: null },
};

const statusColors: Record<string, string> = {
  COMPLETED: 'green', MISSED: 'red', OPEN: 'blue', RESOLVED: 'green',
  PENDING: 'orange', SENT: 'purple', ACTIVE: 'green',
};

export default function CustomerTimeline({ events, loading = false, height }: CustomerTimelineProps) {
  if (loading) {
    return <div style={{ textAlign: 'center', padding: 40 }}><Spin /></div>;
  }

  if (!events || events.length === 0) {
    return <Empty description="No activity history" image={Empty.PRESENTED_IMAGE_SIMPLE} />;
  }

  const sortedEvents = [...events].sort((a, b) =>
    new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
  );

  return (
    <div style={{ maxHeight: height, overflow: 'auto', padding: 16 }}>
      <Timeline
        items={sortedEvents.map((event) => {
          const config = eventConfig[event.type] || eventConfig.system;
          return {
            color: config.color,
            dot: config.icon,
            children: (
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Text strong style={{ textTransform: 'capitalize' }}>
                    {event.title}
                  </Text>
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {new Date(event.timestamp).toLocaleString()}
                  </Text>
                </div>
                {event.description && (
                  <div style={{ marginTop: 4 }}>
                    <Text>{event.description}</Text>
                  </div>
                )}
                <div style={{ marginTop: 4 }}>
                  {event.status && <Tag color={statusColors[event.status]}>{event.status}</Tag>}
                  {event.agent && <Tag>{event.agent}</Tag>}
                </div>
              </div>
            ),
          };
        })}
      />
    </div>
  );
}
