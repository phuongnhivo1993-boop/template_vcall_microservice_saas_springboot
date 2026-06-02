'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Table, Card, Tabs, Select, Tag, Typography, Space, Button, Modal, Form,
  Input, message, Row, Col, Statistic, DatePicker, Tooltip, Popconfirm
} from 'antd';
import {
  PlusOutlined, TeamOutlined, DollarOutlined, PhoneOutlined,
  EditOutlined, DeleteOutlined, SwapOutlined, CheckCircleOutlined,
  SearchOutlined
} from '@ant-design/icons';
import { crmApi } from '@/lib/api';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const leadStatusColors: Record<string, string> = {
  NEW: 'blue', CONTACTED: 'cyan', QUALIFIED: 'green',
  PROPOSAL: 'orange', NEGOTIATION: 'yellow', CLOSED_WON: 'green',
  CLOSED_LOST: 'red', DISQUALIFIED: 'default',
};

const oppStageColors: Record<string, string> = {
  PROSPECTING: 'blue', QUALIFICATION: 'cyan', NEEDS_ANALYSIS: 'purple',
  PROPOSAL: 'orange', NEGOTIATION: 'yellow', CLOSED_WON: 'green', CLOSED_LOST: 'red',
};

const activityTypeColors: Record<string, string> = {
  CALL: 'green', EMAIL: 'blue', MEETING: 'purple', TASK: 'orange', NOTE: 'default',
};

