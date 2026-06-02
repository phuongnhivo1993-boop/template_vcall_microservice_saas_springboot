'use client';

import { useState } from 'react';
import { Card, Statistic, Table, Tag, Badge, Typography, Row, Col, Space, Progress, List, Tooltip } from 'antd';
import {
  PhoneOutlined, TeamOutlined, ClockCircleOutlined, CheckCircleOutlined,
  CloseCircleOutlined, StarOutlined, RiseOutlined, UserSwitchOutlined,
  ArrowUpOutlined, ArrowDownOutlined, MinusCircleOutlined, FileTextOutlined
} from '@ant-design/icons';

const { Title, Text } = Typography;

interface Agent {
  id: string;
  name: string;
  status: 'online' | 'busy' | 'away' | 'offline';
  activeCalls: number;
  totalCalls: number;
  avgHandleTime: number;
  avgWaitTime: number;
  csat: number;
  statusDuration: string;
}

interface Queue {
  id: string;
  name: string;
  callsWaiting: number;
  longestWait: number;
  agentsAssigned: number;
  agentsAvailable: number;
  slaHit: number;
}

interface Activity {
  id: string;
  event: string;
  detail: string;
  time: string;
  type: 'call_start' | 'call_end' | 'login' | 'logout' | 'ticket' | 'transfer';
}

const kpiData = {
  activeCalls: 47,
  agentsOnline: 18,
  agentsTotal: 24,
  callsWaiting: 12,
  avgWaitTime: 23,
  slaCompliance: 94.5,
  abandonRate: 3.2,
  serviceLevel: '80/20',
  csat: 4.2,
};

const agentsData: Agent[] = [
  { id: 'AG-001', name: 'Nguyễn Văn An', status: 'online', activeCalls: 2, totalCalls: 48, avgHandleTime: 245, avgWaitTime: 12, csat: 4.8, statusDuration: '2h 15m' },
  { id: 'AG-002', name: 'Trần Thị Bình', status: 'busy', activeCalls: 1, totalCalls: 42, avgHandleTime: 312, avgWaitTime: 18, csat: 4.5, statusDuration: '45m' },
  { id: 'AG-003', name: 'Lê Hoàng Cường', status: 'online', activeCalls: 0, totalCalls: 55, avgHandleTime: 198, avgWaitTime: 8, csat: 4.9, statusDuration: '3h 30m' },
  { id: 'AG-004', name: 'Phạm Minh Đức', status: 'away', activeCalls: 0, totalCalls: 38, avgHandleTime: 287, avgWaitTime: 22, csat: 4.1, statusDuration: '1h 05m' },
  { id: 'AG-005', name: 'Hoàng Thị Hoa', status: 'busy', activeCalls: 3, totalCalls: 61, avgHandleTime: 356, avgWaitTime: 15, csat: 4.3, statusDuration: '28m' },
  { id: 'AG-006', name: 'Vũ Quốc Huy', status: 'offline', activeCalls: 0, totalCalls: 29, avgHandleTime: 265, avgWaitTime: 20, csat: 3.8, statusDuration: '4h 10m' },
  { id: 'AG-007', name: 'Đặng Thúy Linh', status: 'online', activeCalls: 1, totalCalls: 52, avgHandleTime: 221, avgWaitTime: 10, csat: 4.7, statusDuration: '1h 50m' },
  { id: 'AG-008', name: 'Bùi Thanh Nam', status: 'busy', activeCalls: 2, totalCalls: 44, avgHandleTime: 278, avgWaitTime: 14, csat: 4.4, statusDuration: '32m' },
];

const queueData: Queue[] = [
  { id: 'Q-001', name: 'Tổng đài CSKH', callsWaiting: 5, longestWait: 45, agentsAssigned: 8, agentsAvailable: 3, slaHit: 92.3 },
  { id: 'Q-002', name: 'Hỗ trợ kỹ thuật', callsWaiting: 3, longestWait: 62, agentsAssigned: 6, agentsAvailable: 2, slaHit: 85.7 },
  { id: 'Q-003', name: 'Khiếu nại & Bồi thường', callsWaiting: 2, longestWait: 38, agentsAssigned: 4, agentsAvailable: 1, slaHit: 78.4 },
  { id: 'Q-004', name: 'Bán hàng & Tư vấn', callsWaiting: 0, longestWait: 0, agentsAssigned: 5, agentsAvailable: 4, slaHit: 97.1 },
  { id: 'Q-005', name: 'Hỗ trợ kỹ thuật VIP', callsWaiting: 2, longestWait: 28, agentsAssigned: 3, agentsAvailable: 2, slaHit: 96.8 },
];

