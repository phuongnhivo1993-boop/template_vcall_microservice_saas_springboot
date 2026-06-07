'use client';

import { useState, useEffect, useCallback } from 'react';
import { Row, Col, Card, Input, List, Avatar, Badge, Typography, Space, Tag, Segmented, Spin, Alert, Button, Empty, message } from 'antd';
import {
  MessageOutlined, MailOutlined, PhoneOutlined, SmileOutlined,
  SearchOutlined, SendOutlined,
  FacebookOutlined, GlobalOutlined,
} from '@ant-design/icons';
import PageHeader from '@/components/common/PageHeader';
import { chatApi } from '@/lib/api';
import dayjs from 'dayjs';

const { Title, Text, Paragraph } = Typography;

const CHANNELS = [
  { key: 'all', label: 'Tất cả kênh', icon: <GlobalOutlined /> },
  { key: 'chat', label: 'Chat trực tuyến', icon: <MessageOutlined />, color: '#52c41a' },
  { key: 'email', label: 'Email', icon: <MailOutlined />, color: '#722ed1' },
  { key: 'sms', label: 'SMS', icon: <SmileOutlined />, color: '#1890ff' },
  { key: 'facebook', label: 'Facebook Messenger', icon: <FacebookOutlined />, color: '#1877F2' },
  { key: 'zalo', label: 'Zalo', icon: <MessageOutlined />, color: '#0068FF' },
  { key: 'call', label: 'Ghi chú cuộc gọi', icon: <PhoneOutlined />, color: '#faad14' },
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
      const params: Record<string, any> = { page: 0, size: 50 };
      const res = await chatApi.getConversations(params);
      setConversations(res.data?.data?.content || res.data?.content || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load conversations');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadConversations();
  }, [loadConversations]);

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

  const handleSelectConversation = async (conv: Conversation) => {
    setSelectedConv(conv);
    try {
      const res = await chatApi.getMessages(conv.id);
      const msgs = res.data?.data?.content || res.data?.data || res.data || [];
      setMessages(Array.isArray(msgs) ? msgs : []);
    } catch {
      setMessages([]);
    }
  };

  const handleSendMessage = async () => {
    if (!messageInput.trim() || !selectedConv) return;
    try {
      await chatApi.sendMessage(selectedConv.id, { content: messageInput.trim() });
      setMessageInput('');
      const res = await chatApi.getMessages(selectedConv.id);
      const msgs = res.data?.data?.content || res.data?.data || res.data || [];
      setMessages(Array.isArray(msgs) ? msgs : []);
      loadConversations();
    } catch {
      message.error('Failed to send message');
    }
  };

  const channelOptions = CHANNELS.map(ch => ({
    label: <Space><span style={{ color: ch.color }}>{ch.icon}</span>{ch.label}</Space>,
    value: ch.key,
  }));

  if (loading) {
    return <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}><Spin size="large" tip="Đang tải..." /></div>;
  }

  if (error) {
    return <Alert message="Lỗi" description={error} type="error" showIcon action={<Button onClick={loadConversations}>Thử lại</Button>} />;
  }

  return (
    <div style={{ height: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column' }}>
      <PageHeader title="Hộp thư đến" subtitle="Quản lý hội thoại đa kênh" />
      <Card size="small" style={{ marginBottom: 8 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Segmented options={channelOptions} value={channelFilter} onChange={(val) => setChannelFilter(val as string)} />
          </Col>
          <Col>
            <Input
              prefix={<SearchOutlined />}
              placeholder="Tìm kiếm..."
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
              locale={{ emptyText: <Empty description="Không có dữ liệu" image={Empty.PRESENTED_IMAGE_SIMPLE} /> }}
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
                          {dayjs(conv.lastMessageAt).format('DD/MM/YYYY HH:mm')}
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
                        {dayjs(msg.sentAt).format('HH:mm')}
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              <div style={{ borderTop: '1px solid #f0f0f0', paddingTop: 8 }}>
                <Input.TextArea
                  value={messageInput}
                  onChange={(e) => setMessageInput(e.target.value)}
                  placeholder="Nhập tin nhắn..."
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
                  Gửi
                </Button>
              </div>
            </Card>
          </Col>
        )}
      </Row>
    </div>
  );
}
