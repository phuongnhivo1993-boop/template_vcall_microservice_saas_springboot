'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Card, Row, Col, Statistic, Typography, Space, Button, Select,
  DatePicker, Spin, Tag, Table, Empty, message,
} from 'antd';
import {
  EyeOutlined, TeamOutlined, ClockCircleOutlined, BarChartOutlined,
  ReloadOutlined, DownloadOutlined, FileExcelOutlined, DesktopOutlined,
  MobileOutlined, TabletOutlined, HeatMapOutlined,
} from '@ant-design/icons';
import {
  AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts';
import { xrAnalyticsApi, type XRAnalytics } from '@/lib/api/xr-api';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const COLORS = ['#1677ff', '#52c41a', '#faad14', '#ff4d4f', '#722ed1', '#13c2c2'];

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<XRAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [dateRange, setDateRange] = useState<string>('7d');

  const fetchAnalytics = useCallback(async () => {
    setLoading(true);
    try {
      const params = { period: dateRange };
      const [overview, views, devices, sessions, topScenes] = await Promise.all([
        xrAnalyticsApi.getOverview(params).catch(() => ({ data: {} })),
        xrAnalyticsApi.getViewsOverTime(params).catch(() => ({ data: [] })),
        xrAnalyticsApi.getDeviceDistribution(params).catch(() => ({ data: [] })),
        xrAnalyticsApi.getSessionDuration(params).catch(() => ({ data: [] })),
        xrAnalyticsApi.getTopScenes(params).catch(() => ({ data: [] })),
      ]);

      setAnalytics({
        totalViews: overview.data?.totalViews || 0,
        totalSessions: overview.data?.totalSessions || 0,
        avgSessionDuration: overview.data?.avgSessionDuration || 0,
        deviceDistribution: devices.data || [],
        heatmapData: [],
        viewsOverTime: views.data || [],
        topScenes: topScenes.data || [],
      });
    } catch {
      // use fallback data
    } finally {
      setLoading(false);
    }
  }, [dateRange]);

  useEffect(() => { fetchAnalytics(); }, [fetchAnalytics]);

  const handleExportCsv = async () => {
    try {
      const res = await xrAnalyticsApi.exportCsv({ period: dateRange });
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `analytics_${dateRange}_${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Analytics exported as CSV');
    } catch {
      message.error('Export failed');
    }
  };

  const handleExportExcel = async () => {
    try {
      const res = await xrAnalyticsApi.exportExcel({ period: dateRange });
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `analytics_${dateRange}_${new Date().toISOString().slice(0, 10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Analytics exported as Excel');
    } catch {
      message.error('Export failed');
    }
  };

  const formatDuration = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}m ${secs}s`;
  };

  const topSceneColumns = [
    {
      title: 'Rank',
      key: 'rank',
      render: (_: any, __: any, index: number) => (
        <Tag color={index < 3 ? 'gold' : 'default'}>{index + 1}</Tag>
      ),
    },
    { title: 'Scene', dataIndex: 'name', key: 'name' },
    {
      title: 'Views',
      dataIndex: 'views',
      key: 'views',
      render: (v: number) => v.toLocaleString(),
    },
    {
      title: 'Avg Duration',
      dataIndex: 'avgDuration',
      key: 'avgDuration',
      render: (d: number) => formatDuration(d),
    },
  ];

  const deviceIconMap: Record<string, React.ReactNode> = {
    Desktop: <DesktopOutlined />,
    Mobile: <MobileOutlined />,
    'VR Headset': <EyeOutlined />,
    Tablet: <TabletOutlined />,
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Spin size="large" tip="Loading analytics..." />
      </div>
    );
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>XR Analytics</Title>
        <Space>
          <Select
            value={dateRange}
            onChange={setDateRange}
            style={{ width: 120 }}
            options={[
              { value: '24h', label: 'Last 24h' },
              { value: '7d', label: 'Last 7 Days' },
              { value: '30d', label: 'Last 30 Days' },
              { value: '90d', label: 'Last 90 Days' },
            ]}
          />
          <Button icon={<ReloadOutlined />} onClick={fetchAnalytics}>Refresh</Button>
          <Button icon={<DownloadOutlined />} onClick={handleExportCsv}>Export CSV</Button>
          <Button icon={<FileExcelOutlined />} onClick={handleExportExcel}>Export Excel</Button>
        </Space>
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Total Views"
              value={analytics?.totalViews || 0}
              prefix={<EyeOutlined />}
              valueStyle={{ color: '#1677ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Total Sessions"
              value={analytics?.totalSessions || 0}
              prefix={<TeamOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title="Avg Session Duration"
              value={formatDuration(analytics?.avgSessionDuration || 0)}
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} lg={16}>
          <Card title="Views & Sessions Over Time">
            <ResponsiveContainer width="100%" height={320}>
              <AreaChart data={analytics?.viewsOverTime || []}>
                <defs>
                  <linearGradient id="colorViews" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#1677ff" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#1677ff" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="colorSessions" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#52c41a" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#52c41a" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" stroke="#999" />
                <YAxis stroke="#999" />
                <Tooltip />
                <Legend />
                <Area type="monotone" dataKey="views" stroke="#1677ff" fill="url(#colorViews)" strokeWidth={2} />
                <Area type="monotone" dataKey="sessions" stroke="#52c41a" fill="url(#colorSessions)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="Device Distribution">
            <ResponsiveContainer width="100%" height={320}>
              <PieChart>
                <Pie
                  data={analytics?.deviceDistribution || []}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={100}
                  dataKey="count"
                  nameKey="device"
                  label={({ device, percentage }) => `${device} ${percentage}%`}
                >
                  {(analytics?.deviceDistribution || []).map((_, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} lg={12}>
          <Card title="Device Breakdown">
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {(analytics?.deviceDistribution || []).map((d) => (
                <div key={d.device} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                  <span style={{ fontSize: 18, width: 24, textAlign: 'center' }}>
                    {deviceIconMap[d.device] || <DesktopOutlined />}
                  </span>
                  <Text style={{ flex: 1 }}>{d.device}</Text>
                  <Text strong>{d.count.toLocaleString()}</Text>
                  <div
                    style={{
                      width: 100,
                      height: 6,
                      background: '#f0f0f0',
                      borderRadius: 3,
                      overflow: 'hidden',
                    }}
                  >
                    <div
                      style={{
                        width: `${d.percentage}%`,
                        height: '100%',
                        background: '#1677ff',
                        borderRadius: 3,
                      }}
                    />
                  </div>
                  <Text type="secondary" style={{ width: 40, textAlign: 'right' }}>{d.percentage}%</Text>
                </div>
              ))}
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Interaction Heatmap">
            <div
              style={{
                height: 260,
                background: 'linear-gradient(135deg, #ff4d4f 0%, #faad14 50%, #1677ff 100%)',
                borderRadius: 8,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                position: 'relative',
                overflow: 'hidden',
              }}
            >
              <div style={{
                position: 'absolute', inset: 0,
                background: 'radial-gradient(circle at 20% 40%, rgba(255,255,255,0.15) 0%, transparent 50%), radial-gradient(circle at 80% 60%, rgba(255,255,255,0.1) 0%, transparent 40%)',
              }} />
              <div style={{ textAlign: 'center', color: '#fff', position: 'relative', zIndex: 1 }}>
                <HeatMapOutlined style={{ fontSize: 48, opacity: 0.9 }} />
                <Text style={{ color: '#fff', display: 'block', marginTop: 12, fontSize: 18, fontWeight: 600 }}>
                  Heatmap Analysis
                </Text>
                <Text style={{ color: 'rgba(255,255,255,0.75)', display: 'block', marginTop: 8, fontSize: 13 }}>
                  Premium feature — Upgrade to Enterprise plan to access heatmap analytics
                </Text>
                <Button
                  type="primary"
                  ghost
                  style={{ marginTop: 16, borderColor: '#fff', color: '#fff' }}
                >
                  Upgrade Now
                </Button>
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      <Card title="Top Performing Scenes">
        <Table
          columns={topSceneColumns}
          dataSource={analytics?.topScenes || []}
          rowKey="sceneId"
          pagination={false}
        />
      </Card>
    </div>
  );
}
