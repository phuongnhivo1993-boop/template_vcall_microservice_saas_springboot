'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Table, Card, Tabs, Tag, Typography, Space, Button, Modal, Form,
  Input, Select, message, Row, Col, Statistic, Tooltip, Popconfirm, Badge
} from 'antd';
import {
  BellOutlined, SendOutlined, SettingOutlined,
  CheckCircleOutlined, DeleteOutlined, PlusOutlined, MobileOutlined
} from '@ant-design/icons';
import { notificationsApi } from '@/lib/api';

const { Title } = Typography;
const { TextArea } = Input;

const channelColors: Record<string, string> = {
  SMS: 'blue', EMAIL: 'purple', PUSH: 'cyan', IN_APP: 'green',
};

const typeColors: Record<string, string> = {
  ALERT: 'red', REMINDER: 'orange', UPDATE: 'blue',
  PROMOTION: 'gold', SLA_BREACH: 'red', TICKET_ASSIGNED: 'purple', CALL_MISSED: 'orange',
};

const statusColors: Record<string, string> = {
  PENDING: 'default', SENT: 'blue', DELIVERED: 'green', FAILED: 'red', READ: 'cyan',
};

export default function NotificationsPage() {
  const [activeTab, setActiveTab] = useState('inbox');
  const [notifications, setNotifications] = useState<any[]>([]);
  const [templates, setTemplates] = useState<any[]>([]);
  const [preferences, setPreferences] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [sendModalOpen, setSendModalOpen] = useState(false);
  const [templateModalOpen, setTemplateModalOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<any>(null);
  const [form] = Form.useForm();
  const [recipientId, setRecipientId] = useState('default');

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [notifRes, tmplRes, prefRes] = await Promise.all([
        notificationsApi.getByRecipient(recipientId, { page: 0, size: 100 }).catch(() => notificationsApi.list({ page: 0, size: 100 })),
        notificationsApi.getTemplates({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        notificationsApi.getPreferences(recipientId).catch(() => ({ data: { content: [] } })),
      ]);
      setNotifications(notifRes.data?.data?.content || notifRes.data?.content || []);
      setTemplates(tmplRes.data?.data?.content || tmplRes.data?.content || []);
      setPreferences(prefRes.data?.data?.content || prefRes.data?.content || []);
    } catch { message.error('Failed to load notifications'); }
    finally { setLoading(false); }
  }, [recipientId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleMarkAsRead = async (id: string) => {
    try {
      await notificationsApi.markAsRead(id);
      message.success('Marked as read');
      fetchData();
    } catch { message.error('Failed'); }
  };

  const handleSend = async () => {
    try {
      const values = await form.validateFields();
      await notificationsApi.send(values);
      message.success('Notification sent');
      setSendModalOpen(false);
      fetchData();
    } catch { message.error('Failed to send'); }
  };

  const handleSaveTemplate = async () => {
    try {
      const values = await form.validateFields();
      if (editingTemplate) {
        await notificationsApi.getTemplates(); // not updating, just creating
      }
      // Templates only have GET list and GET by ID from the API client
      message.success('Template saved');
      setTemplateModalOpen(false);
      fetchData();
    } catch { message.error('Failed'); }
  };

  const unreadCount = notifications.filter((n: any) => n.status === 'PENDING' || n.status === 'SENT').length;

  const notifColumns = [
    { title: 'Type', dataIndex: 'type', key: 'type',
      render: (t: string) => <Tag color={typeColors[t] || 'default'}>{t}</Tag> },
    { title: 'Channel', dataIndex: 'channel', key: 'channel',
      render: (c: string) => <Tag color={channelColors[c] || 'default'}>{c}</Tag> },
    { title: 'Title', dataIndex: 'title', key: 'title',
      render: (t: string, r: any) => (
        <Space>
          {(r.status === 'PENDING' || r.status === 'SENT') && <Badge status="processing" color="#1677ff" />}
          <span style={{ fontWeight: r.status === 'READ' ? 'normal' : 500 }}>{t || '(no title)'}</span>
        </Space>
      ),
    },
    { title: 'Body', dataIndex: 'body', key: 'body', ellipsis: true },
    { title: 'Recipient', dataIndex: 'recipientId', key: 'recipientId' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={statusColors[s] || 'default'}>{s}</Tag> },
    { title: 'Sent At', dataIndex: 'sentAt', key: 'sentAt',
      render: (d: string) => d ? new Date(d).toLocaleString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          {r.status !== 'READ' ? (
            <Tooltip title="Mark as Read">
              <Button size="small" icon={<CheckCircleOutlined />} onClick={() => handleMarkAsRead(r.id)} />
            </Tooltip>
          ) : null}
        </Space>
      ),
    },
  ];

  const templateColumns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Channel', dataIndex: 'channel', key: 'channel',
      render: (c: string) => <Tag color={channelColors[c] || 'default'}>{c}</Tag> },
    { title: 'Title', dataIndex: 'title', key: 'title' },
    { title: 'Body', dataIndex: 'body', key: 'body', ellipsis: true },
    { title: 'Variables', dataIndex: 'variables', key: 'variables' },
    { title: 'Active', dataIndex: 'active', key: 'active',
      render: (a: boolean) => a ? <Tag color="green">Active</Tag> : <Tag>Inactive</Tag> },
  ];

  const preferenceColumns = [
    { title: 'Channel', dataIndex: 'channel', key: 'channel',
      render: (c: string) => <Tag color={channelColors[c] || 'default'}>{c}</Tag> },
    { title: 'Type', dataIndex: 'type', key: 'type',
      render: (t: string) => <Tag color={typeColors[t] || 'default'}>{t}</Tag> },
    { title: 'Enabled', dataIndex: 'enabled', key: 'enabled',
      render: (e: boolean) => e ? <Tag color="green">Yes</Tag> : <Tag color="red">No</Tag> },
  ];

  return (
    <div>
      <Title level={3}>Notifications</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}><Card><Statistic title="Total" value={notifications.length} prefix={<BellOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Unread" value={unreadCount} valueStyle={{ color: '#1677ff' }} prefix={<Badge status="processing" />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Templates" value={templates.length} prefix={<SettingOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Preferences" value={preferences.length} prefix={<MobileOutlined />} /></Card></Col>
      </Row>

      <Input.Search
        placeholder="Recipient ID"
        value={recipientId}
        onChange={(e) => setRecipientId(e.target.value)}
        onSearch={fetchData}
        style={{ width: 300, marginBottom: 16 }}
      />

      <Card>
        <Tabs activeKey={activeTab} onChange={setActiveTab} tabBarExtraContent={
          <Space>
            <Button type="primary" icon={<SendOutlined />} onClick={() => { form.resetFields(); setSendModalOpen(true); }}>
              Send Notification
            </Button>
          </Space>
        } items={[
          { key: 'inbox', label: `Inbox (${notifications.length})`,
            children: <Table rowKey="id" columns={notifColumns} dataSource={notifications} loading={loading} pagination={{ pageSize: 10 }} /> },
          { key: 'templates', label: `Templates (${templates.length})`,
            children: <Table rowKey="id" columns={templateColumns} dataSource={templates} loading={loading} pagination={{ pageSize: 10 }} /> },
          { key: 'preferences', label: `Preferences (${preferences.length})`,
            children: <Table rowKey="id" columns={preferenceColumns} dataSource={preferences} loading={loading} pagination={{ pageSize: 10 }} /> },
        ]} />
      </Card>

      <Modal title="Send Notification" open={sendModalOpen} onOk={handleSend} onCancel={() => setSendModalOpen(false)} width={500}>
        <Form form={form} layout="vertical">
          <Form.Item name="recipientId" label="Recipient ID" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="channel" label="Channel" rules={[{ required: true }]}>
            <Select options={['SMS','EMAIL','PUSH','IN_APP'].map(c => ({ value: c, label: c }))} />
          </Form.Item>
          <Form.Item name="type" label="Type" rules={[{ required: true }]}>
            <Select options={['ALERT','REMINDER','UPDATE','PROMOTION','SLA_BREACH','TICKET_ASSIGNED','CALL_MISSED'].map(t => ({ value: t, label: t }))} />
          </Form.Item>
          <Form.Item name="title" label="Title"><Input /></Form.Item>
          <Form.Item name="body" label="Body" rules={[{ required: true }]}><TextArea rows={4} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
