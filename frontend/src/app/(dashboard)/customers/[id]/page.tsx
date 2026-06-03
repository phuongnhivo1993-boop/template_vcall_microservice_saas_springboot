'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Card, Descriptions, Tag, Spin, Alert, Button, Space, Typography, Row, Col, Tabs, Statistic, Avatar, Divider, List, Timeline, Empty, Badge } from 'antd';
import { ArrowLeftOutlined, MailOutlined, PhoneOutlined, EnvironmentOutlined, UserOutlined,
         MessageOutlined, FileTextOutlined,
         StarOutlined, ShoppingCartOutlined, ClockCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CustomerTimeline from '@/components/common/CustomerTimeline';
import { customersApi, ticketsApi, callsApi } from '@/lib/api';

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

  const [customerCalls, setCustomerCalls] = useState<any[]>([]);
  const [customerTickets, setCustomerTickets] = useState<any[]>([]);
  const [timeline, setTimeline] = useState<any[]>([]);
  const [subLoading, setSubLoading] = useState(false);

  const fetchCustomer360 = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await customersApi.getById(params.id as string);
      setCustomer(res.data?.data || res.data);
      if (res.data?.data || res.data) {
        fetchCustomerSubData(params.id as string);
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to load customer');
    } finally {
      setLoading(false);
    }
  };

  const fetchCustomerSubData = async (customerId: string) => {
    setSubLoading(true);
    try {
      const [callsRes, ticketsRes] = await Promise.all([
        callsApi.getAll({ customerId, page: 0, size: 10 }).catch(() => ({ data: { content: [] } })),
        ticketsApi.list({ customerId, page: 0, size: 10 }).catch(() => ({ data: { content: [] } })),
      ]);
      setCustomerCalls(callsRes.data?.data?.content || callsRes.data?.content || []);
      const tix = ticketsRes.data?.data?.content || ticketsRes.data?.content || [];
      setCustomerTickets(tix);
      const tl: any[] = [];
      if (Array.isArray(callsRes.data?.data?.content)) {
        callsRes.data.data.content.forEach((c: any) => tl.push({
          id: c.id, type: 'call', title: `Cuộc gọi ${c.direction || ''}`,
          description: `SĐT: ${c.callerNumber || c.callee || ''}`,
          timestamp: c.startTime || c.time, status: c.status, agent: c.agentName,
        }));
      }
      if (Array.isArray(ticketsRes.data?.data?.content)) {
        ticketsRes.data.data.content.forEach((t: any) => tl.push({
          id: t.id, type: 'ticket', title: `Ticket ${t.id || ''}`,
          description: t.subject || t.title, timestamp: t.createdAt,
          status: t.status, agent: t.assignedTo,
        }));
      }
      tl.sort((a: any, b: any) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
      setTimeline(tl);
    } catch {
      // sub data fetch silently fails
    } finally {
      setSubLoading(false);
    }
  };

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
              <CustomerTimeline events={timeline} height={350} />
            </Card>
          </Col>
        </Row>
      ),
    },
    {
      key: 'calls',
      label: <span><PhoneOutlined /> Cuộc gọi ({customerCalls.length})</span>,
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
          dataSource={customerCalls}
          rowKey="id"
          loading={subLoading}
          pagination={{ pageSize: 5 }}
        />
      ),
    },
    {
      key: 'tickets',
      label: <span><FileTextOutlined /> Tickets ({customerTickets.length})</span>,
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
          dataSource={customerTickets}
          rowKey="id"
          loading={subLoading}
          pagination={{ pageSize: 5 }}
        />
      ),
    },
    {
      key: 'timeline',
      label: <span><ClockCircleOutlined /> Lịch sử</span>,
      children: (
        <Card>
          <CustomerTimeline events={timeline} />
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
