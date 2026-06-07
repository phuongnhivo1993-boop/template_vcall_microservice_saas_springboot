'use client';

import { useState, useCallback, useEffect } from 'react';
import {
  Card, Button, Space, Typography, Tooltip, Divider, Input, Form,
  Select, InputNumber, message, Spin, Drawer, List, Tag, Tabs, Modal,
} from 'antd';
import {
  SaveOutlined, EyeOutlined, CloudUploadOutlined, UndoOutlined,
  RedoOutlined, PlusOutlined, DeleteOutlined, DragOutlined,
  VideoCameraOutlined, BlockOutlined, PictureOutlined, AudioOutlined,
  LinkOutlined, InfoCircleOutlined, BulbOutlined, AimOutlined,
  ExpandOutlined, CompressOutlined, RotateRightOutlined, SettingOutlined,
} from '@ant-design/icons';
import { useRouter, useParams } from 'next/navigation';
import { xrScenesApi, type XRScene, type XRHotspot } from '@/lib/api/xr-api';

const { Title, Text } = Typography;
const { Sider } = require('antd').Layout;

interface AssetItem {
  id: string;
  name: string;
  type: '360_VIDEO' | '3D_MODEL' | 'IMAGE' | 'AUDIO';
  thumbnailUrl?: string;
}

const assetTypeIcons: Record<string, React.ReactNode> = {
  '360_VIDEO': <VideoCameraOutlined />,
  '3D_MODEL': <BlockOutlined />,
  'IMAGE': <PictureOutlined />,
  'AUDIO': <AudioOutlined />,
};

const hotspotTypeOptions = [
  { value: 'LINK', label: 'Scene Link', icon: <LinkOutlined /> },
  { value: 'INFO', label: 'Info Point', icon: <InfoCircleOutlined /> },
  { value: 'VIDEO', label: 'Video', icon: <VideoCameraOutlined /> },
  { value: 'AUDIO', label: 'Audio', icon: <AudioOutlined /> },
  { value: 'IMAGE', label: 'Image', icon: <PictureOutlined /> },
];

