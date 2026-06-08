'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Card, Button, Typography, Space, Tag, Upload, Modal, Form,
  Input, Select, message, Row, Col, Statistic, Tooltip, Popconfirm,
  Progress, Empty,
} from 'antd';
import {
  PlusOutlined, UploadOutlined, DeleteOutlined, VideoCameraOutlined,
  BlockOutlined, PictureOutlined, AudioOutlined, EyeOutlined,
  DownloadOutlined, ReloadOutlined, SearchOutlined,
} from '@ant-design/icons';
import { xrAssetsApi, type XRAsset } from '@/lib/api/xr-api';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const assetTypeConfig: Record<string, { color: string; icon: React.ReactNode; label: string }> = {
  '360_VIDEO': { color: 'purple', icon: <VideoCameraOutlined />, label: '360 Video' },
  '3D_MODEL': { color: 'blue', icon: <BlockOutlined />, label: '3D Model' },
  'IMAGE': { color: 'green', icon: <PictureOutlined />, label: 'Image' },
  'AUDIO': { color: 'orange', icon: <AudioOutlined />, label: 'Audio' },
};

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

export default function AssetsPage() {
  const [assets, setAssets] = useState<XRAsset[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploading, setUploading] = useState(false);
  const [typeFilter, setTypeFilter] = useState<string>('ALL');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedAssets, setSelectedAssets] = useState<string[]>([]);

  const fetchAssets = useCallback(async (params?: Record<string, any>) => {
    setLoading(true);
    setError(null);
    try {
      const query: Record<string, any> = { page: 0, size: 200, ...params };
      const res = await xrAssetsApi.list(query);
      setAssets(res.data?.data?.content || res.data?.content || []);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load assets');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchAssets(); }, [fetchAssets]);

  const handleUpload = async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    setUploading(true);
    setUploadProgress(0);
    try {
      await xrAssetsApi.upload(formData, (pct) => setUploadProgress(pct));
      message.success(`${file.name} uploaded successfully`);
      setUploadModalOpen(false);
      fetchAssets();
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      message.error(axiosError?.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = (asset: XRAsset) => {
    Modal.confirm({
      title: 'Delete Asset',
      content: `Are you sure you want to delete "${asset.name}"? This action cannot be undone.`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await xrAssetsApi.delete(asset.id);
          message.success('Asset deleted');
          fetchAssets();
        } catch {
          message.error('Failed to delete asset');
        }
      },
    });
  };

  const handleBulkDelete = () => {
    Modal.confirm({
      title: 'Delete Multiple Assets',
      content: `Are you sure you want to delete ${selectedAssets.length} selected assets?`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await xrAssetsApi.bulkDelete(selectedAssets);
          message.success(`Deleted ${selectedAssets.length} assets`);
          setSelectedAssets([]);
          fetchAssets();
        } catch {
          message.error('Failed to delete assets');
        }
      },
    });
  };

  const filteredAssets = assets.filter((a) => {
    const matchesType = typeFilter === 'ALL' || a.type === typeFilter;
    const matchesSearch = !searchKeyword || a.name.toLowerCase().includes(searchKeyword.toLowerCase());
    return matchesType && matchesSearch;
  });

  const typeStats = assets.reduce((acc, a) => {
    acc[a.type] = (acc[a.type] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  return (
    <div>
      <Title level={3}>Asset Library</Title>

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={6}>
          <Card>
            <Statistic title="Total Assets" value={assets.length} prefix={<BlockOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card>
            <Statistic title="360 Videos" value={typeStats['360_VIDEO'] || 0} prefix={<VideoCameraOutlined />} valueStyle={{ color: '#722ed1' }} />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card>
            <Statistic title="3D Models" value={typeStats['3D_MODEL'] || 0} prefix={<BlockOutlined />} valueStyle={{ color: '#1677ff' }} />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card>
            <Statistic title="Images & Audio" value={(typeStats['IMAGE'] || 0) + (typeStats['AUDIO'] || 0)} prefix={<PictureOutlined />} valueStyle={{ color: '#52c41a' }} />
          </Card>
        </Col>
      </Row>

      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16, flexWrap: 'wrap', gap: 8 }}>
          <Space>
            <Button type="primary" icon={<UploadOutlined />} onClick={() => setUploadModalOpen(true)}>
              Upload Asset
            </Button>
            {selectedAssets.length > 0 && (
              <Popconfirm title={`Delete ${selectedAssets.length} assets?`} onConfirm={handleBulkDelete}>
                <Button danger>Delete Selected ({selectedAssets.length})</Button>
              </Popconfirm>
            )}
            <Button icon={<ReloadOutlined />} onClick={() => fetchAssets()}>Refresh</Button>
          </Space>
          <Space>
            <Input
              placeholder="Search assets..."
              prefix={<SearchOutlined />}
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              style={{ width: 200 }}
              allowClear
            />
            <Select
              value={typeFilter}
              onChange={setTypeFilter}
              style={{ width: 140 }}
              options={[
                { value: 'ALL', label: 'All Types' },
                { value: '360_VIDEO', label: '360 Videos' },
                { value: '3D_MODEL', label: '3D Models' },
                { value: 'IMAGE', label: 'Images' },
                { value: 'AUDIO', label: 'Audio' },
              ]}
            />
          </Space>
        </div>

        {filteredAssets.length === 0 ? (
          <Empty description="No assets found" />
        ) : (
          <Row gutter={[16, 16]}>
            {filteredAssets.map((asset) => {
              const typeInfo = assetTypeConfig[asset.type] || assetTypeConfig['IMAGE'];
              return (
                <Col key={asset.id} xs={24} sm={12} md={8} lg={6}>
                  <Card
                    hoverable
                    style={{ borderRadius: 8 }}
                    cover={
                      <div
                        style={{
                          height: 160,
                          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontSize: 40,
                          color: '#fff',
                        }}
                      >
                        {typeInfo.icon}
                      </div>
                    }
                    actions={[
                      <Tooltip title="Preview" key="preview">
                        <EyeOutlined />
                      </Tooltip>,
                      <Tooltip title="Download" key="download">
                        <DownloadOutlined />
                      </Tooltip>,
                      <Popconfirm title="Delete this asset?" onConfirm={() => handleDelete(asset)} key="delete">
                        <DeleteOutlined style={{ color: '#ff4d4f' }} />
                      </Popconfirm>,
                    ]}
                  >
                    <Card.Meta
                      title={
                        <Space>
                          <Text ellipsis style={{ maxWidth: 140 }}>{asset.name}</Text>
                          <Tag color={typeInfo.color} style={{ fontSize: 10 }}>{typeInfo.label}</Tag>
                        </Space>
                      }
                      description={
                        <Space direction="vertical" size={0}>
                          <Text type="secondary" style={{ fontSize: 12 }}>{formatFileSize(asset.fileSize)}</Text>
                          <Text type="secondary" style={{ fontSize: 12 }}>{dayjs(asset.createdAt).format('DD/MM/YYYY')}</Text>
                        </Space>
                      }
                    />
                  </Card>
                </Col>
              );
            })}
          </Row>
        )}
      </Card>

      <Modal
        title="Upload Asset"
        open={uploadModalOpen}
        onCancel={() => { setUploadModalOpen(false); setUploadProgress(0); }}
        footer={null}
        width={500}
      >
        <Upload.Dragger
          name="file"
          multiple
          customRequest={({ file }) => handleUpload(file as File)}
          showUploadList={false}
          accept=".mp4,.webm,.glb,.gltf,.obj,.fbx,.jpg,.jpeg,.png,.gif,.mp3,.wav,.ogg"
        >
          <p className="ant-upload-drag-icon">
            <UploadOutlined style={{ fontSize: 48, color: '#1677ff' }} />
          </p>
          <p className="ant-upload-text">Click or drag files to upload</p>
          <p className="ant-upload-hint">
            Supports 360° videos, 3D models (GLB/GLTF/OBJ/FBX), images, and audio files
          </p>
        </Upload.Dragger>
        {uploading && (
          <Progress percent={uploadProgress} style={{ marginTop: 16 }} />
        )}
      </Modal>
    </div>
  );
}