const activityData: Activity[] = [
  { id: 'A-001', event: 'Cuộc gọi bắt đầu', detail: 'Nguyễn Văn An - Khách hàng 0987654321', time: '2 phút trước', type: 'call_start' },
  { id: 'A-002', event: 'Cuộc gọi kết thúc', detail: 'Trần Thị Bình - Thời lượng 4:32', time: '3 phút trước', type: 'call_end' },
  { id: 'A-003', event: 'Agent đăng nhập', detail: 'Lê Hoàng Cường đã đăng nhập', time: '5 phút trước', type: 'login' },
  { id: 'A-004', event: 'Ticket được tạo', detail: 'Khách hàng 0977123456 - Hỗ trợ kỹ thuật', time: '7 phút trước', type: 'ticket' },
  { id: 'A-005', event: 'Agent đăng xuất', detail: 'Vũ Quốc Huy đã đăng xuất', time: '10 phút trước', type: 'logout' },
  { id: 'A-006', event: 'Cuộc gọi chuyển', detail: 'Phạm Minh Đức → Nguyễn Văn An', time: '12 phút trước', type: 'transfer' },
  { id: 'A-007', event: 'Cuộc gọi bắt đầu', detail: 'Hoàng Thị Hoa - Khách hàng 0912345678', time: '14 phút trước', type: 'call_start' },
  { id: 'A-008', event: 'Cuộc gọi kết thúc', detail: 'Đặng Thúy Linh - Thời lượng 2:15', time: '16 phút trước', type: 'call_end' },
];

const callVolumeChart = [
  { day: 'T2', calls: 320 }, { day: 'T3', calls: 285 }, { day: 'T4', calls: 356 },
  { day: 'T5', calls: 298 }, { day: 'T6', calls: 412 }, { day: 'T7', calls: 278 },
  { day: 'CN', calls: 195 },
];

const agentChartData = [
  { name: 'Nguyễn Văn An', calls: 48, csat: 4.8 },
  { name: 'Trần Thị Bình', calls: 42, csat: 4.5 },
  { name: 'Lê Hoàng Cường', calls: 55, csat: 4.9 },
  { name: 'Phạm Minh Đức', calls: 38, csat: 4.1 },
  { name: 'Hoàng Thị Hoa', calls: 61, csat: 4.3 },
  { name: 'Vũ Quốc Huy', calls: 29, csat: 3.8 },
];

const statusConfig: Record<string, { color: string; label: string }> = {
  online: { color: '#52c41a', label: 'Online' },
  busy: { color: '#faad14', label: 'Busy' },
  away: { color: '#722ed1', label: 'Away' },
  offline: { color: '#d9d9d9', label: 'Offline' },
};

const activityColors: Record<string, string> = {
  call_start: '#1677ff',
  call_end: '#52c41a',
  login: '#722ed1',
  logout: '#d9d9d9',
  ticket: '#faad14',
  transfer: '#ff4d4f',
};

const activityIcons: Record<string, React.ReactNode> = {
  call_start: <PhoneOutlined />,
  call_end: <CheckCircleOutlined />,
  login: <RiseOutlined />,
  logout: <MinusCircleOutlined />,
  ticket: <FileTextOutlined />,
  transfer: <UserSwitchOutlined />,
};

