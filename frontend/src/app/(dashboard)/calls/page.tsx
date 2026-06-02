'use client';

import { useState, useEffect } from 'react';
import { Table, Card, Input, Select, DatePicker, Row, Col, Tag, Space, Typography, Button, message } from 'antd';
import { SearchOutlined, PhoneOutlined, DownloadOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';

const { Title } = Typography;
const { RangePicker } = DatePicker;

interface CallRecord {
  id: string;
  caller: string;
  callee: string;
  direction: 'inbound' | 'outbound';
  status: string;
  duration: number;
  agent: string;
  time: string;
}

const callData: CallRecord[] = [
  { id: 'CL-001', caller: '+1 (555) 123-4567', callee: '+1 (555) 987-6543', direction: 'inbound', status: 'completed', duration: 245, agent: 'Sarah J.', time: '2026-06-01 09:23' },
  { id: 'CL-002', caller: '+1 (555) 234-5678', callee: '+1 (555) 876-5432', direction: 'outbound', status: 'completed', duration: 180, agent: 'Mike R.', time: '2026-06-01 09:45' },
  { id: 'CL-003', caller: '+1 (555) 345-6789', callee: '+1 (555) 765-4321', direction: 'inbound', status: 'ongoing', duration: 320, agent: 'Emily W.', time: '2026-06-01 10:02' },
  { id: 'CL-004', caller: '+1 (555) 456-7890', callee: '+1 (555) 654-3210', direction: 'inbound', status: 'missed', duration: 0, agent: '-', time: '2026-06-01 10:15' },
  { id: 'CL-005', caller: '+1 (555) 567-8901', callee: '+1 (555) 543-2109', direction: 'outbound', status: 'completed', duration: 412, agent: 'John D.', time: '2026-06-01 10:30' },
  { id: 'CL-006', caller: '+1 (555) 678-9012', callee: '+1 (555) 432-1098', direction: 'inbound', status: 'completed', duration: 156, agent: 'Lisa M.', time: '2026-06-01 10:45' },
  { id: 'CL-007', caller: '+1 (555) 789-0123', callee: '+1 (555) 321-0987', direction: 'inbound', status: 'failed', duration: 30, agent: 'Sarah J.', time: '2026-06-01 11:00' },
  { id: 'CL-008', caller: '+1 (555) 890-1234', callee: '+1 (555) 210-9876', direction: 'outbound', status: 'completed', duration: 520, agent: 'Mike R.', time: '2026-06-01 11:20' },
];

const statusColors: Record<string, string> = {
  completed: '#52c41a',
  ongoing: '#1677ff',
  missed: '#ff4d4f',
  failed: '#faad14',
};

export default function CallsPage() {
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const [agentFilter, setAgentFilter] = useState<string | undefined>();
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<CallRecord[]>([]);

  useEffect(() => {
    const timer = setTimeout(() => {
      setData(callData);
      setLoading(false);
    }, 500);
    return () => clearTimeout(timer);
  }, []);

  const filtered = data.filter((call) => {
    const matchesSearch =
      call.caller.includes(searchText) ||
      call.callee.includes(searchText) ||
      call.id.toLowerCase().includes(searchText.toLowerCase());
    const matchesStatus = !statusFilter || call.status === statusFilter;
    const matchesAgent = !agentFilter || call.agent === agentFilter;
    return matchesSearch && matchesStatus && matchesAgent;
  });

  const handleExport = () => {
    try {
      const headers = ['Call ID', 'Caller', 'Callee', 'Direction', 'Status', 'Duration (s)', 'Agent', 'Time'];
      const csvContent = [headers.join(','), ...filtered.map(r =>
        [r.id, r.caller, r.callee, r.direction, r.status, r.duration, r.agent, r.time].map(v => `"${v}"`).join(',')
      )].join('\n');
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.download = `calls_export_${dayjs().format('YYYYMMDD_HHmmss')}.csv`;
      link.click();
      URL.revokeObjectURL(link.href);
      message.success('Calls exported successfully');
    } catch {
      message.error('Export failed');
    }
  };

  const columns = [
    {
      title: 'Call ID',
      dataIndex: 'id',
      key: 'id',
      render: (id: string) => <a style={{ fontWeight: 500 }}>{id}</a>,
    },
    {
      title: 'Caller',
      dataIndex: 'caller',
      key: 'caller',
    },
    {
      title: 'Callee',
      dataIndex: 'callee',
      key: 'callee',
    },
    {
      title: 'Direction',
      dataIndex: 'direction',
      key: 'direction',
      render: (dir: string) => (
        <Tag icon={<PhoneOutlined />} color={dir === 'inbound' ? 'blue' : 'purple'}>
          {dir.toUpperCase()}
        </Tag>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={statusColors[status]}>{status.toUpperCase()}</Tag>
      ),
    },
    {
      title: 'Duration',
      dataIndex: 'duration',
      key: 'duration',
      render: (secs: number) => {
        if (secs === 0) return '-';
        const mins = Math.floor(secs / 60);
        const s = secs % 60;
        return `${mins}:${s.toString().padStart(2, '0')}`;
      },
    },
    {
      title: 'Agent',
      dataIndex: 'agent',
      key: 'agent',
    },
    {
      title: 'Time',
      dataIndex: 'time',
      key: 'time',
    },
  ];

  return (
    <div>
      <Title level={3}>Calls</Title>
      <Card>
        <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
          <Col xs={24} sm={8}>
            <Input
              placeholder="Search by caller, callee, or ID"
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              allowClear
            />
          </Col>
          <Col xs={12} sm={6}>
            <Select
              placeholder="Filter by status"
              value={statusFilter}
              onChange={setStatusFilter}
              allowClear
              style={{ width: '100%' }}
              options={[
                { value: 'completed', label: 'Completed' },
                { value: 'ongoing', label: 'Ongoing' },
                { value: 'missed', label: 'Missed' },
                { value: 'failed', label: 'Failed' },
              ]}
            />
          </Col>
          <Col xs={12} sm={6}>
            <Select
              placeholder="Filter by agent"
              value={agentFilter}
              onChange={setAgentFilter}
              allowClear
              style={{ width: '100%' }}
              options={[
                { value: 'Sarah J.', label: 'Sarah J.' },
                { value: 'Mike R.', label: 'Mike R.' },
                { value: 'Emily W.', label: 'Emily W.' },
                { value: 'John D.', label: 'John D.' },
                { value: 'Lisa M.', label: 'Lisa M.' },
              ]}
            />
          </Col>
          <Col xs={24} sm={4}>
            <RangePicker style={{ width: '100%' }} />
          </Col>
        </Row>
        <div style={{ textAlign: 'right', marginBottom: 16 }}>
          <Button icon={<DownloadOutlined />} onClick={handleExport} loading={loading}>
            Export CSV
          </Button>
        </div>
        <Table
          dataSource={filtered}
          columns={columns}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10, showSizeChanger: true }}
          locale={{ emptyText: loading ? 'Loading...' : 'No call records found' }}
        />
      </Card>
    </div>
  );
}