export default function SceneBuilderPage() {
  const router = useRouter();
  const params = useParams();
  const sceneId = params.id as string;

  const [scene, setScene] = useState<XRScene | null>(null);
  const [hotspots, setHotspots] = useState<XRHotspot[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [selectedHotspot, setSelectedHotspot] = useState<XRHotspot | null>(null);
  const [assetDrawerOpen, setAssetDrawerOpen] = useState(true);
  const [propertiesOpen, setPropertiesOpen] = useState(true);
  const [assetFilter, setAssetFilter] = useState<string>('ALL');
  const [undoStack, setUndoStack] = useState<XRHotspot[][]>([]);
  const [redoStack, setRedoStack] = useState<XRHotspot[][]>([]);
  const [previewModalOpen, setPreviewModalOpen] = useState(false);

  const [assetLibrary] = useState<AssetItem[]>([
    { id: 'a1', name: 'Ocean 360', type: '360_VIDEO', thumbnailUrl: '' },
    { id: 'a2', name: 'Mountain Scene', type: '360_VIDEO', thumbnailUrl: '' },
    { id: 'a3', name: 'Robot Model', type: '3D_MODEL', thumbnailUrl: '' },
    { id: 'a4', name: 'Car Model', type: '3D_MODEL', thumbnailUrl: '' },
    { id: 'a5', name: 'Background', type: 'IMAGE', thumbnailUrl: '' },
    { id: 'a6', name: 'Ambient Sound', type: 'AUDIO', thumbnailUrl: '' },
  ]);

  const fetchScene = useCallback(async () => {
    try {
      const res = await xrScenesApi.getById(sceneId);
      const data = res.data?.data || res.data;
      setScene(data);
      setHotspots(data.hotspots || []);
    } catch {
      message.error('Failed to load scene');
    } finally {
      setLoading(false);
    }
  }, [sceneId]);

  useEffect(() => { fetchScene(); }, [fetchScene]);

  const pushUndo = (currentState: XRHotspot[]) => {
    setUndoStack((prev) => [...prev.slice(-49), currentState]);
    setRedoStack([]);
  };

  const handleUndo = () => {
    if (undoStack.length === 0) return;
    const prev = undoStack[undoStack.length - 1];
    setUndoStack((s) => s.slice(0, -1));
    setRedoStack((r) => [...r, hotspots]);
    setHotspots(prev);
  };

  const handleRedo = () => {
    if (redoStack.length === 0) return;
    const next = redoStack[redoStack.length - 1];
    setRedoStack((r) => r.slice(0, -1));
    setUndoStack((s) => [...s, hotspots]);
    setHotspots(next);
  };

  const handleSave = async () => {
    if (!scene) return;
    setSaving(true);
    try {
      await xrScenesApi.update(sceneId, { ...scene, hotspots });
      message.success('Scene saved');
    } catch {
      message.error('Failed to save scene');
    } finally {
      setSaving(false);
    }
  };

  const handlePublish = async () => {
    try {
      await xrScenesApi.publish(sceneId);
      message.success('Scene published');
      fetchScene();
    } catch {
      message.error('Failed to publish scene');
    }
  };

  const handleAddHotspot = () => {
    pushUndo(hotspots);
    const newHotspot: XRHotspot = {
      id: `h-${Date.now()}`,
      sceneId,
      name: `Hotspot ${hotspots.length + 1}`,
      type: 'INFO',
      position: { x: 0, y: 0, z: -2 },
      rotation: { x: 0, y: 0, z: 0 },
      scale: { x: 1, y: 1, z: 1 },
    };
    setHotspots([...hotspots, newHotspot]);
    setSelectedHotspot(newHotspot);
  };

  const handleDeleteHotspot = (hotspotId: string) => {
    pushUndo(hotspots);
    setHotspots(hotspots.filter((h) => h.id !== hotspotId));
    if (selectedHotspot?.id === hotspotId) setSelectedHotspot(null);
  };

  const handleUpdateHotspot = (hotspotId: string, updates: Partial<XRHotspot>) => {
    pushUndo(hotspots);
    setHotspots(hotspots.map((h) => (h.id === hotspotId ? { ...h, ...updates } : h)));
    if (selectedHotspot?.id === hotspotId) {
      setSelectedHotspot({ ...selectedHotspot, ...updates });
    }
  };

  const filteredAssets = assetFilter === 'ALL'
    ? assetLibrary
    : assetLibrary.filter((a) => a.type === assetFilter);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Spin size="large" tip="Loading scene..." />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 120px)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Space>
          <Title level={4} style={{ margin: 0 }}>{scene?.name || 'Scene Builder'}</Title>
          <Tag color={scene?.status === 'PUBLISHED' ? 'green' : 'default'}>{scene?.status || 'DRAFT'}</Tag>
        </Space>
        <Space>
          <Tooltip title="Undo">
            <Button icon={<UndoOutlined />} onClick={handleUndo} disabled={undoStack.length === 0} />
          </Tooltip>
          <Tooltip title="Redo">
            <Button icon={<RedoOutlined />} onClick={handleRedo} disabled={redoStack.length === 0} />
          </Tooltip>
          <Button icon={<SaveOutlined />} onClick={handleSave} loading={saving}>Save</Button>
          <Button icon={<EyeOutlined />} onClick={() => setPreviewModalOpen(true)}>Preview</Button>
          <Button type="primary" icon={<CloudUploadOutlined />} onClick={handlePublish}>Publish</Button>
        </Space>
      </div>

      <div style={{ display: 'flex', flex: 1, gap: 16, minHeight: 0 }}>
        <div
          style={{
            width: 280,
            flexShrink: 0,
            background: '#fafafa',
            borderRadius: 8,
            padding: 16,
            overflowY: 'auto',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <Text strong>Asset Library</Text>
          </div>
          <Select
            value={assetFilter}
            onChange={setAssetFilter}
            style={{ width: '100%', marginBottom: 12 }}
            options={[
              { value: 'ALL', label: 'All Types' },
              { value: '360_VIDEO', label: '360 Videos' },
              { value: '3D_MODEL', label: '3D Models' },
              { value: 'IMAGE', label: 'Images' },
              { value: 'AUDIO', label: 'Audio' },
            ]}
          />
          <List
            dataSource={filteredAssets}
            size="small"
            renderItem={(item) => (
              <List.Item
                style={{ padding: '8px', cursor: 'grab', borderRadius: 6, marginBottom: 4 }}
                className="hover:bg-gray-100"
              >
                <Space>
                  <span style={{ fontSize: 18 }}>{assetTypeIcons[item.type]}</span>
                  <Text ellipsis style={{ maxWidth: 160 }}>{item.name}</Text>
                </Space>
              </List.Item>
            )}
          />
        </div>

        <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <div
            style={{
              flex: 1,
              background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
              borderRadius: 8,
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              position: 'relative',
              overflow: 'hidden',
            }}
          >
            <div style={{ textAlign: 'center', color: '#fff' }}>
              <div style={{ fontSize: 64, marginBottom: 16 }}><AimOutlined /></div>
              <Title level={4} style={{ color: '#fff', margin: 0 }}>WebGL Preview</Title>
              <Text style={{ color: 'rgba(255,255,255,0.6)' }}>
                3D scene preview will render here
              </Text>
              <Text style={{ color: 'rgba(255,255,255,0.4)', display: 'block', marginTop: 8 }}>
                {hotspots.length} hotspot(s) placed
              </Text>
            </div>
            {hotspots.map((h) => (
              <Tooltip key={h.id} title={h.name}>
                <div
                  style={{
                    position: 'absolute',
                    left: `${50 + h.position.x * 10}%`,
                    top: `${50 + h.position.y * 10}%`,
                    width: 24,
                    height: 24,
                    borderRadius: '50%',
                    background: selectedHotspot?.id === h.id ? '#ff4d4f' : '#1677ff',
                    border: '2px solid #fff',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: 10,
                    color: '#fff',
                    boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
                    transform: 'translate(-50%, -50%)',
                  }}
                  onClick={() => setSelectedHotspot(h)}
                >
                  {h.type === 'LINK' ? <LinkOutlined /> : h.type === 'INFO' ? <InfoCircleOutlined /> : <BulbOutlined />}
                </div>
              </Tooltip>
            ))}
          </div>

          <div style={{ marginTop: 12, display: 'flex', gap: 8 }}>
            <Button icon={<PlusOutlined />} onClick={handleAddHotspot}>Add Hotspot</Button>
            <Button icon={<ExpandOutlined />} onClick={() => message.info('Fit view applied')}>Fit View</Button>
            <Button icon={<CompressOutlined />} onClick={() => message.info('Camera reset')}>Reset Camera</Button>
          </div>
        </div>

        <div
          style={{
            width: 320,
            flexShrink: 0,
            background: '#fafafa',
            borderRadius: 8,
            padding: 16,
            overflowY: 'auto',
          }}
        >
          <Text strong style={{ display: 'block', marginBottom: 12 }}>Properties</Text>

          {selectedHotspot ? (
            <Form layout="vertical" size="small">
              <Form.Item label="Name">
                <Input
                  value={selectedHotspot.name}
                  onChange={(e) => handleUpdateHotspot(selectedHotspot.id, { name: e.target.value })}
                />
              </Form.Item>
              <Form.Item label="Type">
                <Select
                  value={selectedHotspot.type}
                  onChange={(val) => handleUpdateHotspot(selectedHotspot.id, { type: val })}
                  options={hotspotTypeOptions.map((o) => ({ value: o.value, label: o.label }))}
                />
              </Form.Item>
              <Divider>Position</Divider>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
                <Form.Item label="X">
                  <InputNumber
                    value={selectedHotspot.position.x}
                    onChange={(val) => handleUpdateHotspot(selectedHotspot.id, {
                      position: { ...selectedHotspot.position, x: val || 0 },
                    })}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="Y">
                  <InputNumber
                    value={selectedHotspot.position.y}
                    onChange={(val) => handleUpdateHotspot(selectedHotspot.id, {
                      position: { ...selectedHotspot.position, y: val || 0 },
                    })}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="Z">
                  <InputNumber
                    value={selectedHotspot.position.z}
                    onChange={(val) => handleUpdateHotspot(selectedHotspot.id, {
                      position: { ...selectedHotspot.position, z: val || 0 },
                    })}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </div>
              <Divider>Rotation</Divider>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
                <Form.Item label="X">
                  <InputNumber
                    value={selectedHotspot.rotation.x}
                    onChange={(val) => handleUpdateHotspot(selectedHotspot.id, {
                      rotation: { ...selectedHotspot.rotation, x: val || 0 },
                    })}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="Y">
                  <InputNumber
                    value={selectedHotspot.rotation.y}
                    onChange={(val) => handleUpdateHotspot(selectedHotspot.id, {
                      rotation: { ...selectedHotspot.rotation, y: val || 0 },
                    })}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="Z">
                  <InputNumber
                    value={selectedHotspot.rotation.z}
                    onChange={(val) => handleUpdateHotspot(selectedHotspot.id, {
                      rotation: { ...selectedHotspot.rotation, z: val || 0 },
                    })}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </div>
              <Divider>Scale</Divider>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8 }}>
                <Form.Item label="X">
                  <InputNumber
                    value={selectedHotspot.scale.x}
                    min={0.1}
                    step={0.1}
                    onChange={(val) => handleUpdateHotspot(selectedHotspot.id, {
                      scale: { ...selectedHotspot.scale, x: val || 1 },
                    })}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="Y">
                  <InputNumber
                    value={selectedHotspot.scale.y}
                    min={0.1}
                    step={0.1}
                    onChange={(val) => handleUpdateHotspot(selectedHotspot.id, {
                      scale: { ...selectedHotspot.scale, y: val || 1 },
                    })}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
                <Form.Item label="Z">
                  <InputNumber
                    value={selectedHotspot.scale.z}
                    min={0.1}
                    step={0.1}
                    onChange={(val) => handleUpdateHotspot(selectedHotspot.id, {
                      scale: { ...selectedHotspot.scale, z: val || 1 },
                    })}
                    style={{ width: '100%' }}
                  />
                </Form.Item>
              </div>
              {selectedHotspot.type === 'LINK' && (
                <>
                  <Divider>Link Target</Divider>
                  <Form.Item label="Target Scene">
                    <Input
                      value={selectedHotspot.targetSceneId || ''}
                      onChange={(e) => handleUpdateHotspot(selectedHotspot.id, { targetSceneId: e.target.value })}
                      placeholder="Scene ID"
                    />
                  </Form.Item>
                </>
              )}
              {(selectedHotspot.type === 'INFO') && (
                <Form.Item label="Content">
                  <Input.TextArea
                    rows={3}
                    value={selectedHotspot.content || ''}
                    onChange={(e) => handleUpdateHotspot(selectedHotspot.id, { content: e.target.value })}
                  />
                </Form.Item>
              )}
              <Divider />
              <Button
                danger
                icon={<DeleteOutlined />}
                onClick={() => handleDeleteHotspot(selectedHotspot.id)}
                style={{ width: '100%' }}
              >
                Delete Hotspot
              </Button>
            </Form>
          ) : (
            <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
              <SettingOutlined style={{ fontSize: 32, marginBottom: 8 }} />
              <Text type="secondary">Select a hotspot to edit properties</Text>
            </div>
          )}
        </div>
      </div>

      <Modal
        title="Scene Preview"
        open={previewModalOpen}
        onCancel={() => setPreviewModalOpen(false)}
        width={900}
        footer={null}
      >
        <div
          style={{
            height: 500,
            background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
            borderRadius: 8,
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
          }}
        >
          <div style={{ textAlign: 'center', color: '#fff' }}>
            <AimOutlined style={{ fontSize: 48 }} />
            <Title level={4} style={{ color: '#fff' }}>WebGL Preview Mode</Title>
            <Text style={{ color: 'rgba(255,255,255,0.6)' }}>
              Full 360° preview with hotspot interaction
            </Text>
          </div>
        </div>
      </Modal>
    </div>
  );
}
