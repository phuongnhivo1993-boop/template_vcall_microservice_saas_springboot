'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Button, Tag, Space, Typography, Form, Input, Select, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { SorterResult } from 'antd/es/table/interface';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { Can } from '@/components/common/Can';
import { Permissions } from '@/lib/permissions';
import { customersApi } from '@/lib/api';

const { Title } = Typography;

interface Customer {
  id: string;
  name: string;
  email: string;
  phone: string;
  status: 'active' | 'inactive' | 'blocked';
  plan: string;
  totalCalls: number;
  lastContact: string;
}

const mockCustomers: Customer[] = [
  { id: 'C-001', name: 'John Smith', email: 'john@example.com', phone: '+1 (555) 123-4567', status: 'active', plan: 'Premium', totalCalls: 45, lastContact: '2026-06-01' },
  { id: 'C-002', name: 'Alice Brown', email: 'alice@example.com', phone: '+1 (555) 234-5678', status: 'active', plan: 'Enterprise', totalCalls: 128, lastContact: '2026-06-01' },
  { id: 'C-003', name: 'Bob Wilson', email: 'bob@example.com', phone: '+1 (555) 345-6789', status: 'inactive', plan: 'Basic', totalCalls: 12, lastContact: '2026-05-28' },
  { id: 'C-004', name: 'Carol Davis', email: 'carol@example.com', phone: '+1 (555) 456-7890', status: 'active', plan: 'Premium', totalCalls: 67, lastContact: '2026-06-01' },
  { id: 'C-005', name: 'Tom Harris', email: 'tom@example.com', phone: '+1 (555) 567-8901', status: 'blocked', plan: 'Basic', totalCalls: 3, lastContact: '2026-05-15' },
  { id: 'C-006', name: 'Diana Clark', email: 'diana@example.com', phone: '+1 (555) 678-9012', status: 'active', plan: 'Enterprise', totalCalls: 203, lastContact: '2026-06-01' },
];

const statusColors: Record<string, string> = {
  active: 'green',
  inactive: 'orange',
  blocked: 'red',
};

const planOptions = [
  { value: 'Basic', label: 'Basic' },
  { value: 'Premium', label: 'Premium' },
  { value: 'Enterprise', label: 'Enterprise' },
];

