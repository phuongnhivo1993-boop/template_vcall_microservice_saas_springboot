'use client';

import { useState, useEffect, useCallback } from 'react';
import { Row, Col, Card, Tabs, Input, Badge, Avatar, Typography, Space, Button, List, Tag, Spin, Alert, Select, Statistic, Empty, Tooltip } from 'antd';
import { PhoneOutlined, MessageOutlined, CustomerServiceOutlined, SearchOutlined, UserOutlined, ClockCircleOutlined, CheckCircleOutlined, CloseCircleOutlined, MoreOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import ScreenPop from '@/components/common/ScreenPop';
import { agentsApi, customersApi, callsApi, chatApi, ticketsApi, notificationsApi } from '@/lib/api';

const { Title, Text } = Typography;

interface AgentWorkspaceProps {
}

export default function WorkspacePage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [agent, setAgent] = useState<any>(null);
  const [activeCalls, setActiveCalls] = useState<any[]>([]);
  const [activeChats, setActiveChats] = useState<any[]>([]);
  const [recentCustomers, setRecentCustomers] = useState<any[]>([]);
  const [pendingTickets, setPendingTickets] = useState<any[]>([]);
  const [customerSearch, setCustomerSearch] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [customerSearching, setCustomerSearching] = useState(false);
  const [screenPopVisible, setScreenPopVisible] = useState(false);
  const [incomingCallData, setIncomingCallData] = useState<any>(null);

  const loadWorkspaceData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const results = await Promise.allSettled([
        agentsApi.getProfile('current').catch(() => null),
        callsApi.list({ status: 'ACTIVE', page: 0, size: 50 }).catch(() => null),
        chatApi.getConversations({ status: 'ACTIVE' }).catch(() => null),
        ticketsApi.list({ status: 'OPEN,PENDING', page: 0, size: 10 }).catch(() => null),
      ]);

      if (results[0].status === 'fulfilled') setAgent(results[0].value?.data?.data);
      if (results[1].status === 'fulfilled') setActiveCalls(results[1].value?.data?.data?.content || []);
      if (results[2].status === 'fulfilled') setActiveChats(results[2].value?.data?.data?.content || []);
      if (results[3].status === 'fulfilled') setPendingTickets(results[3].value?.data?.data?.content || []);

      const customersRes = await customersApi.list({ page: 0, size: 5 }).catch(() => null);
      if (customersRes) setRecentCustomers(customersRes.data?.data?.content || []);
    } catch (err) {
      setError('Failed to load workspace data');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadWorkspaceData();
  }, [loadWorkspaceData]);

  useEffect(() => {
    const ringing = activeCalls.find((c: any) => c.status === 'RINGING');
    if (ringing) {
      setIncomingCallData({
        callerNumber: ringing.callerNumber,
        callerName: ringing.callerName,
        customer: {
          id: 'demo-customer-1',
          fullName: ringing.callerName,
          phone: ringing.callerNumber,
          email: `${ringing.callerName?.toLowerCase().replace(/\s+/g, '.')}@email.com` || 'unknown@email.com',
          company: 'VCall Customer',
          totalCalls: 5,
          totalTickets: 2,
          satisfactionScore: 88,
          tags: ['vip', 'support'],
        },
        recentActivity: [
          { id: 'a1', type: 'call', title: 'Previous call - Billing inquiry', description: 'Duration: 3m 42s', timestamp: new Date(Date.now() - 86400000).toISOString() },
          { id: 'a2', type: 'ticket', title: 'Ticket TK-102 opened', description: 'Network issue - In progress', timestamp: new Date(Date.now() - 172800000).toISOString() },
        ],
      });
      setScreenPopVisible(true);
    }
  }, [activeCalls]);

  const handleCustomerSearch = async (value: string) => {
    setCustomerSearch(value);
    if (value.length < 2) { setSearchResults([]); return; }
    setCustomerSearching(true);
    try {
      const res = await customersApi.search(value, { page: 0, size: 10 });
      setSearchResults(res.data?.data?.content || []);
    } catch {
      setSearchResults([
        { id: '1', fullName: 'Nguyen Van A', phone: '0909123456', email: 'a@test.com' },
        { id: '2', fullName: 'Tran Thi B', phone: '0909987654', email: 'b@test.com' },
      ]);
    } finally {
      setCustomerSearching(false);
    }
  };

  const handleAnswerCall = useCallback(() => {
    setScreenPopVisible(false);
    setIncomingCallData(null);
  }, []);

  const handleRejectCall = useCallback(() => {
    setScreenPopVisible(false);
    setIncomingCallData(null);
  }, []);

  const statusColors: Record<string, string> = {
    AVAILABLE: '#52c41a', BUSY: '#faad14', OFFLINE: '#d9d9d9', RINGING: '#1890ff',
    IN_PROGRESS: '#52c41a', OPEN: '#1890ff', PENDING: '#faad14', HIGH: '#ff4d4f',
    MEDIUM: '#faad14', LOW: '#52c41a',
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <Spin size="large" tip="Loading workspace..." />
      </div>
    );
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={loadWorkspaceData}>Retry</Button>} />;
  }

  return (
    <div>
      <Card size="small" style={{ marginBottom: 16 }}>
        <Row justify="space-between" align="middle">
          <Col>
            <Space>
              <Badge status={agent?.status === 'AVAILABLE' ? 'success' : 'warning'} />
              <Text strong>{agent?.fullName || 'Agent'}</Text>
              <Tag color={statusColors[agent?.status]}>{agent?.status}</Tag>
              <Text type="secondary">Code: {agent?.agentCode}</Text>
            </Space>
          </Col>
          <Col>
            <Space>
              <Statistic title="Active Calls" value={activeCalls.length} prefix={<PhoneOutlined />} valueStyle={{ fontSize: 16 }} />
              <Statistic title="Active Chats" value={activeChats.length} prefix={<MessageOutlined />} valueStyle={{ fontSize: 16 }} />
              <Statistic title="Pending Tickets" value={pendingTickets.length} prefix={<CustomerServiceOutlined />} valueStyle={{ fontSize: 16 }} />
            </Space>
          </Col>
        </Row>
      </Card>

      <Row gutter={16}>
        <Col xs={24} lg={16}>
          <Tabs
            defaultActiveKey="calls"
            items={[
              {
                key: 'calls',
                label: <span><PhoneOutlined /> Active Calls ({activeCalls.length})</span>,
                children: (
                  <Card>
                    {activeCalls.length === 0 ? (
                      <Empty description="No active calls" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                    ) : (
                      <List
                        dataSource={activeCalls}
                        renderItem={(call: any) => (
                          <List.Item
                            actions={[
                              <Button key="answer" type="primary" size="small" icon={<PhoneOutlined />}>Answer</Button>,
                              <Button key="end" danger size="small" icon={<CloseCircleOutlined />}>End</Button>,
                              <Tooltip key="more" title="More actions"><Button size="small" icon={<MoreOutlined />} /></Tooltip>,
                            ]}
                          >
                            <List.Item.Meta
                              avatar={<Avatar icon={<PhoneOutlined />} style={{ backgroundColor: call.status === 'RINGING' ? '#faad14' : '#52c41a' }} />}
                              title={<Space>{call.callerName} <Tag>{call.callerNumber}</Tag></Space>}
                              description={<Space><Tag color={statusColors[call.status]}>{call.status}</Tag><Text type="secondary">{call.queueName}</Text><Text type="secondary">Duration: {call.duration}s</Text></Space>}
                            />
                          </List.Item>
                        )}
                      />
                    )}
                  </Card>
                ),
              },
              {
                key: 'chats',
                label: <span><MessageOutlined /> Active Chats ({activeChats.length})</span>,
                children: (
                  <Card>
                    {activeChats.length === 0 ? (
                      <Empty description="No active chats" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                    ) : (
                      <List
                        dataSource={activeChats}
                        renderItem={(chat: any) => (
                          <List.Item
                            actions={[
                              <Button key="open" type="primary" size="small" icon={<MessageOutlined />}>Open</Button>,
                            ]}
                          >
                            <List.Item.Meta
                              avatar={<Badge count={chat.unread} size="small"><Avatar icon={<MessageOutlined />} /></Badge>}
                              title={<Space>{chat.customerName} {chat.unread > 0 && <Tag color="red">{chat.unread} unread</Tag>}</Space>}
                              description={<Text type="secondary">{chat.lastMessage}</Text>}
                            />
                          </List.Item>
                        )}
                      />
                    )}
                  </Card>
                ),
              },
              {
                key: 'tickets',
                label: <span><CustomerServiceOutlined /> Pending Tickets ({pendingTickets.length})</span>,
                children: (
                  <Card>
                    {pendingTickets.length === 0 ? (
                      <Empty description="No pending tickets" image={Empty.PRESENTED_IMAGE_SIMPLE} />
                    ) : (
                      <List
                        dataSource={pendingTickets}
                        renderItem={(ticket: any) => (
                          <List.Item
                            actions={[
                              <Button key="view" type="primary" size="small">View</Button>,
                            ]}
                          >
                            <List.Item.Meta
                              title={<Space>{ticket.title} <Tag color={statusColors[ticket.priority]}>{ticket.priority}</Tag></Space>}
                              description={<Space><Tag>{ticket.status}</Tag><Text type="secondary">Created: {new Date(ticket.createdAt).toLocaleString()}</Text></Space>}
                            />
                          </List.Item>
                        )}
                      />
                    )}
                  </Card>
                ),
              },
            ]}
          />
        </Col>

        <Col xs={24} lg={8}>
          <Space direction="vertical" style={{ width: '100%' }} size={16}>
            <Card title={<Space><SearchOutlined /> Tìm kiếm khách hàng</Space>} size="small">
              <Input.Search
                placeholder="Search by name, phone, email..."
                value={customerSearch}
                onChange={(e) => handleCustomerSearch(e.target.value)}
                loading={customerSearching}
                allowClear
              />
              {searchResults.length > 0 && (
                <List
                  size="small"
                  dataSource={searchResults}
                  style={{ marginTop: 8 }}
                  renderItem={(customer: any) => (
                    <List.Item
                      actions={[<Button key="call" size="small" icon={<PhoneOutlined />} />, <Button key="chat" size="small" icon={<MessageOutlined />} />]}
                    >
                      <List.Item.Meta
                        avatar={<Avatar icon={<UserOutlined />} />}
                        title={customer.fullName}
                        description={customer.phone}
                      />
                    </List.Item>
                  )}
                />
              )}
            </Card>

            <Card title="Quick Stats" size="small">
              <Row gutter={[8, 8]}>
                <Col span={12}><Statistic title="Today's Calls" value={12} prefix={<PhoneOutlined />} /></Col>
                <Col span={12}><Statistic title="Avg Handle Time" value="4m 32s" prefix={<ClockCircleOutlined />} /></Col>
                <Col span={12}><Statistic title="Resolved Today" value={8} prefix={<CheckCircleOutlined />} /></Col>
                <Col span={12}><Statistic title="Satisfaction" value="95%" prefix={<CustomerServiceOutlined />} /></Col>
              </Row>
            </Card>

            <Card title="Recent Customers" size="small">
              <List
                size="small"
                dataSource={recentCustomers}
                locale={{ emptyText: <Empty description="No recent customers" image={Empty.PRESENTED_IMAGE_SIMPLE} /> }}
                renderItem={(customer: any) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<Avatar icon={<UserOutlined />} />}
                      title={customer.fullName}
                      description={customer.phone || customer.email}
                    />
                  </List.Item>
                )}
              />
            </Card>
          </Space>
        </Col>
      </Row>

      <ScreenPop
        visible={screenPopVisible}
        data={incomingCallData}
        onClose={handleRejectCall}
        onAnswer={handleAnswerCall}
        onReject={handleRejectCall}
      />
    </div>
  );
}
