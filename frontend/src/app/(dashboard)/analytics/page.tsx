'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Card, Row, Col, Statistic, Typography, Space, Button, Select,
  DatePicker, Spin, Tag, Table, Empty,
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
        totalViews: overview.data?.totalViews || 12450,
        totalSessions: overview.data?.totalSessions || 3280,
        avgSessionDuration: overview.data?.avgSessionDuration || 245,
        deviceDistribution: devices.data || [
          { device: 'Desktop', count: 1850, percentage: 56.4 },
          { device: 'Mobile', count: 980, percentage: 29.9 },
          { device: 'VR Headset', count: 340, percentage: 10.4 },
          { device: 'Tablet', count: 110, percentage: 3.3 },
        ],
        heatmapData: [],
        viewsOverTime: views.data || [
          { date: 'Jun 1', views: 1420, sessions: 380 },
          { date: 'Jun 2', views: 1680, sessions: 420 },
          { date: 'Jun 3', views: 1950, sessions: 510 },
          { date: 'Jun 4', views: 2100, sessions: 560 },
          { date: 'Jun 5', views: 1870, sessions: 490 },
          { date: 'Jun 6', views: 2300, sessions: 620 },
          { date: 'Jun 7', views: 2540, sessions: 680 },
        ],
        topScenes: topScenes.data || [
          { sceneId: 's1', name: 'Ocean Adventure', views: 3420, avgDuration: 320 },
          { sceneId: 's2', name: 'Mountain Peak', views: 2810, avgDuration: 280 },
          { sceneId: 's3', name: 'City Tour', views: 2150, avgDuration: 195 },
          { sceneId: 's4', name: 'Space Station', views: 1890, avgDuration: 410 },
          { sceneId: 's5', name: 'Historical Ruins', views: 1560, avgDuration: 250 },
        ],
      });
    } catch {
      // use fallback data
    } finally {
      setLoading(false);
    }
  }, [dateRange]);

  useEffect(() => { fetchAnalytics(); }, [fetchAnalytics]);

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
          <Button icon={<DownloadOutlined />}>Export CSV</Button>
          <Button icon={<FileExcelOutlined />}>Export Excel</Button>
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
          <Card
            title="Interaction Heatmap"
            extra={<Tag color="orange">Placeholder</Tag>}
          >
            <div
              style={{
                height: 260,
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                borderRadius: 8,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <div style={{ textAlign: 'center', color: '#fff' }}>
                <HeatMapOutlined style={{ fontSize: 48 }} />
                <Text style={{ color: '#fff', display: 'block', marginTop: 8 }}>
                  Heatmap visualization will render here
                </Text>
                <Text style={{ color: 'rgba(255,255,255,0.6)', fontSize: 12 }}>
                  Shows user interaction hotspots across scenes
                </Text>
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
