'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { Card, Tag, Typography, Space, Button, message, Row, Col, Statistic, Form, Input, Select, Modal } from 'antd';
import { SearchOutlined, PhoneOutlined, DownloadOutlined, CopyOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import CommonTable from '@/components/common/CommonTable';
import CommonSearch from '@/components/common/CommonSearch';
import CommonForm from '@/components/common/CommonForm';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { callsApi } from '@/lib/api';

const { Title } = Typography;

const statusColors: Record<string, string> = {
  completed: '#52c41a',
  ongoing: '#1677ff',
  missed: '#ff4d4f',
  failed: '#faad14',
};

const directionColors: Record<string, string> = {
  inbound: 'blue',
  outbound: 'purple',
};

interface CallRecord {
  id: string;
  caller: string;
  callee: string;
  direction: 'inbound' | 'outbound';
  status: string;
  duration: number;
  agent: string;
  time: string;
}

export default function CallsPage() {
  const router = useRouter();
  const [data, setData] = useState<CallRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useState<Record<string, any>>({});
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCall, setEditingCall] = useState<any>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { ...searchParams, page: 0, size: 100 };
      const res = await callsApi.getAll(params);
      setData(res.data?.data?.content || res.data?.content || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load calls');
    } finally {
      setLoading(false);
    }
  }, [searchParams]);

  useEffect(() => { fetchData(); }, [fetchData]);

  const handleSearch = (values: any) => {
    const params: Record<string, any> = {};
    if (values.search) params.q = values.search;
    if (values.status) params.status = values.status;
    if (values.direction) params.direction = values.direction;
    setSearchParams(params);
  };

  const handleReset = () => {
    setSearchParams({});
  };

  const handleBulkDelete = () => {
    Modal.confirm({
      title: 'Xóa nhiều cuộc gọi',
      content: `Bạn có chắc chắn muốn xóa ${selectedRowKeys.length} cuộc gọi đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await callsApi.bulkDelete(selectedRowKeys);
          message.success(`Đã xóa ${selectedRowKeys.length} cuộc gọi`);
          setSelectedRowKeys([]);
          fetchData();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleCreate = () => {
    setEditingCall(null);
    setModalOpen(true);
  };

  const handleEdit = (record: any) => {
    setEditingCall(record);
    setModalOpen(true);
  };

  const handleDuplicate = (record: any) => {
    setEditingCall({ ...record, id: '' });
    setModalOpen(true);
  };

  const handleDelete = (id: string) => {
    showDeleteConfirm({
      title: 'Xóa cuộc gọi',
      content: 'Bạn có chắc chắn muốn xóa cuộc gọi này?',
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        await callsApi.delete(id);
        message.success('Call deleted');
        fetchData();
      },
    });
  };

  const handleSubmit = async (values: any) => {
    if (editingCall?.id) {
      await callsApi.update(editingCall.id, values);
    } else {
      await callsApi.create(values);
    }
    setModalOpen(false);
    fetchData();
  };

  const handleExportCsv = async () => {
    try {
      const res = await callsApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `calls_${dayjs().format('YYYYMMDD_HHmmss')}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Calls exported');
    } catch {
      message.error('Export failed');
    }
  };

  const handleExportExcel = async () => {
    try {
      const res = await callsApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `calls_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Calls exported');
    } catch {
      message.error('Export failed');
    }
  };

  const columns = [
    {
      title: 'Call ID',
      dataIndex: 'id',
      key: 'id',
      render: (id: string) => <a style={{ fontWeight: 500 }} onClick={() => router.push(`/calls/${id}`)}>{id}</a>,
    },
    { title: 'Caller', dataIndex: 'caller', key: 'caller' },
    { title: 'Callee', dataIndex: 'callee', key: 'callee' },
    {
      title: 'Direction',
      dataIndex: 'direction',
      key: 'direction',
      render: (dir: string) => (
        <Tag icon={<PhoneOutlined />} color={directionColors[dir] || 'default'}>
          {dir?.toUpperCase()}
        </Tag>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={statusColors[status] || 'default'}>{status?.toUpperCase()}</Tag>
      ),
    },
    {
      title: 'Duration',
      dataIndex: 'duration',
      key: 'duration',
      render: (secs: number) => {
        if (!secs) return '-';
        const mins = Math.floor(secs / 60);
        const s = secs % 60;
        return `${mins}:${s.toString().padStart(2, '0')}`;
      },
    },
    { title: 'Agent', dataIndex: 'agent', key: 'agent' },
    { title: 'Time', dataIndex: 'time', key: 'time' },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record: any) => (
        <Space>
          <Button size="small" onClick={() => handleEdit(record)}>Edit</Button>
          <Button size="small" icon={<CopyOutlined />} onClick={() => handleDuplicate(record)}>Nhân bản</Button>
          <Button size="small" danger onClick={() => handleDelete(record.id)}>Delete</Button>
        </Space>
      ),
    },
  ];

  const totalCalls = data.length;
  const completedCalls = data.filter((c) => c.status === 'completed').length;
  const ongoingCalls = data.filter((c) => c.status === 'ongoing').length;
  const missedCalls = data.filter((c) => c.status === 'missed').length;

  return (
    <div>
      <Title level={3}>Calls</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}><Card><Statistic title="Total Calls" value={totalCalls} prefix={<PhoneOutlined />} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Completed" value={completedCalls} valueStyle={{ color: '#52c41a' }} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Ongoing" value={ongoingCalls} valueStyle={{ color: '#1677ff' }} /></Card></Col>
        <Col xs={12} sm={6}><Card><Statistic title="Missed" value={missedCalls} valueStyle={{ color: '#ff4d4f' }} /></Card></Col>
      </Row>

      <Card>
        <CommonSearch
          fields={[
            { name: 'search', label: 'Search', type: 'input', placeholder: 'Search by caller, callee, or ID' },
            {
              name: 'status',
              label: 'Status',
              type: 'select',
              placeholder: 'Filter by status',
              options: [
                { value: 'completed', label: 'Completed' },
                { value: 'ongoing', label: 'Ongoing' },
                { value: 'missed', label: 'Missed' },
                { value: 'failed', label: 'Failed' },
              ],
            },
            {
              name: 'direction',
              label: 'Direction',
              type: 'select',
              placeholder: 'Filter by direction',
              options: [
                { value: 'inbound', label: 'Inbound' },
                { value: 'outbound', label: 'Outbound' },
              ],
            },
          ]}
          onSearch={handleSearch}
          onReset={handleReset}
          loading={loading}
        />
        <SavedFilters currentValues={searchParams} onApply={(v) => { setSearchParams(v); handleSearch(v); }} storageKey="vcall-saved-filters-calls" />
      </Card>

      <div style={{ marginTop: 16 }}>
        <div style={{ marginBottom: 16 }}>
          {selectedRowKeys.length > 0 && (
            <Button danger onClick={handleBulkDelete}>
              Xóa đã chọn ({selectedRowKeys.length})
            </Button>
          )}
        </div>
        <CommonTable
          title="Call Records"
          rowSelection={{ selectedRowKeys, onChange: (keys: React.Key[]) => setSelectedRowKeys(keys as string[]) }}
          columns={columns}
          dataSource={data}
          loading={loading}
          error={error}
          rowKey="id"
          pagination={{ pageSize: 10 }}
          onRefresh={() => { setSelectedRowKeys([]); fetchData(); }}
          onExportCsv={handleExportCsv}
          onExportExcel={handleExportExcel}
          extra={
            <Button type="primary" icon={<PhoneOutlined />} onClick={handleCreate}>
              New Call
            </Button>
          }
        />
      </div>

      <CommonForm
        open={modalOpen}
        title={editingCall?.id ? 'Edit Call' : 'New Call'}
        onClose={() => setModalOpen(false)}
        onSubmit={handleSubmit}
        initialValues={editingCall}
      >
        <Form.Item name="caller" label="Caller" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="callee" label="Callee" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="direction" label="Direction" rules={[{ required: true }]}>
          <Select options={[{ value: 'inbound', label: 'Inbound' }, { value: 'outbound', label: 'Outbound' }]} />
        </Form.Item>
        <Form.Item name="agent" label="Agent"><Input /></Form.Item>
      </CommonForm>
    </div>
  );
}
