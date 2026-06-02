'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Descriptions, Tag, Spin, Alert, Button, Space, Typography, Row, Col, Tabs } from 'antd';
import { ArrowLeftOutlined, MailOutlined, PhoneOutlined, EnvironmentOutlined, UserOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import { customersApi, ticketsApi } from '@/lib/api';

const { Title } = Typography;

const statusColors: Record<string, string> = {
  active: 'green', ACTIVE: 'green',
  inactive: 'orange', INACTIVE: 'orange',
  blocked: 'red', BLOCKED: 'red',
};

export default function CustomerDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [customer, setCustomer] = useState<any>(null);
  const [recentTickets, setRecentTickets] = useState<any[]>([]);
  const [ticketsLoading, setTicketsLoading] = useState(false);

  useEffect(() => {
    fetchCustomer();
  }, [params.id]);

  const fetchCustomer = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await customersApi.getById(params.id as string);
      setCustomer(res.data?.data || res.data);
      fetchRecentTickets();
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load customer details');
    } finally {
      setLoading(false);
    }
  };

  const fetchRecentTickets = async () => {
    setTicketsLoading(true);
    try {
      const res = await ticketsApi.list({ customerId: params.id, page: 0, size: 10 });
      const data = res.data;
      setRecentTickets(data?.content || data?.data || []);
    } catch {
      setRecentTickets([]);
    } finally {
      setTicketsLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error) {
    return (
      <Alert
        message="Error"
        description={error}
        type="error"
        showIcon
        action={<Button onClick={fetchCustomer}>Retry</Button>}
      />
    );
  }

  if (!customer) return null;

  const contacts = customer.contacts || [];
  const addresses = customer.addresses || [];
  const tags = customer.tags || [];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => router.back()}>Back</Button>
        <Title level={4} style={{ margin: 0 }}>Customer Details</Title>
      </Space>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title="Basic Information">
            <Descriptions column={{ xs: 1, sm: 2 }}>
              <Descriptions.Item label="Name">{customer.fullName || customer.name}</Descriptions.Item>
              <Descriptions.Item label="Email">{customer.email || '-'}</Descriptions.Item>
              <Descriptions.Item label="Phone">{customer.phone || '-'}</Descriptions.Item>
              <Descriptions.Item label="Company">{customer.company || customer.companyName || '-'}</Descriptions.Item>
              <Descriptions.Item label="Gender">{customer.gender || '-'}</Descriptions.Item>
              <Descriptions.Item label="Date of Birth">{customer.dateOfBirth || customer.dob ? new Date(customer.dateOfBirth || customer.dob).toLocaleDateString() : '-'}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={statusColors[customer.status?.toUpperCase?.()] || statusColors[customer.status] || 'default'}>
                  {(customer.status || 'UNKNOWN').toUpperCase()}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Plan">{customer.plan || customer.planName || '-'}</Descriptions.Item>
              <Descriptions.Item label="Total Calls">{customer.totalCalls ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="Last Contact">{customer.lastContact ? new Date(customer.lastContact).toLocaleString() : '-'}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          {tags.length > 0 && (
            <Card title="Tags" style={{ marginBottom: 16 }}>
              <Space wrap>
                {tags.map((tag: any, i: number) => (
                  <Tag key={i} color="processing">{tag.name || tag}</Tag>
                ))}
              </Space>
            </Card>
          )}
          <Card title="Summary">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="Contacts">{contacts.length}</Descriptions.Item>
              <Descriptions.Item label="Addresses">{addresses.length}</Descriptions.Item>
              <Descriptions.Item label="Recent Tickets">{recentTickets.length}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
      </Row>

      <Tabs
        style={{ marginTop: 16 }}
        items={[
          {
            key: 'contacts',
            label: `Contacts (${contacts.length})`,
            children: (
              <CommonTable
                columns={[
                  { title: 'Name', dataIndex: 'name', key: 'name' },
                  { title: 'Email', dataIndex: 'email', key: 'email' },
                  { title: 'Phone', dataIndex: 'phone', key: 'phone' },
                  { title: 'Role', dataIndex: 'role', key: 'role', render: (r: string) => r || '-' },
                ]}
                dataSource={contacts}
                loading={false}
                pagination={false}
                rowKey={(r: any) => r.id || r.email}
              />
            ),
          },
          {
            key: 'addresses',
            label: `Addresses (${addresses.length})`,
            children: (
              <CommonTable
                columns={[
                  { title: 'Street', dataIndex: 'street', key: 'street' },
                  { title: 'City', dataIndex: 'city', key: 'city' },
                  { title: 'State', dataIndex: 'state', key: 'state' },
                  { title: 'Zip', dataIndex: 'zipCode', key: 'zipCode' },
                  { title: 'Country', dataIndex: 'country', key: 'country' },
                  { title: 'Type', dataIndex: 'type', key: 'type', render: (t: string) => t || '-' },
                ]}
                dataSource={addresses}
                loading={false}
                pagination={false}
                rowKey={(r: any) => r.id || r.street}
              />
            ),
          },
          {
            key: 'tickets',
            label: `Recent Tickets (${recentTickets.length})`,
            children: (
              <CommonTable
                columns={[
                  { title: 'ID', dataIndex: 'id', key: 'id' },
                  { title: 'Subject', dataIndex: 'subject', key: 'subject' },
                  { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag>{s}</Tag> },
                  { title: 'Priority', dataIndex: 'priority', key: 'priority', render: (p: string) => <Tag>{p}</Tag> },
                  { title: 'Created', dataIndex: 'created', key: 'created', render: (d: string) => d ? new Date(d).toLocaleString() : '-' },
                ]}
                dataSource={recentTickets}
                loading={ticketsLoading}
                pagination={false}
                rowKey="id"
              />
            ),
          },
        ]}
      />
    </div>
  );
}
