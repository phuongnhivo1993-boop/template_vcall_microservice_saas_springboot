'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Card, Table, Tag, Typography, Space, Button, Upload, Modal, Popconfirm,
  message, Row, Col, Statistic, Tooltip, Progress, Empty, Spin,
} from 'antd';
import {
  UploadOutlined, DeleteOutlined, EyeOutlined, ReloadOutlined,
  VideoCameraOutlined, CheckCircleOutlined, ClockCircleOutlined,
  CloseCircleOutlined, SyncOutlined, DownloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { xrVideoApi, type XRVideo } from '@/lib/api/xr-api';

const { Title, Text } = Typography;

const transcodingStatusConfig: Record<string, { color: string; icon: React.ReactNode; label: string }> = {
  PENDING: { color: 'default', icon: <ClockCircleOutlined />, label: 'Pending' },
  PROCESSING: { color: 'processing', icon: <SyncOutlined spin />, label: 'Processing' },
  COMPLETED: { color: 'success', icon: <CheckCircleOutlined />, label: 'Completed' },
  FAILED: { color: 'error', icon: <CloseCircleOutlined />, label: 'Failed' },
};

const formatDuration = (seconds: number) => {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs.toString().padStart(2, '0')}`;
};

export default function VideoPage() {
  const [videos, setVideos] = useState<XRVideo[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);
  const [previewVideo, setPreviewVideo] = useState<XRVideo | null>(null);
  const [uploading, setUploading] = useState(false);

  const fetchVideos = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await xrVideoApi.list({ page: 0, size: 100 });
      setVideos(res.data?.data?.content || res.data?.content || []);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load videos');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchVideos(); }, [fetchVideos]);

  const handleUpload = async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    setUploading(true);
    try {
      await xrVideoApi.upload(formData);
      message.success(`${file.name} uploaded. Transcoding started.`);
      fetchVideos();
    } catch {
      message.error('Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = (video: XRVideo) => {
    Modal.confirm({
      title: 'Delete Video',
      content: `Are you sure you want to delete "${video.name}"?`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await xrVideoApi.delete(video.id);
          message.success('Video deleted');
          fetchVideos();
        } catch {
          message.error('Failed to delete video');
        }
      },
    });
  };

  const handleRetryTranscoding = async (video: XRVideo) => {
    try {
      await xrVideoApi.retryTranscoding(video.id);
      message.success('Retrying transcoding');
      fetchVideos();
    } catch {
      message.error('Failed to retry transcoding');
    }
  };

  const columns: ColumnsType<XRVideo> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record) => (
        <Space>
          <VideoCameraOutlined style={{ color: '#722ed1' }} />
          <Text strong>{name}</Text>
        </Space>
      ),
    },
    {
      title: 'Duration',
      dataIndex: 'duration',
      key: 'duration',
      render: (d: number) => formatDuration(d),
    },
    {
      title: 'Resolution',
      dataIndex: 'resolution',
      key: 'resolution',
    },
    {
      title: 'Status',
      dataIndex: 'transcodingStatus',
      key: 'transcodingStatus',
      render: (status: string) => {
        const config = transcodingStatusConfig[status] || transcodingStatusConfig.PENDING;
        return <Tag color={config.color} icon={config.icon}>{config.label}</Tag>;
      },
    },
    {
      title: 'Progress',
      dataIndex: 'transcodingProgress',
      key: 'transcodingProgress',
      render: (progress: number, record) => {
        if (record.transcodingStatus === 'COMPLETED') {
          return <Progress percent={100} size="small" status="success" />;
        }
        if (record.transcodingStatus === 'FAILED') {
          return <Progress percent={progress} size="small" status="exception" />;
        }
        return <Progress percent={progress} size="small" />;
      },
    },
    {
      title: 'Formats',
      key: 'formats',
      render: (_: any, record) => (
        <Space>
          {(record.formats || []).map((f) => (
            <Tag key={f.quality} style={{ fontSize: 11 }}>{f.quality}</Tag>
          ))}
        </Space>
      ),
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (d: string) => d ? new Date(d).toLocaleDateString() : '-',
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: any, record) => (
        <Space>
          <Tooltip title="Preview">
            <Button size="small" icon={<EyeOutlined />} onClick={() => setPreviewVideo(record)} />
          </Tooltip>
          {record.transcodingStatus === 'FAILED' && (
            <Tooltip title="Retry Transcoding">
              <Button size="small" icon={<SyncOutlined />} onClick={() => handleRetryTranscoding(record)} />
            </Tooltip>
          )}
          <Tooltip title="Download">
            <Button size="small" icon={<DownloadOutlined />} />
          </Tooltip>
          <Tooltip title="Delete">
            <Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)} />
          </Tooltip>
        </Space>
      ),
    },
  ];

  const totalDuration = videos.reduce((sum, v) => sum + v.duration, 0);
  const completedCount = videos.filter((v) => v.transcodingStatus === 'COMPLETED').length;
  const processingCount = videos.filter((v) => v.transcodingStatus === 'PROCESSING').length;

  return (
    <div>
      <Title level={3}>360° Video Manager</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Total Videos" value={videos.length} prefix={<VideoCameraOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Transcoded" value={completedCount} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Processing" value={processingCount} prefix={<SyncOutlined />} valueStyle={{ color: '#1677ff' }} />
          </Card>
        </Col>
      </Row>

      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Space>
            <Upload
              accept="video/*"
              showUploadList={false}
              customRequest={({ file }) => handleUpload(file as File)}
              disabled={uploading}
            >
              <Button type="primary" icon={<UploadOutlined />} loading={uploading}>
                Upload 360° Video
              </Button>
            </Upload>
            {selectedRowKeys.length > 0 && (
              <Popconfirm
                title={`Delete ${selectedRowKeys.length} videos?`}
                onConfirm={async () => {
                  try {
                    await Promise.all(selectedRowKeys.map(id => xrVideoApi.delete(id)));
                    message.success(`${selectedRowKeys.length} videos deleted`);
                    setSelectedRowKeys([]);
                    fetchVideos();
                  } catch {
                    message.error('Failed to delete selected videos');
                  }
                }}
              >
                <Button danger>Delete Selected ({selectedRowKeys.length})</Button>
              </Popconfirm>
            )}
          </Space>
          <Button icon={<ReloadOutlined />} onClick={fetchVideos}>Refresh</Button>
        </div>

        <Table<XRVideo>
          columns={columns}
          dataSource={videos}
          rowKey="id"
          loading={loading}
          rowSelection={{ selectedRowKeys, onChange: (keys) => setSelectedRowKeys(keys as string[]) }}
          pagination={{ showSizeChanger: true, showTotal: (total) => `Total ${total} videos` }}
          scroll={{ x: 'max-content' }}
        />
      </Card>

      <Modal
        title={previewVideo?.name}
        open={!!previewVideo}
        onCancel={() => setPreviewVideo(null)}
        width={800}
        footer={null}
      >
        {previewVideo && (
          <div>
            <div
              style={{
                height: 400,
                background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
                borderRadius: 8,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                marginBottom: 16,
              }}
            >
              <div style={{ textAlign: 'center', color: '#fff' }}>
                <VideoCameraOutlined style={{ fontSize: 64 }} />
                <Title level={4} style={{ color: '#fff' }}>360° Video Player</Title>
                <Text style={{ color: 'rgba(255,255,255,0.6)' }}>
                  Interactive 360° preview will render here
                </Text>
              </div>
            </div>
            <Row gutter={[16, 16]}>
              <Col span={8}>
                <Statistic title="Duration" value={formatDuration(previewVideo.duration)} />
              </Col>
              <Col span={8}>
                <Statistic title="Resolution" value={previewVideo.resolution} />
              </Col>
              <Col span={8}>
                <Statistic
                  title="Status"
                  value={transcodingStatusConfig[previewVideo.transcodingStatus]?.label || 'Unknown'}
                />
              </Col>
            </Row>
          </div>
        )}
      </Modal>
    </div>
  );
}
