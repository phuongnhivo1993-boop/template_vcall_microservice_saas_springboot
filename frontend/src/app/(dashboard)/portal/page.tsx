'use client';

import { useState } from 'react';
import { Card, Row, Col, Typography, Tag, Spin, Alert, Button, Space, Tabs, List, Avatar, Statistic, Empty, Descriptions, Divider, Badge, Result } from 'antd';
import {
  UserOutlined, FileTextOutlined, BookOutlined, PhoneOutlined,
  ClockCircleOutlined, CheckCircleOutlined,
  ExclamationCircleOutlined, SearchOutlined, SettingOutlined,
  SafetyOutlined, MessageOutlined, StarOutlined,
} from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';

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

const mockTickets: PortalTicket[] = [
  { id: 'TK-2026-0456', subject: 'Yêu cầu nâng cấp gói cước doanh nghiệp', status: 'OPEN', priority: 'HIGH', category: 'Dịch vụ', createdAt: '2026-06-01T10:00:00', updatedAt: '2026-06-01T14:30:00', assignedTo: 'Tuan Anh' },
  { id: 'TK-2026-0450', subject: 'Lỗi kết nối mạng khu vực Q1', status: 'RESOLVED', priority: 'MEDIUM', category: 'Kỹ thuật', createdAt: '2026-05-25T08:00:00', updatedAt: '2026-05-26T09:00:00', assignedTo: 'Ky Thuat' },
  { id: 'TK-2026-0440', subject: 'Thay đổi thông tin hóa đơn', status: 'CLOSED', priority: 'LOW', category: 'Hóa đơn', createdAt: '2026-05-20T15:00:00', updatedAt: '2026-05-22T11:00:00', assignedTo: 'Mai Anh' },
];

const mockKnowledgeBase: KnowledgeArticle[] = [
  { id: '1', title: 'Hướng dẫn cấu hình internet tốc độ cao', category: 'Internet', summary: 'Các bước cơ bản để cấu hình modem và kết nối internet tốc độ cao cho doanh nghiệp.', views: 1250, updatedAt: '2026-05-28' },
  { id: '2', title: 'Cách khắc phục lỗi mất kết nối thường gặp', category: 'Kỹ thuật', summary: 'Tổng hợp các lỗi mất kết nối phổ biến và cách khắc phục nhanh chóng.', views: 980, updatedAt: '2026-05-25' },
  { id: '3', title: 'Gói cước doanh nghiệp - So sánh & Lựa chọn', category: 'Dịch vụ', summary: 'So sánh chi tiết các gói cước doanh nghiệp để lựa chọn phù hợp nhất.', views: 2340, updatedAt: '2026-05-20' },
  { id: '4', title: 'Hướng dẫn tra cứu hóa đơn trực tuyến', category: 'Hóa đơn', summary: 'Các bước tra cứu và thanh toán hóa đơn trực tuyến nhanh chóng.', views: 3100, updatedAt: '2026-05-15' },
];

const mockProfile = {
  fullName: 'Nguyen Van Anh',
  email: 'nguyenvananh@email.com',
  phone: '0909 123 456',
  customerCode: 'KH-00123',
  company: 'TechCorp Vietnam',
  segment: 'VIP',
  status: 'ACTIVE',
  totalTickets: 12,
  resolvedTickets: 8,
  openTickets: 3,
  totalCalls: 45,
};

const statusColorMap: Record<string, string> = {
  OPEN: 'blue', RESOLVED: 'green', CLOSED: 'default', IN_PROGRESS: 'orange', ESCALATED: 'red',
};

const priorityColorMap: Record<string, string> = {
  HIGH: 'red', MEDIUM: 'orange', LOW: 'green',
};

