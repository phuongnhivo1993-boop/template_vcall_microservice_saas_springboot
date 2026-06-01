'use client';

import { useState } from 'react';
import { Card, Select, DatePicker, Row, Col, Typography, Table, Tabs, Space } from 'antd';
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';

const { Title } = Typography;
const { RangePicker } = DatePicker;

const callVolumeData = [
  { month: 'Jan', inbound: 1200, outbound: 800 },
  { month: 'Feb', inbound: 1400, outbound: 900 },
  { month: 'Mar', inbound: 1100, outbound: 750 },
  { month: 'Apr', inbound: 1600, outbound: 1100 },
  { month: 'May', inbound: 1800, outbound: 1200 },
  { month: 'Jun', inbound: 1500, outbound: 950 },
];

const agentPerfData = [
  { name: 'Sarah J.', calls: 145, avgDuration: 4.2, resolution: 95 },
  { name: 'Mike R.', calls: 128, avgDuration: 5.1, resolution: 88 },
  { name: 'Emily W.', calls: 112, avgDuration: 3.8, resolution: 92 },
  { name: 'John D.', calls: 98, avgDuration: 6.2, resolution: 85 },
  { name: 'Lisa M.', calls: 56, avgDuration: 4.5, resolution: 90 },
];

const slaData = [
  { name: 'Within SLA', value: 92, color: '#52c41a' },
  { name: 'Breached SLA', value: 8, color: '#ff4d4f' },
];

const topCustomers = [
  { rank: 1, name: 'MediCorp Health', calls: 456, avgDuration: 5.2 },
  { rank: 2, name: 'CareFirst Clinic', calls: 389, avgDuration: 4.8 },
  { rank: 3, name: 'Wellness Plus', calls: 312, avgDuration: 6.1 },
  { rank: 4, name: 'HealthBridge', calls: 278, avgDuration: 3.9 },
  { rank: 5, name: 'PrimeCare Group', calls: 245, avgDuration: 5.5 },
];

export default function ReportsPage() {
  const [reportType, setReportType] = useState('callVolume');

  const tabItems = [
    {
      key: 'charts',
      label: 'Charts',
      children: (
        <Row gutter={[24, 24]}>
          <Col span={24}>
            <Card title="Call Volume (Inbound vs Outbound)">
              <ResponsiveContainer width="100%" height={350}>
                <BarChart data={callVolumeData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis dataKey="month" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="inbound" fill="#1677ff" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="outbound" fill="#722ed1" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </Col>
          <Col span={12}>
            <Card title="Agent Performance">
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={agentPerfData} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                  <XAxis type="number" />
                  <YAxis dataKey="name" type="category" />
                  <Tooltip />
                  <Bar dataKey="calls" fill="#1677ff" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </Col>
          <Col span={12}>
            <Card title="SLA Compliance">
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={slaData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    dataKey="value"
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  >
                    {slaData.map((entry) => (
                      <Cell key={entry.name} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </Card>
          </Col>
        </Row>
      ),
    },
    {
      key: 'tables',
      label: 'Tables',
      children: (
        <Card title="Top Customers by Call Volume">
          <Table
            dataSource={topCustomers}
            rowKey="rank"
            pagination={false}
            columns={[
              { title: 'Rank', dataIndex: 'rank', key: 'rank', width: 60 },
              { title: 'Customer', dataIndex: 'name', key: 'name' },
              { title: 'Total Calls', dataIndex: 'calls', key: 'calls' },
              { title: 'Avg Duration (min)', dataIndex: 'avgDuration', key: 'avgDuration' },
            ]}
          />
        </Card>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>Reports</Title>
        <Space>
          <RangePicker />
          <Select
            value={reportType}
            onChange={setReportType}
            style={{ width: 180 }}
            options={[
              { value: 'callVolume', label: 'Call Volume' },
              { value: 'agentPerf', label: 'Agent Performance' },
              { value: 'sla', label: 'SLA Compliance' },
              { value: 'customer', label: 'Customer Analytics' },
            ]}
          />
        </Space>
      </div>
      <Tabs items={tabItems} />
    </div>
  );
}
