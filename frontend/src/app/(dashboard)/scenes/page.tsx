'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Card, Table, Tag, Typography, Space, Button, Modal, Form,
  Input, Select, message, Tooltip, Popconfirm, Row, Col, Statistic, Tabs,
} from 'antd';
import {
  PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined,
  CloudUploadOutlined, ReloadOutlined, DownloadOutlined, FileExcelOutlined,
  AppstoreOutlined, VideoCameraOutlined, PictureOutlined, BlockOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useRouter } from 'next/navigation';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import { xrScenesApi, type XRScene } from '@/lib/api/xr-api';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const sceneTypeColors: Record<string, string> = {
  INTERACTIVE: 'blue', STATIC: 'default', '360_VIDEO': 'purple', HYBRID: 'cyan',
};

const sceneStatusColors: Record<string, string> = {
  DRAFT: 'default', PUBLISHED: 'green', ARCHIVED: 'orange',
};

const sceneTypeOptions = [
  { value: 'INTERACTIVE', label: 'Interactive' },
  { value: 'STATIC', label: 'Static' },
  { value: '360_VIDEO', label: '360 Video' },
  { value: 'HYBRID', label: 'Hybrid' },
];

const sceneStatusOptions = [
  { value: 'DRAFT', label: 'Draft' },
  { value: 'PUBLISHED', label: 'Published' },
  { value: 'ARCHIVED', label: 'Archived' },
];

export default function ScenesPage() {
  const router = useRouter();
  const [scenes, setScenes] = useState<XRScene[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingScene, setEditingScene] = useState<XRScene | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);
  const [filters, setFilters] = useState<Record<string, any>>({});

  const fetchScenes = useCallback(async (params?: Record<string, any>) => {
    setLoading(true);
    setError(null);
    try {
      const res = await xrScenesApi.list({ page: 0, size: 100, ...params });
      setScenes(res.data?.data?.content || res.data?.content || []);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load scenes');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchScenes(); }, [fetchScenes]);

  const handleCreate = () => {
    setEditingScene(null);
    setModalOpen(true);
  };

  const handleEdit = (scene: XRScene) => {
    setEditingScene(scene);
    setModalOpen(true);
  };

  const handleDelete = (scene: XRScene) => {
    Modal.confirm({
      title: 'Delete Scene',
      content: `Are you sure you want to delete "${scene.name}"? This action cannot be undone.`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await xrScenesApi.delete(scene.id);
          message.success('Scene deleted');
          fetchScenes(filters);
        } catch {
          message.error('Failed to delete scene');
        }
      },
    });
  };

  const handleBulkDelete = () => {
    Modal.confirm({
      title: 'Delete Multiple Scenes',
      content: `Are you sure you want to delete ${selectedRowKeys.length} selected scenes?`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await Promise.all(selectedRowKeys.map((id) => xrScenesApi.delete(id)));
          message.success(`Deleted ${selectedRowKeys.length} scenes`);
          setSelectedRowKeys([]);
          fetchScenes(filters);
        } catch {
          message.error('Failed to delete scenes');
        }
      },
    });
  };

  const handlePublish = async (scene: XRScene) => {
    try {
      await xrScenesApi.publish(scene.id);
      message.success(`"${scene.name}" published`);
      fetchScenes(filters);
    } catch {
      message.error('Failed to publish scene');
    }
  };

  const handleFormSubmit = async (values: any) => {
    if (editingScene?.id) {
      await xrScenesApi.update(editingScene.id, values);
    } else {
      await xrScenesApi.create(values);
    }
    setModalOpen(false);
    fetchScenes(filters);
  };

  const handleExportCsv = async () => {
    try {
      const res = await xrScenesApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `scenes_${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
    } catch { message.error('Failed to export CSV'); }
  };

  const handleExportExcel = async () => {
    try {
      const res = await xrScenesApi.exportExcel();
      const blob = new Blob([res.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `scenes_${new Date().toISOString().slice(0, 10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
    } catch { message.error('Failed to export Excel'); }
  };

  const columns: ColumnsType<XRScene> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <a onClick={() => router.push(`/scenes/${record.id}/builder`)} style={{ fontWeight: 500 }}>
          {name}
        </a>
      ),
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => <Tag color={sceneTypeColors[type] || 'default'}>{type}</Tag>,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => <Tag color={sceneStatusColors[status] || 'default'}>{status}</Tag>,
    },
    {
      title: 'Views',
      dataIndex: 'viewCount',
      key: 'viewCount',
      sorter: (a, b) => a.viewCount - b.viewCount,
      render: (count: number) => count.toLocaleString(),
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (d: string) => d ? dayjs(d).format('DD/MM/YYYY') : '-',
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record) => (
        <Space>
          <Tooltip title="Edit">
            <Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)} />
          </Tooltip>
          <Tooltip title="Preview">
            <Button
              size="small"
              icon={<EyeOutlined />}
              onClick={() => router.push(`/scenes/${record.id}/builder`)}
            />
          </Tooltip>
          {record.status !== 'PUBLISHED' && (
            <Tooltip title="Publish">
              <Button size="small" type="primary" icon={<CloudUploadOutlined />} onClick={() => handlePublish(record)} />
            </Tooltip>
          )}
          <Popconfirm title="Delete this scene?" onConfirm={() => handleDelete(record)}>
            <Tooltip title="Delete">
              <Button size="small" danger icon={<DeleteOutlined />} />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const totalViews = scenes.reduce((sum, s) => sum + s.viewCount, 0);
  const publishedCount = scenes.filter((s) => s.status === 'PUBLISHED').length;
  const draftCount = scenes.filter((s) => s.status === 'DRAFT').length;

  return (
    <div>
      <Title level={3}>VR Scenes</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Total Scenes" value={scenes.length} prefix={<AppstoreOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Published" value={publishedCount} prefix={<CloudUploadOutlined />} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Total Views" value={totalViews} prefix={<EyeOutlined />} />
          </Card>
        </Col>
      </Row>

      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Scene</Button>
          {selectedRowKeys.length > 0 && (
            <Popconfirm title={`Delete ${selectedRowKeys.length} scenes?`} onConfirm={handleBulkDelete}>
              <Button danger>Delete Selected ({selectedRowKeys.length})</Button>
            </Popconfirm>
          )}
          <Button icon={<DownloadOutlined />} onClick={handleExportCsv}>Export CSV</Button>
          <Button icon={<FileExcelOutlined />} onClick={handleExportExcel}>Export Excel</Button>
          <Button icon={<ReloadOutlined />} onClick={() => fetchScenes(filters)}>Refresh</Button>
        </Space>

        <Table<XRScene>
          columns={columns}
          dataSource={scenes}
          rowKey="id"
          loading={loading}
          rowSelection={{ selectedRowKeys, onChange: (keys) => setSelectedRowKeys(keys as string[]) }}
          pagination={{ showSizeChanger: true, showTotal: (total) => `Total ${total} scenes` }}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <CommonForm
        open={modalOpen}
        title={editingScene?.id ? 'Edit Scene' : 'Create Scene'}
        onClose={() => { setModalOpen(false); setEditingScene(null); }}
        onSubmit={handleFormSubmit}
        initialValues={editingScene}
        width={600}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter scene name' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
        <Form.Item name="type" label="Type" rules={[{ required: true, message: 'Please select type' }]}>
          <Select options={sceneTypeOptions} />
        </Form.Item>
      </CommonForm>
    </div>
  );
}