export default function SupervisorPage() {
  const [sortKey, setSortKey] = useState<string>('name');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');

  const handleSort = (key: string) => {
    if (sortKey === key) {
      setSortDir(d => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortDir('asc');
    }
  };

  const sortedAgents = [...agentsData].sort((a, b) => {
    const aVal = (a as any)[sortKey];
    const bVal = (b as any)[sortKey];
    if (typeof aVal === 'number' && typeof bVal === 'number') {
      return sortDir === 'asc' ? aVal - bVal : bVal - aVal;
    }
    return sortDir === 'asc' ? String(aVal).localeCompare(String(bVal)) : String(bVal).localeCompare(String(aVal));
  });

  const agentColumns = [
    {
      title: (
        <span onClick={() => handleSort('name')} style={{ cursor: 'pointer' }}>
          Tên Agent {sortKey === 'name' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
        </span>
      ),
      dataIndex: 'name', key: 'name',
      render: (name: string) => <span style={{ fontWeight: 500 }}>{name}</span>,
    },
    {
      title: 'Trạng thái', dataIndex: 'status', key: 'status',
      render: (status: string) => (
        <Tag color={statusConfig[status]?.color} style={{ borderRadius: 12, paddingInline: 12 }}>
          {statusConfig[status]?.label}
        </Tag>
      ),
    },
    {
      title: (
        <span onClick={() => handleSort('activeCalls')} style={{ cursor: 'pointer' }}>
          Cuộc gọi {sortKey === 'activeCalls' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
        </span>
      ),
      dataIndex: 'activeCalls', key: 'activeCalls',
      render: (val: number) => <Badge count={val} showZero color={val > 0 ? '#1677ff' : '#d9d9d9'} style={{ fontSize: 11 }} />,
    },
    {
      title: (
        <span onClick={() => handleSort('totalCalls')} style={{ cursor: 'pointer' }}>
          Tổng hôm nay {sortKey === 'totalCalls' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
        </span>
      ),
      dataIndex: 'totalCalls', key: 'totalCalls',
    },
    {
      title: (
        <span onClick={() => handleSort('avgHandleTime')} style={{ cursor: 'pointer' }}>
          AHT (s) {sortKey === 'avgHandleTime' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
        </span>
      ),
      dataIndex: 'avgHandleTime', key: 'avgHandleTime', align: 'right' as const,
      render: (val: number) => <span>{val}s</span>,
    },
    {
      title: (
        <span onClick={() => handleSort('avgWaitTime')} style={{ cursor: 'pointer' }}>
          Chờ (s) {sortKey === 'avgWaitTime' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
        </span>
      ),
      dataIndex: 'avgWaitTime', key: 'avgWaitTime', align: 'right' as const,
      render: (val: number) => <span>{val}s</span>,
    },
    {
      title: (
        <span onClick={() => handleSort('csat')} style={{ cursor: 'pointer' }}>
          CSAT {sortKey === 'csat' ? (sortDir === 'asc' ? '▲' : '▼') : ''}
        </span>
      ),
      dataIndex: 'csat', key: 'csat',
      render: (val: number) => <Tag color={val >= 4.5 ? 'green' : val >= 4.0 ? 'orange' : 'red'}>{val.toFixed(1)}</Tag>,
    },
    {
      title: 'Thời gian', dataIndex: 'statusDuration', key: 'statusDuration',
    },
  ];

  const queueColumns = [
    { title: 'Hàng đợi', dataIndex: 'name', key: 'name', render: (n: string) => <span style={{ fontWeight: 500 }}>{n}</span> },
    {
      title: 'Chờ', dataIndex: 'callsWaiting', key: 'callsWaiting',
      render: (v: number) => <Tag color={v > 3 ? 'red' : v > 0 ? 'orange' : 'green'}>{v}</Tag>,
    },
    { title: 'Chờ lâu nhất (s)', dataIndex: 'longestWait', key: 'longestWait', align: 'right' as const, render: (v: number) => v > 0 ? <span style={{ color: v > 60 ? '#ff4d4f' : '#52c41a' }}>{v}s</span> : '-' },
    { title: 'Agent đã phân', dataIndex: 'agentsAssigned', key: 'agentsAssigned' },
    { title: 'Agent rảnh', dataIndex: 'agentsAvailable', key: 'agentsAvailable', render: (v: number) => <Badge count={v} showZero color={v > 0 ? '#52c41a' : '#ff4d4f'} style={{ fontSize: 11 }} /> },
    {
      title: 'SLA %', dataIndex: 'slaHit', key: 'slaHit', align: 'right' as const,
      render: (v: number) => <Progress percent={v} size="small" format={() => `${v}%`} strokeColor={v >= 90 ? '#52c41a' : v >= 80 ? '#faad14' : '#ff4d4f'} style={{ marginBottom: 0 }} />,
    },
  ];

  const maxCalls = Math.max(...callVolumeChart.map(d => d.calls));

  const agentMaxCalls = Math.max(...agentChartData.map(d => d.calls));

  return (
    <div>
      <Title level={3} style={{ marginBottom: 16 }}>Supervisor Dashboard</Title>

      <Row gutter={[12, 12]}>
        <Col xs={12} sm={8} lg={3}>
          <Card size="small" hoverable>
            <Statistic title="Cuộc gọi đang hoạt động" value={kpiData.activeCalls} prefix={<PhoneOutlined style={{ color: '#1677ff' }} />} valueStyle={{ fontSize: 22, color: '#1677ff' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={3}>
          <Card size="small" hoverable>
            <Statistic title="Agent Online/Tổng" value={`${kpiData.agentsOnline}/${kpiData.agentsTotal}`} prefix={<TeamOutlined style={{ color: '#52c41a' }} />} valueStyle={{ fontSize: 22, color: '#52c41a' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={3}>
          <Card size="small" hoverable>
            <Statistic title="Đang chờ" value={kpiData.callsWaiting} prefix={<ClockCircleOutlined style={{ color: '#faad14' }} />} valueStyle={{ fontSize: 22, color: kpiData.callsWaiting > 5 ? '#ff4d4f' : '#faad14' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={3}>
          <Card size="small" hoverable>
            <Statistic title="Thời gian chờ TB" value={kpiData.avgWaitTime} suffix="s" prefix={<ClockCircleOutlined style={{ color: '#722ed1' }} />} valueStyle={{ fontSize: 22, color: '#722ed1' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={4}>
          <Card size="small" hoverable>
            <Statistic title="SLA Tuân thủ" value={kpiData.slaCompliance} precision={1} suffix="%" prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />} valueStyle={{ fontSize: 22, color: '#52c41a' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={3}>
          <Card size="small" hoverable>
            <Statistic title="Tỉ lệ bỏ cuộc" value={kpiData.abandonRate} suffix="%" prefix={<CloseCircleOutlined style={{ color: '#ff4d4f' }} />} valueStyle={{ fontSize: 22, color: '#ff4d4f' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={3}>
          <Card size="small" hoverable>
            <Statistic title="Service Level" value={kpiData.serviceLevel} prefix={<RiseOutlined style={{ color: '#1677ff' }} />} valueStyle={{ fontSize: 22, color: '#1677ff' }} />
          </Card>
        </Col>
        <Col xs={12} sm={8} lg={2}>
          <Card size="small" hoverable>
            <Statistic title="CSAT" value={kpiData.csat} prefix={<StarOutlined style={{ color: '#faad14' }} />} valueStyle={{ fontSize: 22, color: '#faad14' }} precision={1} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 20 }}>
        <Col xs={24} lg={16}>
          <Card title={<Space><TeamOutlined />Hiệu suất Agent</Space>} style={{ marginBottom: 16 }}>
            <Table
              dataSource={sortedAgents}
              columns={agentColumns}
              rowKey="id"
              pagination={false}
              size="small"
              locale={{ emptyText: 'Không có dữ liệu agent' }}
            />
          </Card>
          <Card title={<Space><ClockCircleOutlined />Hàng đợi</Space>}>
            <Table
              dataSource={queueData}
              columns={queueColumns}
              rowKey="id"
              pagination={false}
              size="small"
              locale={{ emptyText: 'Không có dữ liệu hàng đợi' }}
            />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title={<Space><RiseOutlined />Hoạt động gần đây</Space>} style={{ height: '100%' }}>
            <List
              dataSource={activityData}
              renderItem={item => (
                <List.Item style={{ padding: '8px 0', borderBottom: '1px solid #f0f0f0' }}>
                  <List.Item.Meta
                    avatar={
                      <div style={{
                        width: 32, height: 32, borderRadius: '50%',
                        background: activityColors[item.type] || '#d9d9d9',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: 14
                      }}>
                        {activityIcons[item.type]}
                      </div>
                    }
                    title={
                      <Space size={4}>
                        <Tag color={activityColors[item.type]} style={{ fontSize: 11, lineHeight: '18px', padding: '0 6px' }}>{item.event}</Tag>
                        <Text style={{ fontSize: 12, color: '#999' }}>{item.time}</Text>
                      </Space>
                    }
                    description={<Text style={{ fontSize: 12 }}>{item.detail}</Text>}
                  />
                </List.Item>
              )}
              locale={{ emptyText: 'Không có hoạt động' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title={<Space><RiseOutlined />Xu hướng cuộc gọi (7 ngày)</Space>}>
            <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-around', padding: '16px 0', height: 180 }}>
              {callVolumeChart.map(d => (
                <div key={d.day} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', flex: 1 }}>
                  <div style={{
                    width: 28, height: `${(d.calls / maxCalls) * 130}px`, background: 'linear-gradient(to top, #1677ff, #69b1ff)',
                    borderRadius: '6px 6px 2px 2px', transition: 'height 0.4s', minHeight: 4
                  }} />
                  <Text type="secondary" style={{ fontSize: 10, marginTop: 6 }}>{d.day}</Text>
                </div>
              ))}
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={<Space><TeamOutlined />So sánh hiệu suất Agent</Space>}>
            <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-around', padding: '16px 0', height: 180 }}>
              {agentChartData.map(d => (
                <div key={d.name} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', flex: 1 }}>
                  <Tooltip title={`${d.name}: ${d.calls} cuộc gọi, CSAT ${d.csat}`}>
                    <div style={{
                      width: 24, height: `${(d.calls / agentMaxCalls) * 130}px`,
                      background: `rgba(82, 196, 26, ${0.4 + (d.csat / 10)})`,
                      borderRadius: '6px 6px 2px 2px', transition: 'height 0.4s', minHeight: 4,
                      border: '1px solid rgba(82,196,26,0.6)',
                    }} />
                  </Tooltip>
                  <Text type="secondary" style={{ fontSize: 9, marginTop: 6, textAlign: 'center', lineHeight: 1.2 }}>{d.name.split(' ').pop()}</Text>
                </div>
              ))}
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
