'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Card, Table, Button, Tag, Space, Modal, Form, Input, Select, InputNumber, message, Avatar, Tooltip, Spin, Alert, Empty } from 'antd';
import { PlusOutlined, VideoCameraOutlined, UserOutlined, StopOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useRouter } from 'next/navigation';
import { xrCollaborationApi } from '@/lib/api/xr-api';

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

const statusColors: Record<string, string> = {
  WAITING: 'default',
  ACTIVE: 'success',
  ENDED: 'error',
};

export default function CollaborationPage() {
  const router = useRouter();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();
  const [rooms, setRooms] = useState<CollaborationRoom[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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

  useEffect(() => { fetchRooms(); }, [fetchRooms]);

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
          {record.status !== 'ENDED' && (
            <Tooltip title="End Room">
              <Button danger size="small" icon={<StopOutlined />} onClick={() => handleEndRoom(record)} />
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
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>
            Create Room
          </Button>
        }
      >
        {rooms.length === 0 ? (
          <Empty description="No collaboration rooms found" />
        ) : (
          <Table columns={columns} dataSource={rooms} rowKey="id" />
        )}
      </Card>

      <Modal
        title="Create Collaboration Room"
        open={isModalOpen}
        onCancel={() => setIsModalOpen(false)}
        onOk={() => form.submit()}
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item name="name" label="Room Name" rules={[{ required: true }]}>
            <Input placeholder="Enter room name" />
          </Form.Item>
          <Form.Item name="sceneId" label="Scene" rules={[{ required: true }]}>
            <Select placeholder="Select a VR scene">
              <Select.Option value="scene1">Office Tour VR</Select.Option>
              <Select.Option value="scene2">Product Showroom</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="maxParticipants" label="Max Participants" initialValue={10}>
            <InputNumber min={2} max={50} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
