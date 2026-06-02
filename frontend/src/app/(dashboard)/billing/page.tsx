'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Table, Card, Tabs, Tag, Typography, Space, Button, Modal, Form,
  Input, Select, message, Row, Col, Statistic, Tooltip, Popconfirm, Descriptions
} from 'antd';
import {
  PlusOutlined, DollarOutlined, CreditCardOutlined,
  FileTextOutlined, BarChartOutlined
} from '@ant-design/icons';
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
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState<'plan' | 'subscription'>('plan');
  const [editingItem, setEditingItem] = useState<any>(null);
  const [selectedSubscriber, setSelectedSubscriber] = useState('default');
  const [form] = Form.useForm();

  const fetchData = useCallback(async () => {
    setLoading(true);
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
    } catch { message.error('Failed to load billing data'); }
    finally { setLoading(false); }
  }, [selectedSubscriber]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = (type: 'plan' | 'subscription') => {
    setModalType(type);
    setEditingItem(null);
    form.resetFields();
    setModalOpen(true);
  };

  const handleEdit = (item: any, type: 'plan' | 'subscription') => {
    setModalType(type);
    setEditingItem(item);
    form.setFieldsValue(item);
    setModalOpen(true);
  };

  const handleDelete = async (id: number, type: string) => {
    try {
      if (type === 'plan') await billingApi.deletePlan(id);
      message.success('Deleted');
      fetchData();
    } catch { message.error('Delete failed'); }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (modalType === 'plan') {
        if (editingItem) await billingApi.updatePlan(editingItem.id, values);
        else await billingApi.createPlan(values);
      } else {
        if (editingItem) await billingApi.cancelSubscription(editingItem.id);
        else await billingApi.createSubscription(values);
      }
      message.success('Saved');
      setModalOpen(false);
      fetchData();
    } catch { message.error('Failed'); }
  };

  const handleCancelSubscription = async (id: string) => {
    try {
      await billingApi.cancelSubscription(id);
      message.success('Subscription cancelled');
      fetchData();
    } catch { message.error('Failed'); }
  };

  const handlePayInvoice = async (id: string) => {
    try {
      await billingApi.payInvoice(id);
      message.success('Payment recorded');
      fetchData();
    } catch { message.error('Payment failed'); }
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
          <Popconfirm title="Delete plan?" onConfirm={() => handleDelete(r.id, 'plan')}>
            <Tooltip title="Delete"><Button size="small" danger>Delete</Button></Tooltip>
          </Popconfirm>
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
        r.status === 'ACTIVE' ?
        <Popconfirm title="Cancel subscription?" onConfirm={() => handleCancelSubscription(r.id)}>
          <Button size="small" danger>Cancel</Button>
        </Popconfirm> : '-'
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
        r.status === 'PENDING' || r.status === 'OVERDUE' ?
        <Popconfirm title="Mark as paid?" onConfirm={() => handlePayInvoice(r.id)}>
          <Button size="small" type="primary">Pay</Button>
        </Popconfirm> : <Tag>Paid</Tag>
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
        <Tabs activeKey={activeTab} onChange={setActiveTab} tabBarExtraContent={
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => handleCreate(activeTab === 'plans' ? 'plan' : 'subscription')}>
              {activeTab === 'plans' ? 'Add Plan' : 'New Subscription'}
            </Button>
          </Space>
        } items={[
          { key: 'plans', label: `Pricing Plans (${plans.length})`,
            children: <Table rowKey="id" columns={planColumns} dataSource={plans} loading={loading} pagination={{ pageSize: 10 }} /> },
          { key: 'subscriptions', label: `Subscriptions (${subscriptions.length})`,
            children: <Table rowKey="id" columns={subColumns} dataSource={subscriptions} loading={loading} pagination={{ pageSize: 10 }} /> },
          { key: 'invoices', label: `Invoices (${invoices.length})`,
            children: <Table rowKey="id" columns={invoiceColumns} dataSource={invoices} loading={loading} pagination={{ pageSize: 10 }} /> },
          { key: 'usage', label: `Usage (${usage.length})`,
            children: <Table rowKey="id" columns={usageColumns} dataSource={usage} loading={loading} pagination={{ pageSize: 10 }} /> },
        ]} />
      </Card>

      <Modal title={editingItem ? 'Edit Plan' : 'Add Plan'} open={modalOpen && modalType === 'plan'} onOk={handleSubmit} onCancel={() => setModalOpen(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="planType" label="Type" rules={[{ required: true }]}>
            <Select options={['BASIC','PROFESSIONAL','ENTERPRISE','CUSTOM'].map(t => ({ value: t, label: t }))} />
          </Form.Item>
          <Form.Item name="price" label="Price ($)" rules={[{ required: true }]}><Input type="number" /></Form.Item>
          <Form.Item name="currency" label="Currency" initialValue="USD"><Input /></Form.Item>
          <Form.Item name="billingCycle" label="Billing Cycle" initialValue="MONTHLY">
            <Select options={['MONTHLY','QUARTERLY','YEARLY','ONE_TIME'].map(c => ({ value: c, label: c }))} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="New Subscription" open={modalOpen && modalType === 'subscription'} onOk={handleSubmit} onCancel={() => setModalOpen(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="planId" label="Plan ID" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="subscriberId" label="Subscriber ID" rules={[{ required: true }]}><Input /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
