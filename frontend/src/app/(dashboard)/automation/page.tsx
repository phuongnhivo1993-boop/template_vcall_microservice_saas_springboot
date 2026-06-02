'use client';

import { useState, useEffect } from 'react';
import { Card, List, Tag, Button, Space, Typography, Modal, Form, Select, Input, Switch, message, Empty, Alert, Tooltip, Divider } from 'antd';
import { PlusOutlined, ThunderboltOutlined, EditOutlined, DeleteOutlined, PauseCircleOutlined, PlayCircleOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';

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

const MOCK_RULES: AutomationRule[] = [
  {
    id: '1', name: 'Auto-assign High Priority Tickets', description: 'Automatically assign high priority tickets to senior agents',
    trigger: 'ticket.created', triggerLabel: 'Ticket Created',
    conditions: [{ field: 'priority', operator: 'equals', value: 'HIGH' }],
    actions: [{ type: 'assign_ticket', config: { group: 'Senior Agents' } }],
    isActive: true, createdAt: '2026-05-01', updatedAt: '2026-05-10',
  },
  {
    id: '2', name: 'SLA Breach Notification', description: 'Notify supervisor when ticket is about to breach SLA',
    trigger: 'sla.breach', triggerLabel: 'SLA Breach Warning',
    conditions: [{ field: 'time_remaining', operator: 'less_than', value: '30' }],
    actions: [{ type: 'notify_agent', config: { role: 'SUPERVISOR', channel: 'IN_APP' } }],
    isActive: true, createdAt: '2026-04-15', updatedAt: '2026-05-12',
  },
  {
    id: '3', name: 'Missed Call Follow-up', description: 'Create ticket and send SMS for missed calls',
    trigger: 'call.missed', triggerLabel: 'Call Missed',
    conditions: [],
    actions: [
      { type: 'create_ticket', config: { priority: 'MEDIUM', category: 'CALLBACK' } },
      { type: 'send_sms', config: { template: 'missed_call_followup' } },
    ],
    isActive: false, createdAt: '2026-05-05', updatedAt: '2026-05-14',
  },
];

export default function AutomationPage() {
  const [loading, setLoading] = useState(true);
  const [rules, setRules] = useState<AutomationRule[]>([]);
  const [formOpen, setFormOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<AutomationRule | null>(null);

  useEffect(() => {
    setRules(MOCK_RULES);
    setLoading(false);
  }, []);

  const handleSave = async (values: any) => {
    if (editingRule) {
      setRules(prev => prev.map(r => r.id === editingRule.id ? { ...r, ...values } : r));
      message.success('Rule updated');
    } else {
      const newRule: AutomationRule = {
        id: Date.now().toString(),
        ...values,
        triggerLabel: TRIGGER_OPTIONS.find(t => t.value === values.trigger)?.label || values.trigger,
        conditions: [],
        actions: [],
        isActive: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      setRules(prev => [newRule, ...prev]);
      message.success('Rule created');
    }
    setFormOpen(false);
    setEditingRule(null);
  };

  const toggleActive = (rule: AutomationRule) => {
    setRules(prev => prev.map(r => r.id === rule.id ? { ...r, isActive: !r.isActive } : r));
    message.success(`Rule ${rule.isActive ? 'paused' : 'activated'}`);
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
          <Button size="small" danger icon={<DeleteOutlined />} onClick={() => showDeleteConfirm({ onOk: async () => { setRules(prev => prev.filter(r => r.id !== record.id)); message.success('Rule deleted'); } })} />
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
