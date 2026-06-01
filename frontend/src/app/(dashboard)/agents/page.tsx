'use client';

import { useState } from 'react';
import { Table, Card, Select, Tag, Button, Space, Typography, Badge, Switch, Modal, message } from 'antd';
import { PlusOutlined, TeamOutlined } from '@ant-design/icons';

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

const agentsData: Agent[] = [
  { id: 'AG-001', name: 'Sarah Johnson', email: 'sarah@vcall.com', status: 'online', role: 'Senior Agent', group: 'Support', extension: '101', activeCalls: 1, totalCalls: 145, satisfaction: 95 },
  { id: 'AG-002', name: 'Mike Roberts', email: 'mike@vcall.com', status: 'busy', role: 'Agent', group: 'Support', extension: '102', activeCalls: 2, totalCalls: 128, satisfaction: 88 },
  { id: 'AG-003', name: 'Emily Wilson', email: 'emily@vcall.com', status: 'online', role: 'Senior Agent', group: 'Billing', extension: '103', activeCalls: 0, totalCalls: 112, satisfaction: 92 },
  { id: 'AG-004', name: 'John Davis', email: 'john@vcall.com', status: 'break', role: 'Agent', group: 'Support', extension: '104', activeCalls: 0, totalCalls: 98, satisfaction: 85 },
  { id: 'AG-005', name: 'Lisa Martinez', email: 'lisa@vcall.com', status: 'offline', role: 'Trainee', group: 'Support', extension: '105', activeCalls: 0, totalCalls: 56, satisfaction: 90 },
  { id: 'AG-006', name: 'David Chen', email: 'david@vcall.com', status: 'online', role: 'Agent', group: 'Technical', extension: '106', activeCalls: 1, totalCalls: 78, satisfaction: 93 },
];

const statusColors: Record<string, string> = {
  online: '#52c41a',
  busy: '#faad14',
  break: '#722ed1',
  offline: '#d9d9d9',
};

export default function AgentsPage() {
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [groupFilter, setGroupFilter] = useState<string | undefined>();

  const filtered = agentsData.filter((a) => {
    const matchesStatus = !statusFilter || a.status === statusFilter;
    const matchesGroup = !groupFilter || a.group === groupFilter;
    return matchesStatus && matchesGroup;
  });

  const handleStatusChange = (agent: Agent, newStatus: string) => {
    message.success(`${agent.name} status changed to ${newStatus}`);
  };

  const columns = [
    {
      title: 'Agent',
      dataIndex: 'name',
      key: 'name',
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
          options={[
            { value: 'online', label: 'Online' },
            { value: 'busy', label: 'Busy' },
            { value: 'break', label: 'Break' },
            { value: 'offline', label: 'Offline' },
          ]}
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
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>Agents</Title>
        <Button type="primary" icon={<PlusOutlined />}>Add Agent</Button>
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
              { value: 'online', label: 'Online' },
              { value: 'busy', label: 'Busy' },
              { value: 'break', label: 'Break' },
              { value: 'offline', label: 'Offline' },
            ]}
          />
          <Select
            placeholder="Filter by group"
            value={groupFilter}
            onChange={setGroupFilter}
            allowClear
            style={{ width: 160 }}
            options={[
              { value: 'Support', label: 'Support' },
              { value: 'Billing', label: 'Billing' },
              { value: 'Technical', label: 'Technical' },
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
