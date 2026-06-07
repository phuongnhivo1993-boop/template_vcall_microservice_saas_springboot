'use client';

import { useState, useEffect, useCallback, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { useUrlState } from '@/lib/hooks/useUrlState';
import { Button, Tag, Space, Typography, Form, Input, Select, message, Modal } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, CopyOutlined } from '@ant-design/icons';
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
  const [urlParams, setUrlParams] = useUrlState({
    name: '', status: '', plan: '',
    page: '1', pageSize: '10',
  });
  const filters = useMemo(() => {
    const f: Record<string, any> = {};
    if (urlParams.name) f.name = urlParams.name;
    if (urlParams.status) f.status = urlParams.status;
    if (urlParams.plan) f.plan = urlParams.plan;
    return f;
  }, [urlParams]);
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: parseInt(urlParams.page),
    pageSize: parseInt(urlParams.pageSize),
    total: 0,
  });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);
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
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load customers');
      setCustomers([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCustomers(parseInt(urlParams.page), parseInt(urlParams.pageSize), filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleTableChange = (
    pag: TablePaginationConfig,
    _filters: any,
    _sorter: SorterResult<Customer> | SorterResult<Customer>[],
  ) => {
    setUrlParams({ page: String(pag.current), pageSize: String(pag.pageSize) });
    fetchCustomers(pag.current, pag.pageSize, filters);
  };

  const handleSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        cleaned[key] = val;
      }
    });
    setUrlParams({
      name: cleaned.name || '',
      status: cleaned.status || '',
      plan: cleaned.plan || '',
      page: '1',
    });
    fetchCustomers(1, pagination.pageSize, cleaned);
  };

  const handleReset = () => {
    setUrlParams({ name: '', status: '', plan: '', page: '1' });
    fetchCustomers(1, pagination.pageSize);
  };

  const handleBulkDelete = () => {
    Modal.confirm({
      title: 'Xóa nhiều khách hàng',
      content: `Bạn có chắc chắn muốn xóa ${selectedRowKeys.length} khách hàng đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await customersApi.bulkDelete(selectedRowKeys);
          message.success(`Đã xóa ${selectedRowKeys.length} khách hàng`);
          setSelectedRowKeys([]);
          fetchCustomers(pagination.current, pagination.pageSize, filters);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleCreate = () => {
    setEditingCustomer(null);
    setModalOpen(true);
  };

  const handleEdit = (customer: Customer) => {
    setEditingCustomer(customer);
    setModalOpen(true);
  };

  const handleDuplicate = (record: Customer) => {
    setEditingCustomer({ ...record, id: '' } as Customer);
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
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Delete failed');
          throw err;
        }
        fetchCustomers(pagination.current, pagination.pageSize, filters);
      },
    });
  };

  const handleFormSubmit = async (values: any) => {
    try {
      if (editingCustomer?.id) {
        await customersApi.update(editingCustomer.id, values);
        message.success('Customer updated successfully');
      } else {
        await customersApi.create(values);
        message.success('Customer created successfully');
      }
      setModalOpen(false);
      fetchCustomers(pagination.current, pagination.pageSize, filters);
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Failed to save customer');
    }
  };

  const handleExportCsv = async () => {
    try {
      const res = await customersApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `customers_${new Date().toISOString().slice(0,10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Customers exported');
    } catch {
      message.error('Export failed');
    }
  };

  const handleExportExcel = async () => {
    try {
      const res = await customersApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `customers_${new Date().toISOString().slice(0,10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Customers exported');
    } catch {
      message.error('Export failed');
    }
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
          <Button type="link" icon={<CopyOutlined />} onClick={() => handleDuplicate(record)}>
            Nhân bản
          </Button>
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
          initialValues={filters}
        />
        <SavedFilters
          currentValues={filters}
          onApply={(values) => {
            setUrlParams({
              name: values.name || '',
              status: values.status || '',
              plan: values.plan || '',
              page: '1',
            });
            fetchCustomers(1, pagination.pageSize, values);
          }}
          storageKey="vcall-saved-filters-customers"
        />
      </div>
      {selectedRowKeys.length > 0 && (
        <Button danger onClick={handleBulkDelete} style={{ marginBottom: 16 }}>
          Xóa đã chọn ({selectedRowKeys.length})
        </Button>
      )}
      <CommonTable<Customer>
        rowSelection={{ selectedRowKeys, onChange: (keys: React.Key[]) => setSelectedRowKeys(keys as string[]) }}
        columns={columns}
        dataSource={customers}
        loading={loading}
        error={error}
        rowKey="id"
        pagination={pagination}
        onRefresh={() => { setSelectedRowKeys([]); fetchCustomers(pagination.current, pagination.pageSize, filters); }}
        onExportCsv={handleExportCsv}
        onExportExcel={handleExportExcel}
        onTableChange={handleTableChange}
      />
      <CommonForm
        open={modalOpen}
        title={editingCustomer?.id ? 'Edit Customer' : 'Add Customer'}
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
