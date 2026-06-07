'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Statistic, Table, Tag, Badge, Typography, Row, Col, Space, Progress, List, Tooltip, Alert, Button } from 'antd';
import {
  PhoneOutlined, TeamOutlined, ClockCircleOutlined, CheckCircleOutlined,
  CloseCircleOutlined, StarOutlined, RiseOutlined, UserSwitchOutlined,
  ArrowUpOutlined, ArrowDownOutlined, MinusCircleOutlined, FileTextOutlined,
  ReloadOutlined
} from '@ant-design/icons';
import { callsApi, agentsApi, dashboardApi } from '@/lib/api';
import LoadingSkeleton from '@/components/common/LoadingSkeleton';
import PageHeader from '@/components/common/PageHeader';
import dayjs from 'dayjs';

const { Text } = Typography;

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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [kpiData, setKpiData] = useState({
    activeCalls: 0,
    agentsOnline: 0,
    agentsTotal: 0,
    callsWaiting: 0,
    avgWaitTime: 0,
    slaCompliance: 0,
    abandonRate: 0,
    serviceLevel: '0',
    csat: 0,
  });
  const [agentsData, setAgentsData] = useState<Agent[]>([]);
  const [queueData, setQueueData] = useState<Queue[]>([]);
  const [activityData, setActivityData] = useState<Activity[]>([]);
  const [stats, setStats] = useState<any>({});

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsRes, agentsRes, queuesRes] = await Promise.all([
        dashboardApi.getStats(),
        agentsApi.list({ page: 0, size: 50 }),
        callsApi.getQueues(),
      ]);

      const stats = statsRes.data?.data || statsRes.data || {};
      setStats(stats);
      setKpiData({
        activeCalls: stats.activeCalls ?? stats.activeCallsCount ?? 0,
        agentsOnline: stats.agentsOnline ?? 0,
        agentsTotal: stats.agentsTotal ?? 0,
        callsWaiting: stats.callsWaiting ?? 0,
        avgWaitTime: stats.avgWaitTime ?? 0,
        slaCompliance: stats.slaCompliance ?? 0,
        abandonRate: stats.abandonRate ?? 0,
        serviceLevel: stats.serviceLevel ?? '0',
        csat: stats.csat ?? 0,
      });

      const agents = agentsRes.data?.data?.content || agentsRes.data?.data || agentsRes.data || [];
      setAgentsData(Array.isArray(agents) ? agents.map((a: any) => ({
        id: a.id,
        name: a.name || a.fullName || '',
        status: a.status || a.agentStatus || 'offline',
        activeCalls: a.activeCalls ?? 0,
        totalCalls: a.totalCalls ?? 0,
        avgHandleTime: a.avgHandleTime ?? 0,
        avgWaitTime: a.avgWaitTime ?? 0,
        csat: a.csat ?? 0,
        statusDuration: a.statusDuration || '-',
      })) : []);

      const queues = queuesRes.data?.data?.content || queuesRes.data?.data || queuesRes.data || [];
      setQueueData(Array.isArray(queues) ? queues.map((q: any) => ({
        id: q.id,
        name: q.name,
        callsWaiting: q.callsWaiting ?? 0,
        longestWait: q.longestWait ?? 0,
        agentsAssigned: q.agentsAssigned ?? 0,
        agentsAvailable: q.agentsAvailable ?? 0,
        slaHit: q.slaHit ?? 0,
      })) : []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load supervisor data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

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

  const handleExportCsv = async () => {
    try {
      const res = await dashboardApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `supervisor_report_${dayjs().format('YYYYMMDD_HHmmss')}.csv`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      // silent
    }
  };

  const handleExportExcel = async () => {
    try {
      const res = await dashboardApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `supervisor_report_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      // silent
    }
  };

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

  const callVolumeChart = stats.callVolume || [
    { day: 'T2', calls: 320 }, { day: 'T3', calls: 285 }, { day: 'T4', calls: 356 },
    { day: 'T5', calls: 298 }, { day: 'T6', calls: 412 }, { day: 'T7', calls: 278 },
    { day: 'CN', calls: 195 },
  ];

  const maxCalls = Math.max(...callVolumeChart.map((d: any) => d.calls));

  const agentChartData = agentsData.slice(0, 6).map(a => ({
    name: a.name,
    calls: a.totalCalls,
    csat: a.csat,
  }));

  const agentMaxCalls = Math.max(...agentChartData.map(d => d.calls), 1);

  if (loading) {
    return (
      <div>
        <PageHeader title="Giám sát" subtitle="Dashboard giám sát thời gian thực" onExportCsv={handleExportCsv} />
        <LoadingSkeleton type="stats" rows={5} />
      </div>
    );
  }

  if (error) {
    return (
      <div>
        <PageHeader title="Giám sát" subtitle="Dashboard giám sát thời gian thực" onExportCsv={handleExportCsv} />
        <Alert type="error" message={error} showIcon action={<Button onClick={fetchData} icon={<ReloadOutlined />}>Retry</Button>} />
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title="Giám sát"
        subtitle="Dashboard giám sát thời gian thực"
        onExportCsv={handleExportCsv}
        extra={<Button icon={<ReloadOutlined />} onClick={fetchData}>Làm mới</Button>}
      />

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
              locale={{ emptyText: 'Không có hoạt động' }}
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
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title={<Space><RiseOutlined />Xu hướng cuộc gọi (7 ngày)</Space>}>
            <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-around', padding: '16px 0', height: 180 }}>
              {callVolumeChart.map((d: { day: string; calls: number }) => (
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
