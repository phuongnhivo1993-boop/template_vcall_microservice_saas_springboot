'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Card, Table, Button, Tag, Space, Modal, Form, Input, Select, InputNumber, message, Avatar, Tooltip, Spin, Alert, Empty } from 'antd';
import { PlusOutlined, VideoCameraOutlined, UserOutlined, StopOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { xrCollaborationApi, xrScenesApi } from '@/lib/api/xr-api';

interface CollaborationRoom {
  id: string;
  name: string;
  sceneName: string;
  hostUser: string;
  maxParticipants: number;
  currentParticipants: number;
  status: 'WAITING' | 'ACTIVE' | 'ENDED';
  createdAt: string;
}

interface SceneOption {
  id: string;
  name: string;
}

const statusColors: Record<string, string> = {
  WAITING: 'default',
  ACTIVE: 'success',
  ENDED: 'error',
};

export default function CollaborationPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState<CollaborationRoom | null>(null);
  const [form] = Form.useForm();
  const [rooms, setRooms] = useState<CollaborationRoom[]>([]);
  const [scenes, setScenes] = useState<SceneOption[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchText, setSearchText] = useState('');
  const [statusFilter, setStatusFilter] = useState<string | undefined>();

  const fetchRooms = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await xrCollaborationApi.listRooms({ page: 0, size: 100 });
      const data = res.data;
      const content = data?.data?.content || data?.content || (Array.isArray(data) ? data : []);
      setRooms(content);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load rooms');
    } finally {
      setLoading(false);
    }
  }, []);

  const filteredRooms = rooms.filter(r => {
    if (searchText && !r.name.toLowerCase().includes(searchText.toLowerCase())) return false;
    if (statusFilter && r.status !== statusFilter) return false;
    return true;
  });

  const fetchScenes = useCallback(async () => {
    try {
      const res = await xrScenesApi.list({ page: 0, size: 100 });
      const data = res.data?.data || res.data;
      const content = data?.content || (Array.isArray(data) ? data : []);
      setScenes(content.map((s: any) => ({ id: s.id, name: s.name })));
    } catch {
      setScenes([]);
    }
  }, []);

  useEffect(() => {
    fetchRooms();
    fetchScenes();
  }, [fetchRooms, fetchScenes]);

  const handleEditRoom = (record: CollaborationRoom) => {
    setEditingRoom(record);
    form.setFieldsValue({ name: record.name, maxParticipants: record.maxParticipants });
    setIsModalOpen(true);
  };

  const handleDeleteRoom = (record: CollaborationRoom) => {
    Modal.confirm({
      title: 'Delete Room',
      content: `Delete "${record.name}"?`,
      okText: 'Delete',
      okType: 'danger',
      onOk: async () => {
        try {
          await xrCollaborationApi.deleteRoom(record.id);
          message.success('Room deleted');
          fetchRooms();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Failed to delete room');
        }
      },
    });
  };

  const handleEndRoom = (record: CollaborationRoom) => {
    Modal.confirm({
      title: 'End Room',
      content: `End "${record.name}"?`,
      onOk: async () => {
        try {
          await xrCollaborationApi.endRoom(record.id);
          message.success('Room ended');
          fetchRooms();
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Failed to end room');
        }
      },
    });
  };

  const handleJoinRoom = async (record: CollaborationRoom) => {
    try {
      await xrCollaborationApi.joinRoom(record.id);
      message.success('Joining room...');
      fetchRooms();
    } catch (err: any) {
      message.error(err?.response?.data?.message || 'Failed to join room');
    }
  };

  const handleCreate = async (values: any) => {
    try {
      await xrCollaborationApi.createRoom(values);
      message.success('Collaboration room created');
      setIsModalOpen(false);
      form.resetFields();
      fetchRooms();
    } catch (err: any) {
      message.error(err?.response?.data?.message || 'Failed to create room');
    }
  };

  const columns: ColumnsType<CollaborationRoom> = [
    {
      title: 'Room Name',
      dataIndex: 'name',
      key: 'name',
      render: (text) => <strong>{text}</strong>,
    },
    {
      title: 'Scene',
      dataIndex: 'sceneName',
      key: 'sceneName',
    },
    {
      title: 'Host',
      dataIndex: 'hostUser',
      key: 'hostUser',
    },
    {
      title: 'Participants',
      key: 'participants',
      render: (_, record) => (
        <Space>
          <Avatar.Group maxCount={3} size="small">
            {Array.from({ length: record.currentParticipants }).map((_, i) => (
              <Avatar key={i} size="small" icon={<UserOutlined />} />
            ))}
          </Avatar.Group>
          <span>{record.currentParticipants}/{record.maxParticipants}</span>
        </Space>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => <Tag color={statusColors[status]}>{status}</Tag>,
    },
    {
      title: 'Created',
      dataIndex: 'createdAt',
      key: 'createdAt',
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, record) => (
        <Space>
          {record.status === 'ACTIVE' && (
            <Tooltip title="Join Room">
              <Button type="primary" size="small" icon={<VideoCameraOutlined />} onClick={() => handleJoinRoom(record)} />
            </Tooltip>
          )}
          <Tooltip title="Edit Room">
            <Button size="small" icon={<EditOutlined />} onClick={() => handleEditRoom(record)} />
          </Tooltip>
          <Tooltip title="Delete Room">
            <Button danger size="small" icon={<DeleteOutlined />} onClick={() => handleDeleteRoom(record)} />
          </Tooltip>
          {record.status !== 'ENDED' && (
            <Tooltip title="End Room">
              <Button size="small" icon={<StopOutlined />} onClick={() => handleEndRoom(record)} />
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" tip="Loading collaboration rooms..." /></div>;
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={fetchRooms}>Retry</Button>} />;
  }

  return (
    <div className="p-6">
      <Card
        title="Multi-User VR Collaboration"
        extra={
          <Space>
            <Input.Search placeholder="Search rooms..." allowClear onSearch={setSearchText} style={{ width: 200 }} />
            <Select placeholder="Filter status" allowClear style={{ width: 130 }} onChange={setStatusFilter}
              options={[
                { value: 'WAITING', label: 'Waiting' },
                { value: 'ACTIVE', label: 'Active' },
                { value: 'ENDED', label: 'Ended' },
              ]}
            />
            <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingRoom(null); form.resetFields(); setIsModalOpen(true); }}>
              Create Room
            </Button>
          </Space>
        }
      >
        {filteredRooms.length === 0 ? (
          <Empty description="No collaboration rooms found" />
        ) : (
          <Table columns={columns} dataSource={filteredRooms} rowKey="id" pagination={{ pageSize: 10, showSizeChanger: true, showTotal: (total) => `${total} rooms` }} />
        )}
      </Card>

      <Modal
        title={editingRoom ? 'Edit Collaboration Room' : 'Create Collaboration Room'}
        open={isModalOpen}
        onCancel={() => setIsModalOpen(false)}
        onOk={() => form.submit()}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="name" label="Room Name" rules={[{ required: true }]}>
            <Input placeholder="Enter room name" />
          </Form.Item>
          <Form.Item name="sceneId" label="Scene" rules={[{ required: true }]}>
            <Select placeholder="Select a VR scene" showSearch optionFilterProp="label"
              options={scenes.map(s => ({ value: s.id, label: s.name }))} />
          </Form.Item>
          <Form.Item name="maxParticipants" label="Max Participants" initialValue={10}>
            <InputNumber min={2} max={50} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
