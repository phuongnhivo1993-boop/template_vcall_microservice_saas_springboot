'use client';

import { useState, useEffect, useCallback } from 'react';
import { Row, Col, Card, Input, List, Avatar, Badge, Typography, Space, Tag, Segmented, Spin, Alert, Button, Empty } from 'antd';
import {
  MessageOutlined, MailOutlined, PhoneOutlined, SmileOutlined,
  SearchOutlined, SendOutlined,
  FacebookOutlined, GlobalOutlined,
} from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

const CHANNELS = [
  { key: 'all', label: 'All Channels', icon: <GlobalOutlined /> },
  { key: 'chat', label: 'Chat', icon: <MessageOutlined />, color: '#52c41a' },
  { key: 'email', label: 'Email', icon: <MailOutlined />, color: '#722ed1' },
  { key: 'sms', label: 'SMS', icon: <SmileOutlined />, color: '#1890ff' },
  { key: 'facebook', label: 'Facebook', icon: <FacebookOutlined />, color: '#1877F2' },
  { key: 'zalo', label: 'Zalo', icon: <MessageOutlined />, color: '#0068FF' },
  { key: 'call', label: 'Call Notes', icon: <PhoneOutlined />, color: '#faad14' },
];

interface Conversation {
  id: string;
  channel: string;
  customerName: string;
  customerAvatar?: string;
  lastMessage: string;
  lastMessageAt: string;
  unread: number;
  status: string;
  assignedTo?: string;
  tags?: string[];
}

