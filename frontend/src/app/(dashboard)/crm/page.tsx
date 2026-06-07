'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card, Tabs, Select, Tag, Typography, Space, Button, Modal, Form,
  Input, message, Row, Col, Statistic, Tooltip
} from 'antd';
import {
  PlusOutlined, TeamOutlined, DollarOutlined, PhoneOutlined,
  EditOutlined, DeleteOutlined, SwapOutlined, CheckCircleOutlined, CopyOutlined, EyeOutlined
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { crmApi } from '@/lib/api';

const { Title, Text } = Typography;

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

const leadStatusOptions = ['NEW', 'CONTACTED', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST', 'DISQUALIFIED'].map(s => ({ value: s, label: s }));
const sourceOptions = ['MANUAL', 'REFERRAL', 'WEBSITE', 'CALL', 'EMAIL', 'SOCIAL_MEDIA', 'PARTNER', 'OTHER'].map(s => ({ value: s, label: s }));
const oppStageOptions = ['PROSPECTING', 'QUALIFICATION', 'NEEDS_ANALYSIS', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON', 'CLOSED_LOST'].map(s => ({ value: s, label: s }));
const activityTypeOptions = ['CALL', 'EMAIL', 'MEETING', 'TASK', 'NOTE'].map(s => ({ value: s, label: s }));

export default function CrmPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('leads');
  const [leads, setLeads] = useState<any[]>([]);
  const [opportunities, setOpportunities] = useState<any[]>([]);
  const [activities, setActivities] = useState<any[]>([]);
  const [notes, setNotes] = useState<any[]>([]);
  const [loading, setLoading] = useState({ leads: false, opportunities: false, activities: false, notes: false });
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState<'lead' | 'opportunity' | 'activity' | 'note'>('lead');
  const [editingItem, setEditingItem] = useState<any>(null);
  const [convertModalOpen, setConvertModalOpen] = useState(false);
  const [convertingLead, setConvertingLead] = useState<any>(null);
  const [filters, setFilters] = useState<Record<string, any>>({});
  const [selectedLeadKeys, setSelectedLeadKeys] = useState<string[]>([]);
  const [selectedOppKeys, setSelectedOppKeys] = useState<string[]>([]);
  const [selectedActivityKeys, setSelectedActivityKeys] = useState<number[]>([]);
  const [selectedNoteKeys, setSelectedNoteKeys] = useState<number[]>([]);

  const setTabLoading = (tab: string, val: boolean) =>
    setLoading((prev) => ({ ...prev, [tab]: val }));

  const fetchData = useCallback(async (searchParams?: Record<string, any>) => {
    const params = { page: 0, size: 100, ...searchParams };
    setError(null);
    try {
      setTabLoading('leads', true);
      const leadsRes = await crmApi.leads.list(params);
      setLeads(leadsRes.data?.data?.content || leadsRes.data?.content || []);
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Failed to load leads');
    } finally { setTabLoading('leads', false); }

    try {
      setTabLoading('opportunities', true);
      const oppsRes = await crmApi.opportunities.list(params);
      setOpportunities(oppsRes.data?.data?.content || oppsRes.data?.content || []);
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Failed to load opportunities');
    } finally { setTabLoading('opportunities', false); }

    try {
      setTabLoading('activities', true);
      const actsRes = await crmApi.activities.list(params);
      setActivities(actsRes.data?.data?.content || actsRes.data?.content || []);
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Failed to load activities');
    } finally { setTabLoading('activities', false); }

    try {
      setTabLoading('notes', true);
      const notesRes = await crmApi.notes.list(params);
      setNotes(notesRes.data?.data?.content || notesRes.data?.content || []);
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Failed to load notes');
    } finally { setTabLoading('notes', false); }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleCreate = (type: string) => {
    setModalType(type as any);
    setEditingItem(null);
    setModalOpen(true);
  };

  const handleEdit = (item: any, type: string) => {
    setModalType(type as any);
    setEditingItem(item);
    setModalOpen(true);
  };

  const handleDuplicate = (item: any, type: string) => {
    setModalType(type as any);
    setEditingItem({ ...item, id: '' });
    setModalOpen(true);
  };

  const handleBulkDeleteLeads = () => {
    Modal.confirm({
      title: 'Xóa nhiều lead',
      content: `Bạn có chắc chắn muốn xóa ${selectedLeadKeys.length} lead đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await crmApi.leads.bulkDelete(selectedLeadKeys);
          message.success(`Đã xóa ${selectedLeadKeys.length} lead`);
          setSelectedLeadKeys([]);
          fetchData(filters);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeleteOpps = () => {
    Modal.confirm({
      title: 'Xóa nhiều cơ hội',
      content: `Bạn có chắc chắn muốn xóa ${selectedOppKeys.length} cơ hội đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await crmApi.opportunities.bulkDelete(selectedOppKeys);
          message.success(`Đã xóa ${selectedOppKeys.length} cơ hội`);
          setSelectedOppKeys([]);
          fetchData(filters);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeleteActivities = () => {
    Modal.confirm({
      title: 'Xóa nhiều hoạt động',
      content: `Bạn có chắc chắn muốn xóa ${selectedActivityKeys.length} hoạt động đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await crmApi.activities.bulkDelete(selectedActivityKeys);
          message.success(`Đã xóa ${selectedActivityKeys.length} hoạt động`);
          setSelectedActivityKeys([]);
          fetchData(filters);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeleteNotes = () => {
    Modal.confirm({
      title: 'Xóa nhiều ghi chú',
      content: `Bạn có chắc chắn muốn xóa ${selectedNoteKeys.length} ghi chú đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await crmApi.notes.bulkDelete(selectedNoteKeys);
          message.success(`Đã xóa ${selectedNoteKeys.length} ghi chú`);
          setSelectedNoteKeys([]);
          fetchData(filters);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleDeleteConfirm = (id: string, type: string, label: string) => {
    showDeleteConfirm({
      title: `Delete ${type}`,
      content: `Are you sure you want to delete this ${label}?`,
      onOk: async () => {
        if (type === 'lead') await crmApi.leads.delete(id);
        else if (type === 'opportunity') await crmApi.opportunities.delete(id);
        else if (type === 'activity') await crmApi.activities.delete(Number(id));
        else if (type === 'note') await crmApi.notes.delete(Number(id));
        fetchData();
      },
    });
  };

  const handleFormSubmit = async (values: any) => {
    if (modalType === 'lead') {
      if (editingItem?.id) await crmApi.leads.update(editingItem.id, values);
      else await crmApi.leads.create(values);
    } else if (modalType === 'opportunity') {
      if (editingItem?.id) await crmApi.opportunities.update(editingItem.id, values);
      else await crmApi.opportunities.create(values);
    } else if (modalType === 'activity') {
      if (editingItem?.id) await crmApi.activities.update(editingItem.id, values);
      else await crmApi.activities.create(values);
    } else if (modalType === 'note') {
      if (editingItem?.id) await crmApi.notes.update(editingItem.id, values);
      else await crmApi.notes.create(values);
    }
    setModalOpen(false);
    fetchData();
  };

  const handleConvert = async (values: any) => {
    await crmApi.leads.convert(convertingLead.id, values);
    setConvertModalOpen(false);
    message.success('Lead converted to opportunity');
    fetchData();
  };

  const handleSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') cleaned[key] = val;
    });
    setFilters(cleaned);
    fetchData(cleaned);
  };

  const handleReset = () => {
    setFilters({});
    fetchData();
  };

  const handleExportCsv = async () => {
    try {
      const type = activeTab === 'leads' ? 'leads' : activeTab === 'opportunities' ? 'opportunities' : activeTab === 'activities' ? 'activities' : 'notes';
      const res = await crmApi.exportCsv(type);
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${type}_${new Date().toISOString().slice(0,10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success(`${type} exported`);
    } catch {
      message.error('Export failed');
    }
  };
  const handleExportExcel = async () => {
    try {
      const type = activeTab === 'leads' ? 'leads' : activeTab === 'opportunities' ? 'opportunities' : activeTab === 'activities' ? 'activities' : 'notes';
      const res = await crmApi.exportExcel(type);
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${type}_${new Date().toISOString().slice(0,10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success(`${type} exported`);
    } catch {
      message.error('Export failed');
    }
  };

  const leadColumns: ColumnsType<any> = [
    { title: 'Name', key: 'name', render: (_: any, r: any) => <span style={{ fontWeight: 500 }}>{r.firstName} {r.lastName}</span> },
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
          <Tooltip title="View"><Button size="small" icon={<EyeOutlined />} onClick={() => router.push(`/crm/leads/${r.id}`)} /></Tooltip>
          <Tooltip title="Convert to Opportunity">
            <Button size="small" icon={<SwapOutlined />} onClick={() => { setConvertingLead(r); setConvertModalOpen(true); }} />
          </Tooltip>
          <Tooltip title="Edit"><Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(r, 'lead')} /></Tooltip>
          <Tooltip title="Nhân bản"><Button size="small" icon={<CopyOutlined />} onClick={() => handleDuplicate(r, 'lead')} /></Tooltip>
          <Tooltip title="Delete"><Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteConfirm(r.id, 'lead', 'lead')} /></Tooltip>
        </Space>
      ),
    },
  ];

  const oppColumns: ColumnsType<any> = [
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
          <Tooltip title="View"><Button size="small" icon={<EyeOutlined />} onClick={() => router.push(`/crm/opportunities/${r.id}`)} /></Tooltip>
          <Tooltip title="Edit"><Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(r, 'opportunity')} /></Tooltip>
          <Tooltip title="Delete"><Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteConfirm(r.id, 'opportunity', 'opportunity')} /></Tooltip>
        </Space>
      ),
    },
  ];

  const activityColumns: ColumnsType<any> = [
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
          <Tooltip title="Delete"><Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteConfirm(r.id, 'activity', 'activity')} /></Tooltip>
        </Space>
      ),
    },
  ];

  const noteColumns: ColumnsType<any> = [
    { title: 'Title', dataIndex: 'title', key: 'title' },
    { title: 'Content', dataIndex: 'content', key: 'content', ellipsis: true },
    { title: 'Customer', dataIndex: 'customerId', key: 'customerId' },
    { title: 'Created', dataIndex: 'createdAt', key: 'createdAt',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Actions', key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          <Tooltip title="Edit"><Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(r, 'note')} /></Tooltip>
          <Tooltip title="Delete"><Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteConfirm(r.id, 'note', 'note')} /></Tooltip>
        </Space>
      ),
    },
  ];

  const renderFormFields = () => {
    if (modalType === 'lead') return (
      <>
        <Form.Item name="firstName" label="First Name" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="lastName" label="Last Name" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="email" label="Email" rules={[{ type: 'email' }]}><Input /></Form.Item>
        <Form.Item name="phone" label="Phone"><Input /></Form.Item>
        <Form.Item name="company" label="Company"><Input /></Form.Item>
        <Form.Item name="status" label="Status" initialValue="NEW">
          <Select options={leadStatusOptions} />
        </Form.Item>
        <Form.Item name="source" label="Source" initialValue="MANUAL">
          <Select options={sourceOptions} />
        </Form.Item>
      </>
    );
    if (modalType === 'opportunity') return (
      <>
        <Form.Item name="title" label="Title" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="stage" label="Stage" initialValue="PROSPECTING">
          <Select options={oppStageOptions} />
        </Form.Item>
        <Form.Item name="value" label="Value ($)"><Input type="number" /></Form.Item>
        <Form.Item name="probability" label="Probability (%)"><Input type="number" /></Form.Item>
        <Form.Item name="expectedCloseDate" label="Expected Close Date"><Input type="date" /></Form.Item>
      </>
    );
    if (modalType === 'activity') return (
      <>
        <Form.Item name="type" label="Type" rules={[{ required: true }]}>
          <Select options={activityTypeOptions} />
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

  const searchFields = [
    { name: 'keyword', label: 'Keyword', type: 'input' as const, placeholder: 'Search...' },
    ...(activeTab === 'leads' ? [{
      name: 'status' as const, label: 'Status' as const, type: 'select' as const,
      placeholder: 'Filter by status' as const, options: leadStatusOptions,
    }] : []),
    ...(activeTab === 'opportunities' ? [{
      name: 'stage' as const, label: 'Stage' as const, type: 'select' as const,
      placeholder: 'Filter by stage' as const, options: oppStageOptions,
    }] : []),
    ...(activeTab === 'activities' ? [{
      name: 'type' as const, label: 'Type' as const, type: 'select' as const,
      placeholder: 'Filter by type' as const, options: activityTypeOptions,
    }] : []),
  ];

  const stats = {
    totalLeads: leads.length,
    qualifiedLeads: leads.filter(l => l.status === 'QUALIFIED' || l.status === 'PROPOSAL' || l.status === 'NEGOTIATION').length,
    totalOpps: opportunities.length,
    wonOpps: opportunities.filter(o => o.stage === 'CLOSED_WON').length,
  };

  const currentTabKey = activeTab === 'leads' ? 'lead' : activeTab === 'opportunities' ? 'opportunity' : activeTab === 'activities' ? 'activity' : 'note';

  return (
    <div>
      <Title level={3}>CRM</Title>
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}><Card><Statistic title="Total Leads" value={stats.totalLeads} prefix={<TeamOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Qualified Leads" value={stats.qualifiedLeads} valueStyle={{ color: '#52c41a' }} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Opportunities" value={stats.totalOpps} prefix={<DollarOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Won" value={stats.wonOpps} valueStyle={{ color: '#52c41a' }} prefix={<CheckCircleOutlined />} /></Card></Col>
      </Row>

      <CommonSearch
        key={activeTab}
        fields={searchFields}
        onSearch={handleSearch}
        onReset={handleReset}
        loading={loading[activeTab as keyof typeof loading]}
      />
      <SavedFilters currentValues={filters} onApply={(v) => { setFilters(v); handleSearch(v); }} storageKey="vcall-saved-filters-crm" />

      <Card>
        <Tabs activeKey={activeTab} onChange={setActiveTab} tabBarExtraContent={
          <Button type="primary" icon={<PlusOutlined />} onClick={() => handleCreate(currentTabKey)}>
            Add {activeTab === 'leads' ? 'Lead' : activeTab === 'opportunities' ? 'Opportunity' : activeTab === 'activities' ? 'Activity' : 'Note'}
          </Button>
        } items={[
          { key: 'leads', label: `Leads (${leads.length})`, children: (
            <>
              {selectedLeadKeys.length > 0 && (
                <Button danger onClick={handleBulkDeleteLeads} style={{ marginBottom: 16 }}>
                  Xóa đã chọn ({selectedLeadKeys.length})
                </Button>
              )}
              <CommonTable
                rowSelection={{ selectedRowKeys: selectedLeadKeys, onChange: (keys: React.Key[]) => setSelectedLeadKeys(keys as string[]) }}
                columns={leadColumns}
                dataSource={leads}
                loading={loading.leads}
                rowKey="id"
                onRefresh={() => { setSelectedLeadKeys([]); fetchData(filters); }}
                onExportCsv={handleExportCsv}
                onExportExcel={handleExportExcel}
              />
            </>
          )},
          { key: 'opportunities', label: `Opportunities (${opportunities.length})`, children: (
            <>
              {selectedOppKeys.length > 0 && (
                <Button danger onClick={handleBulkDeleteOpps} style={{ marginBottom: 16 }}>
                  Xóa đã chọn ({selectedOppKeys.length})
                </Button>
              )}
              <CommonTable
                rowSelection={{ selectedRowKeys: selectedOppKeys, onChange: (keys: React.Key[]) => setSelectedOppKeys(keys as string[]) }}
                columns={oppColumns}
                dataSource={opportunities}
                loading={loading.opportunities}
                rowKey="id"
                onRefresh={() => { setSelectedOppKeys([]); fetchData(filters); }}
                onExportCsv={handleExportCsv}
                onExportExcel={handleExportExcel}
              />
            </>
          )},
          { key: 'activities', label: `Activities (${activities.length})`, children: (
            <>
              {selectedActivityKeys.length > 0 && (
                <Button danger onClick={handleBulkDeleteActivities} style={{ marginBottom: 16 }}>
                  Xóa đã chọn ({selectedActivityKeys.length})
                </Button>
              )}
              <CommonTable
                rowSelection={{ selectedRowKeys: selectedActivityKeys, onChange: (keys: React.Key[]) => setSelectedActivityKeys(keys as number[]) }}
                columns={activityColumns}
                dataSource={activities}
                loading={loading.activities}
                rowKey="id"
                onRefresh={() => { setSelectedActivityKeys([]); fetchData(filters); }}
                onExportCsv={handleExportCsv}
                onExportExcel={handleExportExcel}
              />
            </>
          )},
          { key: 'notes', label: `Notes (${notes.length})`, children: (
            <>
              {selectedNoteKeys.length > 0 && (
                <Button danger onClick={handleBulkDeleteNotes} style={{ marginBottom: 16 }}>
                  Xóa đã chọn ({selectedNoteKeys.length})
                </Button>
              )}
              <CommonTable
                rowSelection={{ selectedRowKeys: selectedNoteKeys, onChange: (keys: React.Key[]) => setSelectedNoteKeys(keys as number[]) }}
                columns={noteColumns}
                dataSource={notes}
                loading={loading.notes}
                rowKey="id"
                onRefresh={() => { setSelectedNoteKeys([]); fetchData(filters); }}
                onExportCsv={handleExportCsv}
                onExportExcel={handleExportExcel}
              />
            </>
          )},
        ]} />
      </Card>

      <CommonForm
        open={modalOpen}
        title={editingItem ? `Edit ${modalType.charAt(0).toUpperCase() + modalType.slice(1)}` : `Create ${modalType.charAt(0).toUpperCase() + modalType.slice(1)}`}
        onClose={() => { setModalOpen(false); setEditingItem(null); }}
        onSubmit={handleFormSubmit}
        initialValues={editingItem}
        width={600}
      >
        {renderFormFields()}
      </CommonForm>

      <CommonForm
        open={convertModalOpen}
        title={`Convert Lead: ${convertingLead?.firstName || ''} ${convertingLead?.lastName || ''}`}
        onClose={() => setConvertModalOpen(false)}
        onSubmit={handleConvert}
        width={500}
      >
        <Form.Item name="title" label="Opportunity Title" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="stage" label="Stage" initialValue="PROSPECTING">
          <Select options={oppStageOptions} />
        </Form.Item>
        <Form.Item name="value" label="Value ($)"><Input type="number" /></Form.Item>
      </CommonForm>
    </div>
  );
}