export default function PortalPage() {
  const [activeTab, setActiveTab] = useState('tickets');

  const tabItems = [
    {
      key: 'tickets',
      label: <span><FileTextOutlined /> Tickets của tôi</span>,
      children: (
        <CommonTable<PortalTicket>
          columns={[
            { title: 'Mã', dataIndex: 'id', key: 'id', render: (id: string) => <a>{id}</a> },
            { title: 'Tiêu đề', dataIndex: 'subject', key: 'subject' },
            { title: 'Danh mục', dataIndex: 'category', key: 'category', render: (c: string) => <Tag>{c}</Tag> },
            { title: 'Mức độ', dataIndex: 'priority', key: 'priority', render: (p: string) => <Tag color={priorityColorMap[p]}>{p}</Tag> },
            { title: 'Trạng thái', dataIndex: 'status', key: 'status', render: (s: string) => <Badge status={s === 'OPEN' ? 'processing' : s === 'RESOLVED' ? 'success' : 'default'} text={s} /> },
            { title: 'Ngày tạo', dataIndex: 'createdAt', key: 'createdAt', render: (t: string) => new Date(t).toLocaleString('vi-VN') },
            { title: 'Phụ trách', dataIndex: 'assignedTo', key: 'assignedTo' },
          ]}
          dataSource={mockTickets}
          rowKey="id"
          pagination={{ pageSize: 5 }}
          title="Danh sách yêu cầu hỗ trợ"
          onCreateNew={() => {}}
          createLabel="Tạo yêu cầu mới"
        />
      ),
    },
    {
      key: 'knowledge',
      label: <span><BookOutlined /> Kiến thức</span>,
      children: (
        <div>
          <Row gutter={[16, 16]}>
            {mockKnowledgeBase.map((article) => (
              <Col xs={24} sm={12} lg={8} key={article.id}>
                <Card
                  hoverable
                  size="small"
                  title={<Space><BookOutlined /> {article.title}</Space>}
                  extra={<Tag>{article.category}</Tag>}
                >
                  <Paragraph ellipsis={{ rows: 2 }} type="secondary">
                    {article.summary}
                  </Paragraph>
                  <Space>
                    <Text type="secondary" style={{ fontSize: 12 }}><StarOutlined /> {article.views} lượt xem</Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>Cập nhật: {article.updatedAt}</Text>
                  </Space>
                </Card>
              </Col>
            ))}
          </Row>
          {mockKnowledgeBase.length === 0 && (
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
                <Descriptions.Item label="Họ tên"><Text strong>{mockProfile.fullName}</Text></Descriptions.Item>
                <Descriptions.Item label="Mã khách hàng"><Tag color="blue">{mockProfile.customerCode}</Tag></Descriptions.Item>
                <Descriptions.Item label="Email">{mockProfile.email}</Descriptions.Item>
                <Descriptions.Item label="Số điện thoại">{mockProfile.phone}</Descriptions.Item>
                <Descriptions.Item label="Công ty">{mockProfile.company}</Descriptions.Item>
                <Descriptions.Item label="Phân khúc"><Tag color="gold">{mockProfile.segment}</Tag></Descriptions.Item>
                <Descriptions.Item label="Trạng thái"><Tag color="green">{mockProfile.status}</Tag></Descriptions.Item>
              </Descriptions>
            </Card>

            <Card title="Thông tin liên hệ" style={{ marginTop: 16 }}>
              <Descriptions column={1} bordered size="small">
                <Descriptions.Item label="Email nhận thông báo">{mockProfile.email}</Descriptions.Item>
                <Descriptions.Item label="Số điện thoại ưu tiên">{mockProfile.phone}</Descriptions.Item>
                <Descriptions.Item label="Ngôn ngữ ưu tiên">Tiếng Việt</Descriptions.Item>
                <Descriptions.Item label="Phương thức liên hệ ưu tiên">Email</Descriptions.Item>
              </Descriptions>
            </Card>
          </Col>

          <Col xs={24} lg={8}>
            <Card title="Thống kê hoạt động">
              <Row gutter={[12, 12]}>
                <Col span={12}><Statistic title="Tổng yêu cầu" value={mockProfile.totalTickets} prefix={<FileTextOutlined />} valueStyle={{ fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Đã giải quyết" value={mockProfile.resolvedTickets} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#52c41a', fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Đang xử lý" value={mockProfile.openTickets} prefix={<ClockCircleOutlined />} valueStyle={{ color: '#1890ff', fontSize: 20 }} /></Col>
                <Col span={12}><Statistic title="Cuộc gọi" value={mockProfile.totalCalls} prefix={<PhoneOutlined />} valueStyle={{ fontSize: 20 }} /></Col>
              </Row>
            </Card>

            <Card title="Hỗ trợ nhanh" style={{ marginTop: 16 }}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="primary" icon={<FileTextOutlined />} block>Tạo yêu cầu hỗ trợ</Button>
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
          <Title level={4} style={{ margin: 0 }}>
            <SafetyOutlined /> Cổng thông tin khách hàng
          </Title>
          <Tag color="blue">{mockProfile.customerCode}</Tag>
        </Space>
        <Text type="secondary">Xin chào, <Text strong>{mockProfile.fullName}</Text></Text>
      </Space>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}>
          <Card size="small">
            <Statistic title="Yêu cầu đang mở" value={mockProfile.openTickets} prefix={<ExclamationCircleOutlined />} valueStyle={{ color: mockProfile.openTickets > 0 ? '#faad14' : '#52c41a', fontSize: 24 }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small">
            <Statistic title="Đã giải quyết" value={mockProfile.resolvedTickets} prefix={<CheckCircleOutlined />} valueStyle={{ color: '#52c41a', fontSize: 24 }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small">
            <Statistic title="Bài viết đã xem" value="12" prefix={<BookOutlined />} valueStyle={{ fontSize: 24 }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card size="small">
            <Statistic title="Cuộc gọi gần đây" value="5" prefix={<PhoneOutlined />} valueStyle={{ fontSize: 24 }} />
          </Card>
        </Col>
      </Row>

      <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />
    </div>
  );
}
