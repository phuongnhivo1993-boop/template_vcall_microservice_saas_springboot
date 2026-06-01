'use client';

import { useState } from 'react';
import { Table, Card, Input, Button, Space, Typography, Modal, Form, Select, message, Tag } from 'antd';
import { PlusOutlined, SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;

interface Customer {
  id: string;
  name: string;
  email: string;
  phone: string;
  status: 'active' | 'inactive' | 'blocked';
  plan: string;
  totalCalls: number;
  lastContact: string;
}

const customersData: Customer[] = [
  { id: 'C-001', name: 'John Smith', email: 'john@example.com', phone: '+1 (555) 123-4567', status: 'active', plan: 'Premium', totalCalls: 45, lastContact: '2026-06-01' },
  { id: 'C-002', name: 'Alice Brown', email: 'alice@example.com', phone: '+1 (555) 234-5678', status: 'active', plan: 'Enterprise', totalCalls: 128, lastContact: '2026-06-01' },
  { id: 'C-003', name: 'Bob Wilson', email: 'bob@example.com', phone: '+1 (555) 345-6789', status: 'inactive', plan: 'Basic', totalCalls: 12, lastContact: '2026-05-28' },
  { id: 'C-004', name: 'Carol Davis', email: 'carol@example.com', phone: '+1 (555) 456-7890', status: 'active', plan: 'Premium', totalCalls: 67, lastContact: '2026-06-01' },
  { id: 'C-005', name: 'Tom Harris', email: 'tom@example.com', phone: '+1 (555) 567-8901', status: 'blocked', plan: 'Basic', totalCalls: 3, lastContact: '2026-05-15' },
  { id: 'C-006', name: 'Diana Clark', email: 'diana@example.com', phone: '+1 (555) 678-9012', status: 'active', plan: 'Enterprise', totalCalls: 203, lastContact: '2026-06-01' },
];

export default function CustomersPage() {
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form] = Form.useForm();

  const filtered = customersData.filter((c) =>
    c.name.toLowerCase().includes(search.toLowerCase()) ||
    c.email.toLowerCase().includes(search.toLowerCase()) ||
    c.phone.includes(search)
  );

  const handleAddCustomer = () => {
    form.validateFields().then((values) => {
      message.success(`Customer ${values.name} added successfully`);
      form.resetFields();
      setModalOpen(false);
    });
  };

  const columns = [
    { title: 'Name', dataIndex: 'name', key: 'name', render: (name: string) => <a>{name}</a> },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Phone', dataIndex: 'phone', key: 'phone' },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'active' ? 'green' : status === 'inactive' ? 'orange' : 'red'}>
          {status.toUpperCase()}
        </Tag>
      ),
    },
    { title: 'Plan', dataIndex: 'plan', key: 'plan' },
    { title: 'Total Calls', dataIndex: 'totalCalls', key: 'totalCalls' },
    { title: 'Last Contact', dataIndex: 'lastContact', key: 'lastContact' },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>Customers</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          Add Customer
        </Button>
      </div>
      <Card>
        <Space style={{ marginBottom: 16 }}>
          <Input
            placeholder="Search customers..."
            prefix={<SearchOutlined />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            allowClear
            style={{ width: 300 }}
          />
        </Space>
        <Table dataSource={filtered} columns={columns} rowKey="id" pagination={{ pageSize: 10 }} />
      </Card>

      <Modal
        title="Add Customer"
        open={modalOpen}
        onOk={handleAddCustomer}
        onCancel={() => { setModalOpen(false); form.resetFields(); }}
        okText="Add"
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="Phone" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="plan" label="Plan" rules={[{ required: true }]}>
            <Select options={[
              { value: 'Basic', label: 'Basic' },
              { value: 'Premium', label: 'Premium' },
              { value: 'Enterprise', label: 'Enterprise' },
            ]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
