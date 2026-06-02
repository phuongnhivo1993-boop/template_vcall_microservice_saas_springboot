'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Descriptions, Tag, Spin, Alert, Button, Space, Typography, Row, Col, Tabs, Statistic, Avatar, Divider, List, Timeline, Empty, Badge } from 'antd';
import { ArrowLeftOutlined, MailOutlined, PhoneOutlined, EnvironmentOutlined, UserOutlined,
         MessageOutlined, FileTextOutlined,
         StarOutlined, ShoppingCartOutlined, ClockCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CustomerTimeline from '@/components/common/CustomerTimeline';
import { customersApi, ticketsApi } from '@/lib/api';

const { Title, Text } = Typography;

export default function CustomerDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [customer, setCustomer] = useState<any>(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    fetchCustomer360();
  }, [params.id]);

  const fetchCustomer360 = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await customersApi.getById(params.id as string);
      setCustomer(res.data?.data || generateMockCustomer());
    } catch {
      setCustomer(generateMockCustomer());
    } finally {
      setLoading(false);
    }
  };

  const generateMockCustomer = () => ({
    id: params.id,
    fullName: 'Nguyen Van Anh',
    email: 'nguyenvananh@email.com',
    phone: '0909 123 456',
    customerCode: 'KH-00123',
    gender: 'Male',
    dateOfBirth: '1990-05-15',
    company: 'TechCorp Vietnam',
    position: 'Senior Developer',
    nationality: 'Vietnam',
    status: 'ACTIVE',
    totalCalls: 45,
    totalTickets: 12,
    totalRevenue: 15000000,
    lifetimeValue: 45000000,
    satisfactionScore: 92,
    lastContactAt: new Date().toISOString(),
    segment: 'VIP',
    tags: ['tech', 'premium', 'long-term'],
    contacts: [
      { id: 1, name: 'Nguyen Van Anh', email: 'personal@email.com', phone: '0909 123 456', role: 'Primary' },
      { id: 2, name: 'Tran Thi B', email: 'work@email.com', phone: '0912 345 678', role: 'Work' },
    ],
    addresses: [
      { id: 1, street: '123 Nguyen Hue', city: 'Ho Chi Minh', state: '', zipCode: '70000', country: 'Vietnam', type: 'Office' },
    ],
  });

  const mockTimeline = [
    { id: '1', type: 'call' as const, title: 'Cuộc gọi đến - Hỗ trợ kỹ thuật', description: 'Khách hàng gọi về lỗi kết nối internet', timestamp: new Date(Date.now() - 3600000).toISOString(), status: 'COMPLETED', agent: 'Tuan Anh' },
    { id: '2', type: 'ticket' as const, title: 'Ticket #TK-2026-0456', description: 'Yêu cầu nâng cấp gói cước doanh nghiệp', timestamp: new Date(Date.now() - 86400000).toISOString(), status: 'OPEN', agent: 'System' },
    { id: '3', type: 'chat' as const, title: 'Chat hỗ trợ trực tuyến', description: 'Tư vấn gói dịch vụ internet tốc độ cao', timestamp: new Date(Date.now() - 172800000).toISOString(), status: 'RESOLVED' },
    { id: '4', type: 'email' as const, title: 'Email - Báo giá dịch vụ', description: 'Gửi báo giá gói dịch vụ doanh nghiệp', timestamp: new Date(Date.now() - 259200000).toISOString(), status: 'SENT' },
    { id: '5', type: 'call' as const, title: 'Cuộc gọi đi - Chăm sóc khách hàng', description: 'Gọi chúc mừng sinh nhật khách hàng', timestamp: new Date(Date.now() - 604800000).toISOString(), status: 'COMPLETED', agent: 'Mai Anh' },
  ];

  const mockCalls = [
    { id: '1', direction: 'INBOUND', callerNumber: '0909123456', duration: 485, startTime: new Date(Date.now() - 3600000).toISOString(), status: 'COMPLETED', agentName: 'Tuan Anh', disposition: 'RESOLVED' },
    { id: '2', direction: 'OUTBOUND', callerNumber: '0909123456', duration: 320, startTime: new Date(Date.now() - 604800000).toISOString(), status: 'COMPLETED', agentName: 'Mai Anh', disposition: 'FOLLOW_UP' },
  ];

  const mockTickets = [
    { id: 'TK-2026-0456', title: 'Yêu cầu nâng cấp gói cước', priority: 'HIGH', status: 'OPEN', createdAt: new Date(Date.now() - 86400000).toISOString(), assignedTo: 'Tuan Anh' },
    { id: 'TK-2026-0450', title: 'Lỗi kết nối mạng', priority: 'MEDIUM', status: 'RESOLVED', createdAt: new Date(Date.now() - 604800000).toISOString(), assignedTo: 'Ky Thuat' },
  ];

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" tip="Loading customer 360..." /></div>;
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={fetchCustomer360}>Retry</Button>} />;
  }

  if (!customer) return <Empty description="Customer not found" />;

  const tabItems = [
    {
      key: 'overview',
      label: <span><UserOutlined /> Tổng quan</span>,
      children: (
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={16}>
            <Card title="Thông tin khách hàng">
              <Descriptions column={{ xs: 1, sm: 2 }} bordered size="small">
                <Descriptions.Item label="Họ tên"><Text strong>{customer.fullName}</Text></Descriptions.Item>
                <Descriptions.Item label="Mã KH"><Tag color="blue">{customer.customerCode}</Tag></Descriptions.Item>
                <Descriptions.Item label="Email">{customer.email}</Descriptions.Item>
                <Descriptions.Item label="Số điện thoại"><PhoneOutlined /> {customer.phone}</Descriptions.Item>
                <Descriptions.Item label="Giới tính">{customer.gender || '-'}</Descriptions.Item>
                <Descriptions.Item label="Ngày sinh">{customer.dateOfBirth || '-'}</Descriptions.Item>
                <Descriptions.Item label="Công ty">{customer.company || '-'}</Descriptions.Item>
                <Descriptions.Item label="Chức vụ">{customer.position || '-'}</Descriptions.Item>
                <Descriptions.Item label="Quốc tịch">{customer.nationality || '-'}</Descriptions.Item>
                <Descriptions.Item label="Phân khúc"><Tag color="gold">{customer.segment || 'Standard'}</Tag></Descriptions.Item>
                <Descriptions.Item label="Trạng thái"><Tag color={customer.status === 'ACTIVE' ? 'green' : 'red'}>{customer.status}</Tag></Descriptions.Item>
                <Descriptions.Item label="Tags">{customer.tags?.map((t: string) => <Tag key={t}>{t}</Tag>)}</Descriptions.Item>
                <Descriptions.Item label="Lần cuối liên hệ">{customer.lastContactAt ? new Date(customer.lastContactAt).toLocaleString('vi-VN') : '-'}</Descriptions.Item>
              </Descriptions>
            </Card>

            <Card title="Thông tin liên hệ" style={{ marginTop: 16 }}>
              <CommonTable
                columns={[
                  { title: 'Họ tên', dataIndex: 'name', key: 'name' },
                  { title: 'Email', dataIndex: 'email', key: 'email' },
                  { title: 'SĐT', dataIndex: 'phone', key: 'phone' },
                  { title: 'Vai trò', dataIndex: 'role', key: 'role', render: (r: string) => <Tag>{r}</Tag> },
                ]}
                dataSource={customer.contacts || []}
                rowKey="id"
                pagination={false}
              />
            </Card>

            <Card title="Địa chỉ" style={{ marginTop: 16 }}>
              <CommonTable
                columns={[
                  { title: 'Đường', dataIndex: 'street', key: 'street' },
                  { title: 'Thành phố', dataIndex: 'city', key: 'city' },
                  { title: 'Quốc gia', dataIndex: 'country', key: 'country' },
                  { title: 'Loại', dataIndex: 'type', key: 'type', render: (t: string) => <Tag>{t}</Tag> },
                ]}
                dataSource={customer.addresses || []}
                rowKey="id"
                pagination={false}
              />
            </Card>
          </Col>

          <Col xs={24} lg={8}>
            <Card title="Chỉ số KPIs">
              <Row gutter={[12, 12]}>
                <Col span={12}><Statistic title="Tổng cuộc gọi" value={customer.totalCalls || 0} prefix={<PhoneOutlined />} valueStyle={{ fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Ticket" value={customer.totalTickets || 0} prefix={<FileTextOutlined />} valueStyle={{ fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Doanh thu" value={customer.totalRevenue ? `${(customer.totalRevenue / 1000000).toFixed(0)}M` : '0'} prefix={<ShoppingCartOutlined />} valueStyle={{ fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Giá trị suốt đời" value={customer.lifetimeValue ? `${(customer.lifetimeValue / 1000000).toFixed(0)}M` : '0'} prefix={<StarOutlined />} valueStyle={{ fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Hài lòng" value={`${customer.satisfactionScore || 0}%`} prefix={<CheckCircleOutlined />} valueStyle={{ color: customer.satisfactionScore >= 90 ? '#52c41a' : '#faad14', fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Phân khúc" value={customer.segment || 'Standard'} prefix={<UserOutlined />} valueStyle={{ fontSize: 20 }} /></Col>
              </Row>
            </Card>

            <Card title="Hoạt động gần đây" style={{ marginTop: 16 }}>
              <CustomerTimeline events={mockTimeline} height={350} />
            </Card>
          </Col>
        </Row>
      ),
    },
    {
      key: 'calls',
      label: <span><PhoneOutlined /> Cuộc gọi ({mockCalls.length})</span>,
      children: (
        <CommonTable
          columns={[
            { title: 'Hướng', dataIndex: 'direction', key: 'direction', render: (d: string) => <Tag color={d === 'INBOUND' ? 'blue' : 'green'}>{d === 'INBOUND' ? 'Đến' : 'Đi'}</Tag> },
            { title: 'Số điện thoại', dataIndex: 'callerNumber', key: 'callerNumber' },
            { title: 'Thời gian', dataIndex: 'startTime', key: 'startTime', render: (t: string) => new Date(t).toLocaleString('vi-VN') },
            { title: 'Thời lượng', dataIndex: 'duration', key: 'duration', render: (d: number) => `${Math.floor(d / 60)}:${(d % 60).toString().padStart(2, '0')}` },
            { title: 'Agent', dataIndex: 'agentName', key: 'agentName' },
            { title: 'Kết quả', dataIndex: 'disposition', key: 'disposition', render: (d: string) => <Tag>{d}</Tag> },
          ]}
          dataSource={mockCalls}
          rowKey="id"
          pagination={{ pageSize: 5 }}
        />
      ),
    },
    {
      key: 'tickets',
      label: <span><FileTextOutlined /> Tickets ({mockTickets.length})</span>,
      children: (
        <CommonTable
          columns={[
            { title: 'Mã', dataIndex: 'id', key: 'id', render: (id: string) => <a>{id}</a> },
            { title: 'Tiêu đề', dataIndex: 'title', key: 'title' },
            { title: 'Mức độ', dataIndex: 'priority', key: 'priority', render: (p: string) => <Tag color={p === 'HIGH' ? 'red' : p === 'MEDIUM' ? 'orange' : 'green'}>{p}</Tag> },
            { title: 'Trạng thái', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={s === 'OPEN' ? 'blue' : s === 'RESOLVED' ? 'green' : 'orange'}>{s}</Tag> },
            { title: 'Ngày tạo', dataIndex: 'createdAt', key: 'createdAt', render: (t: string) => new Date(t).toLocaleString('vi-VN') },
            { title: 'Phụ trách', dataIndex: 'assignedTo', key: 'assignedTo' },
          ]}
          dataSource={mockTickets}
          rowKey="id"
          pagination={{ pageSize: 5 }}
        />
      ),
    },
    {
      key: 'timeline',
      label: <span><ClockCircleOutlined /> Lịch sử</span>,
      children: (
        <Card>
          <CustomerTimeline events={mockTimeline} />
        </Card>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => router.back()}>Quay lại</Button>
        <Title level={4} style={{ margin: 0 }}>Customer 360: {customer.fullName}</Title>
        <Tag color="blue">{customer.customerCode}</Tag>
      </Space>

      <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />
    </div>
  );
}
