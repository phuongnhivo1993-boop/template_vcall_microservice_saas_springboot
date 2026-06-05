'use client';

import React, { useState } from 'react';
import { Card, Table, Button, Tag, Space, Modal, Form, Input, Select, InputNumber, message, Avatar, Tooltip } from 'antd';
import { PlusOutlined, VideoCameraOutlined, UserOutlined, StopOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';

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
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form] = Form.useForm();

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
              <Button type="primary" size="small" icon={<VideoCameraOutlined />} />
            </Tooltip>
          )}
          {record.status !== 'ENDED' && (
            <Tooltip title="End Room">
              <Button danger size="small" icon={<StopOutlined />} />
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  const data: CollaborationRoom[] = [
    {
      id: '1',
      name: 'VR Training Session - Onboarding',
      sceneName: 'Office Tour VR',
      hostUser: 'Admin',
      maxParticipants: 10,
      currentParticipants: 3,
      status: 'ACTIVE',
      createdAt: '2026-06-05 10:00',
    },
    {
      id: '2',
      name: 'Virtual Showroom Demo',
      sceneName: 'Product Showroom',
      hostUser: 'Sales Team',
      maxParticipants: 5,
      currentParticipants: 0,
      status: 'WAITING',
      createdAt: '2026-06-05 14:30',
    },
  ];

  const handleCreate = (values: any) => {
    console.log('Creating room:', values);
    message.success('Collaboration room created');
    setIsModalOpen(false);
    form.resetFields();
  };

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
        <Table columns={columns} dataSource={data} rowKey="id" />
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
