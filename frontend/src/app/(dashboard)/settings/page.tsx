'use client';

import { Card, Tabs, Form, Input, Button, Switch, Select, Typography, Divider, message, Space, Row, Col } from 'antd';
import { UserOutlined, TeamOutlined, ApiOutlined, DollarOutlined, SafetyOutlined } from '@ant-design/icons';

const { Title } = Typography;

export default function SettingsPage() {
  const [profileForm] = Form.useForm();
  const [orgForm] = Form.useForm();
  const [securityForm] = Form.useForm();

  const handleProfileSave = () => {
    profileForm.validateFields().then(() => message.success('Profile updated'));
  };

  const handleOrgSave = () => {
    orgForm.validateFields().then(() => message.success('Organization updated'));
  };

  const handleSecuritySave = () => {
    securityForm.validateFields().then(() => message.success('Security settings updated'));
  };

  const tabItems = [
    {
      key: 'profile',
      label: <span><UserOutlined /> Profile</span>,
      children: (
        <Card>
          <Title level={4}>Personal Information</Title>
          <Form form={profileForm} layout="vertical" style={{ maxWidth: 600 }}>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="firstName" label="First Name" initialValue="John">
                  <Input />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="lastName" label="Last Name" initialValue="Doe">
                  <Input />
                </Form.Item>
              </Col>
            </Row>
            <Form.Item name="email" label="Email" initialValue="john@vcall.com" rules={[{ type: 'email' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="phone" label="Phone" initialValue="+1 (555) 000-0000">
              <Input />
            </Form.Item>
            <Form.Item name="timezone" label="Timezone" initialValue="America/New_York">
              <Select options={[
                { value: 'America/New_York', label: 'Eastern Time' },
                { value: 'America/Chicago', label: 'Central Time' },
                { value: 'America/Denver', label: 'Mountain Time' },
                { value: 'America/Los_Angeles', label: 'Pacific Time' },
              ]} />
            </Form.Item>
            <Button type="primary" onClick={handleProfileSave}>Save Changes</Button>
          </Form>
        </Card>
      ),
    },
    {
      key: 'organization',
      label: <span><TeamOutlined /> Organization</span>,
      children: (
        <Card>
          <Title level={4}>Organization Settings</Title>
          <Form form={orgForm} layout="vertical" style={{ maxWidth: 600 }}>
            <Form.Item name="orgName" label="Organization Name" initialValue="VCall Healthcare">
              <Input />
            </Form.Item>
            <Form.Item name="industry" label="Industry" initialValue="Healthcare">
              <Input />
            </Form.Item>
            <Form.Item name="maxAgents" label="Max Agents" initialValue="50">
              <Input type="number" />
            </Form.Item>
            <Form.Item name="language" label="Default Language" initialValue="en">
              <Select options={[
                { value: 'en', label: 'English' },
                { value: 'es', label: 'Spanish' },
                { value: 'fr', label: 'French' },
              ]} />
            </Form.Item>
            <Button type="primary" onClick={handleOrgSave}>Save Changes</Button>
          </Form>
        </Card>
      ),
    },
    {
      key: 'channels',
      label: <span><ApiOutlined /> Channels</span>,
      children: (
        <Card>
          <Title level={4}>Communication Channels</Title>
          <Space direction="vertical" size="large" style={{ width: '100%', maxWidth: 600 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <div style={{ fontWeight: 500 }}>Voice Calls</div>
                <div style={{ color: '#999', fontSize: 13 }}>Inbound and outbound voice calls</div>
              </div>
              <Switch defaultChecked />
            </div>
            <Divider style={{ margin: 0 }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <div style={{ fontWeight: 500 }}>SMS / Text</div>
                <div style={{ color: '#999', fontSize: 13 }}>SMS messaging channel</div>
              </div>
              <Switch defaultChecked />
            </div>
            <Divider style={{ margin: 0 }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <div style={{ fontWeight: 500 }}>Email</div>
                <div style={{ color: '#999', fontSize: 13 }}>Email ticketing integration</div>
              </div>
              <Switch defaultChecked />
            </div>
            <Divider style={{ margin: 0 }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <div style={{ fontWeight: 500 }}>Web Chat</div>
                <div style={{ color: '#999', fontSize: 13 }}>Live website chat widget</div>
              </div>
              <Switch />
            </div>
          </Space>
        </Card>
      ),
    },
    {
      key: 'billing',
      label: <span><DollarOutlined /> Billing</span>,
      children: (
        <Card>
          <Title level={4}>Subscription & Billing</Title>
          <div style={{ maxWidth: 600 }}>
            <div style={{
              background: '#f0f5ff',
              border: '1px solid #d6e4ff',
              borderRadius: 8,
              padding: 24,
              marginBottom: 24,
            }}>
              <div style={{ fontSize: 16, fontWeight: 600 }}>Enterprise Plan</div>
              <div style={{ fontSize: 32, fontWeight: 700, color: '#1677ff', margin: '8px 0' }}>
                $299<span style={{ fontSize: 16, fontWeight: 400, color: '#666' }}>/month</span>
              </div>
              <div style={{ color: '#666' }}>50 agents included • Unlimited calls • 24/7 support</div>
            </div>
            <Button type="primary">Upgrade Plan</Button>
            <Button style={{ marginLeft: 12 }}>View Invoices</Button>
          </div>
        </Card>
      ),
    },
    {
      key: 'security',
      label: <span><SafetyOutlined /> Security</span>,
      children: (
        <Card>
          <Title level={4}>Security Settings</Title>
          <Form form={securityForm} layout="vertical" style={{ maxWidth: 600 }}>
            <Form.Item name="currentPassword" label="Current Password">
              <Input.Password />
            </Form.Item>
            <Form.Item name="newPassword" label="New Password">
              <Input.Password />
            </Form.Item>
            <Form.Item name="confirmPassword" label="Confirm Password">
              <Input.Password />
            </Form.Item>
            <Divider />
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <div>
                <div style={{ fontWeight: 500 }}>Two-Factor Authentication</div>
                <div style={{ color: '#999', fontSize: 13 }}>Add an extra layer of security</div>
              </div>
              <Switch />
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <div>
                <div style={{ fontWeight: 500 }}>Session Timeout</div>
                <div style={{ color: '#999', fontSize: 13 }}>Auto logout after inactivity</div>
              </div>
              <Switch defaultChecked />
            </div>
            <Button type="primary" onClick={handleSecuritySave}>Save Changes</Button>
          </Form>
        </Card>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>Settings</Title>
      <Card style={{ padding: 0 }}>
        <Tabs tabPosition="left" items={tabItems} style={{ minHeight: 400 }} />
      </Card>
    </div>
  );
}
