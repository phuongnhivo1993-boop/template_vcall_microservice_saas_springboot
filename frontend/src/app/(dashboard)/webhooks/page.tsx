'use client';

import { useState, useEffect } from 'react';
import { Card, Tag, Button, Space, Typography, Switch, Form, Input, Checkbox, message, Badge, Tooltip, Alert } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ApiOutlined, ReloadOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';

const { Text } = Typography;

interface Webhook {
  id: string;
  name: string;
  url: string;
  events: string[];
  secret: string;
  isActive: boolean;
  lastTriggeredAt: string | null;
  lastResponseCode: number | null;
  failureCount: number;
  createdAt: string;
}

const EVENT_OPTIONS = [
  { value: 'ticket.created', label: 'Ticket Created' },
  { value: 'ticket.updated', label: 'Ticket Updated' },
  { value: 'ticket.resolved', label: 'Ticket Resolved' },
  { value: 'call.ended', label: 'Call Ended' },
  { value: 'call.missed', label: 'Call Missed' },
  { value: 'customer.created', label: 'Customer Created' },
  { value: 'customer.updated', label: 'Customer Updated' },
  { value: 'email.received', label: 'Email Received' },
  { value: 'email.sent', label: 'Email Sent' },
  { value: 'sms.received', label: 'SMS Received' },
  { value: 'chat.message', label: 'Chat Message' },
  { value: 'agent.status', label: 'Agent Status Changed' },
];

const MOCK_WEBHOOKS: Webhook[] = [
  { id: '1', name: 'Zalo Integration', url: 'https://zalo-api.vn/webhook/vcall', events: ['ticket.created', 'ticket.updated', 'customer.updated'], secret: '****', isActive: true, lastTriggeredAt: new Date().toISOString(), lastResponseCode: 200, failureCount: 0, createdAt: '2026-05-01' },
  { id: '2', name: 'Facebook Messenger Bot', url: 'https://graph.facebook.com/webhook', events: ['chat.message', 'customer.created'], secret: '****', isActive: true, lastTriggeredAt: new Date(Date.now() - 3600000).toISOString(), lastResponseCode: 200, failureCount: 1, createdAt: '2026-04-15' },
  { id: '3', name: 'Internal CRM Sync', url: 'https://crm-internal.company.vn/api/sync', events: ['customer.created', 'customer.updated', 'ticket.resolved'], secret: '****', isActive: false, lastTriggeredAt: new Date(Date.now() - 86400000).toISOString(), lastResponseCode: 500, failureCount: 5, createdAt: '2026-03-20' },
  { id: '4', name: 'Slack Notification', url: 'https://hooks.slack.com/services/T00/B00/xxx', events: ['ticket.created', 'call.missed'], secret: '****', isActive: true, lastTriggeredAt: new Date(Date.now() - 7200000).toISOString(), lastResponseCode: 200, failureCount: 0, createdAt: '2026-05-10' },
];

