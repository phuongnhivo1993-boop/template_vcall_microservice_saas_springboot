'use client';

import { useState, useEffect, useCallback } from 'react';
import { Row, Col, Card, Statistic, Typography, Tag, Spin, Alert, message } from 'antd';
import {
  PhoneOutlined,
  TeamOutlined,
  FileTextOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Area,
  AreaChart,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import CommonTable from '@/components/common/CommonTable';
import { dashboardApi, agentsApi, ticketsApi } from '@/lib/api';

const { Title } = Typography;

const statusColors: Record<string, string> = {
  completed: '#52c41a',
  ongoing: '#1677ff',
  missed: '#ff4d4f',
  failed: '#faad14',
};

const PIE_COLORS = ['#1677ff', '#52c41a', '#faad14', '#ff4d4f', '#722ed1', '#999'];

export default function DashboardPage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [stats, setStats] = useState<any>({});
  const [recentCalls, setRecentCalls] = useState<any[]>([]);
  const [agentStatusData, setAgentStatusData] = useState<any[]>([]);
  const [ticketStatusData, setTicketStatusData] = useState<any[]>([]);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [statsRes, callsRes, agentStatusRes, ticketStatusRes] = await Promise.all([
        dashboardApi.getStats().catch(() => ({ data: {} })),
        dashboardApi.getRecentCalls({ page: 0, size: 10 }).catch(() => ({ data: { content: [] } })),
        agentsApi.getStats().catch(() => ({ data: {} })),
        ticketsApi.getStats().catch(() => ({ data: [] })),
      ]);
      setStats(statsRes.data?.data || statsRes.data || {});
      setRecentCalls(callsRes.data?.data?.content || callsRes.data?.content || []);
      setAgentStatusData(agentStatusRes.data?.data || agentStatusRes.data || []);
      setTicketStatusData(ticketStatusRes.data?.data || ticketStatusRes.data || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const callVolumeData = stats.callVolume || [];
  const agentPerformanceData = stats.agentPerformance || [];

  const handleExportCsv = async () => {
    try {
      const res = await dashboardApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `dashboard_export.csv`;
      a.click();
      URL.revokeObjectURL(url);
    } catch { message.error('Failed to export CSV'); }
  };

  const handleExportExcel = async () => {
    try {
      const res = await dashboardApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `dashboard_export.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
    } catch { message.error('Failed to export Excel'); }
  };

  const recentCallColumns = [
    { title: 'Call ID', dataIndex: 'id', key: 'id', render: (id: string) => <strong>{id}</strong> },
    { title: 'Caller', dataIndex: 'caller', key: 'caller' },
    { title: 'Callee', dataIndex: 'callee', key: 'callee' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (s: string) => <Tag color={statusColors[s] || 'default'}>{s?.toUpperCase()}</Tag>,
    },
    {
      title: 'Duration',
      dataIndex: 'duration',
      key: 'duration',
      render: (secs: number) => {
        if (!secs) return '-';
        const mins = Math.floor(secs / 60);
        const s = secs % 60;
        return `${mins}:${s.toString().padStart(2, '0')}`;
      },
    },
    { title: 'Agent', dataIndex: 'agent', key: 'agent' },
    { title: 'Time', dataIndex: 'time', key: 'time', render: (t: string) => t || '-' },
  ];

  const totalCalls = stats.totalCallsToday ?? 0;
  const activeAgents = stats.activeAgents ?? 0;
  const openTickets = stats.openTickets ?? 0;
  const todayRevenue = stats.todayRevenue ?? 0;

  if (error) {
    return (
      <div>
        <Title level={3} style={{ marginBottom: 24 }}>Dashboard</Title>
        <Alert
          message="Error loading dashboard"
          description={error}
          type="error"
          showIcon
          action={<a onClick={fetchData}>Retry</a>}
        />
      </div>
    );
  }

  return (
    <div>
      <Title level={3} style={{ marginBottom: 24 }}>Dashboard</Title>

      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card" hoverable>
              <Statistic
                title="Total Calls Today"
                value={totalCalls}
                prefix={<PhoneOutlined />}
                valueStyle={{ color: '#1677ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card" hoverable>
              <Statistic
                title="Active Agents"
                value={activeAgents}
                prefix={<TeamOutlined />}
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card" hoverable>
              <Statistic
                title="Open Tickets"
                value={openTickets}
                prefix={<FileTextOutlined />}
                valueStyle={{ color: '#faad14' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card" hoverable>
              <Statistic
                title="Today's Revenue"
                value={todayRevenue}
                precision={2}
                prefix={<DollarOutlined />}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
        </Row>

        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col xs={24} lg={14}>
            <Card title="Call Volume Today" className="chart-container">
              {callVolumeData.length === 0 ? (
                <div style={{ height: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#999' }}>
                  No call volume data available
                </div>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <AreaChart data={callVolumeData}>
                    <defs>
                      <linearGradient id="colorCalls" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#1677ff" stopOpacity={0.3} />
                        <stop offset="95%" stopColor="#1677ff" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="time" stroke="#999" />
                    <YAxis stroke="#999" />
                    <Tooltip />
                    <Area type="monotone" dataKey="calls" stroke="#1677ff" fill="url(#colorCalls)" strokeWidth={2} />
                  </AreaChart>
                </ResponsiveContainer>
              )}
            </Card>
          </Col>
          <Col xs={24} lg={10}>
            <Card title="Agent Performance" className="chart-container">
              {agentPerformanceData.length === 0 ? (
                <div style={{ height: 300, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#999' }}>
                  No agent performance data available
                </div>
              ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={agentPerformanceData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="name" stroke="#999" />
                    <YAxis stroke="#999" />
                    <Tooltip />
                    <Bar dataKey="calls" fill="#1677ff" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </Card>
          </Col>
        </Row>

        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col xs={24} lg={12}>
            <Card title="Agent Status Distribution">
              {agentStatusData.length === 0 ? (
                <div style={{ height: 280, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#999' }}>
                  No agent status data available
                </div>
              ) : (
                <ResponsiveContainer width="100%" height={280}>
                  <PieChart>
                    <Pie
                      data={agentStatusData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={100}
                      dataKey="value"
                      label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    >
                      {agentStatusData.map((_: any, index: number) => (
                        <Cell key={index} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="Ticket Status Breakdown">
              {ticketStatusData.length === 0 ? (
                <div style={{ height: 280, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#999' }}>
                  No ticket status data available
                </div>
              ) : (
                <ResponsiveContainer width="100%" height={280}>
                  <PieChart>
                    <Pie
                      data={ticketStatusData}
                      cx="50%"
                      cy="50%"
                      innerRadius={60}
                      outerRadius={100}
                      dataKey="value"
                      label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    >
                      {ticketStatusData.map((_: any, index: number) => (
                        <Cell key={index} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </Card>
          </Col>
        </Row>

        <div style={{ marginTop: 24 }}>
          <CommonTable
            title="Recent Calls"
            columns={recentCallColumns}
            dataSource={recentCalls}
            loading={loading}
            rowKey="id"
            pagination={{ pageSize: 5, showSizeChanger: false }}
            onRefresh={fetchData}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
          />
        </div>
      </Spin>
    </div>
  );
}
