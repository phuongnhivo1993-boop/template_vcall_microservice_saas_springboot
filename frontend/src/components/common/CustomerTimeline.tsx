'use client';

import { Timeline, Typography, Tag, Space } from 'antd';
import { PhoneOutlined, MessageOutlined, MailOutlined, FileTextOutlined, CheckCircleOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface TimelineEvent {
  id: string;
  type: string;
  title: string;
  description?: string;
  timestamp: string;
}

interface CustomerTimelineProps {
  events: TimelineEvent[];
  height?: number | string;
}

const eventIcons: Record<string, React.ReactNode> = {
  call: <PhoneOutlined />,
  chat: <MessageOutlined />,
  email: <MailOutlined />,
  ticket: <FileTextOutlined />,
  note: <FileTextOutlined />,
  resolved: <CheckCircleOutlined />,
};

const eventColors: Record<string, string> = {
  call: '#52c41a',
  chat: '#1890ff',
  email: '#722ed1',
  ticket: '#faad14',
  note: '#d9d9d9',
  resolved: '#52c41a',
};

export default function CustomerTimeline({ events, height }: CustomerTimelineProps) {
  if (!events || events.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: 16 }}>
        <Text type="secondary">No recent activity</Text>
      </div>
    );
  }

  return (
    <div style={{ overflow: 'auto', height: height || 'auto', padding: '8px 0' }}>
      <Timeline
        items={events.map((event) => ({
          color: eventColors[event.type] || '#1890ff',
          dot: eventIcons[event.type] || undefined,
          children: (
            <div>
              <Space>
                <Tag color={eventColors[event.type]} style={{ fontSize: 10, lineHeight: '16px', padding: '0 4px' }}>
                  {event.type.toUpperCase()}
                </Tag>
                <Text strong style={{ fontSize: 13 }}>{event.title}</Text>
              </Space>
              {event.description && (
                <div>
                  <Text type="secondary" style={{ fontSize: 12 }}>{event.description}</Text>
                </div>
              )}
              <div>
                <Text type="secondary" style={{ fontSize: 11 }}>
                  {new Date(event.timestamp).toLocaleString('vi-VN')}
                </Text>
              </div>
            </div>
          ),
        }))}
      />
    </div>
  );
}
