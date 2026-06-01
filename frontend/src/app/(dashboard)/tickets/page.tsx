'use client';

import { useState } from 'react';
import { Table, Card, Select, Tag, Typography, Space, Button, Progress, Badge, message } from 'antd';
import { PlusOutlined, ClockCircleOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

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

const ticketsData: Ticket[] = [
  { id: 'TK-001', subject: 'Cannot reset password', customer: 'John Smith', agent: 'Sarah J.', priority: 'high', status: 'open', category: 'Technical', created: '2026-06-01 09:00', slaDeadline: '2026-06-01 15:00', slaPassed: false },
  { id: 'TK-002', subject: 'Billing discrepancy on invoice #INV-042', customer: 'Alice Brown', agent: 'Mike R.', priority: 'critical', status: 'in_progress', category: 'Billing', created: '2026-06-01 08:30', slaDeadline: '2026-06-01 12:30', slaPassed: true },
  { id: 'TK-003', subject: 'Feature request: call recording export', customer: 'Bob Wilson', agent: 'Emily W.', priority: 'low', status: 'open', category: 'Feature', created: '2026-05-31 14:00', slaDeadline: '2026-06-07 14:00', slaPassed: false },
  { id: 'TK-004', subject: 'Audio issues during calls', customer: 'Carol Davis', agent: 'John D.', priority: 'high', status: 'in_progress', category: 'Technical', created: '2026-06-01 07:00', slaDeadline: '2026-06-01 13:00', slaPassed: false },
  { id: 'TK-005', subject: 'Need call history for audit', customer: 'Tom Harris', agent: 'Lisa M.', priority: 'medium', status: 'resolved', category: 'Request', created: '2026-05-30 10:00', slaDeadline: '2026-06-02 10:00', slaPassed: false },
  { id: 'TK-006', subject: 'Extension not working', customer: 'Diana Clark', agent: 'David C.', priority: 'critical', status: 'open', category: 'Technical', created: '2026-06-01 10:00', slaDeadline: '2026-06-01 11:00', slaPassed: false },
];

const priorityColors: Record<string, string> = {
  low: 'green',
  medium: 'blue',
  high: 'orange',
  critical: 'red',
};

const statusColors: Record<string, string> = {
  open: '#1677ff',
  in_progress: '#faad14',
  resolved: '#52c41a',
  closed: '#d9d9d9',
};

export default function TicketsPage() {
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [priorityFilter, setPriorityFilter] = useState<string | undefined>();

  const filtered = ticketsData.filter((t) => {
    const matchesStatus = !statusFilter || t.status === statusFilter;
    const matchesPriority = !priorityFilter || t.priority === priorityFilter;
    return matchesStatus && matchesPriority;
  });

  const handleStatusChange = (ticket: Ticket, newStatus: string) => {
    message.success(`Ticket ${ticket.id} moved to ${newStatus}`);
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      render: (id: string) => <a style={{ fontWeight: 500 }}>{id}</a>,
    },
    { title: 'Subject', dataIndex: 'subject', key: 'subject' },
    { title: 'Customer', dataIndex: 'customer', key: 'customer' },
    { title: 'Agent', dataIndex: 'agent', key: 'agent' },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      render: (p: string) => <Tag color={priorityColors[p]}>{p.toUpperCase()}</Tag>,
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
          options={[
            { value: 'open', label: 'Open' },
            { value: 'in_progress', label: 'In Progress' },
            { value: 'resolved', label: 'Resolved' },
            { value: 'closed', label: 'Closed' },
          ]}
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
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>Tickets</Title>
        <Button type="primary" icon={<PlusOutlined />}>Create Ticket</Button>
      </div>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Select
            placeholder="Filter by status"
            value={statusFilter}
            onChange={setStatusFilter}
            allowClear
            style={{ width: 160 }}
            options={[
              { value: 'open', label: 'Open' },
              { value: 'in_progress', label: 'In Progress' },
              { value: 'resolved', label: 'Resolved' },
              { value: 'closed', label: 'Closed' },
            ]}
          />
          <Select
            placeholder="Filter by priority"
            value={priorityFilter}
            onChange={setPriorityFilter}
            allowClear
            style={{ width: 160 }}
            options={[
              { value: 'low', label: 'Low' },
              { value: 'medium', label: 'Medium' },
              { value: 'high', label: 'High' },
              { value: 'critical', label: 'Critical' },
            ]}
          />
        </Space>
        <Table
          dataSource={filtered}
          columns={columns}
          rowKey="id"
          pagination={{ pageSize: 10 }}
        />
      </Card>
    </div>
  );
}
