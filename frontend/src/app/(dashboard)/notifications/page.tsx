'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Tabs, Tag, Typography, Space, Button, message, Row, Col, Statistic, Badge, Input, Form, Select, Modal } from 'antd';
import {
  BellOutlined, SendOutlined, SettingOutlined,
  CheckCircleOutlined, MobileOutlined, PlusOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
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
  const [error, setError] = useState<string | null>(null);
  const [sendModalOpen, setSendModalOpen] = useState(false);
  const [templateModalOpen, setTemplateModalOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<any>(null);
  const [recipientId, setRecipientId] = useState('default');
  const [form] = Form.useForm();
  const [selectedNotifKeys, setSelectedNotifKeys] = useState<string[]>([]);
  const [selectedTemplateKeys, setSelectedTemplateKeys] = useState<number[]>([]);
  const [selectedPrefKeys, setSelectedPrefKeys] = useState<string[]>([]);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [notifRes, tmplRes, prefRes] = await Promise.all([
        notificationsApi.getAll({ page: 0, size: 100 }),
        notificationsApi.getTemplates({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        notificationsApi.getPreferences(recipientId).catch(() => ({ data: { content: [] } })),
      ]);
      setNotifications(notifRes.data?.data?.content || notifRes.data?.content || []);
      setTemplates(tmplRes.data?.data?.content || tmplRes.data?.content || []);
      setPreferences(prefRes.data?.data?.content || prefRes.data?.content || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load notifications');
    } finally {
      setLoading(false);
    }
  }, [recipientId]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleBulkDeleteNotifications = () => {
    Modal.confirm({
      title: 'Xóa nhiều thông báo',
      content: `Bạn có chắc chắn muốn xóa ${selectedNotifKeys.length} thông báo đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await notificationsApi.bulkDelete(selectedNotifKeys);
          message.success(`Đã xóa ${selectedNotifKeys.length} thông báo`);
          setSelectedNotifKeys([]);
          fetchData();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeleteTemplates = () => {
    Modal.confirm({
      title: 'Xóa nhiều mẫu',
      content: `Bạn có chắc chắn muốn xóa ${selectedTemplateKeys.length} mẫu đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await notificationsApi.bulkDeleteTemplates(selectedTemplateKeys);
          message.success(`Đã xóa ${selectedTemplateKeys.length} mẫu`);
          setSelectedTemplateKeys([]);
          fetchData();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeletePreferences = () => {
    Modal.confirm({
      title: 'Xóa nhiều tùy chọn',
      content: `Bạn có chắc chắn muốn xóa ${selectedPrefKeys.length} tùy chọn đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await notificationsApi.bulkDeletePreferences(selectedPrefKeys);
          message.success(`Đã xóa ${selectedPrefKeys.length} tùy chọn`);
          setSelectedPrefKeys([]);
          fetchData();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

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

  const handleDeleteNotification = async (id: string) => {
    try {
      await notificationsApi.delete(id);
      message.success('Deleted');
      fetchData();
    } catch { message.error('Delete failed'); }
  };

  const handleCreateTemplate = () => {
    setEditingTemplate(null);
    form.resetFields();
    setTemplateModalOpen(true);
  };

  const handleSaveTemplate = async () => {
    try {
      const values = await form.validateFields();
      if (editingTemplate) {
        await notificationsApi.updateTemplate(editingTemplate.id, values);
      } else {
        await notificationsApi.createTemplate(values);
      }
      message.success('Template saved');
      setTemplateModalOpen(false);
      fetchData();
    } catch { message.error('Failed'); }
  };

  const handleExportCsv = async () => {
    try {
      const res = await notificationsApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `notifications_${dayjs().format('YYYYMMDD_HHmmss')}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Exported');
    } catch { message.error('Export failed'); }
  };

  const handleExportExcel = async () => {
    try {
      const res = await notificationsApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `notifications_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Exported');
    } catch { message.error('Export failed'); }
  };

  const unreadCount = notifications.filter((n: any) => n.status === 'PENDING' || n.status === 'SENT').length;

  const notifColumns = [
    { title: 'Type', dataIndex: 'type', key: 'type',
      render: (t: string) => <Tag color={typeColors[t] || 'default'}>{t}</Tag> },
    { title: 'Channel', dataIndex: 'channel', key: 'channel',
      render: (c: string) => <Tag color={channelColors[c] || 'default'}>{c}</Tag> },
    {
      title: 'Title', dataIndex: 'title', key: 'title',
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
    {
      title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          {r.status !== 'READ' ? (
            <Button size="small" icon={<CheckCircleOutlined />} onClick={() => handleMarkAsRead(r.id)} />
          ) : null}
          <Button size="small" danger onClick={() => handleDeleteNotification(r.id)}>Delete</Button>
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
    {
      title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Button size="small" onClick={() => { setEditingTemplate(r); form.setFieldsValue(r); setTemplateModalOpen(true); }}>
          Edit
        </Button>
      ),
    },
  ];

  const preferenceColumns = [
    { title: 'Channel', dataIndex: 'channel', key: 'channel',
      render: (c: string) => <Tag color={channelColors[c] || 'default'}>{c}</Tag> },
    { title: 'Type', dataIndex: 'type', key: 'type',
      render: (t: string) => <Tag color={typeColors[t] || 'default'}>{t}</Tag> },
    { title: 'Enabled', dataIndex: 'enabled', key: 'enabled',
      render: (e: boolean) => e ? <Tag color="green">Yes</Tag> : <Tag color="red">No</Tag> },
  ];

  const tabContent = (key: string) => {
    switch (key) {
      case 'inbox':
        return (
          <>
            {selectedNotifKeys.length > 0 && (
              <Button danger onClick={handleBulkDeleteNotifications} style={{ marginBottom: 16 }}>
                Xóa đã chọn ({selectedNotifKeys.length})
              </Button>
            )}
            <CommonTable
              rowSelection={{ selectedRowKeys: selectedNotifKeys, onChange: (keys: React.Key[]) => setSelectedNotifKeys(keys as string[]) }}
              columns={notifColumns}
              dataSource={notifications}
              loading={loading}
              error={error}
              rowKey="id"
              pagination={{ pageSize: 10 }}
              onRefresh={() => { setSelectedNotifKeys([]); fetchData(); }}
              onExportCsv={handleExportCsv}
              onExportExcel={handleExportExcel}
              extra={
                <Button type="primary" icon={<SendOutlined />} onClick={() => { form.resetFields(); setSendModalOpen(true); }}>
                  Send Notification
                </Button>
              }
            />
          </>
        );
      case 'templates':
        return (
          <>
            {selectedTemplateKeys.length > 0 && (
              <Button danger onClick={handleBulkDeleteTemplates} style={{ marginBottom: 16 }}>
                Xóa đã chọn ({selectedTemplateKeys.length})
              </Button>
            )}
            <CommonTable
              rowSelection={{ selectedRowKeys: selectedTemplateKeys, onChange: (keys: React.Key[]) => setSelectedTemplateKeys(keys as number[]) }}
              columns={templateColumns}
              dataSource={templates}
              loading={loading}
              error={error}
              rowKey="id"
              pagination={{ pageSize: 10 }}
              onRefresh={() => { setSelectedTemplateKeys([]); fetchData(); }}
              extra={
                <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateTemplate}>
                  Add Template
                </Button>
              }
            />
          </>
        );
      case 'preferences':
        return (
          <>
            {selectedPrefKeys.length > 0 && (
              <Button danger onClick={handleBulkDeletePreferences} style={{ marginBottom: 16 }}>
                Xóa đã chọn ({selectedPrefKeys.length})
              </Button>
            )}
            <CommonTable
              rowSelection={{ selectedRowKeys: selectedPrefKeys, onChange: (keys: React.Key[]) => setSelectedPrefKeys(keys as string[]) }}
              columns={preferenceColumns}
              dataSource={preferences}
              loading={loading}
              error={error}
              rowKey="id"
              pagination={{ pageSize: 10 }}
              onRefresh={() => { setSelectedPrefKeys([]); fetchData(); }}
            />
          </>
        );
      default:
        return null;
    }
  };

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
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
          { key: 'inbox', label: `Inbox (${notifications.length})`, children: tabContent('inbox') },
          { key: 'templates', label: `Templates (${templates.length})`, children: tabContent('templates') },
          { key: 'preferences', label: `Preferences (${preferences.length})`, children: tabContent('preferences') },
        ]} />
      </Card>

      <CommonForm
        open={sendModalOpen}
        title="Send Notification"
        onClose={() => setSendModalOpen(false)}
        onSubmit={handleSend}
        width={500}
      >
        <Form.Item name="recipientId" label="Recipient ID" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="channel" label="Channel" rules={[{ required: true }]}>
          <Select options={['SMS', 'EMAIL', 'PUSH', 'IN_APP'].map(c => ({ value: c, label: c }))} />
        </Form.Item>
        <Form.Item name="type" label="Type" rules={[{ required: true }]}>
          <Select options={['ALERT', 'REMINDER', 'UPDATE', 'PROMOTION', 'SLA_BREACH', 'TICKET_ASSIGNED', 'CALL_MISSED'].map(t => ({ value: t, label: t }))} />
        </Form.Item>
        <Form.Item name="title" label="Title"><Input /></Form.Item>
        <Form.Item name="body" label="Body" rules={[{ required: true }]}><TextArea rows={4} /></Form.Item>
      </CommonForm>

      <CommonForm
        open={templateModalOpen}
        title={editingTemplate ? 'Edit Template' : 'Add Template'}
        onClose={() => setTemplateModalOpen(false)}
        onSubmit={handleSaveTemplate}
        initialValues={editingTemplate}
        width={500}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="channel" label="Channel" rules={[{ required: true }]}>
          <Select options={['SMS', 'EMAIL', 'PUSH', 'IN_APP'].map(c => ({ value: c, label: c }))} />
        </Form.Item>
        <Form.Item name="title" label="Title"><Input /></Form.Item>
        <Form.Item name="body" label="Body" rules={[{ required: true }]}><TextArea rows={4} /></Form.Item>
        <Form.Item name="variables" label="Variables (comma-separated)"><Input placeholder="var1, var2, var3" /></Form.Item>
      </CommonForm>
    </div>
  );
}
