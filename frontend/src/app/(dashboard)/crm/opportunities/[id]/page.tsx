'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
  Card, Descriptions, Tag, Spin, Alert, Button, Space, Typography, Row, Col, Form,
  Input, Select, Modal, message, Empty, Timeline, Progress, Statistic
} from 'antd';
import {
  ArrowLeftOutlined, EditOutlined, ClockCircleOutlined, UserOutlined,
  DollarOutlined, CalendarOutlined
} from '@ant-design/icons';
import { crmApi } from '@/lib/api';

const { Title, Text } = Typography;

const oppStageColors: Record<string, string> = {
  PROSPECTING: 'blue', QUALIFICATION: 'cyan', NEEDS_ANALYSIS: 'purple',
  PROPOSAL: 'orange', NEGOTIATION: 'yellow', CLOSED_WON: 'green', CLOSED_LOST: 'red',
};

const activityTypeColors: Record<string, string> = {
  CALL: 'green', EMAIL: 'blue', MEETING: 'purple', TASK: 'orange', NOTE: 'default',
};

const oppStageOptions = ['PROSPECTING', 'QUALIFICATION', 'NEEDS_ANALYSIS', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST'].map(s => ({ value: s, label: s }));

export default function OpportunityDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [opportunity, setOpportunity] = useState<any>(null);
  const [activities, setActivities] = useState<any[]>([]);
  const [notes, setNotes] = useState<any[]>([]);
  const [editModalOpen, setEditModalOpen] = useState(false);

  const fetchOpportunity = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await crmApi.opportunities.getById(params.id as string);
      setOpportunity(res.data?.data || res.data);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load opportunity');
    } finally {
      setLoading(false);
    }
  }, [params.id]);

  const fetchRelatedData = useCallback(async () => {
    try {
      const [actRes, noteRes] = await Promise.all([
        crmApi.activities.list({ opportunityId: params.id, page: 0, size: 50 }).catch(() => ({ data: { content: [] } })),
        crmApi.notes.list({ opportunityId: params.id, page: 0, size: 50 }).catch(() => ({ data: { content: [] } })),
      ]);
      setActivities(actRes.data?.data?.content || actRes.data?.content || []);
      setNotes(noteRes.data?.data?.content || noteRes.data?.content || []);
    } catch {
      // silently fail
    }
  }, [params.id]);

  useEffect(() => {
    fetchOpportunity();
    fetchRelatedData();
  }, [fetchOpportunity, fetchRelatedData]);

  const handleEdit = async (values: any) => {
    try {
      await crmApi.opportunities.update(params.id as string, values);
      message.success('Opportunity updated');
      setEditModalOpen(false);
      fetchOpportunity();
    } catch (err: any) {
      message.error(err?.response?.data?.message || 'Update failed');
    }
  };

  const handleStageChange = async (stage: string) => {
    try {
      await crmApi.opportunities.updateStage(params.id as string, stage);
      message.success('Stage updated');
      fetchOpportunity();
    } catch (err: any) {
      message.error(err?.response?.data?.message || 'Stage update failed');
    }
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" tip="Loading opportunity..." /></div>;
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={fetchOpportunity}>Retry</Button>} />;
  }

  if (!opportunity) return <Empty description="Opportunity not found" />;

  const timeline: any[] = [];
  activities.forEach((a: any) => timeline.push({
    type: 'activity', activityType: a.type, title: a.subject,
    description: a.description, timestamp: a.startDate,
  }));
  notes.forEach((n: any) => timeline.push({
    type: 'note', title: n.title, description: n.content, timestamp: n.createdAt,
  }));
  timeline.sort((a: any, b: any) => new Date(b.timestamp || 0).getTime() - new Date(a.timestamp || 0).getTime());

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => router.back()}>Back</Button>
        <Title level={4} style={{ margin: 0 }}>Opportunity Details</Title>
        <Tag color={oppStageColors[opportunity.stage] || 'blue'}>{opportunity.stage}</Tag>
      </Space>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}>
          <Card><Statistic title="Value" value={opportunity.value ? `$${opportunity.value.toLocaleString()}` : '-'} prefix={<DollarOutlined />} valueStyle={{ color: '#52c41a' }} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="Probability" value={opportunity.probability ? `${opportunity.probability}%` : '-'} prefix={<CalendarOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="Expected Close" value={opportunity.expectedCloseDate ? new Date(opportunity.expectedCloseDate).toLocaleDateString() : '-'} prefix={<CalendarOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="Stage" value={opportunity.stage || '-'} valueStyle={{ color: oppStageColors[opportunity.stage] ? undefined : undefined }} /></Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card
            title={opportunity.title}
            extra={
              <Button icon={<EditOutlined />} onClick={() => setEditModalOpen(true)}>Edit</Button>
            }
          >
            <Descriptions column={{ xs: 1, sm: 2 }} bordered size="small">
              <Descriptions.Item label="Title" span={2}>{opportunity.title}</Descriptions.Item>
              <Descriptions.Item label="Stage">
                <Tag color={oppStageColors[opportunity.stage] || 'blue'}>{opportunity.stage}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Value"><DollarOutlined /> ${opportunity.value?.toLocaleString() || '-'}</Descriptions.Item>
              <Descriptions.Item label="Probability">{opportunity.probability ? `${opportunity.probability}%` : '-'}</Descriptions.Item>
              <Descriptions.Item label="Expected Close Date">
                {opportunity.expectedCloseDate ? new Date(opportunity.expectedCloseDate).toLocaleDateString('vi-VN') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Contact Person"><UserOutlined /> {opportunity.contactPerson || opportunity.contactName || '-'}</Descriptions.Item>
              <Descriptions.Item label="Assigned To"><UserOutlined /> {opportunity.assignedTo || '-'}</Descriptions.Item>
              <Descriptions.Item label="Description" span={2}>{opportunity.description || '-'}</Descriptions.Item>
              <Descriptions.Item label="Created">{opportunity.createdAt ? new Date(opportunity.createdAt).toLocaleString('vi-VN') : '-'}</Descriptions.Item>
              <Descriptions.Item label="Updated">{opportunity.updatedAt ? new Date(opportunity.updatedAt).toLocaleString('vi-VN') : '-'}</Descriptions.Item>
            </Descriptions>

            <div style={{ marginTop: 16 }}>
              <Text strong style={{ marginRight: 8 }}>Quick Stage Update:</Text>
              <Space wrap>
                {oppStageOptions.map(opt => (
                  <Tag
                    key={opt.value}
                    color={opportunity.stage === opt.value ? oppStageColors[opt.value] : 'default'}
                    style={{ cursor: 'pointer' }}
                    onClick={() => handleStageChange(opt.value)}
                  >
                    {opt.label}
                  </Tag>
                ))}
              </Space>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title="Activity Timeline">
            {timeline.length === 0 ? (
              <Empty description="No activities yet" />
            ) : (
              <Timeline
                items={timeline.map((item: any) => ({
                  color: item.type === 'note' ? 'gray' : (activityTypeColors[item.activityType] || 'blue'),
                  children: (
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <Text strong>
                          {item.type === 'note' ? 'Note' : item.activityType}
                          {item.title ? `: ${item.title}` : ''}
                        </Text>
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          {item.timestamp ? new Date(item.timestamp).toLocaleDateString('vi-VN') : ''}
                        </Text>
                      </div>
                      {item.description && <div style={{ marginTop: 4, color: '#666' }}>{item.description}</div>}
                    </div>
                  ),
                }))}
              />
            )}
          </Card>
        </Col>
      </Row>

      <Modal
        title="Edit Opportunity"
        open={editModalOpen}
        onCancel={() => setEditModalOpen(false)}
        onOk={() => {
          const form = document.querySelector('.edit-opp-form')?.closest('form');
          form?.dispatchEvent(new Event('submit', { cancelable: true }));
        }}
        okText="Save"
      >
        <Form
          className="edit-opp-form"
          layout="vertical"
          onFinish={handleEdit}
          initialValues={opportunity}
        >
          <Form.Item name="title" label="Title" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="stage" label="Stage"><Select options={oppStageOptions} /></Form.Item>
          <Form.Item name="value" label="Value ($)"><Input type="number" /></Form.Item>
          <Form.Item name="probability" label="Probability (%)"><Input type="number" /></Form.Item>
          <Form.Item name="expectedCloseDate" label="Expected Close Date"><Input type="date" /></Form.Item>
          <Form.Item name="contactPerson" label="Contact Person"><Input /></Form.Item>
          <Form.Item name="description" label="Description"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
