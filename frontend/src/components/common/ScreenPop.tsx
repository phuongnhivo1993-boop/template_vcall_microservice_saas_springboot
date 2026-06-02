'use client';

import { Modal, Descriptions, Tag, Space, Button, Typography, Avatar, Divider } from 'antd';
import { PhoneOutlined, UserOutlined, StarOutlined } from '@ant-design/icons';
import CustomerTimeline from './CustomerTimeline';

const { Text, Title } = Typography;

interface ScreenPopCustomer {
  id: string;
  fullName: string;
  phone: string;
  email: string;
  company: string;
  totalCalls: number;
  totalTickets: number;
  satisfactionScore: number;
  tags: string[];
}

interface ScreenPopData {
  callerNumber: string;
  callerName?: string;
  customer?: ScreenPopCustomer;
  recentActivity?: any[];
}

interface ScreenPopProps {
  visible: boolean;
  data: ScreenPopData | null;
  onClose: () => void;
  onAnswer: () => void;
  onReject: () => void;
}

export default function ScreenPop({ visible, data, onClose, onAnswer, onReject }: ScreenPopProps) {
  if (!data) return null;

  const { callerNumber, callerName, customer } = data;

  return (
    <Modal
      title={
        <Space>
          <PhoneOutlined style={{ color: '#52c41a', fontSize: 20 }} />
          <span>Incoming Call</span>
        </Space>
      }
      open={visible}
      onCancel={onClose}
      footer={
        <Space style={{ width: '100%', justifyContent: 'center' }}>
          <Button danger size="large" icon={<PhoneOutlined rotate={135} />} onClick={onReject}>
            Reject
          </Button>
          <Button type="primary" size="large" icon={<PhoneOutlined />} onClick={onAnswer} style={{ background: '#52c41a', borderColor: '#52c41a' }}>
            Answer
          </Button>
        </Space>
      }
      width={500}
      closable={false}
      maskClosable={false}
    >
      {customer ? (
        <>
          <div style={{ textAlign: 'center', marginBottom: 16 }}>
            <Avatar size={64} icon={<UserOutlined />} style={{ backgroundColor: '#1890ff' }} />
            <Title level={4} style={{ margin: '8px 0 0' }}>{customer.fullName}</Title>
            <Text type="secondary">{customer.company}</Text>
          </div>

          <Descriptions column={2} size="small" bordered>
            <Descriptions.Item label="Phone">{customer.phone}</Descriptions.Item>
            <Descriptions.Item label="Email">{customer.email}</Descriptions.Item>
            <Descriptions.Item label="Total Calls">{customer.totalCalls}</Descriptions.Item>
            <Descriptions.Item label="Tickets">{customer.totalTickets}</Descriptions.Item>
            <Descriptions.Item label="Satisfaction">
              <Tag color={customer.satisfactionScore >= 90 ? 'green' : customer.satisfactionScore >= 70 ? 'orange' : 'red'}>
                <StarOutlined /> {customer.satisfactionScore}%
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Tags">
              <Space>{customer.tags?.map(t => <Tag key={t}>{t}</Tag>) || '-'}</Space>
            </Descriptions.Item>
          </Descriptions>

          <Divider>Recent Activity</Divider>
          <CustomerTimeline
            events={data.recentActivity || []}
            height={200}
          />
        </>
      ) : (
        <div style={{ textAlign: 'center', padding: 20 }}>
          <Title level={4}>Unknown Caller</Title>
          <Text>{callerNumber}</Text>
          <div style={{ marginTop: 16 }}>
            <Text type="secondary">{callerName || 'No caller information available'}</Text>
          </div>
        </div>
      )}
    </Modal>
  );
}
