'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Row, Col, Typography, Tag, Spin, Alert, Button, Space, Tabs, Statistic, Empty, Descriptions, Modal, Form, Input, Select, message } from 'antd';
import {
  UserOutlined, FileTextOutlined, BookOutlined, PhoneOutlined,
  ClockCircleOutlined, CheckCircleOutlined,
  ExclamationCircleOutlined,
  SafetyOutlined, MessageOutlined, StarOutlined,
} from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import { ticketsApi, knowledgeBaseApi, settingsApi } from '@/lib/api';
import { useRouter } from 'next/navigation';

const { Title, Text, Paragraph } = Typography;

interface PortalTicket {
  id: string;
  subject: string;
  status: string;
  priority: string;
  category: string;
  createdAt: string;
  updatedAt: string;
  assignedTo: string;
}

interface KnowledgeArticle {
  id: string;
  title: string;
  category: string;
  summary: string;
  views: number;
  updatedAt: string;
}

const statusColorMap: Record<string, string> = {
  OPEN: 'blue', RESOLVED: 'green', CLOSED: 'default', IN_PROGRESS: 'orange', ESCALATED: 'red',
};

const priorityColorMap: Record<string, string> = {
  HIGH: 'red', MEDIUM: 'orange', LOW: 'green',
};

export default function PortalPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('tickets');
  const [tickets, setTickets] = useState<PortalTicket[]>([]);
  const [articles, setArticles] = useState<KnowledgeArticle[]>([]);
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [ticketModalOpen, setTicketModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [ticketForm] = Form.useForm();

  const fetchPortalData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [ticketsRes, articlesRes, profileRes] = await Promise.all([
        ticketsApi.list({ page: 0, size: 10 }),
        knowledgeBaseApi.list({ page: 0, size: 100 }).catch(() => ({ data: { content: [] } })),
        settingsApi.getProfile().catch(() => null),
      ]);
      setTickets(ticketsRes.data?.data?.content || ticketsRes.data?.content || []);
      setArticles(articlesRes.data?.data?.content || articlesRes.data?.content || []);
      if (profileRes) setProfile(profileRes.data?.data || profileRes.data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load portal data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchPortalData(); }, [fetchPortalData]);

  const handleCreateTicket = () => {
    ticketForm.resetFields();
    setTicketModalOpen(true);
  };

  const handleTicketSubmit = async (values: any) => {
    setSubmitting(true);
    try {
      await ticketsApi.create(values);
      message.success('Yêu cầu hỗ trợ đã được tạo');
      setTicketModalOpen(false);
      ticketForm.resetFields();
      fetchPortalData();
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Tạo yêu cầu thất bại');
    } finally {
      setSubmitting(false);
    }
  };

  const openTicketCount = tickets.filter((t) => t.status === 'OPEN' || t.status === 'IN_PROGRESS').length;
  const resolvedTicketCount = tickets.filter((t) => t.status === 'RESOLVED' || t.status === 'CLOSED').length;
  const profileData = profile || {};

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={fetchPortalData}>Retry</Button>} />;
  }

  const tabItems = [
    {
      key: 'tickets',
      label: <span><FileTextOutlined /> Tickets của tôi</span>,
      children: (
        <CommonTable<PortalTicket>
          columns={[
            { title: 'Mã', dataIndex: 'id', key: 'id', render: (id: string) => <a onClick={() => router.push(`/tickets/${id}`)}>{id}</a> },
            { title: 'Tiêu đề', dataIndex: 'subject', key: 'subject' },
            { title: 'Danh mục', dataIndex: 'category', key: 'category', render: (c: string) => <Tag>{c}</Tag> },
            { title: 'Mức độ', dataIndex: 'priority', key: 'priority', render: (p: string) => <Tag color={priorityColorMap[p]}>{p}</Tag> },
            { title: 'Trạng thái', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={s === 'OPEN' ? 'blue' : s === 'RESOLVED' ? 'green' : 'default'}>{s}</Tag> },
            { title: 'Ngày tạo', dataIndex: 'createdAt', key: 'createdAt', render: (t: string) => new Date(t).toLocaleString('vi-VN') },
            { title: 'Phụ trách', dataIndex: 'assignedTo', key: 'assignedTo' },
          ]}
          dataSource={tickets}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 5 }}
          title="Danh sách yêu cầu hỗ trợ"
        />
      ),
    },
    {
      key: 'knowledge',
      label: <span><BookOutlined /> Kiến thức</span>,
      children: (
        <div>
          <Row gutter={[16, 16]}>
            {articles.map((article) => (
              <Col xs={24} sm={12} lg={8} key={article.id}>
                <Card
                  hoverable
                  size="small"
                  title={<span><BookOutlined /> {article.title}</span>}
                  extra={<Tag>{article.category}</Tag>}
                >
                  <Typography.Paragraph ellipsis={{ rows: 2 }} type="secondary">
                    {article.summary}
                  </Typography.Paragraph>
                  <Space>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}><StarOutlined /> {article.views || 0} lượt xem</Typography.Text>
                    <Typography.Text type="secondary" style={{ fontSize: 12 }}>Cập nhật: {article.updatedAt ? new Date(article.updatedAt).toLocaleDateString('vi-VN') : '-'}</Typography.Text>
                  </Space>
                </Card>
              </Col>
            ))}
          </Row>
          {articles.length === 0 && (
            <Empty description="Chưa có bài viết nào" />
          )}
        </div>
      ),
    },
    {
      key: 'profile',
      label: <span><UserOutlined /> Thông tin cá nhân</span>,
      children: (
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={16}>
            <Card title="Thông tin tài khoản">
              <Descriptions column={{ xs: 1, sm: 2 }} bordered size="small">
                <Descriptions.Item label="Họ tên"><Typography.Text strong>{profileData.fullName || profileData.name || '-'}</Typography.Text></Descriptions.Item>
                <Descriptions.Item label="Mã khách hàng"><Tag color="blue">{profileData.customerCode || profileData.id || '-'}</Tag></Descriptions.Item>
                <Descriptions.Item label="Email">{profileData.email || '-'}</Descriptions.Item>
                <Descriptions.Item label="Số điện thoại">{profileData.phone || '-'}</Descriptions.Item>
                <Descriptions.Item label="Công ty">{profileData.company || '-'}</Descriptions.Item>
                <Descriptions.Item label="Phân khúc"><Tag color="gold">{profileData.segment || 'Standard'}</Tag></Descriptions.Item>
                <Descriptions.Item label="Trạng thái"><Tag color={profileData.status === 'ACTIVE' ? 'green' : 'red'}>{profileData.status || 'ACTIVE'}</Tag></Descriptions.Item>
              </Descriptions>
            </Card>

            <Card title="Thông tin liên hệ" style={{ marginTop: 16 }}>
              <Descriptions column={1} bordered size="small">
                <Descriptions.Item label="Email nhận thông báo">{profileData.email || '-'}</Descriptions.Item>
                <Descriptions.Item label="Số điện thoại ưu tiên">{profileData.phone || '-'}</Descriptions.Item>
                <Descriptions.Item label="Ngôn ngữ ưu tiên">Tiếng Việt</Descriptions.Item>
                <Descriptions.Item label="Phương thức liên hệ ưu tiên">Email</Descriptions.Item>
              </Descriptions>
            </Card>
          </Col>

          <Col xs={24} lg={8}>
            <Card title="Thống kê hoạt động">
              <Row gutter={[12, 12]}>
                <Col span={12}><Statistic title="Tổng yêu cầu" value={tickets.length} prefix={<FileTextOutlined />} valueStyle={{ fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Đã giải quyết" value={resolvedTicketCount} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#52c41a', fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Đang xử lý" value={openTicketCount} prefix={<ClockCircleOutlined />} valueStyle={{ color: '#1890ff', fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Bài viết" value={articles.length} prefix={<BookOutlined />} valueStyle={{ fontSize: 20 }} /></Col>
              </Row>
            </Card>

            <Card title="Hỗ trợ nhanh" style={{ marginTop: 16 }}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" icon={<FileTextOutlined />} block onClick={handleCreateTicket}>Tạo yêu cầu hỗ trợ</Button>
                <Button icon={<PhoneOutlined />} block>Gọi hỗ trợ: 1900 1234</Button>
                <Button icon={<MessageOutlined />} block>Chat trực tuyến</Button>
              </Space>
            </Card>
          </Col>
        </Row>
      ),
    },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }}>
        <Space>
          <Typography.Title level={4} style={{ margin: 0 }}>
            <SafetyOutlined /> Cổng thông tin khách hàng
          </Typography.Title>
          <Tag color="blue">{profileData.customerCode || '-'}</Tag>
        </Space>
        <Typography.Text type="secondary">Xin chào, <Typography.Text strong>{profileData.fullName || profileData.name || 'Khách hàng'}</Typography.Text></Typography.Text>
      </Space>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}>
          <Card size="small">
            <Statistic title="Yêu cầu đang mở" value={openTicketCount} prefix={<ExclamationCircleOutlined />} valueStyle={{ color: openTicketCount > 0 ? '#faad14' : '#52c41a', fontSize: 24 }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small">
            <Statistic title="Đã giải quyết" value={resolvedTicketCount} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#52c41a', fontSize: 24 }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small">
            <Statistic title="Bài viết" value={articles.length} prefix={<BookOutlined />} valueStyle={{ fontSize: 24 }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small">
            <Statistic title="Cuộc gọi gần đây" value={0} prefix={<PhoneOutlined />} valueStyle={{ fontSize: 24 }} />
          </Card>
        </Col>
      </Row>

      <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />

      <Modal
        title="Tạo yêu cầu hỗ trợ"
        open={ticketModalOpen}
        onCancel={() => setTicketModalOpen(false)}
        onOk={() => ticketForm.submit()}
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form form={ticketForm} layout="vertical" onFinish={handleTicketSubmit}>
          <Form.Item name="subject" label="Tiêu đề" rules={[{ required: true, message: 'Vui lòng nhập tiêu đề' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Mô tả" rules={[{ required: true, message: 'Vui lòng nhập mô tả' }]}>
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item name="priority" label="Mức độ ưu tiên" rules={[{ required: true }]} initialValue="MEDIUM">
            <Select options={[
              { value: 'LOW', label: 'Thấp' },
              { value: 'MEDIUM', label: 'Trung bình' },
              { value: 'HIGH', label: 'Cao' },
            ]} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
