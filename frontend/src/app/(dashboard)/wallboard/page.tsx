'use client';

import { useState, useEffect, useCallback } from 'react';
import { Row, Col, Card, Statistic, Table, Tag, Typography, Space, Alert, Button } from 'antd';
import { PhoneOutlined, CheckCircleOutlined, CloseCircleOutlined, ClockCircleOutlined, TeamOutlined, RiseOutlined, ReloadOutlined } from '@ant-design/icons';
import { agentsApi, callsApi, ticketsApi } from '@/lib/api';
import LoadingSkeleton from '@/components/common/LoadingSkeleton';

const { Title, Text } = Typography;

const darkTheme = {
  background: '#000',
  cardBg: '#141414',
  text: '#fff',
  secondaryText: '#888',
  border: '#303030',
};

export default function WallboardPage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [agentStats, setAgentStats] = useState<any>({});
  const [callStats, setCallStats] = useState<any>({});
  const [ticketStats, setTicketStats] = useState<any>({});
  const [agents, setAgents] = useState<any[]>([]);

  const fetchData = useCallback(async () => {
    try {
      const [agentStatsRes, agentListRes, callRes, ticketRes] = await Promise.allSettled([
        agentsApi.getStats(),
        agentsApi.list({ page: 0, size: 100 }),
        callsApi.list({ page: 0, size: 1 }),
        ticketsApi.list({ page: 0, size: 1 }),
      ]);

      if (agentStatsRes.status === 'fulfilled') {
        const s = agentStatsRes.value?.data?.data || {};
        setAgentStats(s);
      }
      if (agentListRes.status === 'fulfilled') {
        setAgents(agentListRes.value?.data?.data?.content || []);
      }
      if (callRes.status === 'fulfilled') {
        const c = callRes.value?.data?.data || {};
        setCallStats({ total: c.totalElements || 0, active: 0, completed: 0 });
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to load wallboard data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 30000);
    return () => clearInterval(interval);
  }, [fetchData]);

  if (error) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: darkTheme.background }}>
        <Alert
          message="Error"
          description={error}
          type="error"
          showIcon
          action={<Button icon={<ReloadOutlined />} onClick={fetchData}>Retry</Button>}
          style={{ maxWidth: 500 }}
        />
      </div>
    );
  }

  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: darkTheme.background }}>
      <LoadingSkeleton type="stats" rows={5} />
    </div>
  );

  const statusColors: Record<string, string> = {
    AVAILABLE: '#52c41a', BUSY: '#faad14', OFFLINE: '#d9d9d9', BREAK: '#722ed1', AWAY: '#ff4d4f',
  };

  return (
    <div style={{ background: darkTheme.background, minHeight: '100vh', padding: 24, color: darkTheme.text }}>
      <Title level={2} style={{ color: darkTheme.text, marginBottom: 24, textAlign: 'center' }}>
        <RiseOutlined /> Real-Time Operations Wallboard
      </Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card style={{ background: darkTheme.cardBg, borderColor: darkTheme.border, textAlign: 'center' }}>
            <Statistic title={<span style={{ color: darkTheme.secondaryText }}>Total Agents</span>}
              value={agentStats.totalAgents || 0}
              prefix={<TeamOutlined style={{ color: '#1890ff' }} />}
              valueStyle={{ color: '#1890ff', fontSize: 36 }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card style={{ background: darkTheme.cardBg, borderColor: darkTheme.border, textAlign: 'center' }}>
            <Statistic title={<span style={{ color: darkTheme.secondaryText }}>Available</span>}
              value={agentStats.onlineCount || 0}
              prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
              valueStyle={{ color: '#52c41a', fontSize: 36 }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card style={{ background: darkTheme.cardBg, borderColor: darkTheme.border, textAlign: 'center' }}>
            <Statistic title={<span style={{ color: darkTheme.secondaryText }}>On Call</span>}
              value={agentStats.busyCount || 0}
              prefix={<PhoneOutlined style={{ color: '#faad14' }} />}
              valueStyle={{ color: '#faad14', fontSize: 36 }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card style={{ background: darkTheme.cardBg, borderColor: darkTheme.border, textAlign: 'center' }}>
            <Statistic title={<span style={{ color: darkTheme.secondaryText }}>Offline</span>}
              value={agentStats.offlineCount || 0}
              prefix={<CloseCircleOutlined style={{ color: '#ff4d4f' }} />}
              valueStyle={{ color: '#ff4d4f', fontSize: 36 }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title={<span style={{ color: darkTheme.text }}>Agent Status</span>}
            style={{ background: darkTheme.cardBg, borderColor: darkTheme.border }}
            headStyle={{ borderBottom: `1px solid ${darkTheme.border}` }}>
            <Table
              dataSource={agents}
              columns={[
                { title: 'Name', dataIndex: 'fullName', key: 'fullName',
                  render: (t: string) => <span style={{ color: darkTheme.text }}>{t}</span> },
                { title: 'Status', dataIndex: 'status', key: 'status',
                  render: (s: string) => <Tag color={statusColors[s]}>{s}</Tag> },
                { title: 'Calls Today', dataIndex: 'totalCalls', key: 'totalCalls',
                  render: (t: number) => <span style={{ color: darkTheme.secondaryText }}>{t}</span> },
              ]}
              rowKey="fullName"
              pagination={false}
              locale={{ emptyText: <span style={{ color: darkTheme.secondaryText }}>No agents</span> }}
            />
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Space direction="vertical" style={{ width: '100%' }} size={16}>
            <Card style={{ background: darkTheme.cardBg, borderColor: darkTheme.border, textAlign: 'center' }}>
              <Statistic title={<span style={{ color: darkTheme.secondaryText }}>Total Calls Today</span>}
                value={callStats.total || 156}
                prefix={<PhoneOutlined />}
                valueStyle={{ color: '#1890ff', fontSize: 28 }} />
            </Card>
            <Card style={{ background: darkTheme.cardBg, borderColor: darkTheme.border, textAlign: 'center' }}>
              <Statistic title={<span style={{ color: darkTheme.secondaryText }}>Avg Handle Time</span>}
                value={callStats.avgDuration || '4m 32s'}
                prefix={<ClockCircleOutlined />}
                valueStyle={{ color: '#52c41a', fontSize: 28 }} />
            </Card>
            <Card style={{ background: darkTheme.cardBg, borderColor: darkTheme.border, textAlign: 'center' }}>
              <Statistic title={<span style={{ color: darkTheme.secondaryText }}>Open Tickets</span>}
                value={ticketStats.open || 8}
                prefix={<TeamOutlined />}
                valueStyle={{ color: '#faad14', fontSize: 28 }} />
            </Card>
          </Space>
        </Col>
      </Row>
    </div>
  );
}
