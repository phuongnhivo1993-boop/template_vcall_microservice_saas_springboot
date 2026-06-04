'use client';

import { useState, useEffect, useCallback, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { useUrlState } from '@/lib/hooks/useUrlState';
import { Button, Tag, Space, Badge, Form, Input, Select, message, Typography, Modal } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined, CopyOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { Can } from '@/components/common/Can';
import { Permissions } from '@/lib/permissions';
import { agentsApi } from '@/lib/api';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { SorterResult } from 'antd/es/table/interface';

const { Title } = Typography;

interface Agent {
  id: string;
  name: string;
  email: string;
  status: 'online' | 'offline' | 'busy' | 'break';
  role: string;
  group: string;
  extension: string;
  activeCalls: number;
  totalCalls: number;
  satisfaction: number;
}

const statusColors: Record<string, string> = {
  online: '#52c41a',
  busy: '#faad14',
  break: '#722ed1',
  offline: '#d9d9d9',
};

const statusOptions = [
  { value: 'online', label: 'Online' },
  { value: 'busy', label: 'Busy' },
  { value: 'break', label: 'Break' },
  { value: 'offline', label: 'Offline' },
];

export default function AgentsPage() {
  const router = useRouter();
  const [agents, setAgents] = useState<Agent[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [urlParams, setUrlParams] = useUrlState({
    name: '', status: '', group: '',
    page: '1', pageSize: '10',
  });
  const filters = useMemo(() => {
    const f: Record<string, any> = {};
    if (urlParams.name) f.name = urlParams.name;
    if (urlParams.status) f.status = urlParams.status;
    if (urlParams.group) f.group = urlParams.group;
    return f;
  }, [urlParams]);
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: parseInt(urlParams.page),
    pageSize: parseInt(urlParams.pageSize),
    total: 0,
  });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingAgent, setEditingAgent] = useState<Agent | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  const fetchAgents = useCallback(async (page = 1, size = 10, params?: Record<string, any>) => {
    setLoading(true);
    setError(null);
    try {
      const res = await agentsApi.list({ page: page - 1, size, ...params });
      const data = res.data;
      if (data.content) {
        setAgents(data.content);
        setPagination((prev) => ({
          ...prev,
          current: data.page + 1,
          pageSize: data.size,
          total: data.totalElements,
        }));
      } else if (Array.isArray(data)) {
        setAgents(data);
      } else if (data.data) {
        setAgents(Array.isArray(data.data) ? data.data : []);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load agents');
      setAgents([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAgents(parseInt(urlParams.page), parseInt(urlParams.pageSize), filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleTableChange = (
    pag: TablePaginationConfig,
    _filters: any,
    _sorter: SorterResult<Agent> | SorterResult<Agent>[],
  ) => {
    setUrlParams({ page: String(pag.current), pageSize: String(pag.pageSize) });
    fetchAgents(pag.current, pag.pageSize, filters);
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
      group: cleaned.group || '',
      page: '1',
    });
    fetchAgents(1, pagination.pageSize, cleaned);
  };

  const handleReset = () => {
    setUrlParams({ name: '', status: '', group: '', page: '1' });
    fetchAgents(1, pagination.pageSize);
  };

  const handleStatusChange = async (agent: Agent, newStatus: string) => {
    try {
      await agentsApi.updateStatus(agent.id, newStatus);
      message.success(`${agent.name} status changed to ${newStatus}`);
      fetchAgents(pagination.current, pagination.pageSize, filters);
    } catch (err: any) {
      message.error(err?.response?.data?.message || 'Failed to update status');
    }
  };

  const handleBulkDelete = () => {
    Modal.confirm({
      title: 'Xóa nhiều đại lý',
      content: `Bạn có chắc chắn muốn xóa ${selectedRowKeys.length} đại lý đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await agentsApi.bulkDelete(selectedRowKeys);
          message.success(`Đã xóa ${selectedRowKeys.length} đại lý`);
          setSelectedRowKeys([]);
          fetchAgents(pagination.current, pagination.pageSize, filters);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleCreate = () => {
    setEditingAgent(null);
    setModalOpen(true);
  };

  const handleEdit = (agent: Agent) => {
    setEditingAgent(agent);
    setModalOpen(true);
  };

  const handleDuplicate = (record: Agent) => {
    setEditingAgent({ ...record, id: '' } as Agent);
    setModalOpen(true);
  };

  const handleDelete = (agent: Agent) => {
    showDeleteConfirm({
      title: 'Delete Agent',
      content: `Are you sure you want to delete ${agent.name}? This action cannot be undone.`,
      onOk: async () => {
        await agentsApi.delete(agent.id);
        fetchAgents(pagination.current, pagination.pageSize, filters);
      },
    });
  };

  const handleFormSubmit = async (values: any) => {
    if (editingAgent?.id) {
      await agentsApi.update(editingAgent.id, values);
    } else {
      await agentsApi.create(values);
    }
    fetchAgents(pagination.current, pagination.pageSize, filters);
  };

  const handleExportCsv = async () => {
    try {
      const res = await agentsApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `agents_${new Date().toISOString().slice(0,10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Agents exported');
    } catch {
      message.error('Export failed');
    }
  };

  const handleExportExcel = async () => {
    try {
      const res = await agentsApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `agents_${new Date().toISOString().slice(0,10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Agents exported');
    } catch {
      message.error('Export failed');
    }
  };

  const columns: ColumnsType<Agent> = [
    {
      title: 'Agent',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
      render: (name: string, record: Agent) => (
        <Space>
          <Badge status={record.status === 'online' ? 'success' : record.status === 'busy' ? 'warning' : record.status === 'break' ? 'processing' : 'default'} />
          <span style={{ fontWeight: 500 }}>{name}</span>
        </Space>
      ),
    },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string, record: Agent) => (
        <Select
          value={status}
          onChange={(val) => handleStatusChange(record, val)}
          size="small"
          style={{ width: 110 }}
          options={statusOptions}
        />
      ),
    },
    { title: 'Role', dataIndex: 'role', key: 'role' },
    { title: 'Group', dataIndex: 'group', key: 'group' },
    { title: 'Ext', dataIndex: 'extension', key: 'extension' },
    {
      title: 'Active Calls',
      dataIndex: 'activeCalls',
      key: 'activeCalls',
      render: (val: number) => (
        <Tag color={val > 0 ? 'blue' : 'default'}>{val}</Tag>
      ),
    },
    { title: 'Total Calls', dataIndex: 'totalCalls', key: 'totalCalls' },
    {
      title: 'Satisfaction',
      dataIndex: 'satisfaction',
      key: 'satisfaction',
      render: (val: number) => (
        <Tag color={val >= 90 ? 'green' : val >= 80 ? 'orange' : 'red'}>{val}%</Tag>
      ),
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Agent) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => router.push(`/agents/${record.id}`)}>
            View
          </Button>
          <Can I={Permissions.AGENT_EDIT}>
            <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
              Edit
            </Button>
          </Can>
          <Button type="link" icon={<CopyOutlined />} onClick={() => handleDuplicate(record)}>
            Nhân bản
          </Button>
          <Can I={Permissions.AGENT_DELETE}>
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
      options: statusOptions,
    },
    {
      name: 'group',
      label: 'Group',
      type: 'select' as const,
      placeholder: 'Filter by group',
      options: [
        { value: 'Support', label: 'Support' },
        { value: 'Billing', label: 'Billing' },
        { value: 'Technical', label: 'Technical' },
      ],
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>Agents</Title>
        <Can I={Permissions.AGENT_CREATE}>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            Add Agent
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
              group: values.group || '',
              page: '1',
            });
            fetchAgents(1, pagination.pageSize, values);
          }}
          storageKey="vcall-saved-filters-agents"
        />
      </div>
      {selectedRowKeys.length > 0 && (
        <Button danger onClick={handleBulkDelete} style={{ marginBottom: 16 }}>
          Xóa đã chọn ({selectedRowKeys.length})
        </Button>
      )}
      <CommonTable<Agent>
        rowSelection={{ selectedRowKeys, onChange: (keys: React.Key[]) => setSelectedRowKeys(keys as string[]) }}
        columns={columns}
        dataSource={agents}
        loading={loading}
        error={error}
        rowKey="id"
        pagination={pagination}
        onRefresh={() => { setSelectedRowKeys([]); fetchAgents(pagination.current, pagination.pageSize, filters); }}
        onExportCsv={handleExportCsv}
        onExportExcel={handleExportExcel}
        onTableChange={handleTableChange}
      />
      <CommonForm
        open={modalOpen}
        title={editingAgent?.id ? 'Edit Agent' : 'Add Agent'}
        onClose={() => { setModalOpen(false); setEditingAgent(null); }}
        onSubmit={handleFormSubmit}
        initialValues={editingAgent}
        width={600}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter name' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email', message: 'Please enter a valid email' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="role" label="Role" rules={[{ required: true, message: 'Please select role' }]}>
          <Select options={[
            { value: 'Senior Agent', label: 'Senior Agent' },
            { value: 'Agent', label: 'Agent' },
            { value: 'Trainee', label: 'Trainee' },
          ]} />
        </Form.Item>
        <Form.Item name="group" label="Group" rules={[{ required: true, message: 'Please select group' }]}>
          <Select options={[
            { value: 'Support', label: 'Support' },
            { value: 'Billing', label: 'Billing' },
            { value: 'Technical', label: 'Technical' },
          ]} />
        </Form.Item>
        <Form.Item name="extension" label="Extension" rules={[{ required: true, message: 'Please enter extension' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="status" label="Status" initialValue="offline">
          <Select options={statusOptions} />
        </Form.Item>
      </CommonForm>
    </div>
  );
}
