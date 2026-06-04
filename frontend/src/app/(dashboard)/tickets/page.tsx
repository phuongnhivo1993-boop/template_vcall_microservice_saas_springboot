'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Button, Tag, Space, Typography, Form, Input, Select, message, Badge, Progress } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ClockCircleOutlined, EyeOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { SorterResult } from 'antd/es/table/interface';
import dayjs from 'dayjs';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { Can } from '@/components/common/Can';
import { Permissions } from '@/lib/permissions';
import { ticketsApi } from '@/lib/api';

const { Title } = Typography;

interface Ticket {
  id: string;
  subject: string;
  customer: string;
  agent: string;
  priority: 'low' | 'medium' | 'high' | 'critical';
  status: 'open' | 'in_progress' | 'resolved' | 'closed';
  category: string;
  created: string;
  slaDeadline: string;
  slaPassed: boolean;
}



const priorityColors: Record<string, string> = {
  low: 'green',
  medium: 'blue',
  high: 'orange',
  critical: 'red',
};

const statusOptions = [
  { value: 'open', label: 'Open' },
  { value: 'in_progress', label: 'In Progress' },
  { value: 'resolved', label: 'Resolved' },
  { value: 'closed', label: 'Closed' },
];

const priorityOptions = [
  { value: 'low', label: 'Low' },
  { value: 'medium', label: 'Medium' },
  { value: 'high', label: 'High' },
  { value: 'critical', label: 'Critical' },
];

const categoryOptions = [
  { value: 'Technical', label: 'Technical' },
  { value: 'Billing', label: 'Billing' },
  { value: 'Feature', label: 'Feature' },
  { value: 'Request', label: 'Request' },
];

