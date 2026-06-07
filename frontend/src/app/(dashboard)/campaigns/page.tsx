'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Card, Tabs, Tag, Typography, Space, Button, Modal, Form,
  Input, Select, message, Row, Col, Statistic, Tooltip, Upload, Descriptions
} from 'antd';
import {
  PlusOutlined, PlayCircleOutlined, PauseCircleOutlined, StopOutlined,
  UploadOutlined, TeamOutlined, BarChartOutlined, EditOutlined, DeleteOutlined, CopyOutlined
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
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

const campaignTypeOptions = ['OUTBOUND', 'INBOUND', 'PREVIEW', 'PREDICTIVE', 'PROGRESSIVE'].map(t => ({ value: t, label: t }));
const strategyOptions = ['SEQUENTIAL', 'RANDOM', 'PRIORITY'].map(s => ({ value: s, label: s }));

export default function CampaignsPage() {
  const [activeTab, setActiveTab] = useState('campaigns');
  const [campaigns, setCampaigns] = useState<any[]>([]);
  const [selectedCampaign, setSelectedCampaign] = useState<any>(null);
  const [members, setMembers] = useState<any[]>([]);
  const [results, setResults] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [campaignModalOpen, setCampaignModalOpen] = useState(false);
  const [editingCampaign, setEditingCampaign] = useState<any>(null);
  const [memberModalOpen, setMemberModalOpen] = useState(false);
  const [filters, setFilters] = useState<Record<string, any>>({});
  const [selectedCampaignRowKeys, setSelectedCampaignRowKeys] = useState<string[]>([]);
  const [selectedMemberRowKeys, setSelectedMemberRowKeys] = useState<number[]>([]);
  const [selectedResultRowKeys, setSelectedResultRowKeys] = useState<string[]>([]);

  const fetchCampaigns = useCallback(async (params?: Record<string, any>) => {
    setLoading(true);
    setError(null);
    try {
      const res = await campaignsApi.list({ page: 0, size: 100, ...params });
      const data = res.data?.data?.content || res.data?.content || [];
      setCampaigns(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load campaigns');
    } finally {
      setLoading(false);
    }
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
    } catch {
      message.error('Failed to load campaign details');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingCampaign(null);
    setCampaignModalOpen(true);
  };

  const handleEdit = (campaign: any) => {
    setEditingCampaign(campaign);
    setCampaignModalOpen(true);
  };

  const handleDuplicate = (record: any) => {
    setEditingCampaign({ ...record, id: '' });
    setCampaignModalOpen(true);
  };

  const handleBulkDeleteCampaigns = () => {
    Modal.confirm({
      title: 'Xóa nhiều chiến dịch',
      content: `Bạn có chắc chắn muốn xóa ${selectedCampaignRowKeys.length} chiến dịch đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await campaignsApi.bulkDelete(selectedCampaignRowKeys);
          message.success(`Đã xóa ${selectedCampaignRowKeys.length} chiến dịch`);
          setSelectedCampaignRowKeys([]);
          fetchCampaigns(filters);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeleteMembers = () => {
    Modal.confirm({
      title: 'Xóa nhiều thành viên',
      content: `Bạn có chắc chắn muốn xóa ${selectedMemberRowKeys.length} thành viên đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          if (selectedCampaign) {
            await campaignsApi.bulkDeleteMembers(selectedCampaign.id, selectedMemberRowKeys);
            message.success(`Đã xóa ${selectedMemberRowKeys.length} thành viên`);
            setSelectedMemberRowKeys([]);
            fetchCampaignDetail(selectedCampaign);
          }
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleBulkDeleteResults = () => {
    if (!selectedCampaign) return;
    Modal.confirm({
      title: 'Xóa nhiều kết quả',
      content: `Bạn có chắc chắn muốn xóa ${selectedResultRowKeys.length} kết quả đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await campaignsApi.bulkDeleteResults(selectedCampaign.id, selectedResultRowKeys.map(Number));
          message.success(`Đã xóa ${selectedResultRowKeys.length} kết quả`);
          setSelectedResultRowKeys([]);
          fetchCampaignDetail(selectedCampaign);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleDelete = (campaign: any) => {
    showDeleteConfirm({
      title: 'Delete Campaign',
      content: `Are you sure you want to delete ${campaign.name}? This action cannot be undone.`,
      onOk: async () => {
        await campaignsApi.delete(campaign.id);
        fetchCampaigns(filters);
      },
    });
  };

  const handleFormSubmit = async (values: any) => {
    if (editingCampaign?.id) {
      await campaignsApi.update(editingCampaign.id, values);
    } else {
      await campaignsApi.create(values);
    }
    setCampaignModalOpen(false);
    fetchCampaigns(filters);
  };

  const handleStatusAction = async (id: string, action: 'start' | 'pause' | 'stop') => {
    try {
      if (action === 'start') await campaignsApi.start(id);
      else if (action === 'pause') await campaignsApi.pause(id);
      else await campaignsApi.stop(id);
      message.success(`Campaign ${action}ed`);
      fetchCampaigns(filters);
    } catch {
      message.error(`Failed to ${action} campaign`);
    }
  };

  const handleImport = async (file: File) => {
    if (!selectedCampaign) return;
    const formData = new FormData();
    formData.append('file', file);
    try {
      await campaignsApi.importMembers(selectedCampaign.id, formData);
      message.success('Members imported');
      fetchCampaignDetail(selectedCampaign);
    } catch {
      message.error('Import failed');
    }
  };

  const handleAddMember = async (values: any) => {
    if (!selectedCampaign) return;
    await campaignsApi.addMember(selectedCampaign.id, values);
    setMemberModalOpen(false);
    message.success('Member added');
    fetchCampaignDetail(selectedCampaign);
  };

  const handleSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') cleaned[key] = val;
    });
    setFilters(cleaned);
    fetchCampaigns(cleaned);
  };

  const handleReset = () => {
    setFilters({});
    fetchCampaigns();
  };

  const handleExportCsv = async () => {
    try {
      const res = await campaignsApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `campaigns_${new Date().toISOString().slice(0,10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Campaigns exported');
    } catch {
      message.error('Export failed');
    }
  };
  const handleExportExcel = async () => {
    try {
      const res = await campaignsApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `campaigns_${new Date().toISOString().slice(0,10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Campaigns exported');
    } catch {
      message.error('Export failed');
    }
  };

  const campaignColumns: ColumnsType<any> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (n: string, r: any) => (
        <a onClick={() => { setActiveTab('detail'); fetchCampaignDetail(r); }} style={{ fontWeight: 500 }}>{n}</a>
      ),
    },
    { title: 'Type', dataIndex: 'type', key: 'type' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (s: string) => <Tag color={campaignStatusColors[s] || 'default'}>{s}</Tag>,
    },
    { title: 'Strategy', dataIndex: 'strategy', key: 'strategy' },
    {
      title: 'Start',
      dataIndex: 'scheduleStart',
      key: 'scheduleStart',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-',
    },
    {
      title: 'End',
      dataIndex: 'scheduleEnd',
      key: 'scheduleEnd',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-',
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, r: any) => (
        <Space>
          {(r.status === 'DRAFT' || r.status === 'PAUSED') && (
            <Tooltip title="Start">
              <Button size="small" type="primary" icon={<PlayCircleOutlined />} onClick={() => handleStatusAction(r.id, 'start')} />
            </Tooltip>
          )}
          {r.status === 'RUNNING' && (
            <>
              <Tooltip title="Pause">
                <Button size="small" icon={<PauseCircleOutlined />} onClick={() => handleStatusAction(r.id, 'pause')} />
              </Tooltip>
              <Tooltip title="Stop">
                <Button size="small" danger icon={<StopOutlined />} onClick={() => handleStatusAction(r.id, 'stop')} />
              </Tooltip>
            </>
          )}
          <Tooltip title="Edit">
            <Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(r)} />
          </Tooltip>
          <Tooltip title="Nhân bản">
            <Button size="small" icon={<CopyOutlined />} onClick={() => handleDuplicate(r)} />
          </Tooltip>
          <Tooltip title="Delete">
            <Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(r)} />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const memberColumns: ColumnsType<any> = [
    { title: 'Name', dataIndex: 'contactName', key: 'contactName' },
    { title: 'Phone', dataIndex: 'contactPhone', key: 'contactPhone' },
    { title: 'Email', dataIndex: 'contactEmail', key: 'contactEmail' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (s: string) => <Tag color={memberStatusColors[s] || 'default'}>{s}</Tag>,
    },
    { title: 'Priority', dataIndex: 'priority', key: 'priority' },
    {
      title: 'Called At',
      dataIndex: 'lastCalledAt',
      key: 'lastCalledAt',
      render: (d: string) => d ? new Date(d).toLocaleString() : '-',
    },
  ];

  const resultColumns: ColumnsType<any> = [
    { title: 'Member', dataIndex: 'campaignMemberId', key: 'campaignMemberId' },
    {
      title: 'Result',
      dataIndex: 'resultType',
      key: 'resultType',
      render: (t: string) => <Tag>{t}</Tag>,
    },
    { title: 'Notes', dataIndex: 'notes', key: 'notes', ellipsis: true },
    {
      title: 'Duration',
      dataIndex: 'duration',
      key: 'duration',
      render: (d: number) => d ? `${d}s` : '-',
    },
    { title: 'Agent', dataIndex: 'agentId', key: 'agentId' },
    {
      title: 'Date',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (d: string) => d ? new Date(d).toLocaleString() : '-',
    },
  ];

  const searchFields = [
    { name: 'name', label: 'Name', type: 'input' as const, placeholder: 'Search by name' },
    {
      name: 'status',
      label: 'Status',
      type: 'select' as const,
      placeholder: 'Filter by status',
      options: Object.entries(campaignStatusColors).map(([value]) => ({ value, label: value })),
    },
    {
      name: 'type',
      label: 'Type',
      type: 'select' as const,
      placeholder: 'Filter by type',
      options: campaignTypeOptions,
    },
  ];

  const totalMembers = members.length;
  const completedMembers = members.filter((m: any) => m.status === 'COMPLETED' || m.status === 'CONTACTED').length;

  return (
    <div>
      <Title level={3}>Campaigns</Title>

      <Card>
        <Tabs
          activeKey={activeTab}
          onChange={(key) => { setActiveTab(key); if (key === 'campaigns') setSelectedCampaign(null); }}
          tabBarExtraContent={
            activeTab === 'campaigns' ? (
              <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>
                Create Campaign
              </Button>
            ) : undefined
          }
          items={[
            {
              key: 'campaigns',
              label: `Campaigns (${campaigns.length})`,
              children: (
                <>
                  <CommonSearch
                    fields={searchFields}
                    onSearch={handleSearch}
                    onReset={handleReset}
                    loading={loading}
                  />
                  <SavedFilters currentValues={filters} onApply={(v) => { setFilters(v); handleSearch(v); }} storageKey="vcall-saved-filters-campaigns" />
                  {selectedCampaignRowKeys.length > 0 && (
                    <Button danger onClick={handleBulkDeleteCampaigns} style={{ marginBottom: 16 }}>
                      Xóa đã chọn ({selectedCampaignRowKeys.length})
                    </Button>
                  )}
                  <CommonTable
                    rowSelection={{ selectedRowKeys: selectedCampaignRowKeys, onChange: (keys: React.Key[]) => setSelectedCampaignRowKeys(keys as string[]) }}
                    columns={campaignColumns}
                    dataSource={campaigns}
                    loading={loading}
                    error={error}
                    rowKey="id"
                    onRefresh={() => { setSelectedCampaignRowKeys([]); fetchCampaigns(filters); }}
                    onExportCsv={handleExportCsv}
                    onExportExcel={handleExportExcel}
                  />
                </>
              ),
            },
            ...(selectedCampaign ? [{
              key: 'detail',
              label: `Detail: ${selectedCampaign.name}`,
              children: detailLoading ? (
                <CommonTable columns={[]} dataSource={[]} loading={true} rowKey="id" />
              ) : (
                <div>
                  <Descriptions title="Campaign Info" bordered column={2} style={{ marginBottom: 16 }}>
                    <Descriptions.Item label="Name">{selectedCampaign.name}</Descriptions.Item>
                    <Descriptions.Item label="Status">
                      <Tag color={campaignStatusColors[selectedCampaign.status]}>{selectedCampaign.status}</Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="Type">{selectedCampaign.type}</Descriptions.Item>
                    <Descriptions.Item label="Strategy">{selectedCampaign.strategy}</Descriptions.Item>
                    <Descriptions.Item label="Schedule">
                      {selectedCampaign.scheduleStart ? new Date(selectedCampaign.scheduleStart).toLocaleDateString() : '-'}
                      {' → '}
                      {selectedCampaign.scheduleEnd ? new Date(selectedCampaign.scheduleEnd).toLocaleDateString() : '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label="Description">{selectedCampaign.description || '-'}</Descriptions.Item>
                  </Descriptions>

                  <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
                    <Col span={8}>
                      <Card><Statistic title="Total Members" value={totalMembers} prefix={<TeamOutlined />} /></Card>
                    </Col>
                    <Col span={8}>
                      <Card>
                        <Statistic
                          title="Completed"
                          value={completedMembers}
                          suffix={`/ ${totalMembers}`}
                          valueStyle={{ color: '#52c41a' }}
                        />
                      </Card>
                    </Col>
                    <Col span={8}>
                      <Card><Statistic title="Results" value={results.length} prefix={<BarChartOutlined />} /></Card>
                    </Col>
                  </Row>

                  <Space style={{ marginBottom: 16 }}>
                    <Upload accept=".csv,.json" showUploadList={false} customRequest={({ file }) => handleImport(file as File)}>
                      <Button icon={<UploadOutlined />}>Import Members</Button>
                    </Upload>
                    <Button icon={<PlusOutlined />} onClick={() => { setMemberModalOpen(true); }}>
                      Add Member
                    </Button>
                  </Space>

                  <Tabs items={[
                    {
                      key: 'members',
                      label: `Members (${members.length})`,
                      children: (
                        <>
                          {selectedMemberRowKeys.length > 0 && (
                            <Button danger onClick={handleBulkDeleteMembers} style={{ marginBottom: 16 }}>
                              Xóa đã chọn ({selectedMemberRowKeys.length})
                            </Button>
                          )}
                          <CommonTable
                            rowSelection={{ selectedRowKeys: selectedMemberRowKeys, onChange: (keys: React.Key[]) => setSelectedMemberRowKeys(keys as number[]) }}
                            columns={memberColumns}
                            dataSource={members}
                            loading={detailLoading}
                            rowKey="id"
                            onRefresh={() => { setSelectedMemberRowKeys([]); fetchCampaignDetail(selectedCampaign); }}
                          />
                        </>
                      ),
                    },
                    {
                      key: 'results',
                      label: `Results (${results.length})`,
                      children: (
                        <>
                          {selectedResultRowKeys.length > 0 && (
                            <Button danger onClick={handleBulkDeleteResults} style={{ marginBottom: 16 }}>
                              Xóa đã chọn ({selectedResultRowKeys.length})
                            </Button>
                          )}
                          <CommonTable
                            rowSelection={{ selectedRowKeys: selectedResultRowKeys, onChange: (keys: React.Key[]) => setSelectedResultRowKeys(keys as string[]) }}
                            columns={resultColumns}
                            dataSource={results}
                            loading={detailLoading}
                            rowKey="id"
                            onRefresh={() => { setSelectedResultRowKeys([]); fetchCampaignDetail(selectedCampaign); }}
                          />
                        </>
                      ),
                    },
                  ]} />
                </div>
              ),
            }] : []),
          ]}
        />
      </Card>

      <CommonForm
        open={campaignModalOpen}
        title={editingCampaign?.id ? 'Edit Campaign' : 'Create Campaign'}
        onClose={() => { setCampaignModalOpen(false); setEditingCampaign(null); }}
        onSubmit={handleFormSubmit}
        initialValues={editingCampaign}
        width={600}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter campaign name' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
        <Form.Item name="type" label="Type" rules={[{ required: true, message: 'Please select type' }]}>
          <Select options={campaignTypeOptions} />
        </Form.Item>
        <Form.Item name="strategy" label="Strategy" initialValue="SEQUENTIAL">
          <Select options={strategyOptions} />
        </Form.Item>
        <Form.Item name="scheduleStart" label="Start Date">
          <Input type="date" />
        </Form.Item>
        <Form.Item name="scheduleEnd" label="End Date">
          <Input type="date" />
        </Form.Item>
      </CommonForm>

      <CommonForm
        open={memberModalOpen}
        title="Add Member"
        onClose={() => setMemberModalOpen(false)}
        onSubmit={handleAddMember}
        width={500}
      >
        <Form.Item name="contactName" label="Name">
          <Input />
        </Form.Item>
        <Form.Item name="contactPhone" label="Phone" rules={[{ required: true, message: 'Please enter phone' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="contactEmail" label="Email">
          <Input />
        </Form.Item>
        <Form.Item name="priority" label="Priority">
          <Input type="number" />
        </Form.Item>
      </CommonForm>
    </div>
  );
}
