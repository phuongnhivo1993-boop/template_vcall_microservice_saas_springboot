'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Descriptions, Tag, Spin, Alert, Button, Space, Typography, Row, Col, Statistic, Tabs } from 'antd';
import { ArrowLeftOutlined, PhoneOutlined, CheckCircleOutlined, StarOutlined, ClockCircleOutlined, TeamOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import { agentsApi } from '@/lib/api';

const { Title } = Typography;

const statusColors: Record<string, string> = {
  ONLINE: '#52c41a', ON_LINE: '#52c41a', online: '#52c41a',
  OFFLINE: '#d9d9d9', offline: '#d9d9d9',
  BUSY: '#faad14', busy: '#faad14',
  BREAK: '#722ed1', break: '#722ed1',
  AWAY: '#ff4d4f',
};

export default function AgentDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [agent, setAgent] = useState<any>(null);

  const fetchAgent = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await agentsApi.getById(params.id as string);
      setAgent(res.data?.data || res.data);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load agent details');
    } finally {
      setLoading(false);
    }
  }, [params.id]);

  useEffect(() => {
    fetchAgent();
  }, [fetchAgent]);

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error) {
    return (
      <Alert
        message="Error"
        description={error}
        type="error"
        showIcon
        action={<Button onClick={fetchAgent}>Retry</Button>}
      />
    );
  }

  if (!agent) return null;

  const colorKey = agent.status?.toUpperCase?.() || agent.status;
  const statusColor = statusColors[colorKey] || '#1677ff';

  const session = agent.currentSession || agent.session;

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => router.back()}>Back</Button>
        <Title level={4} style={{ margin: 0 }}>Agent Details</Title>
      </Space>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title="Basic Information">
            <Descriptions column={{ xs: 1, sm: 2 }}>
              <Descriptions.Item label="Name">{agent.fullName || agent.name}</Descriptions.Item>
              <Descriptions.Item label="Agent Code">{agent.agentCode || agent.code || agent.id}</Descriptions.Item>
              <Descriptions.Item label="Email">{agent.email}</Descriptions.Item>
              <Descriptions.Item label="Phone">{agent.phone || '-'}</Descriptions.Item>
              <Descriptions.Item label="Extension">{agent.extension || '-'}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={statusColor}>{(agent.status || 'UNKNOWN').toUpperCase()}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Role">{agent.role || agent.jobTitle || '-'}</Descriptions.Item>
              <Descriptions.Item label="Group">{agent.group || agent.team || '-'}</Descriptions.Item>
              <Descriptions.Item label="Max Concurrent Calls">{agent.maxConcurrentCalls ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="Created At">{agent.createdAt ? new Date(agent.createdAt).toLocaleString() : '-'}</Descriptions.Item>
            </Descriptions>
          </Card>

          {agent.groups && agent.groups.length > 0 && (
            <Card title="Agent Groups" style={{ marginTop: 16 }}>
              <Space wrap>
                {agent.groups.map((g: any, i: number) => (
                  <Tag key={i} color="processing">{g.name || g}</Tag>
                ))}
              </Space>
            </Card>
          )}
        </Col>
        <Col xs={24} lg={8}>
          <Card title="Performance">
            <Row gutter={[16, 16]}>
              <Col span={12}><Statistic title="Total Calls" value={agent.totalCalls ?? agent.totalCallsCount ?? 0} prefix={<PhoneOutlined />} /></Col>
              <Col span={12}><Statistic title="Active Calls" value={agent.activeCalls ?? 0} prefix={<PhoneOutlined />} /></Col>
              <Col span={12}><Statistic title="Satisfaction" value={agent.satisfaction ?? 0} suffix="%" prefix={<StarOutlined />} /></Col>
              <Col span={12}><Statistic title="Status" value={agent.status === 'ONLINE' || agent.status === 'online' || agent.status === 'ON_LINE' ? 'Online' : 'Offline'} prefix={<CheckCircleOutlined />} /></Col>
            </Row>
          </Card>

          {session && (
            <Card title="Current Session" style={{ marginTop: 16 }}>
              <Descriptions column={1} size="small">
                <Descriptions.Item label="Login Time">
                  {session.loginTime || session.startTime ? new Date(session.loginTime || session.startTime).toLocaleString() : '-'}
                </Descriptions.Item>
                <Descriptions.Item label="Duration">
                  {session.duration ? `${session.duration} min` : '-'}
                </Descriptions.Item>
                <Descriptions.Item label="Session Type">
                  <Tag>{session.type || session.sessionType || 'Default'}</Tag>
                </Descriptions.Item>
              </Descriptions>
            </Card>
          )}
        </Col>
      </Row>

      <Card title="Status History" style={{ marginTop: 16 }}>
        <CommonTable
          columns={[
            { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={statusColors[s?.toUpperCase?.()] || '#1677ff'}>{s}</Tag> },
            { title: 'Reason', dataIndex: 'reason', key: 'reason', render: (r: string) => r || '-' },
            { title: 'Changed At', dataIndex: 'changedAt', key: 'changedAt', render: (d: string) => d ? new Date(d).toLocaleString() : '-' },
          ]}
          dataSource={agent.statusHistory || []}
          loading={false}
          pagination={false}
        />
      </Card>
    </div>
  );
}
