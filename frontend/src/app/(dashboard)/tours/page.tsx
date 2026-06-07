'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Card, Table, Tag, Typography, Space, Button, Modal, Form,
  Input, Select, message, Tooltip, Popconfirm, Row, Col, Statistic,
  Empty,
} from 'antd';
import {
  PlusOutlined, EditOutlined, DeleteOutlined, EyeOutlined,
  CloudUploadOutlined, ReloadOutlined, DownloadOutlined, FileExcelOutlined,
  UnorderedListOutlined, AppstoreOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useRouter } from 'next/navigation';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import { xrToursApi, type XRTour } from '@/lib/api/xr-api';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const tourStatusColors: Record<string, string> = {
  DRAFT: 'default', PUBLISHED: 'green',
};

export default function ToursPage() {
  const router = useRouter();
  const [tours, setTours] = useState<XRTour[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTour, setEditingTour] = useState<XRTour | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);

  const fetchTours = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await xrToursApi.list({ page: 0, size: 100 });
      setTours(res.data?.data?.content || res.data?.content || []);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load tours');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchTours(); }, [fetchTours]);

  const handleCreate = () => {
    setEditingTour(null);
    setModalOpen(true);
  };

  const handleEdit = (tour: XRTour) => {
    setEditingTour(tour);
    setModalOpen(true);
  };

  const handleDelete = (tour: XRTour) => {
    Modal.confirm({
      title: 'Delete Tour',
      content: `Are you sure you want to delete "${tour.name}"? This action cannot be undone.`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await xrToursApi.delete(tour.id);
          message.success('Tour deleted');
          fetchTours();
        } catch {
          message.error('Failed to delete tour');
        }
      },
    });
  };

  const handlePublish = async (tour: XRTour) => {
    try {
      await xrToursApi.publish(tour.id);
      message.success(`"${tour.name}" published`);
      fetchTours();
    } catch {
      message.error('Failed to publish tour');
    }
  };

  const handleFormSubmit = async (values: any) => {
    try {
      if (editingTour?.id) {
        await xrToursApi.update(editingTour.id, values);
      } else {
        await xrToursApi.create(values);
      }
      message.success(editingTour?.id ? 'Tour updated' : 'Tour created');
      setModalOpen(false);
      fetchTours();
    } catch {
      message.error(editingTour?.id ? 'Failed to update tour' : 'Failed to create tour');
    }
  };

  const columns: ColumnsType<XRTour> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <a onClick={() => router.push(`/tours/${record.id}`)} style={{ fontWeight: 500 }}>
          {name}
        </a>
      ),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      render: (d: string) => d || <Text type="secondary">-</Text>,
    },
    {
      title: 'Scenes',
      key: 'scenes',
      render: (_: any, record) => (
        <Tag color="blue">{(record.scenes || []).length} scenes</Tag>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => <Tag color={tourStatusColors[status] || 'default'}>{status}</Tag>,
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
            <Button size="small" icon={<EyeOutlined />} onClick={() => router.push(`/tours/${record.id}`)} />
          </Tooltip>
          {record.status !== 'PUBLISHED' && (
            <Tooltip title="Publish">
              <Button size="small" type="primary" icon={<CloudUploadOutlined />} onClick={() => handlePublish(record)} />
            </Tooltip>
          )}
          <Popconfirm title="Delete this tour?" onConfirm={() => handleDelete(record)}>
            <Tooltip title="Delete">
              <Button size="small" danger icon={<DeleteOutlined />} />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const publishedCount = tours.filter((t) => t.status === 'PUBLISHED').length;
  const totalViews = tours.reduce((sum, t) => sum + t.viewCount, 0);

  return (
    <div>
      <Title level={3}>Virtual Tours</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Total Tours" value={tours.length} prefix={<UnorderedListOutlined />} />
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
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleCreate}>New Tour</Button>
            {selectedRowKeys.length > 0 && (
              <Popconfirm
                title={`Delete ${selectedRowKeys.length} tours?`}
                onConfirm={async () => {
                  try {
                    await Promise.all(selectedRowKeys.map(id => xrToursApi.delete(id)));
                    message.success(`${selectedRowKeys.length} tours deleted`);
                    setSelectedRowKeys([]);
                    fetchTours();
                  } catch {
                    message.error('Failed to delete selected tours');
                  }
                }}
              >
                <Button danger>Delete Selected ({selectedRowKeys.length})</Button>
              </Popconfirm>
            )}
          </Space>
          <Button icon={<ReloadOutlined />} onClick={fetchTours}>Refresh</Button>
        </div>

        <Table<XRTour>
          columns={columns}
          dataSource={tours}
          rowKey="id"
          loading={loading}
          rowSelection={{ selectedRowKeys, onChange: (keys) => setSelectedRowKeys(keys as string[]) }}
          pagination={{ showSizeChanger: true, showTotal: (total) => `Total ${total} tours` }}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <CommonForm
        open={modalOpen}
        title={editingTour?.id ? 'Edit Tour' : 'Create Tour'}
        onClose={() => { setModalOpen(false); setEditingTour(null); }}
        onSubmit={handleFormSubmit}
        initialValues={editingTour}
        width={600}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter tour name' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
      </CommonForm>
    </div>
  );
}
