'use client';

import { Row, Col, Card, Statistic, Typography, List, Tag, Table, Tabs } from 'antd';
import {
  PhoneOutlined,
  TeamOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
} from '@ant-design/icons';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Area,
  AreaChart,
} from 'recharts';

const { Title } = Typography;

const callVolumeData = [
  { time: '00:00', calls: 12 },
  { time: '04:00', calls: 8 },
  { time: '08:00', calls: 45 },
  { time: '10:00', calls: 78 },
  { time: '12:00', calls: 65 },
  { time: '14:00', calls: 88 },
  { time: '16:00', calls: 92 },
  { time: '18:00', calls: 74 },
  { time: '20:00', calls: 42 },
  { time: '22:00', calls: 20 },
];

const agentPerformanceData = [
  { name: 'Sarah J.', calls: 48, avgDuration: 4.2, satisfaction: 95 },
  { name: 'Mike R.', calls: 42, avgDuration: 5.1, satisfaction: 88 },
  { name: 'Emily W.', calls: 38, avgDuration: 3.8, satisfaction: 92 },
  { name: 'John D.', calls: 35, avgDuration: 6.2, satisfaction: 85 },
  { name: 'Lisa M.', calls: 32, avgDuration: 4.5, satisfaction: 90 },
];

const recentActivity = [
  { id: '1', action: 'Call completed', agent: 'Sarah J.', customer: 'John Smith', time: '2 min ago', status: 'success' },
  { id: '2', action: 'Ticket created', agent: 'Mike R.', customer: 'Alice Brown', time: '5 min ago', status: 'warning' },
  { id: '3', action: 'Call transferred', agent: 'Emily W.', customer: 'Bob Wilson', time: '8 min ago', status: 'info' },
  { id: '4', action: 'Customer added', agent: 'Lisa M.', customer: 'Carol Davis', time: '12 min ago', status: 'default' },
  { id: '5', action: 'Call missed', agent: '-', customer: 'Tom Harris', time: '15 min ago', status: 'error' },
];

const columns = [
  { title: 'Action', dataIndex: 'action', key: 'action' },
  { title: 'Agent', dataIndex: 'agent', key: 'agent' },
  { title: 'Customer', dataIndex: 'customer', key: 'customer' },
  {
    title: 'Time',
    dataIndex: 'time',
    key: 'time',
    render: (text: string) => <span style={{ color: '#999' }}>{text}</span>,
  },
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    render: (status: string) => {
      const colors: Record<string, string> = {
        success: '#52c41a',
        warning: '#faad14',
        info: '#1677ff',
        error: '#ff4d4f',
        default: '#999',
      };
      return <Tag color={colors[status] || colors.default}>{status}</Tag>;
    },
  },
];

export default function DashboardPage() {
  return (
    <div>
      <Title level={3} style={{ marginBottom: 24 }}>Dashboard</Title>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stat-card" hoverable>
            <Statistic
              title="Total Calls Today"
              value={486}
              prefix={<PhoneOutlined />}
              suffix={<small style={{ color: '#52c41a', fontSize: 14 }}><ArrowUpOutlined /> 12%</small>}
              valueStyle={{ color: '#1677ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stat-card" hoverable>
            <Statistic
              title="Active Agents"
              value={24}
              prefix={<TeamOutlined />}
              suffix={<small style={{ color: '#52c41a', fontSize: 14 }}><ArrowUpOutlined /> 3</small>}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stat-card" hoverable>
            <Statistic
              title="Open Tickets"
              value={38}
              prefix={<FileTextOutlined />}
              suffix={<small style={{ color: '#ff4d4f', fontSize: 14 }}><ArrowDownOutlined /> 5</small>}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card className="stat-card" hoverable>
            <Statistic
              title="SLA Compliance"
              value={98.5}
              precision={1}
              prefix={<CheckCircleOutlined />}
              suffix="%"
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} lg={14}>
          <Card title="Call Volume Today" className="chart-container">
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
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="Agent Performance" className="chart-container">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={agentPerformanceData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="name" stroke="#999" />
                <YAxis stroke="#999" />
                <Tooltip />
                <Bar dataKey="calls" fill="#1677ff" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Card title="Recent Activity" style={{ marginTop: 24 }}>
        <Table
          dataSource={recentActivity}
          columns={columns}
          pagination={false}
          rowKey="id"
          size="middle"
        />
      </Card>
    </div>
  );
}
