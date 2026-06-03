'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Tag, Button, Space, Typography, Input, message, Alert, Tooltip, Divider } from 'antd';
import { PlusOutlined, ThunderboltOutlined, EditOutlined, DeleteOutlined, PauseCircleOutlined, PlayCircleOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { automationApi } from '@/lib/api';

const { Title, Text } = Typography;

interface AutomationRule {
  id: string;
  name: string;
  description: string;
  trigger: string;
  triggerLabel: string;
  conditions: { field: string; operator: string; value: string }[];
  actions: { type: string; config: Record<string, string> }[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

const TRIGGER_OPTIONS = [
  { value: 'ticket.created', label: 'Ticket Created', group: 'Ticket' },
  { value: 'ticket.updated', label: 'Ticket Updated', group: 'Ticket' },
  { value: 'ticket.status_changed', label: 'Ticket Status Changed', group: 'Ticket' },
  { value: 'call.ended', label: 'Call Ended', group: 'Call' },
  { value: 'call.missed', label: 'Call Missed', group: 'Call' },
  { value: 'customer.created', label: 'Customer Created', group: 'Customer' },
  { value: 'email.received', label: 'Email Received', group: 'Email' },
  { value: 'chat.message_received', label: 'Chat Message Received', group: 'Chat' },
  { value: 'sla.breach', label: 'SLA Breach Warning', group: 'SLA' },
  { value: 'sla.escalated', label: 'SLA Escalated', group: 'SLA' },
];

const ACTION_OPTIONS = [
  { value: 'assign_ticket', label: 'Assign Ticket to Agent', group: 'Ticket' },
  { value: 'update_ticket_status', label: 'Update Ticket Status', group: 'Ticket' },
  { value: 'update_ticket_priority', label: 'Update Ticket Priority', group: 'Ticket' },
  { value: 'send_email', label: 'Send Email Notification', group: 'Notification' },
  { value: 'send_sms', label: 'Send SMS Notification', group: 'Notification' },
  { value: 'send_webhook', label: 'Send Webhook', group: 'Integration' },
  { value: 'create_ticket', label: 'Create Child Ticket', group: 'Ticket' },
  { value: 'notify_agent', label: 'Notify Agent', group: 'Notification' },
  { value: 'escalate_ticket', label: 'Escalate Ticket', group: 'Ticket' },
];

export default function AutomationPage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [rules, setRules] = useState<AutomationRule[]>([]);
  const [formOpen, setFormOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<AutomationRule | null>(null);

  const fetchRules = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await automationApi.list({ page: 0, size: 100 });
      setRules(res.data?.data?.content || res.data?.content || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load automation rules');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchRules();
  }, [fetchRules]);

  const handleSave = async (values: any) => {
    try {
      if (editingRule) {
        await automationApi.update(editingRule.id, values);
        message.success('Rule updated');
      } else {
        await automationApi.create(values);
        message.success('Rule created');
      }
      setFormOpen(false);
      setEditingRule(null);
      fetchRules();
    } catch (err: any) {
      message.error(err?.message || 'Failed to save rule');
    }
  };

  const toggleActive = async (rule: AutomationRule) => {
    try {
      await automationApi.toggle(rule.id, !rule.isActive);
      message.success(`Rule ${rule.isActive ? 'paused' : 'activated'}`);
      fetchRules();
    } catch (err: any) {
      message.error(err?.message || 'Failed to toggle rule');
    }
  };

  const handleDeleteRule = async (id: string) => {
    showDeleteConfirm({
      title: 'Delete Rule',
      content: 'Are you sure you want to delete this rule?',
      onOk: async () => {
        await automationApi.delete(id);
        message.success('Rule deleted');
        fetchRules();
      },
    });
  };

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name', render: (n: string, r: AutomationRule) => (
      <Space><ThunderboltOutlined style={{ color: r.isActive ? '#faad14' : '#d9d9d9' }} /><Text strong>{n}</Text></Space>
    )},
    { title: 'Trigger', dataIndex: 'triggerLabel', key: 'trigger', render: (t: string) => <Tag color="blue">{t}</Tag> },
    { title: 'Conditions', key: 'conditions', render: (_: any, r: AutomationRule) => (
      <Space size={4}>{r.conditions.length > 0 ? r.conditions.map((c, i) => <Tag key={i}>{c.field} {c.operator} {c.value}</Tag>) : <Text type="secondary">No conditions</Text>}</Space>
    )},
    { title: 'Actions', key: 'actions', render: (_: any, r: AutomationRule) => (
      <Space>{r.actions.map((a, i) => <Tag key={i} color="green">{a.type.replace(/_/g, ' ')}</Tag>)}</Space>
    )},
    { title: 'Status', dataIndex: 'isActive', key: 'isActive', render: (a: boolean) => <Tag color={a ? 'green' : 'red'}>{a ? 'Active' : 'Paused'}</Tag> },
    {
      title: 'Actions', key: 'actions',
      render: (_: any, record: AutomationRule) => (
        <Space>
          <Tooltip title={record.isActive ? 'Pause' : 'Activate'}>
            <Button size="small" icon={record.isActive ? <PauseCircleOutlined /> : <PlayCircleOutlined />}
              onClick={() => toggleActive(record)} />
          </Tooltip>
          <Button size="small" icon={<EditOutlined />} onClick={() => { setEditingRule(record); setFormOpen(true); }} />
          <Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteRule(record.id)} />
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card
        title={<Space><ThunderboltOutlined /> Automation Rules</Space>}
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingRule(null); setFormOpen(true); }}>
            New Rule
          </Button>
        }
      >
        <Alert
          message="Automation rules help streamline workflows by automatically triggering actions based on events."
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />
        <CommonTable
          columns={columns}
          dataSource={rules}
          rowKey="id"
          loading={loading}
          error={error}
          onRefresh={fetchRules}
          pagination={false}
        />
      </Card>

      <CommonForm
        open={formOpen}
        title={editingRule ? 'Edit Rule' : 'New Automation Rule'}
        onClose={() => { setFormOpen(false); setEditingRule(null); }}
        onSubmit={handleSave}
        initialValues={editingRule || { name: '', description: '', trigger: '' }}
      >
        <div style={{ padding: 8 }}>
          <div style={{ marginBottom: 16 }}>
            <label>Rule Name *</label>
            <Input name="name" placeholder="e.g., Auto-assign High Priority Tickets" />
          </div>
          <div style={{ marginBottom: 16 }}>
            <label>Description</label>
            <Input name="description" placeholder="Describe what this rule does" />
          </div>
          <div style={{ marginBottom: 16 }}>
            <label>Trigger Event *</label>
            <select name="trigger" style={{ width: '100%', padding: 8, border: '1px solid #d9d9d9', borderRadius: 6 }}>
              {Object.entries(
                TRIGGER_OPTIONS.reduce((acc, t) => {
                  (acc[t.group] = acc[t.group] || []).push(t);
                  return acc;
                }, {} as Record<string, typeof TRIGGER_OPTIONS>)
              ).map(([group, options]) => (
                <optgroup key={group} label={group}>
                  {options.map(opt => <option key={opt.value} value={opt.value}>{opt.label}</option>)}
                </optgroup>
              ))}
            </select>
          </div>
          <Divider>Conditions & Actions</Divider>
          <Text type="secondary">Advanced conditions and actions configuration will be available in the next update.</Text>
        </div>
      </CommonForm>
    </div>
  );
}
