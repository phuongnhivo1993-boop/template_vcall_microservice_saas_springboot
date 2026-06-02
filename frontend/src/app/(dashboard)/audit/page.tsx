'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Table, Card, Tabs, Tag, Typography, Space, Button, Modal, Form,
  Input, Select, message, Row, Col, Statistic, DatePicker, Popconfirm
} from 'antd';
import {
  SearchOutlined, SafetyCertificateOutlined, WarningOutlined,
  AuditOutlined, FileTextOutlined
} from '@ant-design/icons';
import { auditApi } from '@/lib/api';

const { Title } = Typography;
const { RangePicker } = DatePicker;

const actionColors: Record<string, string> = {
  CREATE: 'green', UPDATE: 'blue', DELETE: 'red',
  LOGIN: 'cyan', LOGOUT: 'default', ASSIGN: 'purple',
  ESCALATE: 'orange', STATUS_CHANGE: 'yellow',
};

const severityColors: Record<string, string> = {
  LOW: 'default', MEDIUM: 'orange', HIGH: 'red', CRITICAL: 'red',
};

const fraudStatusColors: Record<string, string> = {
  OPEN: 'red', INVESTIGATING: 'orange', RESOLVED: 'green', FALSE_POSITIVE: 'default',
};

export default function AuditPage() {
  const [activeTab, setActiveTab] = useState('logs');
  const [logs, setLogs] = useState<any[]>([]);
  const [securityLogs, setSecurityLogs] = useState<any[]>([]);
  const [fraudAlerts, setFraudAlerts] = useState<any[]>([]);
  const [reconciliations, setReconciliations] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState<Record<string, any>>({});
  const [logDetail, setLogDetail] = useState<any>(null);
  const [detailOpen, setDetailOpen] = useState(false);

  const fetchLogs = useCallback(async () => {
    setLoading(true);
    try {
      const [logsRes, secRes, fraudRes, recRes] = await Promise.all([
        auditApi.search({ ...searchParams, page: 0, size: 100 }).catch(() => auditApi.list({ page: 0, size: 100 })),
        auditApi.getSecurityLogs({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        auditApi.getFraudAlerts({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        auditApi.getReconciliations({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
      ]);
      setLogs(logsRes.data?.data?.content || logsRes.data?.content || []);
      setSecurityLogs(secRes.data?.data?.content || secRes.data?.content || []);
      setFraudAlerts(fraudRes.data?.data?.content || fraudRes.data?.content || []);
      setReconciliations(recRes.data?.data?.content || recRes.data?.content || []);
    } catch { message.error('Failed to load audit data'); }
    finally { setLoading(false); }
  }, [searchParams]);

  useEffect(() => { fetchLogs(); }, [fetchLogs]);

  const handleViewDetail = async (id: string) => {
    try {
      const res = await auditApi.getById(id);
      setLogDetail(res.data?.data || res.data);
      setDetailOpen(true);
    } catch { message.error('Failed to load detail'); }
  };

  const handleResolveFraud = async (id: string, status: string) => {
    try {
      await auditApi.updateFraudAlertStatus(id, status);
      message.success(`Alert ${status}`);
      fetchLogs();
    } catch { message.error('Failed to update'); }
  };

  const logColumns = [
    { title: 'Action', dataIndex: 'action', key: 'action',
      render: (a: string) => <Tag color={actionColors[a] || 'default'}>{a}</Tag> },
    { title: 'Actor', dataIndex: 'actorId', key: 'actorId' },
    { title: 'Resource', dataIndex: 'resource', key: 'resource' },
    { title: 'Resource ID', dataIndex: 'resourceId', key: 'resourceId' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={s === 'SUCCESS' ? 'green' : 'red'}>{s || 'SUCCESS'}</Tag> },
    { title: 'Timestamp', dataIndex: 'timestamp', key: 'timestamp',
      render: (t: string) => t ? new Date(t).toLocaleString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => <Button size="small" onClick={() => handleViewDetail(r.id)}>View</Button> },
  ];

  const securityColumns = [
    { title: 'Event Type', dataIndex: 'eventType', key: 'eventType' },
    { title: 'Actor', dataIndex: 'actorId', key: 'actorId' },
    { title: 'IP Address', dataIndex: 'ipAddress', key: 'ipAddress' },
    { title: 'User Agent', dataIndex: 'userAgent', key: 'userAgent', ellipsis: true },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={s === 'SUCCESS' ? 'green' : 'red'}>{s}</Tag> },
    { title: 'Timestamp', dataIndex: 'timestamp', key: 'timestamp',
      render: (t: string) => t ? new Date(t).toLocaleString() : '-' },
  ];

  const fraudColumns = [
    { title: 'Alert Type', dataIndex: 'alertType', key: 'alertType' },
    { title: 'Severity', dataIndex: 'severity', key: 'severity',
      render: (s: string) => <Tag color={severityColors[s] || 'default'}>{s}</Tag> },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={fraudStatusColors[s] || 'default'}>{s}</Tag> },
    { title: 'Actor', dataIndex: 'actorId', key: 'actorId' },
    { title: 'Description', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: 'Timestamp', dataIndex: 'detectedAt', key: 'detectedAt',
      render: (t: string) => t ? new Date(t).toLocaleString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        r.status === 'OPEN' || r.status === 'INVESTIGATING' ? (
          <Space>
            <Button size="small" type="primary" onClick={() => handleResolveFraud(r.id, 'RESOLVED')}>Resolve</Button>
            <Button size="small" onClick={() => handleResolveFraud(r.id, 'FALSE_POSITIVE')}>False Alarm</Button>
          </Space>
        ) : <Tag color="green">Handled</Tag>
      ),
    },
  ];

  const reconciliationColumns = [
    { title: 'Type', dataIndex: 'reconciliationType', key: 'reconciliationType' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={s === 'COMPLETED' ? 'green' : s === 'FAILED' ? 'red' : 'orange'}>{s}</Tag> },
    { title: 'Discrepancies', dataIndex: 'discrepancyCount', key: 'discrepancyCount',
      render: (c: number) => c && c > 0 ? <Tag color="red">{c}</Tag> : <Tag color="green">0</Tag> },
    { title: 'Started', dataIndex: 'startedAt', key: 'startedAt',
      render: (t: string) => t ? new Date(t).toLocaleString() : '-' },
    { title: 'Completed', dataIndex: 'completedAt', key: 'completedAt',
      render: (t: string) => t ? new Date(t).toLocaleString() : '-' },
    { title: 'Notes', dataIndex: 'notes', key: 'notes', ellipsis: true },
  ];

  return (
    <div>
      <Title level={3}>Audit & Security</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}><Card><Statistic title="Audit Logs" value={logs.length} prefix={<AuditOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Security Events" value={securityLogs.length} prefix={<SafetyCertificateOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Fraud Alerts" value={fraudAlerts.length} prefix={<WarningOutlined />} valueStyle={{ color: fraudAlerts.length > 0 ? '#ff4d4f' : undefined }} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Reconciliations" value={reconciliations.length} prefix={<FileTextOutlined />} /></Card></Col>
      </Row>

      <Card>
        <Tabs activeKey={activeTab} onChange={setActiveTab} tabBarExtraContent={
          activeTab === 'logs' ? (
            <Space>
              <Input.Search
                placeholder="Search by action, resource..."
                onSearch={(val) => setSearchParams(prev => ({ ...prev, q: val || undefined }))}
                style={{ width: 250 }}
              />
              <Select
                placeholder="Action"
                allowClear
                style={{ width: 130 }}
                onChange={(val) => setSearchParams(prev => ({ ...prev, action: val || undefined }))}
                options={['CREATE','UPDATE','DELETE','LOGIN','LOGOUT','ASSIGN','STATUS_CHANGE'].map(a => ({ value: a, label: a }))}
              />
              <Button icon={<SearchOutlined />} onClick={fetchLogs}>Search</Button>
            </Space>
          ) : undefined
        } items={[
          { key: 'logs', label: `Audit Logs (${logs.length})`,
            children: <Table rowKey="id" columns={logColumns} dataSource={logs} loading={loading} pagination={{ pageSize: 10 }}
              expandable={{ expandedRowRender: (r: any) => <pre style={{ fontSize: 12 }}>{JSON.stringify(r.details || r.changes || r, null, 2)}</pre> }} /> },
          { key: 'security', label: `Security (${securityLogs.length})`,
            children: <Table rowKey="id" columns={securityColumns} dataSource={securityLogs} loading={loading} pagination={{ pageSize: 10 }} /> },
          { key: 'fraud', label: `Fraud Alerts (${fraudAlerts.length})`,
            children: <Table rowKey="id" columns={fraudColumns} dataSource={fraudAlerts} loading={loading} pagination={{ pageSize: 10 }} /> },
          { key: 'reconciliation', label: `Reconciliation (${reconciliations.length})`,
            children: <Table rowKey="id" columns={reconciliationColumns} dataSource={reconciliations} loading={loading} pagination={{ pageSize: 10 }} /> },
        ]} />
      </Card>

      <Modal title="Audit Log Detail" open={detailOpen} onCancel={() => setDetailOpen(false)} footer={null} width={700}>
        {logDetail && (
          <pre style={{ fontSize: 13, whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
            {JSON.stringify(logDetail, null, 2)}
          </pre>
        )}
      </Modal>
    </div>
  );
}
