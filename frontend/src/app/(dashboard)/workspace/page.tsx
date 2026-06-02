'use client';

import { useState, useEffect, useCallback } from 'react';
import {
  Layout, Card, Row, Col, Table, Tag, Button, Badge, Tabs, Input,
  Typography, Space, Avatar, Tooltip, List, Switch, Dropdown, Menu, Spin, Alert,
} from 'antd';
import {
  PhoneOutlined, MessageOutlined, CustomerServiceOutlined,
  SearchOutlined, PlusOutlined, BellOutlined, UserOutlined,
  ClockCircleOutlined, CheckCircleOutlined, CloseCircleOutlined,
  MinusCircleOutlined, MailOutlined, FileTextOutlined,
  RightOutlined, LeftOutlined, DownOutlined, ReloadOutlined,
} from '@ant-design/icons';
import { callsApi, chatApi, ticketsApi, notificationsApi, agentsApi } from '@/lib/api';

const { Header, Sider, Content } = Layout;
const { Title, Text } = Typography;

type AgentStatus = 'available' | 'busy' | 'away' | 'offline';

interface ActiveCall {
  id: string;
  caller: string;
  callee: string;
  duration: string;
  status: 'ringing' | 'in-progress' | 'hold' | 'completed';
  queue: string;
}

interface ChatConversation {
  id: string;
  customer: string;
  avatar: string;
  lastMessage: string;
  time: string;
  unread: number;
  channel: 'web' | 'facebook' | 'zalo' | 'whatsapp';
}

interface TicketItem {
  id: string;
  subject: string;
  customer: string;
  priority: 'low' | 'medium' | 'high' | 'critical';
  status: 'open' | 'in_progress' | 'resolved' | 'closed';
}

interface Notification {
  id: string;
  title: string;
  description: string;
  time: string;
  read: boolean;
  type: 'call' | 'chat' | 'ticket' | 'system';
}

const agentStatusColors: Record<AgentStatus, string> = {
  available: '#52c41a',
  busy: '#faad14',
  away: '#ff4d4f',
  offline: '#d9d9d9',
};

const priorityColors: Record<string, string> = {
  low: 'green', medium: 'blue', high: 'orange', critical: 'red',
};

const statusColors: Record<string, string> = {
  open: '#1677ff', in_progress: '#faad14', resolved: '#52c41a', closed: '#d9d9d9',
};

const callStatusColors: Record<string, string> = {
  ringing: '#faad14', 'in-progress': '#52c41a', hold: '#ff4d4f', completed: '#d9d9d9',
};

const notificationIcons: Record<string, React.ReactNode> = {
  call: <PhoneOutlined style={{ color: '#1677ff' }} />,
  chat: <MessageOutlined style={{ color: '#52c41a' }} />,
  ticket: <FileTextOutlined style={{ color: '#faad14' }} />,
  system: <BellOutlined style={{ color: '#722ed1' }} />,
};

const callColumns = [
  { title: 'Caller', dataIndex: 'caller', key: 'caller', width: 130 },
  { title: 'Duration', dataIndex: 'duration', key: 'duration', width: 80, render: (t: string) => <Text code>{t}</Text> },
  {
    title: 'Status', dataIndex: 'status', key: 'status', width: 110,
    render: (s: string) => <Tag color={callStatusColors[s]}>{s.replace('-', ' ').toUpperCase()}</Tag>,
  },
  {
    title: 'Actions', key: 'actions', width: 120,
    render: (_: unknown, record: ActiveCall) => (
      <Space size="small">
        {record.status === 'in-progress' && <Tooltip title="Hold"><Button size="small" icon={<MinusCircleOutlined />} /></Tooltip>}
        {record.status !== 'completed' && <Tooltip title="End"><Button size="small" danger icon={<CloseCircleOutlined />} /></Tooltip>}
      </Space>
    ),
  },
];

const ticketColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80, render: (id: string) => <a style={{ fontWeight: 500 }}>{id}</a> },
  { title: 'Subject', dataIndex: 'subject', key: 'subject', ellipsis: true },
  { title: 'Customer', dataIndex: 'customer', key: 'customer', width: 120 },
  { title: 'Priority', dataIndex: 'priority', key: 'priority', width: 90, render: (p: string) => <Tag color={priorityColors[p]}>{p.toUpperCase()}</Tag> },
  { title: 'Status', dataIndex: 'status', key: 'status', width: 100, render: (s: string) => <Tag color={statusColors[s]}>{s.replace('_', ' ')}</Tag> },
];

export default function WorkspacePage() {
  const [agentStatus, setAgentStatus] = useState<AgentStatus>('available');
  const [notifPanelOpen, setNotifPanelOpen] = useState(true);
  const [searchCustomer, setSearchCustomer] = useState('');
  const [interactionSearch, setInteractionSearch] = useState('');
  const [activeTab, setActiveTab] = useState<string>('calls');

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [activeCalls, setActiveCalls] = useState<ActiveCall[]>([]);
  const [chatConversations, setChatConversations] = useState<ChatConversation[]>([]);
  const [ticketItems, setTicketItems] = useState<TicketItem[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [customerData, setCustomerData] = useState({
    name: 'John Smith',
    email: '',
    phone: '',
    segment: 'Standard',
    totalCalls: 0,
    csatScore: 0,
    tags: [] as string[],
  });

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [callsRes, chatRes, ticketsRes, notifsRes] = await Promise.all([
        callsApi.list({ page: 0, size: 20 }).catch(() => ({ data: { data: [] } })),
        chatApi.getConversations().catch(() => ({ data: { data: [] } })),
        ticketsApi.list({ page: 0, size: 20 }).catch(() => ({ data: { data: [] } })),
        notificationsApi.list({ page: 0, size: 20 }).catch(() => ({ data: { data: [] } })),
      ]);

      const calls = callsRes.data?.data?.content || callsRes.data?.data || callsRes.data || [];
      setActiveCalls(Array.isArray(calls) ? calls.map((c: any) => ({
        id: c.id, caller: c.caller || c.callerName || '', callee: c.callee || c.calleeName || '',
        duration: c.duration || '00:00', status: c.status || 'in-progress', queue: c.queue || '',
      })) : []);

      const chats = chatRes.data?.data?.content || chatRes.data?.data || chatRes.data || [];
      setChatConversations(Array.isArray(chats) ? chats.map((c: any) => ({
        id: c.id, customer: c.customerName || c.customer || '', avatar: c.customerName?.charAt(0)?.toUpperCase() || '?',
        lastMessage: c.lastMessage || '', time: c.lastMessageAt || '', unread: c.unreadCount ?? 0,
        channel: c.channel || 'web',
      })) : []);

      const tix = ticketsRes.data?.data?.content || ticketsRes.data?.data || ticketsRes.data || [];
      setTicketItems(Array.isArray(tix) ? tix.map((t: any) => ({
        id: t.id, subject: t.title || t.subject || '', customer: t.customerName || t.customer || '',
        priority: t.priority || 'medium', status: t.status || 'open',
      })) : []);

      const notifs = notifsRes.data?.data?.content || notifsRes.data?.data || notifsRes.data || [];
      setNotifications(Array.isArray(notifs) ? notifs.map((n: any) => ({
        id: n.id, title: n.title, description: n.description || n.message || '',
        time: n.createdAt || n.time || '', read: n.read ?? false, type: n.type || 'system',
      })) : []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load workspace data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAsRead = async (id: string) => {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
    try { await notificationsApi.markAsRead(id); } catch { /* silent */ }
  };

  const markAllAsRead = async () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    try {
      if (notifications.length > 0) {
        const userId = notifications[0].id;
        await notificationsApi.markAllAsRead(userId);
      }
    } catch { /* silent */ }
  };

  const statusMenuItems = [
    { key: 'available', label: 'Available', icon: <CheckCircleOutlined style={{ color: '#52c41a' }} /> },
    { key: 'busy', label: 'Busy', icon: <MinusCircleOutlined style={{ color: '#faad14' }} /> },
    { key: 'away', label: 'Away', icon: <CloseCircleOutlined style={{ color: '#ff4d4f' }} /> },
    { key: 'offline', label: 'Offline', icon: <ClockCircleOutlined style={{ color: '#d9d9d9' }} /> },
  ];

  const statusActions = (
    <Menu
      items={statusMenuItems.map((item) => ({
        ...item,
        onClick: () => setAgentStatus(item.key as AgentStatus),
      }))}
    />
  );

  const filteredCalls = activeCalls;
  const filteredChats = chatConversations.filter(
    (c) => c.customer.toLowerCase().includes(interactionSearch.toLowerCase()),
  );
  const filteredTickets = ticketItems.filter(
    (t) => t.subject.toLowerCase().includes(interactionSearch.toLowerCase()) ||
      t.customer.toLowerCase().includes(interactionSearch.toLowerCase()),
  );

  const filteredNotifs = notifications.filter(
    (n) => n.title.toLowerCase().includes(searchCustomer.toLowerCase()) ||
      n.description.toLowerCase().includes(searchCustomer.toLowerCase()),
  );

  if (loading && activeCalls.length === 0) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error && activeCalls.length === 0) {
    return (
      <div style={{ padding: 24 }}>
        <Title level={3}>Agent Workspace</Title>
        <Alert type="error" message={error} showIcon action={<Button onClick={fetchData} icon={<ReloadOutlined />}>Retry</Button>} />
      </div>
    );
  }

  return (
    <Layout style={{ height: 'calc(100vh - 104px)', background: '#f5f5f5', borderRadius: 8, overflow: 'hidden' }}>
      <Header style={{
        background: '#fff', padding: '0 24px', display: 'flex', alignItems: 'center',
        justifyContent: 'space-between', borderBottom: '1px solid #f0f0f0', height: 56, lineHeight: '56px',
      }}>
        <Space size="middle">
          <Title level={5} style={{ margin: 0, whiteSpace: 'nowrap' }}>Agent Workspace</Title>
          <Dropdown overlay={statusActions} trigger={['click']}>
            <Button style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <Badge status={agentStatusColors[agentStatus] as 'success' | 'warning' | 'error' | 'default'} />
              {agentStatus.charAt(0).toUpperCase() + agentStatus.slice(1)}
              <DownOutlined />
            </Button>
          </Dropdown>
          <Text type="secondary"><ClockCircleOutlined /> Online</Text>
        </Space>
        <Space size="large">
          <Tooltip title="Active Calls">
            <Badge count={activeCalls.length} size="small" offset={[2, -2]}>
              <PhoneOutlined style={{ fontSize: 18, color: '#1677ff' }} />
            </Badge>
          </Tooltip>
          <Tooltip title="Unread Chats">
            <Badge count={chatConversations.reduce((s, c) => s + (c.unread > 0 ? 1 : 0), 0)} size="small">
              <MessageOutlined style={{ fontSize: 18, color: '#52c41a' }} />
            </Badge>
          </Tooltip>
          <Tooltip title="Open Tickets">
            <Badge count={ticketItems.filter((t) => t.status !== 'closed' && t.status !== 'resolved').length} size="small">
              <FileTextOutlined style={{ fontSize: 18, color: '#faad14' }} />
            </Badge>
          </Tooltip>
          <Badge count={unreadCount} size="small">
            <Button
              type="text"
              icon={<BellOutlined style={{ fontSize: 18 }} />}
              onClick={() => setNotifPanelOpen(!notifPanelOpen)}
            />
          </Badge>
        </Space>
      </Header>

      <Layout style={{ flex: 1, background: '#f5f5f5' }}>
        <Sider width={320} theme="light" style={{ background: '#fff', borderRight: '1px solid #f0f0f0', overflowY: 'auto' }}>
          <div style={{ padding: 16 }}>
            <Input
              prefix={<SearchOutlined />}
              placeholder="Search customers..."
              value={searchCustomer}
              onChange={(e) => setSearchCustomer(e.target.value)}
              style={{ marginBottom: 16 }}
            />

            <Card size="small" style={{ marginBottom: 16 }}>
              <Space direction="vertical" style={{ width: '100%' }} size={8}>
                <Space align="center">
                  <Avatar size={48} icon={<UserOutlined />} style={{ backgroundColor: '#1677ff' }} />
                  <div>
                    <Text strong style={{ fontSize: 15 }}>{customerData.name}</Text>
                    <div>
                      <Tag color="gold" style={{ fontSize: 11 }}>{customerData.segment}</Tag>
                    </div>
                  </div>
                </Space>
                <div><Text type="secondary" style={{ fontSize: 12 }}>Email:</Text><br /><Text>{customerData.email || 'No email'}</Text></div>
                <div><Text type="secondary" style={{ fontSize: 12 }}>Phone:</Text><br /><Text>{customerData.phone || 'No phone'}</Text></div>
                <Row gutter={8}>
                  <Col span={12}>
                    <Card size="small" style={{ textAlign: 'center', background: '#f5f5f5' }}>
                      <Text type="secondary" style={{ fontSize: 11 }}>Total Calls</Text>
                      <br /><Text strong style={{ fontSize: 20 }}>{customerData.totalCalls}</Text>
                    </Card>
                  </Col>
                  <Col span={12}>
                    <Card size="small" style={{ textAlign: 'center', background: '#f5f5f5' }}>
                      <Text type="secondary" style={{ fontSize: 11 }}>CSAT</Text>
                      <br /><Text strong style={{ fontSize: 20, color: '#52c41a' }}>{customerData.csatScore}%</Text>
                    </Card>
                  </Col>
                </Row>
                <Space wrap>
                  {customerData.tags.map((tag) => (
                    <Tag key={tag} color="blue">{tag}</Tag>
                  ))}
                </Space>
              </Space>
            </Card>

            <Space direction="vertical" style={{ width: '100%' }}>
              <Button type="primary" icon={<PhoneOutlined />} block>Call</Button>
              <Button icon={<MessageOutlined />} block>Chat</Button>
              <Button icon={<FileTextOutlined />} block>Create Ticket</Button>
              <Button icon={<UserOutlined />} block>View Full Profile</Button>
            </Space>
          </div>
        </Sider>

        <Content style={{ padding: 16, overflow: 'auto' }}>
          <Input
            prefix={<SearchOutlined />}
            placeholder="Search interactions..."
            value={interactionSearch}
            onChange={(e) => setInteractionSearch(e.target.value)}
            style={{ marginBottom: 12, maxWidth: 400 }}
          />
          <Card style={{ minHeight: 400 }}>
            <Tabs activeKey={activeTab} onChange={setActiveTab} tabBarExtraContent={
              <Space>
                <Button icon={<ReloadOutlined />} size="small" onClick={fetchData}>Refresh</Button>
                <Button type="primary" icon={<PlusOutlined />} size="small">New Interaction</Button>
              </Space>
            }>
              <Tabs.TabPane
                tab={<span><PhoneOutlined /> Active Calls <Badge count={filteredCalls.length} size="small" style={{ backgroundColor: '#1677ff' }} /></span>}
                key="calls"
              >
                <Table
                  dataSource={filteredCalls}
                  columns={callColumns}
                  rowKey="id"
                  pagination={false}
                  size="small"
                  locale={{ emptyText: 'No active calls' }}
                />
              </Tabs.TabPane>
              <Tabs.TabPane
                tab={<span><MessageOutlined /> Chat <Badge count={chatConversations.reduce((s, c) => s + c.unread, 0)} size="small" /></span>}
                key="chat"
              >
                <List
                  dataSource={filteredChats}
                  locale={{ emptyText: 'No chat conversations' }}
                  renderItem={(item) => (
                    <List.Item
                      style={{ cursor: 'pointer', padding: '10px 12px' }}
                      extra={item.unread > 0 ? <Badge count={item.unread} size="small" /> : null}
                    >
                      <List.Item.Meta
                        avatar={<Avatar style={{ backgroundColor: '#1677ff' }}>{item.avatar}</Avatar>}
                        title={
                          <Space>
                            <Text strong>{item.customer}</Text>
                            <Tag style={{ fontSize: 10, lineHeight: '16px', padding: '0 4px' }}>{item.channel}</Tag>
                            <Text type="secondary" style={{ fontSize: 12 }}>{item.time}</Text>
                          </Space>
                        }
                        description={<Text type="secondary" style={{ fontSize: 13 }}>{item.lastMessage}</Text>}
                      />
                    </List.Item>
                  )}
                />
              </Tabs.TabPane>
              <Tabs.TabPane
                tab={<span><FileTextOutlined /> Tickets <Badge count={filteredTickets.length} size="small" style={{ backgroundColor: '#faad14' }} /></span>}
                key="tickets"
              >
                <Table
                  dataSource={filteredTickets}
                  columns={ticketColumns}
                  rowKey="id"
                  pagination={false}
                  size="small"
                  locale={{ emptyText: 'No tickets assigned' }}
                />
              </Tabs.TabPane>
            </Tabs>
          </Card>
        </Content>

        {notifPanelOpen && (
          <Sider width={340} theme="light" style={{ background: '#fff', borderLeft: '1px solid #f0f0f0', overflowY: 'auto' }}>
            <div style={{ padding: '12px 16px', borderBottom: '1px solid #f0f0f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Space>
                <BellOutlined />
                <Text strong>Notifications</Text>
                {unreadCount > 0 && <Badge count={unreadCount} size="small" />}
              </Space>
              <Space size="small">
                {unreadCount > 0 && <Button type="link" size="small" onClick={markAllAsRead}>Mark all read</Button>}
                <Tooltip title="Close panel">
                  <Button type="text" size="small" icon={<RightOutlined />} onClick={() => setNotifPanelOpen(false)} />
                </Tooltip>
              </Space>
            </div>
            <List
              dataSource={filteredNotifs}
              locale={{ emptyText: 'No notifications' }}
              renderItem={(item) => (
                <List.Item
                  style={{
                    padding: '10px 16px', cursor: 'pointer', background: item.read ? 'transparent' : '#f0f5ff',
                    borderBottom: '1px solid #f5f5f5',
                  }}
                  onClick={() => markAsRead(item.id)}
                  actions={!item.read ? [<Button key="read" type="link" size="small" onClick={(e) => { e.stopPropagation(); markAsRead(item.id); }}>Mark read</Button>] : []}
                >
                  <List.Item.Meta
                    avatar={notificationIcons[item.type] || <BellOutlined />}
                    title={<Space><Text strong={!item.read} style={{ fontSize: 13 }}>{item.title}</Text><Text type="secondary" style={{ fontSize: 11 }}>{item.time}</Text></Space>}
                    description={<Text type="secondary" style={{ fontSize: 12 }}>{item.description}</Text>}
                  />
                </List.Item>
              )}
            />
          </Sider>
        )}
      </Layout>

      <div style={{ position: 'fixed', bottom: 32, right: 32, display: 'flex', flexDirection: 'column', gap: 8, zIndex: 100 }}>
        <Tooltip title="New Call">
          <Button type="primary" shape="circle" size="large" icon={<PhoneOutlined />} style={{ width: 48, height: 48, boxShadow: '0 4px 12px rgba(22,119,255,0.4)' }} />
        </Tooltip>
        <Tooltip title="New Chat">
          <Button shape="circle" size="large" icon={<MessageOutlined />} style={{ width: 48, height: 48, background: '#52c41a', borderColor: '#52c41a', color: '#fff', boxShadow: '0 4px 12px rgba(82,196,26,0.4)' }} />
        </Tooltip>
        <Tooltip title="Create Ticket">
          <Button shape="circle" size="large" icon={<FileTextOutlined />} style={{ width: 48, height: 48, background: '#faad14', borderColor: '#faad14', color: '#fff', boxShadow: '0 4px 12px rgba(250,173,20,0.4)' }} />
        </Tooltip>
      </div>
    </Layout>
  );
}
