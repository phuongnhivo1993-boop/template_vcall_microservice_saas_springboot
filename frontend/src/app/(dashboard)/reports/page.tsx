'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Select, DatePicker, Row, Col, Typography, Tabs, Space, message, Button, Modal, Form, Input, Radio, Tag } from 'antd';
import { ScheduleOutlined, ClockCircleOutlined } from '@ant-design/icons';
import {
  BarChart,
  Bar,
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
import dayjs from 'dayjs';
import CommonTable from '@/components/common/CommonTable';
import CommonSearch from '@/components/common/CommonSearch';
import { reportsApi } from '@/lib/api';

const { Title } = Typography;
const { RangePicker } = DatePicker;

const PIE_COLORS = ['#1677ff', '#52c41a', '#faad14', '#ff4d4f', '#722ed1', '#999'];

const FALLBACK_CALL_VOLUME = [
  { month: 'Jan', inbound: 1200, outbound: 800 },
  { month: 'Feb', inbound: 1400, outbound: 900 },
  { month: 'Mar', inbound: 1100, outbound: 750 },
  { month: 'Apr', inbound: 1600, outbound: 1100 },
  { month: 'May', inbound: 1800, outbound: 1200 },
  { month: 'Jun', inbound: 1500, outbound: 950 },
];

const FALLBACK_AGENT_PERF = [
  { name: 'Sarah J.', calls: 145, avgDuration: 4.2, resolution: 95 },
  { name: 'Mike R.', calls: 128, avgDuration: 5.1, resolution: 88 },
  { name: 'Emily W.', calls: 112, avgDuration: 3.8, resolution: 92 },
  { name: 'John D.', calls: 98, avgDuration: 6.2, resolution: 85 },
  { name: 'Lisa M.', calls: 56, avgDuration: 4.5, resolution: 90 },
];

const FALLBACK_SLA = [
  { name: 'Within SLA', value: 92, color: '#52c41a' },
  { name: 'Breached SLA', value: 8, color: '#ff4d4f' },
];

const FALLBACK_TOP_CUSTOMERS = [
  { rank: 1, name: 'MediCorp Health', calls: 456, avgDuration: 5.2 },
  { rank: 2, name: 'CareFirst Clinic', calls: 389, avgDuration: 4.8 },
  { rank: 3, name: 'Wellness Plus', calls: 312, avgDuration: 6.1 },
  { rank: 4, name: 'HealthBridge', calls: 278, avgDuration: 3.9 },
  { rank: 5, name: 'PrimeCare Group', calls: 245, avgDuration: 5.5 },
];

const frequencyOptions = [
  { value: 'DAILY', label: 'Daily' },
  { value: 'WEEKLY', label: 'Weekly' },
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'CRON', label: 'Cron Expression' },
];

const deliveryOptions = [
  { value: 'EMAIL', label: 'Email' },
  { value: 'EXPORT', label: 'Export (Server)' },
];

