'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Tag, Button, Space, Typography, message, Alert, Tooltip, Form, Select, Modal, Input } from 'antd';
import { PlusOutlined, ThunderboltOutlined, EditOutlined, DeleteOutlined, PauseCircleOutlined, PlayCircleOutlined, CopyOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { automationApi } from '@/lib/api';
import type { TablePaginationConfig } from 'antd/es/table';
import type { SorterResult } from 'antd/es/table/interface';

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
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);
  const [searchFilters, setSearchFilters] = useState<Record<string, any>>({});
  const [sortField, setSortField] = useState<string>('name');
  const [sortOrder, setSortOrder] = useState<'ascend' | 'descend'>('ascend');
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0,
  });

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

  const handleSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        cleaned[key] = val;
      }
    });
    setSearchFilters(cleaned);
  };

  const handleReset = () => {
    setSearchFilters({});
  };

  const handleTableChange = (
    pag: TablePaginationConfig,
    _filters: any,
    sorter: SorterResult<any> | SorterResult<any>[],
  ) => {
    setPagination((prev) => ({ ...prev, current: pag.current, pageSize: pag.pageSize }));
    if (!Array.isArray(sorter) && sorter.field) {
      setSortField(sorter.field as string);
      setSortOrder(sorter.order || 'ascend');
    }
  };

  const getFilteredData = (data: AutomationRule[]) => {
    let filtered = [...data];
    Object.entries(searchFilters).forEach(([key, value]) => {
      if (value) {
        filtered = filtered.filter((item) => {
          if (key === 'name') {
            return item.name.toLowerCase().includes(String(value).toLowerCase());
          }
          if (key === 'isActive') {
            return item.isActive === (value === 'true');
          }
          return true;
        });
      }
    });
    if (sortField && sortOrder) {
      filtered.sort((a, b) => {
        const aVal = a[sortField as keyof AutomationRule];
        const bVal = b[sortField as keyof AutomationRule];
        if (aVal === bVal) return 0;
        if (aVal === null || aVal === undefined) return 1;
        if (bVal === null || bVal === undefined) return -1;
        const comparison = String(aVal).localeCompare(String(bVal));
        return sortOrder === 'ascend' ? comparison : -comparison;
      });
    }
    return filtered;
  };

  const handleSave = async (values: any) => {
    try {
      if (editingRule?.id) {
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

  const handleDuplicate = (record: AutomationRule) => {
    setEditingRule({ ...record, id: '' } as AutomationRule);
    setFormOpen(true);
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

  const handleBulkDelete = () => {
    Modal.confirm({
      title: 'Xóa nhiều quy tắc',
      content: `Bạn có chắc chắn muốn xóa ${selectedRowKeys.length} quy tắc đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await automationApi.bulkDelete(selectedRowKeys);
          message.success(`Đã xóa ${selectedRowKeys.length} quy tắc`);
          setSelectedRowKeys([]);
          fetchRules();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
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
    { title: 'Name', dataIndex: 'name', key: 'name', sorter: true, render: (n: string, r: AutomationRule) => (
      <Space><ThunderboltOutlined style={{ color: r.isActive ? '#faad14' : '#d9d9d9' }} /><Text strong>{n}</Text></Space>
    )},
    { title: 'Trigger', dataIndex: 'triggerLabel', key: 'trigger', sorter: true, render: (t: string) => <Tag color="blue">{t}</Tag> },
    { title: 'Conditions', key: 'conditions', render: (_: any, r: AutomationRule) => (
      <Space size={4}>{r.conditions.length > 0 ? r.conditions.map((c, i) => <Tag key={i}>{c.field} {c.operator} {c.value}</Tag>) : <Text type="secondary">No conditions</Text>}</Space>
    )},
    { title: 'Actions', key: 'actions', render: (_: any, r: AutomationRule) => (
      <Space>{r.actions.map((a, i) => <Tag key={i} color="green">{a.type.replace(/_/g, ' ')}</Tag>)}</Space>
    )},
    { title: 'Status', dataIndex: 'isActive', key: 'isActive', sorter: true, render: (a: boolean) => <Tag color={a ? 'green' : 'red'}>{a ? 'Active' : 'Paused'}</Tag> },
    {
      title: 'Actions', key: 'actions',
      render: (_: any, record: AutomationRule) => (
        <Space>
          <Tooltip title={record.isActive ? 'Pause' : 'Activate'}>
            <Button size="small" icon={record.isActive ? <PauseCircleOutlined /> : <PlayCircleOutlined />}
              onClick={() => toggleActive(record)} />
          </Tooltip>
          <Tooltip title="Edit"><Button size="small" icon={<EditOutlined />} onClick={() => { setEditingRule(record); setFormOpen(true); }} /></Tooltip>
          <Tooltip title="Duplicate"><Button size="small" icon={<CopyOutlined />} onClick={() => handleDuplicate(record)} /></Tooltip>
          <Tooltip title="Delete"><Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteRule(record.id)} /></Tooltip>
        </Space>
      ),
    },
  ];

  const searchFields = [
    { name: 'name', label: 'Name', type: 'input' as const, placeholder: 'Search by name' },
    {
      name: 'isActive',
      label: 'Status',
      type: 'select' as const,
      placeholder: 'Filter by status',
      options: [
        { value: 'true', label: 'Active' },
        { value: 'false', label: 'Paused' },
      ],
    },
    {
      name: 'trigger',
      label: 'Trigger Type',
      type: 'select' as const,
      placeholder: 'Filter by trigger',
      options: TRIGGER_OPTIONS.map(opt => ({ value: opt.value, label: opt.label })),
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
          <CommonSearch
            fields={searchFields}
            onSearch={handleSearch}
            onReset={handleReset}
            loading={loading}
            initialValues={searchFilters}
          />
          <SavedFilters
            currentValues={searchFilters}
            onApply={(values) => {
              setSearchFilters(values);
            }}
            storageKey="vcall-saved-filters-automation"
          />
        </div>
        {selectedRowKeys.length > 0 && (
          <Button danger onClick={handleBulkDelete} style={{ marginBottom: 16 }}>
            Xóa đã chọn ({selectedRowKeys.length})
          </Button>
        )}
        <CommonTable
          rowSelection={{ selectedRowKeys, onChange: (keys: React.Key[]) => setSelectedRowKeys(keys as string[]) }}
          columns={columns}
          dataSource={getFilteredData(rules)}
          rowKey="id"
          loading={loading}
          error={error}
          onRefresh={() => { setSelectedRowKeys([]); fetchRules(); }}
          pagination={{ ...pagination, total: getFilteredData(rules).length }}
          onTableChange={handleTableChange}
        />
      </Card>

      <CommonForm
        open={formOpen}
        title={editingRule?.id ? 'Edit Rule' : 'New Automation Rule'}
        onClose={() => { setFormOpen(false); setEditingRule(null); }}
        onSubmit={handleSave}
        initialValues={editingRule || { name: '', description: '', trigger: '' }}
      >
        <Form.Item name="name" label="Rule Name" rules={[{ required: true, message: 'Please enter rule name' }]}>
          <Input placeholder="e.g., Auto-assign High Priority Tickets" />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={2} placeholder="Describe what this rule does" />
        </Form.Item>
        <Form.Item name="trigger" label="Trigger Event" rules={[{ required: true, message: 'Please select a trigger' }]}>
          <Select placeholder="Select trigger event">
            {TRIGGER_OPTIONS.map(opt => (
              <Select.OptGroup key={opt.group} label={opt.group}>
                <Select.Option key={opt.value} value={opt.value}>{opt.label}</Select.Option>
              </Select.OptGroup>
            ))}
          </Select>
        </Form.Item>
        <Form.Item name="conditions" label="Conditions" initialValue={[]}>
          <Select mode="tags" placeholder="Select conditions (optional)" tokenSeparators={[',']}>
            <Select.Option value="priority_high">Priority = High</Select.Option>
            <Select.Option value="priority_urgent">Priority = Urgent</Select.Option>
            <Select.Option value="category_billing">Category = Billing</Select.Option>
            <Select.Option value="category_technical">Category = Technical</Select.Option>
            <Select.Option value="customer_vip">Customer = VIP</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="actions" label="Actions" rules={[{ required: true, message: 'Please select at least one action' }]}>
          <Select mode="multiple" placeholder="Select actions to perform">
            {ACTION_OPTIONS.map(opt => (
              <Select.Option key={opt.value} value={opt.value}>{opt.label}</Select.Option>
            ))}
          </Select>
        </Form.Item>
      </CommonForm>
    </div>
  );
}
