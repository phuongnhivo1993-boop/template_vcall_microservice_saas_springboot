'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Tabs, Tag, Typography, Space, Button, message, Row, Col, Statistic, Input, Form, Select, Tooltip } from 'antd';
import {
  PlusOutlined, DollarOutlined, CreditCardOutlined,
  FileTextOutlined, BarChartOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { billingApi } from '@/lib/api';

const { Title } = Typography;

const planTypeColors: Record<string, string> = {
  BASIC: 'blue', PROFESSIONAL: 'purple', ENTERPRISE: 'gold', CUSTOM: 'green',
};

const subscriptionStatusColors: Record<string, string> = {
  ACTIVE: 'green', PENDING: 'orange', EXPIRED: 'red', CANCELLED: 'default',
};

const invoiceStatusColors: Record<string, string> = {
  DRAFT: 'default', PENDING: 'orange', PAID: 'green', OVERDUE: 'red', CANCELLED: 'default',
};

export default function BillingPage() {
  const [activeTab, setActiveTab] = useState('plans');
  const [plans, setPlans] = useState<any[]>([]);
  const [subscriptions, setSubscriptions] = useState<any[]>([]);
  const [invoices, setInvoices] = useState<any[]>([]);
  const [usage, setUsage] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [formType, setFormType] = useState<'plan' | 'subscription'>('plan');
  const [editingItem, setEditingItem] = useState<any>(null);
  const [selectedSubscriber, setSelectedSubscriber] = useState('default');
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [plansRes, subsRes, invRes, usageRes] = await Promise.all([
        billingApi.getPlans({ page: 0, size: 100 }),
        billingApi.getSubscriptions(selectedSubscriber, { page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        billingApi.getInvoices(selectedSubscriber, { page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        billingApi.getUsage(selectedSubscriber, { page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
      ]);
      setPlans(plansRes.data?.data?.content || plansRes.data?.content || []);
      setSubscriptions(subsRes.data?.data?.content || subsRes.data?.content || []);
      setInvoices(invRes.data?.data?.content || invRes.data?.content || []);
      setUsage(usageRes.data?.data?.content || usageRes.data?.content || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load billing data');
    } finally {
      setLoading(false);
    }
  }, [selectedSubscriber]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = (type: 'plan' | 'subscription') => {
    setFormType(type);
    setEditingItem(null);
    form.resetFields();
    setFormOpen(true);
  };

  const handleEdit = (item: any, type: 'plan' | 'subscription') => {
    setFormType(type);
    setEditingItem(item);
    form.setFieldsValue(item);
    setFormOpen(true);
  };

  const handleDelete = (id: number) => {
    showDeleteConfirm({
      title: 'Delete Plan',
      content: 'Are you sure you want to delete this plan?',
      onOk: async () => {
        await billingApi.deletePlan(id);
        message.success('Plan deleted');
        fetchData();
      },
    });
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    if (formType === 'plan') {
      if (editingItem) await billingApi.updatePlan(editingItem.id, values);
      else await billingApi.createPlan(values);
    } else {
      if (editingItem) await billingApi.cancelSubscription(editingItem.id);
      else await billingApi.createSubscription(values);
    }
    message.success('Saved');
    setFormOpen(false);
    fetchData();
  };

  const handleCancelSubscription = (id: string) => {
    showDeleteConfirm({
      title: 'Cancel Subscription',
      content: 'Are you sure you want to cancel this subscription?',
      onOk: async () => {
        await billingApi.cancelSubscription(id);
        message.success('Subscription cancelled');
        fetchData();
      },
    });
  };

  const handlePayInvoice = async (id: string) => {
    try {
      await billingApi.payInvoice(id);
      message.success('Payment recorded');
      fetchData();
    } catch { message.error('Payment failed'); }
  };

  const handleExportCsv = async () => {
    try {
      const res = await billingApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `billing_${dayjs().format('YYYYMMDD_HHmmss')}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Exported');
    } catch { message.error('Export failed'); }
  };

  const handleExportExcel = async () => {
    try {
      const res = await billingApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `billing_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Exported');
    } catch { message.error('Export failed'); }
  };

  const planColumns = [
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Type', dataIndex: 'planType', key: 'planType',
      render: (t: string) => <Tag color={planTypeColors[t] || 'default'}>{t}</Tag> },
    { title: 'Price', dataIndex: 'price', key: 'price',
      render: (p: number) => `$${p?.toFixed(2) || '0.00'}` },
    { title: 'Currency', dataIndex: 'currency', key: 'currency' },
    { title: 'Billing Cycle', dataIndex: 'billingCycle', key: 'billingCycle' },
    { title: 'Active', dataIndex: 'isActive', key: 'isActive',
      render: (a: boolean) => a ? <Tag color="green">Active</Tag> : <Tag>Inactive</Tag> },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          <Tooltip title="Edit"><Button size="small" onClick={() => handleEdit(r, 'plan')}>Edit</Button></Tooltip>
          <Button size="small" danger onClick={() => handleDelete(r.id)}>Delete</Button>
        </Space>
      ),
    },
  ];

  const subColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id' },
    { title: 'Plan', dataIndex: 'planId', key: 'planId' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={subscriptionStatusColors[s]}>{s}</Tag> },
    { title: 'Start', dataIndex: 'startDate', key: 'startDate',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'End', dataIndex: 'endDate', key: 'endDate',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        r.status === 'ACTIVE' ? (
          <Button size="small" danger onClick={() => handleCancelSubscription(r.id)}>Cancel</Button>
        ) : '-'
      ),
    },
  ];

  const invoiceColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id' },
    { title: 'Invoice #', dataIndex: 'invoiceNumber', key: 'invoiceNumber' },
    { title: 'Amount', dataIndex: 'totalAmount', key: 'totalAmount',
      render: (a: number) => `$${a?.toFixed(2) || '0.00'}` },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={invoiceStatusColors[s]}>{s}</Tag> },
    { title: 'Issue Date', dataIndex: 'issueDate', key: 'issueDate',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Due Date', dataIndex: 'dueDate', key: 'dueDate',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        r.status === 'PENDING' || r.status === 'OVERDUE' ? (
          <Button size="small" type="primary" onClick={() => handlePayInvoice(r.id)}>Pay</Button>
        ) : <Tag>Paid</Tag>
      ),
    },
  ];

  const usageColumns = [
    { title: 'Type', dataIndex: 'usageType', key: 'usageType' },
    { title: 'Quantity', dataIndex: 'quantity', key: 'quantity' },
    { title: 'Unit', dataIndex: 'unit', key: 'unit' },
    { title: 'Cost', dataIndex: 'totalCost', key: 'totalCost',
      render: (c: number) => `$${c?.toFixed(2) || '0.00'}` },
    { title: 'Date', dataIndex: 'recordedAt', key: 'recordedAt',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
  ];

  const totalRevenue = invoices.filter((i: any) => i.status === 'PAID').reduce((sum: number, i: any) => sum + (i.totalAmount || 0), 0);
  const activeSubscriptions = subscriptions.filter((s: any) => s.status === 'ACTIVE').length;

  const tabContent = (key: string) => {
    switch (key) {
      case 'plans':
        return (
          <CommonTable
            columns={planColumns}
            dataSource={plans}
            loading={loading}
            error={error}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            onRefresh={fetchData}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
            extra={
              <Button type="primary" icon={<PlusOutlined />} onClick={() => handleCreate('plan')}>
                Add Plan
              </Button>
            }
          />
        );
      case 'subscriptions':
        return (
          <CommonTable
            columns={subColumns}
            dataSource={subscriptions}
            loading={loading}
            error={error}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            onRefresh={fetchData}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
            extra={
              <Button type="primary" icon={<PlusOutlined />} onClick={() => handleCreate('subscription')}>
                New Subscription
              </Button>
            }
          />
        );
      case 'invoices':
        return (
          <CommonTable
            columns={invoiceColumns}
            dataSource={invoices}
            loading={loading}
            error={error}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            onRefresh={fetchData}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
          />
        );
      case 'usage':
        return (
          <CommonTable
            columns={usageColumns}
            dataSource={usage}
            loading={loading}
            error={error}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            onRefresh={fetchData}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div>
      <Title level={3}>Billing & Subscriptions</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}><Card><Statistic title="Active Plans" value={plans.length} prefix={<DollarOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Active Subscriptions" value={activeSubscriptions} prefix={<CreditCardOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Total Invoices" value={invoices.length} prefix={<FileTextOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Revenue" value={totalRevenue} prefix={<BarChartOutlined />} precision={2} valueStyle={{ color: '#52c41a' }} /></Card></Col>
      </Row>

      <Input.Search
        placeholder="Subscriber ID"
        value={selectedSubscriber}
        onChange={(e) => setSelectedSubscriber(e.target.value)}
        onSearch={fetchData}
        style={{ width: 300, marginBottom: 16 }}
      />

      <Card>
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
          { key: 'plans', label: `Pricing Plans (${plans.length})`, children: tabContent('plans') },
          { key: 'subscriptions', label: `Subscriptions (${subscriptions.length})`, children: tabContent('subscriptions') },
          { key: 'invoices', label: `Invoices (${invoices.length})`, children: tabContent('invoices') },
          { key: 'usage', label: `Usage (${usage.length})`, children: tabContent('usage') },
        ]} />
      </Card>

      <CommonForm
        open={formOpen}
        title={formType === 'plan' ? (editingItem ? 'Edit Plan' : 'Add Plan') : 'New Subscription'}
        onClose={() => setFormOpen(false)}
        onSubmit={handleSubmit}
        initialValues={editingItem}
        width={500}
      >
        {formType === 'plan' ? (
          <>
            <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item name="planType" label="Type" rules={[{ required: true }]}>
              <Select options={['BASIC', 'PROFESSIONAL', 'ENTERPRISE', 'CUSTOM'].map(t => ({ value: t, label: t }))} />
            </Form.Item>
            <Form.Item name="price" label="Price ($)" rules={[{ required: true }]}><Input type="number" /></Form.Item>
            <Form.Item name="currency" label="Currency" initialValue="USD"><Input /></Form.Item>
            <Form.Item name="billingCycle" label="Billing Cycle" initialValue="MONTHLY">
              <Select options={['MONTHLY', 'QUARTERLY', 'YEARLY', 'ONE_TIME'].map(c => ({ value: c, label: c }))} />
            </Form.Item>
          </>
        ) : (
          <>
            <Form.Item name="planId" label="Plan ID" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item name="subscriberId" label="Subscriber ID" rules={[{ required: true }]}><Input /></Form.Item>
          </>
        )}
      </CommonForm>
    </div>
  );
}
