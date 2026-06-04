'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Tag, Button, Space, Typography, Switch, Form, Input, Checkbox, message, Badge, Tooltip, Alert, Modal } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ApiOutlined, ReloadOutlined, CopyOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { webhooksApi } from '@/lib/api';

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

export default function WebhooksPage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [webhooks, setWebhooks] = useState<Webhook[]>([]);
  const [formOpen, setFormOpen] = useState(false);
  const [editingWebhook, setEditingWebhook] = useState<Webhook | null>(null);
  const [testResult, setTestResult] = useState<string | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  const fetchWebhooks = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await webhooksApi.list({ page: 0, size: 100 });
      setWebhooks(res.data?.data?.content || res.data?.content || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load webhooks');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchWebhooks();
  }, [fetchWebhooks]);

  const toggleWebhook = async (webhook: Webhook) => {
    try {
      await webhooksApi.update(webhook.id, { isActive: !webhook.isActive });
      message.success(`${webhook.name} ${webhook.isActive ? 'disabled' : 'enabled'}`);
      fetchWebhooks();
    } catch (err: any) {
      message.error(err?.message || 'Failed to toggle webhook');
    }
  };

  const handleSave = async (values: any) => {
    try {
      if (editingWebhook?.id) {
        await webhooksApi.update(editingWebhook.id, values);
        message.success('Webhook updated');
      } else {
        await webhooksApi.create(values);
        message.success('Webhook created');
      }
      setFormOpen(false);
      setEditingWebhook(null);
      fetchWebhooks();
    } catch (err: any) {
      message.error(err?.message || 'Failed to save webhook');
    }
  };

  const handleDuplicate = (record: Webhook) => {
    setEditingWebhook({ ...record, id: '' } as Webhook);
    setFormOpen(true);
  };

  const testWebhook = async (webhook: Webhook) => {
    setTestResult(`Testing ${webhook.url}...`);
    try {
      const res = await webhooksApi.test(webhook.id);
      setTestResult(`✅ ${webhook.url} - ${res.status} OK`);
      fetchWebhooks();
    } catch (err: any) {
      setTestResult(`❌ ${webhook.url} - ${err?.message || 'Connection failed'}`);
    }
    setTimeout(() => setTestResult(null), 3000);
  };

  const handleBulkDelete = () => {
    Modal.confirm({
      title: 'Xóa nhiều webhook',
      content: `Bạn có chắc chắn muốn xóa ${selectedRowKeys.length} webhook đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await webhooksApi.bulkDelete(selectedRowKeys);
          message.success(`Đã xóa ${selectedRowKeys.length} webhook`);
          setSelectedRowKeys([]);
          fetchWebhooks();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleDeleteWebhook = async (id: string) => {
    showDeleteConfirm({
      title: 'Delete Webhook',
      content: 'Are you sure you want to delete this webhook?',
      onOk: async () => {
        await webhooksApi.delete(id);
        message.success('Webhook deleted');
        fetchWebhooks();
      },
    });
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
          <Button size="small" icon={<CopyOutlined />} onClick={() => handleDuplicate(record)} />
          <Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteWebhook(record.id)} />
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
        {selectedRowKeys.length > 0 && (
          <Button danger onClick={handleBulkDelete} style={{ marginBottom: 16 }}>
            Xóa đã chọn ({selectedRowKeys.length})
          </Button>
        )}
        <CommonTable rowSelection={{ selectedRowKeys, onChange: (keys: React.Key[]) => setSelectedRowKeys(keys as string[]) }} columns={columns} dataSource={webhooks} rowKey="id" loading={loading} error={error} onRefresh={() => { setSelectedRowKeys([]); fetchWebhooks(); }} pagination={false} />
      </Card>

      <CommonForm
        open={formOpen}
        title={editingWebhook?.id ? 'Edit Webhook' : 'New Webhook'}
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