export default function CustomersPage() {
  const router = useRouter();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [filters, setFilters] = useState<Record<string, any>>({});
  const [useMock, setUseMock] = useState(false);

  const fetchCustomers = useCallback(async (page = 1, size = 10, params?: Record<string, any>) => {
    setLoading(true);
    setError(null);
    try {
      const res = await customersApi.list({ page: page - 1, size, ...params });
      const data = res.data;
      if (data.content) {
        setCustomers(data.content);
        setPagination((prev) => ({
          ...prev,
          current: data.page + 1,
          pageSize: data.size,
          total: data.totalElements,
        }));
      } else if (Array.isArray(data)) {
        setCustomers(data);
      } else if (data.data) {
        setCustomers(Array.isArray(data.data) ? data.data : []);
      }
      setUseMock(false);
    } catch (err: any) {
      if (!useMock) {
        setUseMock(true);
        const filtered = mockCustomers.filter((c) => {
          if (!params) return true;
          if (params.name && !c.name.toLowerCase().includes(params.name.toLowerCase())) return false;
          if (params.status && c.status !== params.status) return false;
          if (params.plan && c.plan !== params.plan) return false;
          return true;
        });
        setCustomers(filtered);
        setPagination((prev) => ({ ...prev, total: filtered.length }));
      } else {
        setError(err?.response?.data?.message || err?.message || 'Failed to load customers');
        setCustomers([]);
      }
    } finally {
      setLoading(false);
    }
  }, [useMock]);

  useEffect(() => {
    fetchCustomers(1, pagination.pageSize);
  }, []);

  const handleTableChange = (
    pag: TablePaginationConfig,
    _filters: any,
    _sorter: SorterResult<Customer> | SorterResult<Customer>[],
  ) => {
    fetchCustomers(pag.current, pag.pageSize, filters);
  };

  const handleSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        cleaned[key] = val;
      }
    });
    setFilters(cleaned);
    if (useMock) {
      const filtered = mockCustomers.filter((c) => {
        if (cleaned.name && !c.name.toLowerCase().includes(cleaned.name.toLowerCase())) return false;
        if (cleaned.status && c.status !== cleaned.status) return false;
        if (cleaned.plan && c.plan !== cleaned.plan) return false;
        return true;
      });
      setCustomers(filtered);
      setPagination((prev) => ({ ...prev, total: filtered.length }));
    } else {
      fetchCustomers(1, pagination.pageSize, cleaned);
    }
  };

  const handleReset = () => {
    setFilters({});
    if (useMock) {
      setCustomers(mockCustomers);
      setPagination((prev) => ({ ...prev, total: mockCustomers.length }));
    } else {
      fetchCustomers(1, pagination.pageSize);
    }
  };

  const handleCreate = () => {
    setEditingCustomer(null);
    setModalOpen(true);
  };

  const handleEdit = (customer: Customer) => {
    setEditingCustomer(customer);
    setModalOpen(true);
  };

  const handleDelete = (customer: Customer) => {
    showDeleteConfirm({
      title: 'Delete Customer',
      content: `Are you sure you want to delete ${customer.name}? This action cannot be undone.`,
      onOk: async () => {
        try {
          await customersApi.delete(customer.id);
          message.success('Deleted successfully');
        } catch {
          if (useMock) message.success('Deleted successfully (mock)');
          else throw new Error('Delete failed');
        }
        fetchCustomers(pagination.current, pagination.pageSize, filters);
      },
    });
  };

  const handleFormSubmit = async (values: any) => {
    if (editingCustomer) {
      await customersApi.update(editingCustomer.id, values);
    } else {
      await customersApi.create(values);
    }
    fetchCustomers(pagination.current, pagination.pageSize, filters);
  };

  const handleExportCsv = () => {
    message.info('CSV export triggered');
  };

  const handleExportExcel = () => {
    message.info('Excel export triggered');
  };

  const columns: ColumnsType<Customer> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
      render: (name: string) => <span style={{ fontWeight: 500 }}>{name}</span>,
    },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Phone', dataIndex: 'phone', key: 'phone' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={statusColors[status] || 'default'}>{status.toUpperCase()}</Tag>
      ),
    },
    { title: 'Plan', dataIndex: 'plan', key: 'plan' },
    { title: 'Total Calls', dataIndex: 'totalCalls', key: 'totalCalls' },
    { title: 'Last Contact', dataIndex: 'lastContact', key: 'lastContact' },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Customer) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => router.push(`/customers/${record.id}`)}>
            View
          </Button>
          <Can I={Permissions.CUSTOMER_EDIT}>
            <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
              Edit
            </Button>
          </Can>
          <Can I={Permissions.CUSTOMER_DELETE}>
            <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
              Delete
            </Button>
          </Can>
        </Space>
      ),
    },
  ];

  const searchFields = [
    { name: 'name', label: 'Name', type: 'input' as const, placeholder: 'Search by name' },
    {
      name: 'status',
      label: 'Status',
      type: 'select' as const,
      placeholder: 'Filter by status',
      options: [
        { value: 'active', label: 'Active' },
        { value: 'inactive', label: 'Inactive' },
        { value: 'blocked', label: 'Blocked' },
      ],
    },
    {
      name: 'plan',
      label: 'Plan',
      type: 'select' as const,
      placeholder: 'Filter by plan',
      options: planOptions,
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>Customers</Title>
        <Can I={Permissions.CUSTOMER_CREATE}>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            Add Customer
          </Button>
        </Can>
      </div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
        <CommonSearch
          fields={searchFields}
          onSearch={handleSearch}
          onReset={handleReset}
          loading={loading}
        />
        <SavedFilters
          currentValues={filters}
          onApply={(values) => {
            setFilters(values);
            if (useMock) {
              let filtered = [...mockCustomers];
              if (values.name) filtered = filtered.filter((c) => c.name.toLowerCase().includes(values.name.toLowerCase()));
              if (values.status) filtered = filtered.filter((c) => c.status === values.status);
              if (values.plan) filtered = filtered.filter((c) => c.plan === values.plan);
              setCustomers(filtered);
              setPagination((prev) => ({ ...prev, total: filtered.length }));
            } else {
              fetchCustomers(1, pagination.pageSize, values);
            }
          }}
          storageKey="vcall-saved-filters-customers"
        />
      </div>
      <CommonTable<Customer>
        columns={columns}
        dataSource={customers}
        loading={loading}
        error={error}
        rowKey="id"
        pagination={pagination}
        onRefresh={() => fetchCustomers(pagination.current, pagination.pageSize, filters)}
        onExportCsv={handleExportCsv}
        onExportExcel={handleExportExcel}
        onTableChange={handleTableChange}
      />
      <CommonForm
        open={modalOpen}
        title={editingCustomer ? 'Edit Customer' : 'Add Customer'}
        onClose={() => { setModalOpen(false); setEditingCustomer(null); }}
        onSubmit={handleFormSubmit}
        initialValues={editingCustomer}
        width={600}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter name' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email', message: 'Please enter a valid email' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="phone" label="Phone" rules={[{ required: true, message: 'Please enter phone' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="plan" label="Plan" rules={[{ required: true, message: 'Please select plan' }]}>
          <Select options={planOptions} />
        </Form.Item>
        <Form.Item name="status" label="Status" initialValue="active">
          <Select options={[
            { value: 'active', label: 'Active' },
            { value: 'inactive', label: 'Inactive' },
            { value: 'blocked', label: 'Blocked' },
          ]} />
        </Form.Item>
      </CommonForm>
    </div>
  );
}