export default function InboxPage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [channelFilter, setChannelFilter] = useState<string>('all');
  const [searchText, setSearchText] = useState('');
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [selectedConv, setSelectedConv] = useState<Conversation | null>(null);
  const [messages, setMessages] = useState<any[]>([]);
  const [messageInput, setMessageInput] = useState('');

  const loadConversations = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const allConversations: Conversation[] = [
        { id: 'ch1', channel: 'chat', customerName: 'Nguyen Van Anh', lastMessage: 'Tôi cần hỗ trợ về dịch vụ internet', lastMessageAt: new Date().toISOString(), unread: 2, status: 'ACTIVE', tags: ['internet', 'urgent'] },
        { id: 'ch2', channel: 'chat', customerName: 'Tran Thi Binh', lastMessage: 'Cảm ơn bạn đã hỗ trợ!', lastMessageAt: new Date(Date.now() - 1800000).toISOString(), unread: 0, status: 'ACTIVE' },
        { id: 'em1', channel: 'email', customerName: 'Le Van Cuong', lastMessage: 'Re: Yêu cầu báo giá gói dịch vụ doanh nghiệp', lastMessageAt: new Date(Date.now() - 3600000).toISOString(), unread: 1, status: 'PENDING', tags: ['quote'] },
        { id: 'em2', channel: 'email', customerName: 'Pham Thi Dung', lastMessage: 'Khiếu nại về hóa đơn tháng 5', lastMessageAt: new Date(Date.now() - 7200000).toISOString(), unread: 0, status: 'RESOLVED', tags: ['complaint', 'billing'] },
        { id: 'sms1', channel: 'sms', customerName: 'Hoang Van Em', lastMessage: 'Mã OTP của bạn là 123456', lastMessageAt: new Date(Date.now() - 300000).toISOString(), unread: 0, status: 'SENT' },
        { id: 'sms2', channel: 'sms', customerName: 'Vu Thi Phuong', lastMessage: 'Quý khách đã đăng ký thành công gói cước 4G', lastMessageAt: new Date(Date.now() - 86400000).toISOString(), unread: 0, status: 'DELIVERED' },
        { id: 'fb1', channel: 'facebook', customerName: 'Minh Anh Nguyen', lastMessage: 'Shop ơi, mình đặt hàng khi nào có?', lastMessageAt: new Date(Date.now() - 600000).toISOString(), unread: 3, status: 'ACTIVE', tags: ['facebook'] },
        { id: 'zl1', channel: 'zalo', customerName: 'Thanh Huyen', lastMessage: 'Em chào anh, bên mình còn phòng hội nghị không ạ?', lastMessageAt: new Date(Date.now() - 900000).toISOString(), unread: 1, status: 'ACTIVE', tags: ['booking'] },
        { id: 'cl1', channel: 'call', customerName: 'Dinh Van Giang', lastMessage: 'Gọi để hủy dịch vụ - đã xử lý', lastMessageAt: new Date(Date.now() - 4800000).toISOString(), unread: 0, status: 'COMPLETED', tags: ['cancellation'] },
        { id: 'cl2', channel: 'call', customerName: 'Nguyen Thi Hanh', lastMessage: 'Khách hàng gọi hỏi về khuyến mãi tháng 6', lastMessageAt: new Date(Date.now() - 5400000).toISOString(), unread: 0, status: 'MISSED', tags: ['promo'] },
      ];
      setConversations(allConversations);
    } catch (err) {
      setError('Failed to load conversations');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadConversations();
  }, [channelFilter, loadConversations]);

  const getChannelIcon = (channel: string) => {
    const ch = CHANNELS.find(c => c.key === channel);
    return ch?.icon || <MessageOutlined />;
  };

  const getChannelColor = (channel: string) => {
    const ch = CHANNELS.find(c => c.key === channel);
    return ch?.color || '#1890ff';
  };

  const filteredConversations = conversations.filter(conv => {
    if (channelFilter !== 'all' && conv.channel !== channelFilter) return false;
    if (searchText && !conv.customerName.toLowerCase().includes(searchText.toLowerCase()) &&
        !conv.lastMessage.toLowerCase().includes(searchText.toLowerCase())) return false;
    return true;
  });

  const handleSelectConversation = (conv: Conversation) => {
    setSelectedConv(conv);
    setMessages([
      { id: 'm1', content: 'Xin chào, tôi cần hỗ trợ', senderType: 'customer', sentAt: new Date(Date.now() - 600000).toISOString() },
      { id: 'm2', content: 'Dạ vâng, anh/chị cần hỗ trợ vấn đề gì ạ?', senderType: 'agent', sentAt: new Date(Date.now() - 540000).toISOString() },
      { id: 'm3', content: conv.lastMessage, senderType: 'customer', sentAt: conv.lastMessageAt },
    ]);
  };

  const handleSendMessage = () => {
    if (!messageInput.trim() || !selectedConv) return;
    const newMsg = {
      id: Date.now().toString(),
      content: messageInput.trim(),
      senderType: 'agent' as const,
      sentAt: new Date().toISOString(),
    };
    setMessages(prev => [...prev, newMsg]);
    setMessageInput('');
  };

  const channelOptions = CHANNELS.map(ch => ({
    label: <Space><span style={{ color: ch.color }}>{ch.icon}</span>{ch.label}</Space>,
    value: ch.key,
  }));

  if (loading) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}><Spin size="large" tip="Loading inbox..." /></div>;
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={loadConversations}>Retry</Button>} />;
  }

  return (
    <div style={{ height: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column' }}>
      <Card size="small" style={{ marginBottom: 8 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Segmented options={channelOptions} value={channelFilter} onChange={(val) => setChannelFilter(val as string)} />
          </Col>
          <Col>
            <Input
              prefix={<SearchOutlined />}
              placeholder="Search conversations..."
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              style={{ width: 280 }}
              allowClear
            />
          </Col>
        </Row>
      </Card>

      <Row gutter={8} style={{ flex: 1, overflow: 'hidden' }}>
        <Col xs={24} md={selectedConv ? 9 : 24} style={{ height: '100%', overflow: 'auto' }}>
          <Card size="small" style={{ height: '100%' }}>
            <List
              dataSource={filteredConversations}
              locale={{ emptyText: <Empty description="No conversations found" image={Empty.PRESENTED_IMAGE_SIMPLE} /> }}
              renderItem={(conv) => (
                <List.Item
                  onClick={() => handleSelectConversation(conv)}
                  style={{
                    cursor: 'pointer',
                    background: selectedConv?.id === conv.id ? '#e6f7ff' : 'transparent',
                    padding: '8px 12px',
                    borderRadius: 6,
                    marginBottom: 2,
                  }}
                  actions={[
                    <Badge key="unread" count={conv.unread} size="small" />,
                  ]}
                >
                  <List.Item.Meta
                    avatar={
                      <Badge count={<Avatar size={16} icon={getChannelIcon(conv.channel)} style={{ backgroundColor: getChannelColor(conv.channel), fontSize: 10 }} />} offset={[-5, 30]}>
                        <Avatar style={{ backgroundColor: getChannelColor(conv.channel) }}>
                          {conv.customerName.charAt(0).toUpperCase()}
                        </Avatar>
                      </Badge>
                    }
                    title={
                      <Space>
                        <Text strong={conv.unread > 0}>{conv.customerName}</Text>
                        <Tag color={getChannelColor(conv.channel)} style={{ fontSize: 10, lineHeight: '16px', padding: '0 4px' }}>
                          {conv.channel.toUpperCase()}
                        </Tag>
                      </Space>
                    }
                    description={
                      <div>
                        <Paragraph ellipsis={{ rows: 1 }} style={{ margin: 0, fontSize: 12 }}>
                          {conv.lastMessage}
                        </Paragraph>
                        <Text type="secondary" style={{ fontSize: 11 }}>
                          {new Date(conv.lastMessageAt).toLocaleString('vi-VN')}
                        </Text>
                      </div>
                    }
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>

        {selectedConv && (
          <Col xs={24} md={15} style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Card size="small" style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
              <div style={{ borderBottom: '1px solid #f0f0f0', paddingBottom: 8, marginBottom: 8 }}>
                <Space>
                  <Avatar style={{ backgroundColor: getChannelColor(selectedConv.channel) }}>
                    {selectedConv.customerName.charAt(0).toUpperCase()}
                  </Avatar>
                  <div>
                    <Text strong>{selectedConv.customerName}</Text>
                    <div>
                      <Tag color={getChannelColor(selectedConv.channel)}>{selectedConv.channel.toUpperCase()}</Tag>
                      {selectedConv.tags?.map(tag => <Tag key={tag}>{tag}</Tag>)}
                    </div>
                  </div>
                </Space>
              </div>

              <div style={{ flex: 1, overflow: 'auto', padding: '8px 0' }}>
                {messages.map((msg) => (
                  <div key={msg.id} style={{
                    display: 'flex',
                    justifyContent: msg.senderType === 'agent' ? 'flex-end' : 'flex-start',
                    marginBottom: 12,
                  }}>
                    <div style={{
                      maxWidth: '70%',
                      padding: '8px 12px',
                      borderRadius: 12,
                      background: msg.senderType === 'agent' ? '#1890ff' : '#f0f0f0',
                      color: msg.senderType === 'agent' ? '#fff' : '#000',
                    }}>
                      <div>{msg.content}</div>
                      <div style={{ fontSize: 10, marginTop: 4, opacity: 0.7, textAlign: 'right' }}>
                        {new Date(msg.sentAt).toLocaleTimeString('vi-VN')}
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div style={{ borderTop: '1px solid #f0f0f0', paddingTop: 8 }}>
                <Input.TextArea
                  value={messageInput}
                  onChange={(e) => setMessageInput(e.target.value)}
                  placeholder="Type a message..."
                  autoSize={{ minRows: 2, maxRows: 4 }}
                  onPressEnter={(e) => {
                    if (!e.shiftKey) {
                      e.preventDefault();
                      handleSendMessage();
                    }
                  }}
                  style={{ marginBottom: 8 }}
                />
                <Button type="primary" icon={<SendOutlined />} onClick={handleSendMessage} disabled={!messageInput.trim()}>
                  Send
                </Button>
              </div>
            </Card>
          </Col>
        )}
      </Row>
    </div>
  );
}