export default function CrmPage() {
  const [activeTab, setActiveTab] = useState('leads');
  const [leads, setLeads] = useState<any[]>([]);
  const [opportunities, setOpportunities] = useState<any[]>([]);
  const [activities, setActivities] = useState<any[]>([]);
  const [notes, setNotes] = useState<any[]>([]);
  const [loading, setLoading] = useState({ leads: false, opportunities: false, activities: false, notes: false });
  const [leadSearch, setLeadSearch] = useState('');
  const [oppSearch, setOppSearch] = useState('');
  const [activitySearch, setActivitySearch] = useState('');
  const [noteSearch, setNoteSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState<'lead' | 'opportunity' | 'activity' | 'note'>('lead');
  const [editingItem, setEditingItem] = useState<any>(null);
  const [form] = Form.useForm();
  const [convertModalOpen, setConvertModalOpen] = useState(false);
  const [convertingLead, setConvertingLead] = useState<any>(null);

  const fetchData = useCallback(async () => {
    try {
      const [leadsRes, oppsRes, actsRes, notesRes] = await Promise.all([
        crmApi.leads.list({ page: 0, size: 100 }),
        crmApi.opportunities.list({ page: 0, size: 100 }),
        crmApi.activities.list({ page: 0, size: 100 }),
        crmApi.notes.list({ page: 0, size: 100 }),
      ]);
      setLeads(leadsRes.data?.data?.content || leadsRes.data?.content || []);
      setOpportunities(oppsRes.data?.data?.content || oppsRes.data?.content || []);
      setActivities(actsRes.data?.data?.content || actsRes.data?.content || []);
      setNotes(notesRes.data?.data?.content || notesRes.data?.content || []);
    } catch (err) {
      message.error('Failed to load CRM data');
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = (type: string) => {
    setModalType(type as any);
    setEditingItem(null);
    form.resetFields();
    setModalOpen(true);
  };

  const handleEdit = (item: any, type: string) => {
    setModalType(type as any);
    setEditingItem(item);
    form.setFieldsValue(item);
    setModalOpen(true);
  };

  const handleDelete = async (id: string, type: string) => {
    try {
      if (type === 'lead') await crmApi.leads.delete(id);
      else if (type === 'opportunity') await crmApi.opportunities.delete(id);
      else if (type === 'activity') await crmApi.activities.delete(Number(id));
      else if (type === 'note') await crmApi.notes.delete(Number(id));
      message.success('Deleted successfully');
      fetchData();
    } catch { message.error('Delete failed'); }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (modalType === 'lead') {
        if (editingItem) await crmApi.leads.update(editingItem.id, values);
        else await crmApi.leads.create(values);
      } else if (modalType === 'opportunity') {
        if (editingItem) await crmApi.opportunities.update(editingItem.id, values);
        else await crmApi.opportunities.create(values);
      } else if (modalType === 'activity') {
        if (editingItem) await crmApi.activities.update(editingItem.id, values);
        else await crmApi.activities.create(values);
      } else if (modalType === 'note') {
        if (editingItem) await crmApi.notes.update(editingItem.id, values);
        else await crmApi.notes.create(values);
      }
      message.success('Saved successfully');
      setModalOpen(false);
      fetchData();
    } catch { message.error('Validation failed'); }
  };

  const handleConvert = async () => {
    try {
      const values = await form.validateFields();
      await crmApi.leads.convert(convertingLead.id, values);
      message.success('Lead converted to opportunity');
      setConvertModalOpen(false);
      fetchData();
    } catch { message.error('Conversion failed'); }
  };

  const leadColumns = [
    { title: 'Name', key: 'name', render: (_: any, r: any) => <a>{r.firstName} {r.lastName}</a> },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Company', dataIndex: 'company', key: 'company' },
    { title: 'Phone', dataIndex: 'phone', key: 'phone' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (s: string) => <Tag color={leadStatusColors[s] || 'blue'}>{s}</Tag> },
    { title: 'Source', dataIndex: 'source', key: 'source' },
    { title: 'Created', dataIndex: 'createdAt', key: 'createdAt',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          <Tooltip title="Convert to Opportunity">
            <Button size="small" icon={<SwapOutlined />} onClick={() => { setConvertingLead(r); form.resetFields(); setConvertModalOpen(true); }} />
          </Tooltip>
          <Tooltip title="Edit"><Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(r, 'lead')} /></Tooltip>
          <Popconfirm title="Delete this lead?" onConfirm={() => handleDelete(r.id, 'lead')}>
            <Tooltip title="Delete"><Button size="small" danger icon={<DeleteOutlined />} /></Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const oppColumns = [
    { title: 'Title', dataIndex: 'title', key: 'title' },
    { title: 'Stage', dataIndex: 'stage', key: 'stage',
      render: (s: string) => <Tag color={oppStageColors[s] || 'blue'}>{s}</Tag> },
    { title: 'Value', dataIndex: 'value', key: 'value',
      render: (v: number) => v ? `$${v.toLocaleString()}` : '-' },
    { title: 'Probability', dataIndex: 'probability', key: 'probability',
      render: (p: number) => p ? `${p}%` : '-' },
    { title: 'Expected Close', dataIndex: 'expectedCloseDate', key: 'expectedCloseDate',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Created', dataIndex: 'createdAt', key: 'createdAt',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          <Tooltip title="Edit"><Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(r, 'opportunity')} /></Tooltip>
          <Popconfirm title="Delete this opportunity?" onConfirm={() => handleDelete(r.id, 'opportunity')}>
            <Tooltip title="Delete"><Button size="small" danger icon={<DeleteOutlined />} /></Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const activityColumns = [
    { title: 'Type', dataIndex: 'type', key: 'type',
      render: (t: string) => <Tag color={activityTypeColors[t] || 'default'}>{t}</Tag> },
    { title: 'Subject', dataIndex: 'subject', key: 'subject' },
    { title: 'Description', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: 'Assigned To', dataIndex: 'assignedToName', key: 'assignedToName' },
    { title: 'Date', dataIndex: 'startDate', key: 'startDate',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          <Tooltip title="Edit"><Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(r, 'activity')} /></Tooltip>
          <Popconfirm title="Delete?" onConfirm={() => handleDelete(r.id, 'activity')}>
            <Tooltip title="Delete"><Button size="small" danger icon={<DeleteOutlined />} /></Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const noteColumns = [
    { title: 'Title', dataIndex: 'title', key: 'title' },
    { title: 'Content', dataIndex: 'content', key: 'content', ellipsis: true },
    { title: 'Customer', dataIndex: 'customerId', key: 'customerId' },
    { title: 'Created', dataIndex: 'createdAt', key: 'createdAt',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          <Tooltip title="Edit"><Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(r, 'note')} /></Tooltip>
          <Popconfirm title="Delete?" onConfirm={() => handleDelete(r.id, 'note')}>
            <Tooltip title="Delete"><Button size="small" danger icon={<DeleteOutlined />} /></Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const renderForm = () => {
    if (modalType === 'lead') return (
      <>
        <Form.Item name="firstName" label="First Name" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="lastName" label="Last Name" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="email" label="Email" rules={[{ type: 'email' }]}><Input /></Form.Item>
        <Form.Item name="phone" label="Phone"><Input /></Form.Item>
        <Form.Item name="company" label="Company"><Input /></Form.Item>
        <Form.Item name="status" label="Status" initialValue="NEW">
          <Select options={['NEW','CONTACTED','QUALIFIED','PROPOSAL','NEGOTIATION','CLOSED_WON','CLOSED_LOST','DISQUALIFIED'].map(s => ({ value: s, label: s }))} />
        </Form.Item>
        <Form.Item name="source" label="Source" initialValue="MANUAL">
          <Select options={['MANUAL','REFERRAL','WEBSITE','CALL','EMAIL','SOCIAL_MEDIA','PARTNER','OTHER'].map(s => ({ value: s, label: s }))} />
        </Form.Item>
      </>
    );
    if (modalType === 'opportunity') return (
      <>
        <Form.Item name="title" label="Title" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="stage" label="Stage" initialValue="PROSPECTING">
          <Select options={['PROSPECTING','QUALIFICATION','NEEDS_ANALYSIS','PROPOSAL','NEGOTIATION','CLOSED_WON','CLOSED_LOST'].map(s => ({ value: s, label: s }))} />
        </Form.Item>
        <Form.Item name="value" label="Value ($)"><Input type="number" /></Form.Item>
        <Form.Item name="probability" label="Probability (%)"><Input type="number" /></Form.Item>
        <Form.Item name="expectedCloseDate" label="Expected Close Date"><Input type="date" /></Form.Item>
      </>
    );
    if (modalType === 'activity') return (
      <>
        <Form.Item name="type" label="Type" rules={[{ required: true }]}>
          <Select options={['CALL','EMAIL','MEETING','TASK','NOTE'].map(s => ({ value: s, label: s }))} />
        </Form.Item>
        <Form.Item name="subject" label="Subject" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="description" label="Description"><Input.TextArea rows={3} /></Form.Item>
        <Form.Item name="startDate" label="Start Date"><Input type="date" /></Form.Item>
        <Form.Item name="endDate" label="End Date"><Input type="date" /></Form.Item>
      </>
    );
    return (
      <>
        <Form.Item name="title" label="Title" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="content" label="Content" rules={[{ required: true }]}><Input.TextArea rows={4} /></Form.Item>
        <Form.Item name="customerId" label="Customer ID"><Input /></Form.Item>
      </>
    );
  };

  const filteredLeads = leads.filter(l =>
    !leadSearch ||
    (l.firstName || '').toLowerCase().includes(leadSearch.toLowerCase()) ||
    (l.lastName || '').toLowerCase().includes(leadSearch.toLowerCase()) ||
    (l.email || '').toLowerCase().includes(leadSearch.toLowerCase()) ||
    (l.company || '').toLowerCase().includes(leadSearch.toLowerCase()) ||
    (l.phone || '').includes(leadSearch)
  );

  const filteredOpps = opportunities.filter(o =>
    !oppSearch ||
    (o.title || '').toLowerCase().includes(oppSearch.toLowerCase()) ||
    (o.stage || '').toLowerCase().includes(oppSearch.toLowerCase())
  );

  const filteredActivities = activities.filter(a =>
    !activitySearch ||
    (a.subject || '').toLowerCase().includes(activitySearch.toLowerCase()) ||
    (a.type || '').toLowerCase().includes(activitySearch.toLowerCase())
  );

  const filteredNotes = notes.filter(n =>
    !noteSearch ||
    (n.title || '').toLowerCase().includes(noteSearch.toLowerCase()) ||
    (n.content || '').toLowerCase().includes(noteSearch.toLowerCase())
  );

  const stats = {
    totalLeads: leads.length,
    qualifiedLeads: leads.filter(l => l.status === 'QUALIFIED' || l.status === 'PROPOSAL' || l.status === 'NEGOTIATION').length,
    totalOpps: opportunities.length,
    wonOpps: opportunities.filter(o => o.stage === 'CLOSED_WON').length,
  };

  return (
    <div>
      <Title level={3}>CRM</Title>
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}><Card><Statistic title="Total Leads" value={stats.totalLeads} prefix={<TeamOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Qualified Leads" value={stats.qualifiedLeads} valueStyle={{ color: '#52c41a' }} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Opportunities" value={stats.totalOpps} prefix={<DollarOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Won" value={stats.wonOpps} valueStyle={{ color: '#52c41a' }} prefix={<CheckCircleOutlined />} /></Card></Col>
      </Row>

      <Card>
        <Tabs activeKey={activeTab} onChange={setActiveTab} tabBarExtraContent={
          <Space>
            {activeTab === 'leads' && (
              <Input prefix={<SearchOutlined />} placeholder="Search leads..." value={leadSearch} onChange={e => setLeadSearch(e.target.value)} style={{ width: 200 }} allowClear />
            )}
            {activeTab === 'opportunities' && (
              <Input prefix={<SearchOutlined />} placeholder="Search opportunities..." value={oppSearch} onChange={e => setOppSearch(e.target.value)} style={{ width: 200 }} allowClear />
            )}
            {activeTab === 'activities' && (
              <Input prefix={<SearchOutlined />} placeholder="Search activities..." value={activitySearch} onChange={e => setActivitySearch(e.target.value)} style={{ width: 200 }} allowClear />
            )}
            {activeTab === 'notes' && (
              <Input prefix={<SearchOutlined />} placeholder="Search notes..." value={noteSearch} onChange={e => setNoteSearch(e.target.value)} style={{ width: 200 }} allowClear />
            )}
            <Button type="primary" icon={<PlusOutlined />} onClick={() => handleCreate(activeTab === 'leads' ? 'lead' : activeTab === 'opportunities' ? 'opportunity' : activeTab === 'activities' ? 'activity' : 'note')}>
              Add {activeTab === 'leads' ? 'Lead' : activeTab === 'opportunities' ? 'Opportunity' : activeTab === 'activities' ? 'Activity' : 'Note'}
            </Button>
          </Space>
        } items={[
          { key: 'leads', label: `Leads (${filteredLeads.length})`, children: <Table rowKey="id" columns={leadColumns} dataSource={filteredLeads} loading={loading.leads} pagination={{ pageSize: 10 }} /> },
          { key: 'opportunities', label: `Opportunities (${filteredOpps.length})`, children: <Table rowKey="id" columns={oppColumns} dataSource={filteredOpps} loading={loading.opportunities} pagination={{ pageSize: 10 }} /> },
          { key: 'activities', label: `Activities (${filteredActivities.length})`, children: <Table rowKey="id" columns={activityColumns} dataSource={filteredActivities} loading={loading.activities} pagination={{ pageSize: 10 }} /> },
          { key: 'notes', label: `Notes (${filteredNotes.length})`, children: <Table rowKey="id" columns={noteColumns} dataSource={filteredNotes} loading={loading.notes} pagination={{ pageSize: 10 }} /> },
        ]} />
      </Card>

      <Modal title={editingItem ? 'Edit' : 'Create'} open={modalOpen} onOk={handleSubmit} onCancel={() => setModalOpen(false)}>
        <Form form={form} layout="vertical">{renderForm()}</Form>
      </Modal>

      <Modal title={`Convert Lead: ${convertingLead?.firstName} ${convertingLead?.lastName}`} open={convertModalOpen} onOk={handleConvert} onCancel={() => setConvertModalOpen(false)}>
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="Opportunity Title" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="stage" label="Stage" initialValue="PROSPECTING">
            <Select options={['PROSPECTING','QUALIFICATION','NEEDS_ANALYSIS','PROPOSAL','NEGOTIATION','CLOSED_WON','CLOSED_LOST'].map(s => ({ value: s, label: s }))} />
          </Form.Item>
          <Form.Item name="value" label="Value ($)"><Input type="number" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
