'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Descriptions, Tag, Spin, Alert, Button, Space, Typography, Row, Col, Timeline, Progress, Divider, Empty } from 'antd';
import { ArrowLeftOutlined, ClockCircleOutlined, UserOutlined, TeamOutlined, MessageOutlined, CheckCircleOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import { ticketsApi } from '@/lib/api';

const { Title, Text } = Typography;

const priorityColors: Record<string, string> = {
  low: 'green', LOW: 'green',
  medium: 'blue', MEDIUM: 'blue',
  high: 'orange', HIGH: 'orange',
  critical: 'red', CRITICAL: 'red',
};

const statusColors: Record<string, string> = {
  open: '#1677ff', OPEN: '#1677ff',
  in_progress: '#faad14', IN_PROGRESS: '#faad14', inprogress: '#faad14',
  resolved: '#52c41a', RESOLVED: '#52c41a',
  closed: '#d9d9d9', CLOSED: '#d9d9d9',
};

const slaColors: Record<string, string> = {
  within_sla: '#52c41a', WITHIN_SLA: '#52c41a',
  warning: '#faad14', WARNING: '#faad14',
  breached: '#ff4d4f', BREACHED: '#ff4d4f',
};

export default function TicketDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [ticket, setTicket] = useState<any>(null);
  const [comments, setComments] = useState<any[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(false);

  const fetchComments = useCallback(async () => {
    setCommentsLoading(true);
    try {
      const res = await ticketsApi.getComments(params.id as string);
      setComments(res.data?.data?.content || res.data?.data || res.data || []);
    } catch {
      setComments([]);
    } finally {
      setCommentsLoading(false);
    }
  }, [params.id]);

  const fetchTicket = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await ticketsApi.getById(params.id as string);
      setTicket(res.data?.data || res.data);
      fetchComments();
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load ticket details');
    } finally {
      setLoading(false);
    }
  }, [params.id, fetchComments]);

  useEffect(() => {
    fetchTicket();
  }, [fetchTicket]);

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
        action={<Button onClick={fetchTicket}>Retry</Button>}
      />
    );
  }

  if (!ticket) return null;

  const slaStatus = ticket.slaStatus || (ticket.slaPassed ? 'breached' : 'within_sla');
  const slaColor = slaColors[slaStatus?.toUpperCase?.()] || slaColors[slaStatus] || '#1677ff';

  const slaTotal = ticket.slaDeadline && ticket.created
    ? Math.max(1, new Date(ticket.slaDeadline).getTime() - new Date(ticket.created).getTime())
    : 0;
  const slaRemaining = ticket.slaDeadline
    ? Math.max(0, new Date(ticket.slaDeadline).getTime() - Date.now())
    : 0;
  const slaPercent = slaTotal > 0 ? Math.round((1 - slaRemaining / slaTotal) * 100) : 0;

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => router.back()}>Back</Button>
        <Title level={4} style={{ margin: 0 }}>Ticket Details</Title>
      </Space>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title={`Ticket ${ticket.id || ticket.ticketNumber || ''}`}>
            <Descriptions column={{ xs: 1, sm: 2 }}>
              <Descriptions.Item label="Subject" span={2}>{ticket.subject || ticket.title}</Descriptions.Item>
              <Descriptions.Item label="Description" span={2}>{ticket.description || '-'}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={statusColors[ticket.status?.toUpperCase?.()] || statusColors[ticket.status] || 'default'}>
                  {(ticket.status || 'UNKNOWN').replace('_', ' ').toUpperCase()}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Priority">
                <Tag color={priorityColors[ticket.priority?.toUpperCase?.()] || priorityColors[ticket.priority] || 'default'}>
                  {(ticket.priority || 'NONE').toUpperCase()}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Category">{ticket.category || '-'}</Descriptions.Item>
              <Descriptions.Item label="Created">{ticket.created ? new Date(ticket.created).toLocaleString() : '-'}</Descriptions.Item>
              <Descriptions.Item label="Updated">{ticket.updatedAt ? new Date(ticket.updatedAt).toLocaleString() : '-'}</Descriptions.Item>
              <Descriptions.Item label="Resolved At">{ticket.resolvedAt ? new Date(ticket.resolvedAt).toLocaleString() : '-'}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="Assignment" style={{ marginBottom: 16 }}>
            <Descriptions column={1} size="small">
              <Descriptions.Item label={<><UserOutlined /> Agent</>}>{ticket.assignedTo || ticket.agent || 'Unassigned'}</Descriptions.Item>
              <Descriptions.Item label={<><TeamOutlined /> Group</>}>{ticket.assignedGroup || ticket.group || '-'}</Descriptions.Item>
            </Descriptions>
          </Card>
          <Card title="SLA Information">
            {ticket.slaDeadline ? (
              <>
                <Progress
                  percent={slaPercent}
                  strokeColor={slaPercent > 80 ? '#ff4d4f' : slaPercent > 50 ? '#faad14' : '#52c41a'}
                  format={() => `${slaPercent}%`}
                />
                <Descriptions column={1} size="small" style={{ marginTop: 12 }}>
                  <Descriptions.Item label="Deadline">{new Date(ticket.slaDeadline).toLocaleString()}</Descriptions.Item>
                  <Descriptions.Item label="Status">
                    <Tag color={slaColor}>
                      {slaStatus?.replace('_', ' ').toUpperCase() || 'UNKNOWN'}
                    </Tag>
                  </Descriptions.Item>
                  {ticket.slaRule && <Descriptions.Item label="SLA Rule">{ticket.slaRule}</Descriptions.Item>}
                </Descriptions>
              </>
            ) : (
              <Text type="secondary">No SLA configured</Text>
            )}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="Status History">
            <CommonTable
              columns={[
                { title: 'From', dataIndex: 'fromStatus', key: 'fromStatus', render: (s: string) => s ? <Tag>{s}</Tag> : '-' },
                { title: 'To', dataIndex: 'toStatus', key: 'toStatus', render: (s: string) => <Tag color={statusColors[s?.toUpperCase?.()] || statusColors[s]}>{s}</Tag> },
                { title: 'Changed By', dataIndex: 'changedBy', key: 'changedBy', render: (u: string) => u || '-' },
                { title: 'Changed At', dataIndex: 'changedAt', key: 'changedAt', render: (d: string) => d ? new Date(d).toLocaleString() : '-' },
              ]}
              dataSource={ticket.statusHistory || []}
              loading={false}
              pagination={false}
              rowKey={(r: any) => r.id || Math.random().toString()}
            />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={`Comments (${comments.length})`}>
            {comments.length === 0 && !commentsLoading ? (
              <Empty description="No comments yet" />
            ) : (
              <Timeline
                items={comments.map((c: any) => ({
                  color: c.isInternal ? 'orange' : 'blue',
                  children: (
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <Text strong>{c.author || c.user || 'Unknown'}</Text>
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          {c.createdAt ? new Date(c.createdAt).toLocaleString() : ''}
                        </Text>
                      </div>
                      <div style={{ marginTop: 4 }}>{c.body || c.content || c.text}</div>
                      {c.isInternal && <Tag color="orange" style={{ marginTop: 4 }}>Internal</Tag>}
                    </div>
                  ),
                }))}
              />
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
}
