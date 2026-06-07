'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
  Card, Descriptions, Tag, Spin, Alert, Button, Space, Typography, Row, Col, Form,
  Input, Select, Modal, message, Empty, Timeline, Tooltip
} from 'antd';
import {
  ArrowLeftOutlined, MailOutlined, PhoneOutlined, BankOutlined, EditOutlined,
  SwapOutlined, ClockCircleOutlined, UserOutlined, CheckCircleOutlined
} from '@ant-design/icons';
import { crmApi } from '@/lib/api';

const { Title, Text } = Typography;

const leadStatusColors: Record<string, string> = {
  NEW: 'blue', CONTACTED: 'cyan', QUALIFIED: 'green',
  PROPOSAL: 'orange', NEGOTIATION: 'yellow', CLOSED_WON: 'green',
  CLOSED_LOST: 'red', DISQUALIFIED: 'default',
};

const activityTypeColors: Record<string, string> = {
  CALL: 'green', EMAIL: 'blue', MEETING: 'purple', TASK: 'orange', NOTE: 'default',
};

const leadStatusOptions = ['NEW', 'CONTACTED', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST', 'DISQUALIFIED'].map(s => ({ value: s, label: s }));
const sourceOptions = ['MANUAL', 'REFERRAL', 'WEBSITE', 'CALL', 'EMAIL', 'SOCIAL_MEDIA', 'PARTNER', 'OTHER'].map(s => ({ value: s, label: s }));

export default function LeadDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lead, setLead] = useState<any>(null);
  const [activities, setActivities] = useState<any[]>([]);
  const [notes, setNotes] = useState<any[]>([]);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [convertModalOpen, setConvertModalOpen] = useState(false);

  const fetchLead = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await crmApi.leads.getById(params.id as string);
      setLead(res.data?.data || res.data);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load lead');
    } finally {
      setLoading(false);
    }
  }, [params.id]);

  const fetchRelatedData = useCallback(async () => {
    try {
      const [actRes, noteRes] = await Promise.all([
        crmApi.activities.list({ leadId: params.id, page: 0, size: 50 }).catch(() => ({ data: { content: [] } })),
        crmApi.notes.list({ leadId: params.id, page: 0, size: 50 }).catch(() => ({ data: { content: [] } })),
      ]);
      setActivities(actRes.data?.data?.content || actRes.data?.content || []);
      setNotes(noteRes.data?.data?.content || noteRes.data?.content || []);
    } catch {
      // silently fail
    }
  }, [params.id]);

  useEffect(() => {
    fetchLead();
    fetchRelatedData();
  }, [fetchLead, fetchRelatedData]);

  const handleEdit = async (values: any) => {
    try {
      await crmApi.leads.update(params.id as string, values);
      message.success('Lead updated');
      setEditModalOpen(false);
      fetchLead();
    } catch (err: any) {
      message.error(err?.response?.data?.message || 'Update failed');
    }
  };

  const handleConvert = async (values: any) => {
    try {
      await crmApi.leads.convert(params.id as string, values);
      message.success('Lead converted to opportunity');
      setConvertModalOpen(false);
      router.push('/crm');
    } catch (err: any) {
      message.error(err?.response?.data?.message || 'Conversion failed');
    }
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" tip="Loading lead..." /></div>;
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={fetchLead}>Retry</Button>} />;
  }

  if (!lead) return <Empty description="Lead not found" />;

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
        <Title level={4} style={{ margin: 0 }}>Lead Details</Title>
        <Tag color={leadStatusColors[lead.status] || 'blue'}>{lead.status}</Tag>
      </Space>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card
            title={`${lead.firstName || ''} ${lead.lastName || ''}`}
            extra={
              <Space>
                <Button icon={<EditOutlined />} onClick={() => setEditModalOpen(true)}>Edit</Button>
                <Button icon={<SwapOutlined />} type="primary" onClick={() => setConvertModalOpen(true)}>Convert to Opportunity</Button>
              </Space>
            }
          >
            <Descriptions column={{ xs: 1, sm: 2 }} bordered size="small">
              <Descriptions.Item label="First Name">{lead.firstName || '-'}</Descriptions.Item>
              <Descriptions.Item label="Last Name">{lead.lastName || '-'}</Descriptions.Item>
              <Descriptions.Item label={<><MailOutlined /> Email</>}>{lead.email || '-'}</Descriptions.Item>
              <Descriptions.Item label={<><PhoneOutlined /> Phone</>}>{lead.phone || '-'}</Descriptions.Item>
              <Descriptions.Item label={<><BankOutlined /> Company</>}>{lead.company || '-'}</Descriptions.Item>
              <Descriptions.Item label="Source"><Tag>{lead.source || '-'}</Tag></Descriptions.Item>
              <Descriptions.Item label="Status"><Tag color={leadStatusColors[lead.status] || 'blue'}>{lead.status}</Tag></Descriptions.Item>
              <Descriptions.Item label="Assigned To"><UserOutlined /> {lead.assignedTo || '-'}</Descriptions.Item>
              <Descriptions.Item label="Created">{lead.createdAt ? new Date(lead.createdAt).toLocaleString('vi-VN') : '-'}</Descriptions.Item>
              <Descriptions.Item label="Updated">{lead.updatedAt ? new Date(lead.updatedAt).toLocaleString('vi-VN') : '-'}</Descriptions.Item>
              <Descriptions.Item label="Notes" span={2}>{lead.notes || '-'}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title="Activity Timeline">
            {timeline.length === 0 ? (
              <Empty description="No activities yet" />
            ) : (
              <Timeline
                items={timeline.map((item: any, idx: number) => ({
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
        title="Edit Lead"
        open={editModalOpen}
        onCancel={() => setEditModalOpen(false)}
        onOk={() => {
          const form = document.querySelector('.edit-lead-form')?.closest('form');
          form?.dispatchEvent(new Event('submit', { cancelable: true }));
        }}
        okText="Save"
      >
        <Form
          className="edit-lead-form"
          layout="vertical"
          onFinish={handleEdit}
          initialValues={lead}
        >
          <Form.Item name="firstName" label="First Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="lastName" label="Last Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="email" label="Email" rules={[{ type: 'email' }]}><Input /></Form.Item>
          <Form.Item name="phone" label="Phone"><Input /></Form.Item>
          <Form.Item name="company" label="Company"><Input /></Form.Item>
          <Form.Item name="status" label="Status"><Select options={leadStatusOptions} /></Form.Item>
          <Form.Item name="source" label="Source"><Select options={sourceOptions} /></Form.Item>
          <Form.Item name="notes" label="Notes"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`Convert Lead: ${lead.firstName || ''} ${lead.lastName || ''}`}
        open={convertModalOpen}
        onCancel={() => setConvertModalOpen(false)}
        onOk={() => {
          const form = document.querySelector('.convert-lead-form')?.closest('form');
          form?.dispatchEvent(new Event('submit', { cancelable: true }));
        }}
        okText="Convert"
      >
        <Form className="convert-lead-form" layout="vertical" onFinish={handleConvert}>
          <Form.Item name="title" label="Opportunity Title" rules={[{ required: true }]}>
            <Input defaultValue={`${lead.firstName || ''} ${lead.lastName || ''} - Opportunity`} />
          </Form.Item>
          <Form.Item name="stage" label="Stage" initialValue="PROSPECTING">
            <Select options={['PROSPECTING', 'QUALIFICATION', 'NEEDS_ANALYSIS', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST'].map(s => ({ value: s, label: s }))} />
          </Form.Item>
          <Form.Item name="value" label="Value ($)"><Input type="number" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
