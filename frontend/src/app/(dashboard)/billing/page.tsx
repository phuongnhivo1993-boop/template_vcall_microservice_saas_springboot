'use client';

import { useState, useEffect, useCallback, useMemo } from 'react';
import { Card, Tabs, Tag, Typography, Space, Button, message, Row, Col, Statistic, Form, Select, Tooltip, Modal, Input } from 'antd';
import {
  PlusOutlined, DollarOutlined, CreditCardOutlined,
  FileTextOutlined, BarChartOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { billingApi } from '@/lib/api';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { SorterResult } from 'antd/es/table/interface';

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
  const [selectedPlanKeys, setSelectedPlanKeys] = useState<number[]>([]);
  const [selectedSubKeys, setSelectedSubKeys] = useState<string[]>([]);
  const [selectedInvoiceKeys, setSelectedInvoiceKeys] = useState<string[]>([]);
  const [selectedUsageKeys, setSelectedUsageKeys] = useState<string[]>([]);
  const [searchFilters, setSearchFilters] = useState<Record<string, any>>({});
  const [sortField, setSortField] = useState<string>('name');
  const [sortOrder, setSortOrder] = useState<'ascend' | 'descend'>('ascend');

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
    if (!Array.isArray(sorter) && sorter.field) {
      setSortField(sorter.field as string);
      setSortOrder(sorter.order || 'ascend');
    }
  };

  const getFilteredData = (data: any[]) => {
    let filtered = [...data];
    Object.entries(searchFilters).forEach(([key, value]) => {
      if (value) {
        filtered = filtered.filter((item) =>
          String(item[key]).toLowerCase().includes(String(value).toLowerCase())
        );
      }
    });
    if (sortField && sortOrder) {
      filtered.sort((a, b) => {
        const aVal = a[sortField];
        const bVal = b[sortField];
        if (aVal === bVal) return 0;
        if (aVal === null || aVal === undefined) return 1;
        if (bVal === null || bVal === undefined) return -1;
        const comparison = String(aVal).localeCompare(String(bVal));
        return sortOrder === 'ascend' ? comparison : -comparison;
      });
    }
    return filtered;
  };

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

  const handleBulkDeletePlans = () => {
    Modal.confirm({
      title: 'Xóa nhiều gói',
      content: `Bạn có chắc chắn muốn xóa ${selectedPlanKeys.length} gói đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await billingApi.bulkDeletePlans(selectedPlanKeys);
          message.success(`Đã xóa ${selectedPlanKeys.length} gói`);
          setSelectedPlanKeys([]);
          fetchData();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeleteSubscriptions = () => {
    Modal.confirm({
      title: 'Xóa nhiều đăng ký',
      content: `Bạn có chắc chắn muốn xóa ${selectedSubKeys.length} đăng ký đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await billingApi.bulkDeleteSubscriptions(selectedSubKeys);
          message.success(`Đã xóa ${selectedSubKeys.length} đăng ký`);
          setSelectedSubKeys([]);
          fetchData();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeleteInvoices = () => {
    Modal.confirm({
      title: 'Xóa nhiều hóa đơn',
      content: `Bạn có chắc chắn muốn xóa ${selectedInvoiceKeys.length} hóa đơn đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await billingApi.bulkDeleteInvoices(selectedInvoiceKeys);
          message.success(`Đã xóa ${selectedInvoiceKeys.length} hóa đơn`);
          setSelectedInvoiceKeys([]);
          fetchData();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeleteUsage = () => {
    Modal.confirm({
      title: 'Xóa nhiều mục sử dụng',
      content: `Bạn có chắc chắn muốn xóa ${selectedUsageKeys.length} mục sử dụng đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await billingApi.bulkDeleteUsage(selectedUsageKeys);
          message.success(`Đã xóa ${selectedUsageKeys.length} mục sử dụng`);
          setSelectedUsageKeys([]);
          fetchData();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
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
    { title: 'Name', dataIndex: 'name', key: 'name', sorter: true },
    { title: 'Type', dataIndex: 'planType', key: 'planType', sorter: true,
      render: (t: string) => <Tag color={planTypeColors[t] || 'default'}>{t}</Tag> },
    { title: 'Price', dataIndex: 'price', key: 'price', sorter: true,
      render: (p: number) => `$${p?.toFixed(2) || '0.00'}` },
    { title: 'Currency', dataIndex: 'currency', key: 'currency' },
    { title: 'Billing Cycle', dataIndex: 'billingCycle', key: 'billingCycle', sorter: true },
    { title: 'Active', dataIndex: 'isActive', key: 'isActive', sorter: true,
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
    { title: 'Plan', dataIndex: 'planId', key: 'planId', sorter: true },
    { title: 'Status', dataIndex: 'status', key: 'status', sorter: true,
      render: (s: string) => <Tag color={subscriptionStatusColors[s]}>{s}</Tag> },
    { title: 'Start', dataIndex: 'startDate', key: 'startDate', sorter: true,
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
    { title: 'Invoice #', dataIndex: 'invoiceNumber', key: 'invoiceNumber', sorter: true },
    { title: 'Amount', dataIndex: 'totalAmount', key: 'totalAmount', sorter: true,
      render: (a: number) => `$${a?.toFixed(2) || '0.00'}` },
    { title: 'Status', dataIndex: 'status', key: 'status', sorter: true,
      render: (s: string) => <Tag color={invoiceStatusColors[s]}>{s}</Tag> },
    { title: 'Issue Date', dataIndex: 'issueDate', key: 'issueDate', sorter: true,
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
    { title: 'Type', dataIndex: 'usageType', key: 'usageType', sorter: true },
    { title: 'Quantity', dataIndex: 'quantity', key: 'quantity', sorter: true },
    { title: 'Unit', dataIndex: 'unit', key: 'unit' },
    { title: 'Cost', dataIndex: 'totalCost', key: 'totalCost', sorter: true,
      render: (c: number) => `$${c?.toFixed(2) || '0.00'}` },
    { title: 'Date', dataIndex: 'recordedAt', key: 'recordedAt', sorter: true,
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
  ];

  const totalRevenue = invoices.filter((i: any) => i.status === 'PAID').reduce((sum: number, i: any) => sum + (i.totalAmount || 0), 0);
  const activeSubscriptions = subscriptions.filter((s: any) => s.status === 'ACTIVE').length;

  const searchFields = [
    { name: 'subscriberId', label: 'Subscriber ID', type: 'input' as const, placeholder: 'Search by subscriber ID' },
    {
      name: 'planType',
      label: 'Plan Type',
      type: 'select' as const,
      placeholder: 'Filter by plan type',
      options: [
        { value: 'BASIC', label: 'Basic' },
        { value: 'PROFESSIONAL', label: 'Professional' },
        { value: 'ENTERPRISE', label: 'Enterprise' },
        { value: 'CUSTOM', label: 'Custom' },
      ],
    },
    {
      name: 'status',
      label: 'Status',
      type: 'select' as const,
      placeholder: 'Filter by status',
      options: [
        { value: 'ACTIVE', label: 'Active' },
        { value: 'PENDING', label: 'Pending' },
        { value: 'EXPIRED', label: 'Expired' },
        { value: 'CANCELLED', label: 'Cancelled' },
      ],
    },
  ];

  const tabContent = (key: string) => {
    switch (key) {
      case 'plans':
        return (
          <>
            {selectedPlanKeys.length > 0 && (
              <Button danger onClick={handleBulkDeletePlans} style={{ marginBottom: 16 }}>
                Xóa đã chọn ({selectedPlanKeys.length})
              </Button>
            )}
            <CommonTable
              rowSelection={{ selectedRowKeys: selectedPlanKeys, onChange: (keys: React.Key[]) => setSelectedPlanKeys(keys as number[]) }}
              columns={planColumns}
              dataSource={getFilteredData(plans)}
              loading={loading}
              error={error}
              rowKey="id"
              pagination={{ pageSize: 10 }}
              onRefresh={() => { setSelectedPlanKeys([]); fetchData(); }}
              onExportCsv={handleExportCsv}
              onExportExcel={handleExportExcel}
              onTableChange={handleTableChange}
              extra={
                <Button type="primary" icon={<PlusOutlined />} onClick={() => handleCreate('plan')}>
                  Add Plan
                </Button>
              }
            />
          </>
        );
      case 'subscriptions':
        return (
          <>
            {selectedSubKeys.length > 0 && (
              <Button danger onClick={handleBulkDeleteSubscriptions} style={{ marginBottom: 16 }}>
                Xóa đã chọn ({selectedSubKeys.length})
              </Button>
            )}
            <CommonTable
              rowSelection={{ selectedRowKeys: selectedSubKeys, onChange: (keys: React.Key[]) => setSelectedSubKeys(keys as string[]) }}
              columns={subColumns}
              dataSource={getFilteredData(subscriptions)}
              loading={loading}
              error={error}
              rowKey="id"
              pagination={{ pageSize: 10 }}
              onRefresh={() => { setSelectedSubKeys([]); fetchData(); }}
              onExportCsv={handleExportCsv}
              onExportExcel={handleExportExcel}
              onTableChange={handleTableChange}
              extra={
                <Button type="primary" icon={<PlusOutlined />} onClick={() => handleCreate('subscription')}>
                  New Subscription
                </Button>
              }
            />
          </>
        );
      case 'invoices':
        return (
          <>
            {selectedInvoiceKeys.length > 0 && (
              <Button danger onClick={handleBulkDeleteInvoices} style={{ marginBottom: 16 }}>
                Xóa đã chọn ({selectedInvoiceKeys.length})
              </Button>
            )}
            <CommonTable
              rowSelection={{ selectedRowKeys: selectedInvoiceKeys, onChange: (keys: React.Key[]) => setSelectedInvoiceKeys(keys as string[]) }}
              columns={invoiceColumns}
              dataSource={getFilteredData(invoices)}
              loading={loading}
              error={error}
              rowKey="id"
              pagination={{ pageSize: 10 }}
              onRefresh={() => { setSelectedInvoiceKeys([]); fetchData(); }}
              onExportCsv={handleExportCsv}
              onExportExcel={handleExportExcel}
              onTableChange={handleTableChange}
            />
          </>
        );
      case 'usage':
        return (
          <>
            {selectedUsageKeys.length > 0 && (
              <Button danger onClick={handleBulkDeleteUsage} style={{ marginBottom: 16 }}>
                Xóa đã chọn ({selectedUsageKeys.length})
              </Button>
            )}
            <CommonTable
              rowSelection={{ selectedRowKeys: selectedUsageKeys, onChange: (keys: React.Key[]) => setSelectedUsageKeys(keys as string[]) }}
              columns={usageColumns}
              dataSource={getFilteredData(usage)}
              loading={loading}
              error={error}
              rowKey="id"
              pagination={{ pageSize: 10 }}
              onRefresh={() => { setSelectedUsageKeys([]); fetchData(); }}
              onExportCsv={handleExportCsv}
              onExportExcel={handleExportExcel}
              onTableChange={handleTableChange}
            />
          </>
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
          storageKey="vcall-saved-filters-billing"
        />
      </div>

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
