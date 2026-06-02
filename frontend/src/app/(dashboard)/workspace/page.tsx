'use client';

import { useState } from 'react';
import {
  Layout, Card, Row, Col, Table, Tag, Button, Badge, Tabs, Input, Select,
  Typography, Space, Avatar, Tooltip, List, Switch, Dropdown, Menu,
} from 'antd';
import {
  PhoneOutlined, MessageOutlined, CustomerServiceOutlined,
  SearchOutlined, PlusOutlined, BellOutlined, UserOutlined,
  ClockCircleOutlined, CheckCircleOutlined, CloseCircleOutlined,
  MinusCircleOutlined, MailOutlined, FileTextOutlined,
  RightOutlined, LeftOutlined, DownOutlined,
} from '@ant-design/icons';

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

interface Ticket {
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

const activeCallsData: ActiveCall[] = [
  { id: 'CL-001', caller: 'John Smith', callee: 'Sarah J.', duration: '04:32', status: 'in-progress', queue: 'Support' },
  { id: 'CL-002', caller: 'Alice Brown', callee: 'Mike R.', duration: '00:45', status: 'ringing', queue: 'Billing' },
  { id: 'CL-003', caller: 'Bob Wilson', callee: 'Emily W.', duration: '12:10', status: 'in-progress', queue: 'Technical' },
  { id: 'CL-004', caller: 'Carol Davis', callee: 'John D.', duration: '02:15', status: 'hold', queue: 'Support' },
];

const chatConversationsData: ChatConversation[] = [
  { id: 'CH-001', customer: 'John Smith', avatar: 'JS', lastMessage: 'I still need help with the password reset', time: '2m', unread: 3, channel: 'web' },
  { id: 'CH-002', customer: 'Alice Brown', avatar: 'AB', lastMessage: 'Thanks for the update!', time: '5m', unread: 0, channel: 'facebook' },
  { id: 'CH-003', customer: 'Bob Wilson', avatar: 'BW', lastMessage: 'When will the feature be available?', time: '12m', unread: 1, channel: 'zalo' },
  { id: 'CH-004', customer: 'Diana Clark', avatar: 'DC', lastMessage: 'Extension 204 is still not working', time: '1h', unread: 2, channel: 'web' },
  { id: 'CH-005', customer: 'Tom Harris', avatar: 'TH', lastMessage: 'Please send the call history report', time: '2h', unread: 0, channel: 'whatsapp' },
];

const ticketsData: Ticket[] = [
  { id: 'TK-001', subject: 'Cannot reset password', customer: 'John Smith', priority: 'high', status: 'open' },
  { id: 'TK-002', subject: 'Billing discrepancy on invoice #INV-042', customer: 'Alice Brown', priority: 'critical', status: 'in_progress' },
  { id: 'TK-003', subject: 'Feature request: call recording export', customer: 'Bob Wilson', priority: 'low', status: 'open' },
  { id: 'TK-004', subject: 'Audio issues during calls', customer: 'Carol Davis', priority: 'high', status: 'in_progress' },
  { id: 'TK-005', subject: 'Extension not working', customer: 'Diana Clark', priority: 'critical', status: 'open' },
];

const notificationsData: Notification[] = [
  { id: 'N-001', title: 'New call assigned', description: 'Call from John Smith routed to your queue', time: '2 min ago', read: false, type: 'call' },
  { id: 'N-002', title: 'Ticket escalated', description: 'TK-002 has been escalated to Tier 2 support', time: '5 min ago', read: false, type: 'ticket' },
  { id: 'N-003', title: 'New chat message', description: 'Alice Brown sent a new message in chat CH-002', time: '8 min ago', read: false, type: 'chat' },
  { id: 'N-004', title: 'SLA warning', description: 'TK-004 is approaching SLA deadline (15 min remaining)', time: '12 min ago', read: true, type: 'system' },
  { id: 'N-005', title: 'Call completed', description: 'Call with Bob Wilson completed. Duration: 12:10', time: '15 min ago', read: true, type: 'call' },
  { id: 'N-006', title: 'Shift reminder', description: 'Your shift ends in 30 minutes', time: '30 min ago', read: true, type: 'system' },
];

const notificationIcons: Record<string, React.ReactNode> = {
  call: <PhoneOutlined style={{ color: '#1677ff' }} />,
  chat: <MessageOutlined style={{ color: '#52c41a' }} />,
  ticket: <FileTextOutlined style={{ color: '#faad14' }} />,
  system: <BellOutlined style={{ color: '#722ed1' }} />,
};

const customerData = {
  name: 'John Smith',
  email: 'john.smith@email.com',
  phone: '+84 912 345 678',
  segment: 'VIP',
  totalCalls: 24,
  csatScore: 92,
  tags: ['Premium', 'Recurring', 'High Value'],
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
  const [notifications, setNotifications] = useState(notificationsData);
  const [interactionSearch, setInteractionSearch] = useState('');
  const [activeTab, setActiveTab] = useState<string>('calls');

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAsRead = (id: string) => {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
  };

  const markAllAsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
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

  const filteredCalls = activeCallsData;
  const filteredChats = chatConversationsData.filter(
    (c) => c.customer.toLowerCase().includes(interactionSearch.toLowerCase()),
  );
  const filteredTickets = ticketsData.filter(
    (t) => t.subject.toLowerCase().includes(interactionSearch.toLowerCase()) ||
      t.customer.toLowerCase().includes(interactionSearch.toLowerCase()),
  );

  const filteredNotifs = notifications.filter(
    (n) => n.title.toLowerCase().includes(searchCustomer.toLowerCase()) ||
      n.description.toLowerCase().includes(searchCustomer.toLowerCase()),
  );

  return (
    <Layout style={{ height: 'calc(100vh - 104px)', background: '#f5f5f5', borderRadius: 8, overflow: 'hidden' }}>
      {/* Top status bar */}
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
          <Text type="secondary"><ClockCircleOutlined /> Online: 03h 42m</Text>
        </Space>
        <Space size="large">
          <Tooltip title="Active Calls">
            <Badge count={activeCallsData.length} size="small" offset={[2, -2]}>
              <PhoneOutlined style={{ fontSize: 18, color: '#1677ff' }} />
            </Badge>
          </Tooltip>
          <Tooltip title="Waiting Chats">
            <Badge count={chatConversationsData.reduce((s, c) => s + (c.unread > 0 ? 1 : 0), 0)} size="small">
              <MessageOutlined style={{ fontSize: 18, color: '#52c41a' }} />
            </Badge>
          </Tooltip>
          <Tooltip title="Open Tickets">
            <Badge count={ticketsData.filter((t) => t.status !== 'closed' && t.status !== 'resolved').length} size="small">
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
        {/* Left sidebar - Customer Info */}
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
                <div><Text type="secondary" style={{ fontSize: 12 }}>Email:</Text><br /><Text>{customerData.email}</Text></div>
                <div><Text type="secondary" style={{ fontSize: 12 }}>Phone:</Text><br /><Text>{customerData.phone}</Text></div>
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

        {/* Main content - Interactions Panel */}
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
              <Button type="primary" icon={<PlusOutlined />} size="small">New Interaction</Button>
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
                tab={<span><MessageOutlined /> Chat <Badge count={chatConversationsData.reduce((s, c) => s + c.unread, 0)} size="small" /></span>}
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

        {/* Right sidebar - Notifications */}
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
                    avatar={notificationIcons[item.type]}
                    title={<Space><Text strong={!item.read} style={{ fontSize: 13 }}>{item.title}</Text><Text type="secondary" style={{ fontSize: 11 }}>{item.time}</Text></Space>}
                    description={<Text type="secondary" style={{ fontSize: 12 }}>{item.description}</Text>}
                  />
                </List.Item>
              )}
            />
            {!notifPanelOpen && (
              <div style={{ position: 'absolute', top: 12, left: -20 }}>
                <Tooltip title="Open notifications">
                  <Button type="text" icon={<LeftOutlined />} onClick={() => setNotifPanelOpen(true)} />
                </Tooltip>
              </div>
            )}
          </Sider>
        )}
      </Layout>

      {/* Floating Quick Actions */}
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
