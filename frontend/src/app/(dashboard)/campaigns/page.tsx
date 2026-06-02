'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Table, Card, Tabs, Tag, Typography, Space, Button, Modal, Form,
  Input, Select, message, Row, Col, Statistic, Tooltip, Popconfirm, Upload, Descriptions
} from 'antd';
import {
  PlusOutlined, PlayCircleOutlined, PauseCircleOutlined, StopOutlined,
  UploadOutlined, TeamOutlined, BarChartOutlined
} from '@ant-design/icons';
import { campaignsApi } from '@/lib/api';

const { Title, Text } = Typography;

const campaignStatusColors: Record<string, string> = {
  DRAFT: 'default', SCHEDULED: 'blue', RUNNING: 'green',
  PAUSED: 'orange', COMPLETED: 'purple', CANCELLED: 'red',
};

const memberStatusColors: Record<string, string> = {
  PENDING: 'default', CALLING: 'processing', CONTACTED: 'green',
  NOT_CONTACTED: 'orange', FAILED: 'red', COMPLETED: 'green',
};

export default function CampaignsPage() {
  const [activeTab, setActiveTab] = useState('campaigns');
  const [campaigns, setCampaigns] = useState<any[]>([]);
  const [selectedCampaign, setSelectedCampaign] = useState<any>(null);
  const [members, setMembers] = useState<any[]>([]);
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCampaign, setEditingCampaign] = useState<any>(null);
  const [memberModalOpen, setMemberModalOpen] = useState(false);
  const [form] = Form.useForm();

  const fetchCampaigns = useCallback(async () => {
    setLoading(true);
    try {
      const res = await campaignsApi.list({ page: 0, size: 100 });
      setCampaigns(res.data?.data?.content || res.data?.content || []);
    } catch { message.error('Failed to load campaigns'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { fetchCampaigns(); }, [fetchCampaigns]);

  const fetchCampaignDetail = async (campaign: any) => {
    setSelectedCampaign(campaign);
    setDetailLoading(true);
    try {
      const [membersRes, resultsRes] = await Promise.all([
        campaignsApi.getMembers(campaign.id, { page: 0, size: 100 }),
        campaignsApi.getResults(campaign.id, { page: 0, size: 100 }),
      ]);
      setMembers(membersRes.data?.data?.content || membersRes.data?.content || []);
      setResults(resultsRes.data?.data?.content || resultsRes.data?.content || []);
    } catch { message.error('Failed to load campaign details'); }
    finally { setDetailLoading(false); }
  };

  const handleCreate = () => {
    setEditingCampaign(null);
    form.resetFields();
    setModalOpen(true);
  };

  const handleEdit = (campaign: any) => {
    setEditingCampaign(campaign);
    form.setFieldsValue(campaign);
    setModalOpen(true);
  };

  const handleDelete = async (id: string) => {
    try {
      await campaignsApi.delete(id);
      message.success('Campaign deleted');
      fetchCampaigns();
    } catch { message.error('Delete failed'); }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingCampaign) await campaignsApi.update(editingCampaign.id, values);
      else await campaignsApi.create(values);
      message.success('Campaign saved');
      setModalOpen(false);
      fetchCampaigns();
    } catch { message.error('Validation failed'); }
  };

  const handleStatusAction = async (id: string, action: 'start' | 'pause' | 'stop') => {
    try {
      if (action === 'start') await campaignsApi.start(id);
      else if (action === 'pause') await campaignsApi.pause(id);
      else await campaignsApi.stop(id);
      message.success(`Campaign ${action}ed`);
      fetchCampaigns();
    } catch { message.error(`Failed to ${action} campaign`); }
  };

  const handleImport = async (file: File) => {
    if (!selectedCampaign) return;
    const formData = new FormData();
    formData.append('file', file);
    try {
      await campaignsApi.importMembers(selectedCampaign.id, formData);
      message.success('Members imported');
      fetchCampaignDetail(selectedCampaign);
    } catch { message.error('Import failed'); }
  };

  const campaignColumns = [
    { title: 'Name', dataIndex: 'name', key: 'name', render: (n: string, r: any) => <a onClick={() => { setActiveTab('detail'); fetchCampaignDetail(r); }}>{n}</a> },
    { title: 'Type', dataIndex: 'type', key: 'type' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={campaignStatusColors[s] || 'default'}>{s}</Tag> },
    { title: 'Strategy', dataIndex: 'strategy', key: 'strategy' },
    { title: 'Start', dataIndex: 'scheduleStart', key: 'scheduleStart',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'End', dataIndex: 'scheduleEnd', key: 'scheduleEnd',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    {
      title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          {r.status === 'DRAFT' || r.status === 'PAUSED' ? (
            <Tooltip title="Start"><Button size="small" type="primary" icon={<PlayCircleOutlined />} onClick={() => handleStatusAction(r.id, 'start')} /></Tooltip>
          ) : null}
          {r.status === 'RUNNING' ? (
            <>
              <Tooltip title="Pause"><Button size="small" icon={<PauseCircleOutlined />} onClick={() => handleStatusAction(r.id, 'pause')} /></Tooltip>
              <Tooltip title="Stop"><Button size="small" danger icon={<StopOutlined />} onClick={() => handleStatusAction(r.id, 'stop')} /></Tooltip>
            </>
          ) : null}
          <Tooltip title="Edit"><Button size="small" onClick={() => handleEdit(r)}>Edit</Button></Tooltip>
          <Popconfirm title="Delete campaign?" onConfirm={() => handleDelete(r.id)}>
            <Tooltip title="Delete"><Button size="small" danger>Delete</Button></Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const memberColumns = [
    { title: 'Name', dataIndex: 'contactName', key: 'contactName' },
    { title: 'Phone', dataIndex: 'contactPhone', key: 'contactPhone' },
    { title: 'Email', dataIndex: 'contactEmail', key: 'contactEmail' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={memberStatusColors[s] || 'default'}>{s}</Tag> },
    { title: 'Priority', dataIndex: 'priority', key: 'priority' },
    { title: 'Called At', dataIndex: 'lastCalledAt', key: 'lastCalledAt',
      render: (d: string) => d ? new Date(d).toLocaleString() : '-' },
  ];

  const resultColumns = [
    { title: 'Member', dataIndex: 'campaignMemberId', key: 'campaignMemberId' },
    { title: 'Result', dataIndex: 'resultType', key: 'resultType',
      render: (t: string) => <Tag>{t}</Tag> },
    { title: 'Notes', dataIndex: 'notes', key: 'notes', ellipsis: true },
    { title: 'Duration', dataIndex: 'duration', key: 'duration',
      render: (d: number) => d ? `${d}s` : '-' },
    { title: 'Agent', dataIndex: 'agentId', key: 'agentId' },
    { title: 'Date', dataIndex: 'createdAt', key: 'createdAt',
      render: (d: string) => d ? new Date(d).toLocaleString() : '-' },
  ];

  const totalMembers = members.length;
  const completedMembers = members.filter((m: any) => m.status === 'COMPLETED' || m.status === 'CONTACTED').length;

  return (
    <div>
      <Title level={3}>Campaigns</Title>

      <Card>
        <Tabs activeKey={activeTab} onChange={(key) => { setActiveTab(key); if (key === 'campaigns') setSelectedCampaign(null); }}
          tabBarExtraContent={activeTab === 'campaigns' ? <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>Create Campaign</Button> : undefined}
          items={[
            { key: 'campaigns', label: `Campaigns (${campaigns.length})`,
              children: <Table rowKey="id" columns={campaignColumns} dataSource={campaigns} loading={loading} pagination={{ pageSize: 10 }} /> },
            ...(selectedCampaign ? [{
              key: 'detail', label: `Detail: ${selectedCampaign.name}`,
              children: detailLoading ? <p>Loading...</p> : (
                <div>
                  <Descriptions title="Campaign Info" bordered column={2} style={{ marginBottom: 16 }}>
                    <Descriptions.Item label="Name">{selectedCampaign.name}</Descriptions.Item>
                    <Descriptions.Item label="Status"><Tag color={campaignStatusColors[selectedCampaign.status]}>{selectedCampaign.status}</Tag></Descriptions.Item>
                    <Descriptions.Item label="Type">{selectedCampaign.type}</Descriptions.Item>
                    <Descriptions.Item label="Strategy">{selectedCampaign.strategy}</Descriptions.Item>
                    <Descriptions.Item label="Schedule">{selectedCampaign.scheduleStart ? new Date(selectedCampaign.scheduleStart).toLocaleDateString() : '-'} → {selectedCampaign.scheduleEnd ? new Date(selectedCampaign.scheduleEnd).toLocaleDateString() : '-'}</Descriptions.Item>
                    <Descriptions.Item label="Description">{selectedCampaign.description || '-'}</Descriptions.Item>
                  </Descriptions>

                  <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
                    <Col span={8}><Card><Statistic title="Total Members" value={totalMembers} prefix={<TeamOutlined />} /></Card></Col>
                    <Col span={8}><Card><Statistic title="Completed" value={completedMembers} suffix={`/ ${totalMembers}`} valueStyle={{ color: '#52c41a' }} /></Card></Col>
                    <Col span={8}><Card><Statistic title="Results" value={results.length} prefix={<BarChartOutlined />} /></Card></Col>
                  </Row>

                  <Space style={{ marginBottom: 16 }}>
                    <Upload accept=".csv,.json" showUploadList={false} customRequest={({ file }) => handleImport(file as File)}>
                      <Button icon={<UploadOutlined />}>Import Members</Button>
                    </Upload>
                    <Button icon={<PlusOutlined />} onClick={() => setMemberModalOpen(true)}>Add Member</Button>
                  </Space>

                  <Tabs items={[
                    { key: 'members', label: `Members (${members.length})`, children: <Table rowKey="id" columns={memberColumns} dataSource={members} pagination={{ pageSize: 10 }} /> },
                    { key: 'results', label: `Results (${results.length})`, children: <Table rowKey="id" columns={resultColumns} dataSource={results} pagination={{ pageSize: 10 }} /> },
                  ]} />
                </div>
              ),
            }] : []),
          ]} />
      </Card>

      <Modal title={editingCampaign ? 'Edit Campaign' : 'Create Campaign'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)} width={600}>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="description" label="Description"><Input.TextArea rows={3} /></Form.Item>
          <Form.Item name="type" label="Type" rules={[{ required: true }]}>
            <Select options={['OUTBOUND','INBOUND','PREVIEW','PREDICTIVE','PROGRESSIVE'].map(t => ({ value: t, label: t }))} />
          </Form.Item>
          <Form.Item name="strategy" label="Strategy" initialValue="SEQUENTIAL">
            <Select options={['SEQUENTIAL','RANDOM','PRIORITY'].map(s => ({ value: s, label: s }))} />
          </Form.Item>
          <Form.Item name="scheduleStart" label="Start Date"><Input type="date" /></Form.Item>
          <Form.Item name="scheduleEnd" label="End Date"><Input type="date" /></Form.Item>
        </Form>
      </Modal>

      <Modal title="Add Member" open={memberModalOpen} onOk={async () => { try { await form.validateFields(); await campaignsApi.addMember(selectedCampaign.id, form.getFieldsValue()); message.success('Member added'); setMemberModalOpen(false); fetchCampaignDetail(selectedCampaign); } catch { message.error('Failed'); } }} onCancel={() => setMemberModalOpen(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="contactName" label="Name"><Input /></Form.Item>
          <Form.Item name="contactPhone" label="Phone" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="contactEmail" label="Email"><Input /></Form.Item>
          <Form.Item name="priority" label="Priority"><Input type="number" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