export default function ReportsPage() {
  const [activeTab, setActiveTab] = useState('charts');
  const [reportDefinitions, setReportDefinitions] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [chartsLoading, setChartsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reportType, setReportType] = useState('callVolume');
  const [scheduleModalOpen, setScheduleModalOpen] = useState(false);
  const [scheduleForm] = Form.useForm();
  const [scheduleSubmitting, setScheduleSubmitting] = useState(false);
  const [selectedFrequency, setSelectedFrequency] = useState('DAILY');
  const [callVolumeData, setCallVolumeData] = useState(FALLBACK_CALL_VOLUME);
  const [agentPerfData, setAgentPerfData] = useState(FALLBACK_AGENT_PERF);
  const [slaData, setSlaData] = useState(FALLBACK_SLA);
  const [topCustomers, setTopCustomers] = useState(FALLBACK_TOP_CUSTOMERS);

  const fetchDefinitions = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await reportsApi.getAll({ page: 0, size: 100 });
      setReportDefinitions(res.data?.data?.content || res.data?.content || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load report definitions');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'definitions') {
      fetchDefinitions();
    }
  }, [activeTab, fetchDefinitions]);

    const fetchChartsData = useCallback(async () => {
    setChartsLoading(true);
    try {
      const [analyticsRes, agentPerfRes] = await Promise.allSettled([
        reportsApi.getAnalytics({ period: 'MONTHLY' }),
        reportsApi.getAgentPerformance({ period: 'MONTHLY' }),
      ]);

      if (analyticsRes.status === 'fulfilled') {
        const data = analyticsRes.value.data?.data || analyticsRes.value.data;
        if (data?.callVolume) setCallVolumeData(data.callVolume);
        if (data?.sla) setSlaData(data.sla);
        if (data?.topCustomers) setTopCustomers(data.topCustomers);
      }
      if (agentPerfRes.status === 'fulfilled') {
        const data = agentPerfRes.value.data?.data || agentPerfRes.value.data;
        if (data?.agents) setAgentPerfData(data.agents);
      }
    } catch {
      // Fallback data already set
    } finally {
      setChartsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchChartsData();
  }, [fetchChartsData]);

  const handleExportCsv = async () => {
    try {
      const res = await reportsApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `reports_${dayjs().format('YYYYMMDD_HHmmss')}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Exported');
    } catch { message.error('Export failed'); }
  };

  const handleExportExcel = async () => {
    try {
      const res = await reportsApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `reports_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Exported');
    } catch { message.error('Export failed'); }
  };

  const handleSearch = (values: any) => {
    if (activeTab === 'definitions') {
      fetchDefinitions();
    }
  };

  const handleReset = () => {
    if (activeTab === 'definitions') {
      fetchDefinitions();
    }
  };

  const handleScheduleSubmit = async (values: any) => {
    setScheduleSubmitting(true);
    try {
      await reportsApi.createSchedule({
        reportDefinitionId: values.reportDefinitionId,
        frequency: values.frequency,
        cronExpression: values.frequency === 'CRON' ? values.cronExpression : undefined,
        deliveryMethod: values.deliveryMethod,
        recipients: values.recipients?.split(',').map((e: string) => e.trim()).filter(Boolean) || [],
        active: true,
      });
      message.success('Report scheduled successfully');
      setScheduleModalOpen(false);
      scheduleForm.resetFields();
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Failed to create schedule');
    } finally {
      setScheduleSubmitting(false);
    }
  };

  const definitionColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id' },
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Type', dataIndex: 'reportType', key: 'reportType' },
    { title: 'Description', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: 'Schedule', dataIndex: 'schedule', key: 'schedule', render: (s: string) => s || '-' },
    { title: 'Created', dataIndex: 'createdAt', key: 'createdAt',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
  ];

  const tabItems = [
    {
      key: 'charts',
      label: 'Charts',
      children: chartsLoading ? (
        <Card><div style={{ textAlign: 'center', padding: 60 }}><Typography.Text type="secondary">Loading chart data...</Typography.Text></div></Card>
      ) : (
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
          <Col xs={24} lg={12}>
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
          <Col xs={24} lg={12}>
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
        <CommonTable
          title="Top Customers by Call Volume"
          columns={[
            { title: 'Rank', dataIndex: 'rank', key: 'rank', width: 60 },
            { title: 'Customer', dataIndex: 'name', key: 'name' },
            { title: 'Total Calls', dataIndex: 'calls', key: 'calls' },
            { title: 'Avg Duration (min)', dataIndex: 'avgDuration', key: 'avgDuration' },
          ]}
          dataSource={topCustomers}
          rowKey="rank"
          pagination={false}
        />
      ),
    },
    {
      key: 'definitions',
      label: `Definitions (${reportDefinitions.length})`,
      children: (
        <CommonTable
          columns={definitionColumns}
          dataSource={reportDefinitions}
          loading={loading}
          error={error}
          rowKey="id"
          pagination={{ pageSize: 10 }}
          onRefresh={fetchDefinitions}
          onExportCsv={handleExportCsv}
          onExportExcel={handleExportExcel}
          extra={
            <Button type="primary" icon={<ScheduleOutlined />} onClick={() => setScheduleModalOpen(true)}>
              Schedule Report
            </Button>
          }
        />
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

      {activeTab === 'definitions' && (
        <CommonSearch
          fields={[
            { name: 'search', label: 'Search', type: 'input', placeholder: 'Search by name or type' },
            {
              name: 'reportType',
              label: 'Type',
              type: 'select',
              placeholder: 'Filter by type',
              options: [
                { value: 'CALL_VOLUME', label: 'Call Volume' },
                { value: 'AGENT_PERFORMANCE', label: 'Agent Performance' },
                { value: 'SLA', label: 'SLA Compliance' },
              ],
            },
          ]}
          onSearch={handleSearch}
          onReset={handleReset}
        />
      )}

      <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />

      <Modal
        title="Schedule Report"
        open={scheduleModalOpen}
        onCancel={() => { setScheduleModalOpen(false); scheduleForm.resetFields(); }}
        onOk={() => scheduleForm.submit()}
        confirmLoading={scheduleSubmitting}
        width={600}
        destroyOnClose
      >
        <Form
          form={scheduleForm}
          layout="vertical"
          onFinish={handleScheduleSubmit}
          initialValues={{ frequency: 'DAILY', deliveryMethod: 'EMAIL' }}
        >
          <Form.Item
            name="reportDefinitionId"
            label="Report Definition"
            rules={[{ required: true, message: 'Please select a report' }]}
          >
            <Select
              showSearch
              placeholder="Select a report definition"
              optionFilterProp="label"
              options={reportDefinitions.map((d: any) => ({
                value: d.id,
                label: `${d.name} (${d.reportType || 'N/A'})`,
              }))}
            />
          </Form.Item>

          <Form.Item
            name="frequency"
            label="Frequency"
            rules={[{ required: true, message: 'Please select frequency' }]}
          >
            <Radio.Group
              options={frequencyOptions}
              onChange={(e) => setSelectedFrequency(e.target.value)}
            />
          </Form.Item>

          {selectedFrequency === 'CRON' && (
            <Form.Item
              name="cronExpression"
              label="Cron Expression"
              rules={[{ required: true, message: 'Please enter cron expression' }]}
              extra={<span style={{ fontSize: 12 }}>e.g. <Tag>0 0 * * *</Tag> (daily at midnight)</span>}
            >
              <Input placeholder="0 0 * * *" />
            </Form.Item>
          )}

          <Form.Item
            name="deliveryMethod"
            label="Delivery Method"
            rules={[{ required: true, message: 'Please select delivery method' }]}
          >
            <Radio.Group options={deliveryOptions} />
          </Form.Item>

          <Form.Item
            name="recipients"
            label="Recipient Emails"
            rules={[{ required: true, message: 'Please enter at least one email' }]}
            extra="Separate multiple emails with commas"
          >
            <Input.TextArea rows={2} placeholder="user@example.com, admin@example.com" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