export default function TicketsPage() {
  const router = useRouter();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTicket, setEditingTicket] = useState<Ticket | null>(null);
  const [filters, setFilters] = useState<Record<string, any>>({});
  const fetchTickets = useCallback(async (page = 1, size = 10, params?: Record<string, any>) => {
    setLoading(true);
    setError(null);
    try {
      const res = await ticketsApi.list({ page: page - 1, size, ...params });
      const data = res.data;
      if (data.content) {
        setTickets(data.content);
        setPagination((prev) => ({
          ...prev,
          current: data.page + 1,
          pageSize: data.size,
          total: data.totalElements,
        }));
      } else if (Array.isArray(data)) {
        setTickets(data);
      } else if (data.data) {
        setTickets(Array.isArray(data.data) ? data.data : []);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load tickets');
      setTickets([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTickets(1, pagination.pageSize);
  }, []);

  const handleTableChange = (
    pag: TablePaginationConfig,
    _filters: any,
    _sorter: SorterResult<Ticket> | SorterResult<Ticket>[],
  ) => {
    fetchTickets(pag.current, pag.pageSize, filters);
  };

  const handleSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        cleaned[key] = val;
      }
    });
    setFilters(cleaned);
    fetchTickets(1, pagination.pageSize, cleaned);
  };

  const handleReset = () => {
    setFilters({});
    fetchTickets(1, pagination.pageSize);
  };

  const handleStatusChange = async (ticket: Ticket, newStatus: string) => {
    try {
      await ticketsApi.updateStatus(ticket.id, newStatus);
      message.success(`Ticket ${ticket.id} moved to ${newStatus}`);
      fetchTickets(pagination.current, pagination.pageSize, filters);
    } catch (err: any) {
      message.error(err?.response?.data?.message || 'Failed to update status');
    }
  };

  const handleCreate = () => {
    setEditingTicket(null);
    setModalOpen(true);
  };

  const handleEdit = (ticket: Ticket) => {
    setEditingTicket(ticket);
    setModalOpen(true);
  };

  const handleDelete = (ticket: Ticket) => {
    showDeleteConfirm({
      title: 'Delete Ticket',
      content: `Are you sure you want to delete ticket ${ticket.id}? This action cannot be undone.`,
      onOk: async () => {
        await ticketsApi.delete(ticket.id);
        fetchTickets(pagination.current, pagination.pageSize, filters);
      },
    });
  };

  const handleFormSubmit = async (values: any) => {
    if (editingTicket) {
      await ticketsApi.update(editingTicket.id, values);
    } else {
      await ticketsApi.create(values);
    }
    fetchTickets(pagination.current, pagination.pageSize, filters);
  };

  const handleExportCsv = async () => {
    try {
      const res = await ticketsApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `tickets_${new Date().toISOString().slice(0,10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Tickets exported');
    } catch {
      message.error('Export failed');
    }
  };

  const handleExportExcel = async () => {
    try {
      const res = await ticketsApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `tickets_${new Date().toISOString().slice(0,10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Tickets exported');
    } catch {
      message.error('Export failed');
    }
  };

  const columns: ColumnsType<Ticket> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      sorter: true,
      render: (id: string) => <span style={{ fontWeight: 500 }}>{id}</span>,
    },
    { title: 'Subject', dataIndex: 'subject', key: 'subject' },
    { title: 'Customer', dataIndex: 'customer', key: 'customer' },
    { title: 'Agent', dataIndex: 'agent', key: 'agent' },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      render: (p: string) => (
        <Tag color={priorityColors[p] || 'default'}>{p.toUpperCase()}</Tag>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string, record: Ticket) => (
        <Select
          value={status}
          onChange={(val) => handleStatusChange(record, val)}
          size="small"
          style={{ width: 130 }}
          options={statusOptions}
        />
      ),
    },
    { title: 'Category', dataIndex: 'category', key: 'category' },
    {
      title: 'SLA',
      key: 'sla',
      render: (_: unknown, record: Ticket) => {
        const total = dayjs(record.slaDeadline).diff(dayjs(record.created), 'hour');
        const remaining = dayjs(record.slaDeadline).diff(dayjs(), 'hour');
        const pct = Math.max(0, Math.min(100, ((total - remaining) / total) * 100));
        return (
          <Space>
            <Progress
              type="circle"
              percent={Math.round(pct)}
              size={32}
              strokeColor={pct > 80 ? '#ff4d4f' : pct > 50 ? '#faad14' : '#52c41a'}
            />
            {record.slaPassed && <Tag color="red" icon={<ClockCircleOutlined />}>Overdue</Tag>}
          </Space>
        );
      },
    },
    { title: 'Created', dataIndex: 'created', key: 'created' },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Ticket) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => router.push(`/tickets/${record.id}`)}>
            View
          </Button>
          <Can I={Permissions.TICKET_EDIT}>
            <Button type="link" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
              Edit
            </Button>
          </Can>
          <Can I={Permissions.TICKET_DELETE}>
            <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
              Delete
            </Button>
          </Can>
        </Space>
      ),
    },
  ];

  const searchFields = [
    { name: 'subject', label: 'Subject', type: 'input' as const, placeholder: 'Search by subject' },
    {
      name: 'status',
      label: 'Status',
      type: 'select' as const,
      placeholder: 'Filter by status',
      options: statusOptions,
    },
    {
      name: 'priority',
      label: 'Priority',
      type: 'select' as const,
      placeholder: 'Filter by priority',
      options: priorityOptions,
    },
    {
      name: 'category',
      label: 'Category',
      type: 'select' as const,
      placeholder: 'Filter by category',
      options: categoryOptions,
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>Tickets</Title>
        <Can I={Permissions.TICKET_CREATE}>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
            Create Ticket
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
            fetchTickets(1, pagination.pageSize, values);
          }}
          storageKey="vcall-saved-filters-tickets"
        />
      </div>
      <CommonTable<Ticket>
        columns={columns}
        dataSource={tickets}
        loading={loading}
        error={error}
        rowKey="id"
        pagination={pagination}
        onRefresh={() => fetchTickets(pagination.current, pagination.pageSize, filters)}
        onExportCsv={handleExportCsv}
        onExportExcel={handleExportExcel}
        onTableChange={handleTableChange}
      />
      <CommonForm
        open={modalOpen}
        title={editingTicket ? 'Edit Ticket' : 'Create Ticket'}
        onClose={() => { setModalOpen(false); setEditingTicket(null); }}
        onSubmit={handleFormSubmit}
        initialValues={editingTicket}
        width={600}
      >
        <Form.Item name="subject" label="Subject" rules={[{ required: true, message: 'Please enter subject' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="customer" label="Customer" rules={[{ required: true, message: 'Please enter customer' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="agent" label="Agent" rules={[{ required: true, message: 'Please enter agent' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="priority" label="Priority" rules={[{ required: true, message: 'Please select priority' }]}>
          <Select options={priorityOptions} />
        </Form.Item>
        <Form.Item name="status" label="Status" initialValue="open">
          <Select options={statusOptions} />
        </Form.Item>
        <Form.Item name="category" label="Category" rules={[{ required: true, message: 'Please select category' }]}>
          <Select options={categoryOptions} />
        </Form.Item>
      </CommonForm>
    </div>
  );
}