export default function WebhooksPage() {
  const [loading, setLoading] = useState(true);
  const [webhooks, setWebhooks] = useState<Webhook[]>([]);
  const [formOpen, setFormOpen] = useState(false);
  const [editingWebhook, setEditingWebhook] = useState<Webhook | null>(null);
  const [testResult, setTestResult] = useState<string | null>(null);

  useEffect(() => {
    setWebhooks(MOCK_WEBHOOKS);
    setLoading(false);
  }, []);

  const toggleWebhook = (webhook: Webhook) => {
    setWebhooks(prev => prev.map(w => w.id === webhook.id ? { ...w, isActive: !w.isActive } : w));
    message.success(`${webhook.name} ${webhook.isActive ? 'disabled' : 'enabled'}`);
  };

  const handleSave = async (values: any) => {
    if (editingWebhook) {
      setWebhooks(prev => prev.map(w => w.id === editingWebhook.id ? { ...w, ...values } : w));
      message.success('Webhook updated');
    } else {
      const newWebhook: Webhook = {
        id: Date.now().toString(), ...values, isActive: true,
        lastTriggeredAt: null, lastResponseCode: null, failureCount: 0,
        createdAt: new Date().toISOString(),
      };
      setWebhooks(prev => [newWebhook, ...prev]);
      message.success('Webhook created');
    }
    setFormOpen(false);
    setEditingWebhook(null);
  };

  const testWebhook = async (webhook: Webhook) => {
    setTestResult(`Testing ${webhook.url}...`);
    setTimeout(() => {
      const success = Math.random() > 0.3;
      setTestResult(success ? `✅ ${webhook.url} - 200 OK` : `❌ ${webhook.url} - Connection failed`);
      setWebhooks(prev => prev.map(w => w.id === webhook.id ? {
        ...w, lastTriggeredAt: new Date().toISOString(),
        lastResponseCode: success ? 200 : 500,
        failureCount: success ? w.failureCount : w.failureCount + 1,
      } : w));
    }, 1500);
  };

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name', render: (n: string, r: Webhook) => (
      <Space><ApiOutlined style={{ color: r.isActive ? '#1890ff' : '#d9d9d9' }} /><Text strong>{n}</Text></Space>
    )},
    { title: 'URL', dataIndex: 'url', key: 'url', render: (u: string) => <Text copyable style={{ maxWidth: 250, display: 'inline-block', overflow: 'hidden', textOverflow: 'ellipsis' }}>{u}</Text> },
    { title: 'Events', dataIndex: 'events', key: 'events', render: (e: string[]) => (
      <Space size={4} wrap>{e.map(ev => <Tag key={ev} style={{ fontSize: 10 }}>{ev}</Tag>)}</Space>
    )},
    { title: 'Status', dataIndex: 'isActive', key: 'isActive', render: (a: boolean, r: Webhook) => (
      <Switch checked={a} onChange={() => toggleWebhook(r)} size="small" />
    )},
    { title: 'Last Response', key: 'lastResponse', render: (_: any, r: Webhook) => (
      r.lastResponseCode ? <Tag color={r.lastResponseCode < 300 ? 'green' : 'red'}>{r.lastResponseCode}</Tag> : '-'
    )},
    { title: 'Failures', dataIndex: 'failureCount', key: 'failureCount', render: (c: number) => (
      <Badge count={c} style={{ backgroundColor: c > 0 ? '#ff4d4f' : '#52c41a' }} />
    )},
    {
      title: 'Actions', key: 'actions',
      render: (_: any, record: Webhook) => (
        <Space>
          <Tooltip title="Test webhook">
            <Button size="small" icon={<ReloadOutlined />} onClick={() => testWebhook(record)} />
          </Tooltip>
          <Button size="small" icon={<EditOutlined />} onClick={() => { setEditingWebhook(record); setFormOpen(true); }} />
          <Button size="small" danger icon={<DeleteOutlined />} onClick={() => showDeleteConfirm({
            onOk: async () => { setWebhooks(prev => prev.filter(w => w.id !== record.id)); message.success('Deleted'); }
          })} />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card
        title={<Space><ApiOutlined /> Webhooks</Space>}
        extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingWebhook(null); setFormOpen(true); }}>New Webhook</Button>}
      >
        <Alert
          message="Webhooks allow real-time integration with external services like Zalo, Facebook, Slack, and CRM systems."
          type="info" showIcon style={{ marginBottom: 16 }}
        />
        {testResult && (
          <Alert message={testResult} type={testResult.includes('✅') ? 'success' : 'error'} closable
            onClose={() => setTestResult(null)} style={{ marginBottom: 16 }} />
        )}
        <CommonTable columns={columns} dataSource={webhooks} rowKey="id" loading={loading} pagination={false} />
      </Card>

      <CommonForm
        open={formOpen}
        title={editingWebhook ? 'Edit Webhook' : 'New Webhook'}
        onClose={() => { setFormOpen(false); setEditingWebhook(null); }}
        onSubmit={handleSave}
        initialValues={editingWebhook || { name: '', url: '', events: [], secret: '' }}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter webhook name' }]}>
          <Input placeholder="e.g., Zalo Integration" />
        </Form.Item>
        <Form.Item name="url" label="Callback URL" rules={[{ required: true, message: 'Please enter callback URL' }, { type: 'url', message: 'Please enter a valid URL' }]}>
          <Input placeholder="https://your-service.com/webhook" />
        </Form.Item>
        <Form.Item name="secret" label="Secret Key">
          <Input placeholder="Optional: secret for HMAC verification" />
        </Form.Item>
        <Form.Item name="events" label="Events" rules={[{ required: true, message: 'Please select at least one event' }]}>
          <Checkbox.Group>
            <Space direction="vertical" style={{ display: 'flex', flexWrap: 'wrap', maxHeight: 280 }} size={4}>
              {EVENT_OPTIONS.map(event => (
                <Checkbox key={event.value} value={event.value}>{event.label}</Checkbox>
              ))}
            </Space>
          </Checkbox.Group>
        </Form.Item>
      </CommonForm>
    </div>
  );
}
