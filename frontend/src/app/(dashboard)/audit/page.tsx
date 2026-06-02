'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Tabs, Tag, Typography, Space, Button, message, Row, Col, Statistic, Modal } from 'antd';
import {
  SafetyCertificateOutlined, WarningOutlined,
  AuditOutlined, FileTextOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import CommonTable from '@/components/common/CommonTable';
import CommonSearch from '@/components/common/CommonSearch';
import { auditApi } from '@/lib/api';

const { Title } = Typography;

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
  const [error, setError] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useState<Record<string, any>>({});
  const [logDetail, setLogDetail] = useState<any>(null);
  const [detailOpen, setDetailOpen] = useState(false);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [logsRes, secRes, fraudRes, recRes] = await Promise.all([
        auditApi.search({ ...searchParams, page: 0, size: 100 }).catch(() => auditApi.getAll({ page: 0, size: 100 })),
        auditApi.getSecurityLogs({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        auditApi.getFraudAlerts({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        auditApi.getReconciliations({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
      ]);
      setLogs(logsRes.data?.data?.content || logsRes.data?.content || []);
      setSecurityLogs(secRes.data?.data?.content || secRes.data?.content || []);
      setFraudAlerts(fraudRes.data?.data?.content || fraudRes.data?.content || []);
      setReconciliations(recRes.data?.data?.content || recRes.data?.content || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load audit data');
    } finally {
      setLoading(false);
    }
  }, [searchParams]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleSearch = (values: any) => {
    const params: Record<string, any> = {};
    if (values.search) params.q = values.search;
    if (values.action) params.action = values.action;
    if (values.severity) params.severity = values.severity;
    setSearchParams(params);
  };

  const handleReset = () => {
    setSearchParams({});
  };

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
      fetchData();
    } catch { message.error('Failed to update'); }
  };

  const handleExportCsv = async () => {
    try {
      const res = await auditApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `audit_${dayjs().format('YYYYMMDD_HHmmss')}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Exported');
    } catch { message.error('Export failed'); }
  };

  const handleExportExcel = async () => {
    try {
      const res = await auditApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `audit_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Exported');
    } catch { message.error('Export failed'); }
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

  const tabContent = (key: string) => {
    switch (key) {
      case 'logs':
        return (
          <CommonTable
            columns={logColumns}
            dataSource={logs}
            loading={loading}
            error={error}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            onRefresh={fetchData}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
            extra={
              <CommonSearch
                fields={[
                  { name: 'search', label: 'Search', type: 'input', placeholder: 'Search by action, resource...' },
                  {
                    name: 'action',
                    label: 'Action',
                    type: 'select',
                    placeholder: 'Action',
                    options: ['CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'ASSIGN', 'STATUS_CHANGE'].map(a => ({ value: a, label: a })),
                  },
                ]}
                onSearch={handleSearch}
                onReset={handleReset}
                loading={loading}
              />
            }
            onTableChange={(_pagination, _filters, sorter: any) => {
              if (sorter?.order) {
                setSearchParams(prev => ({ ...prev, sort: `${sorter.field},${sorter.order === 'ascend' ? 'asc' : 'desc'}` }));
              }
            }}
          />
        );
      case 'security':
        return (
          <CommonTable
            columns={securityColumns}
            dataSource={securityLogs}
            loading={loading}
            error={error}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            onRefresh={fetchData}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
          />
        );
      case 'fraud':
        return (
          <CommonTable
            columns={fraudColumns}
            dataSource={fraudAlerts}
            loading={loading}
            error={error}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            onRefresh={fetchData}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
          />
        );
      case 'reconciliation':
        return (
          <CommonTable
            columns={reconciliationColumns}
            dataSource={reconciliations}
            loading={loading}
            error={error}
            rowKey="id"
            pagination={{ pageSize: 10 }}
            onRefresh={fetchData}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
          />
        );
      default:
        return null;
    }
  };

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
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
          { key: 'logs', label: `Audit Logs (${logs.length})`, children: tabContent('logs') },
          { key: 'security', label: `Security (${securityLogs.length})`, children: tabContent('security') },
          { key: 'fraud', label: `Fraud Alerts (${fraudAlerts.length})`, children: tabContent('fraud') },
          { key: 'reconciliation', label: `Reconciliation (${reconciliations.length})`, children: tabContent('reconciliation') },
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
