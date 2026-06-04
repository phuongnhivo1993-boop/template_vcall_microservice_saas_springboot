'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Tabs, Form, Input, Button, Switch, Select, Typography, Divider, message, Space, Row, Col, Spin, Alert } from 'antd';
import { UserOutlined, TeamOutlined, ApiOutlined, DollarOutlined, SafetyOutlined } from '@ant-design/icons';
import { settingsApi, billingApi } from '@/lib/api';

const { Title } = Typography;

export default function SettingsPage() {
  const [profileForm] = Form.useForm();
  const [orgForm] = Form.useForm();
  const [securityForm] = Form.useForm();

  const [profileLoading, setProfileLoading] = useState(false);
  const [orgLoading, setOrgLoading] = useState(false);
  const [channelsLoading, setChannelsLoading] = useState(false);
  const [billingLoading, setBillingLoading] = useState(false);
  const [securityLoading, setSecurityLoading] = useState(false);

  const [profileSaving, setProfileSaving] = useState(false);
  const [orgSaving, setOrgSaving] = useState(false);
  const [securitySaving, setSecuritySaving] = useState(false);
  const [channelsSaving, setChannelsSaving] = useState(false);

  const [error, setError] = useState<string | null>(null);

  const [channelSettings, setChannelSettings] = useState({
    voice: true, sms: true, email: true, webChat: false,
  });

  const [billingInfo, setBillingInfo] = useState<Record<string, any>>({});
  const [billingLoadingError, setBillingLoadingError] = useState<string | null>(null);

  const [mfaEnabled, setMfaEnabled] = useState(false);
  const [mfaLoading, setMfaLoading] = useState(false);
  const [mfaVerifying, setMfaVerifying] = useState(false);
  const [mfaQrCode, setMfaQrCode] = useState<string | null>(null);
  const [mfaSecret, setMfaSecret] = useState<string | null>(null);

  const fetchProfile = useCallback(async () => {
    setProfileLoading(true);
    setError(null);
    try {
      const res = await settingsApi.getProfile();
      const data = res.data?.data || res.data || {};
      profileForm.setFieldsValue(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load profile');
    } finally {
      setProfileLoading(false);
    }
  }, [profileForm]);

  const fetchOrganization = useCallback(async () => {
    setOrgLoading(true);
    try {
      const res = await settingsApi.getOrganization();
      const data = res.data?.data || res.data || {};
      orgForm.setFieldsValue(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load organization');
    } finally {
      setOrgLoading(false);
    }
  }, [orgForm]);

  const fetchChannels = useCallback(async () => {
    setChannelsLoading(true);
    try {
      const res = await settingsApi.getChannels();
      const data = res.data?.data || res.data || {};
      setChannelSettings({
        voice: data.voice ?? true,
        sms: data.sms ?? true,
        email: data.email ?? true,
        webChat: data.webChat ?? false,
      });
    } catch (err: any) {
      setError(err?.message || 'Failed to load channels');
    } finally {
      setChannelsLoading(false);
    }
  }, []);

  const fetchBilling = useCallback(async () => {
    setBillingLoading(true);
    setBillingLoadingError(null);
    try {
      const res = await billingApi.getPlans({ active: true });
      setBillingInfo(res.data?.data || res.data || {});
    } catch (err: any) {
      setBillingLoadingError(err?.message || 'Failed to load billing info');
    } finally {
      setBillingLoading(false);
    }
  }, []);

  const fetchSecurity = useCallback(async () => {
    setSecurityLoading(true);
    try {
      const res = await settingsApi.getSecurity();
      const data = res.data?.data || res.data || {};
      securityForm.setFieldsValue(data);
    } catch (err: any) {
      setError(err?.message || 'Failed to load security settings');
    } finally {
      setSecurityLoading(false);
    }
  }, [securityForm]);

  useEffect(() => { fetchProfile(); }, [fetchProfile]);
  useEffect(() => { fetchOrganization(); }, [fetchOrganization]);
  useEffect(() => { fetchChannels(); }, [fetchChannels]);
  useEffect(() => { fetchBilling(); }, [fetchBilling]);
  useEffect(() => { fetchSecurity(); }, [fetchSecurity]);

  const handleProfileSave = async () => {
    try {
      const values = await profileForm.validateFields();
      setProfileSaving(true);
      await settingsApi.updateProfile(values);
      message.success('Profile updated');
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err?.message || 'Failed to update profile');
    } finally {
      setProfileSaving(false);
    }
  };

  const handleOrgSave = async () => {
    try {
      const values = await orgForm.validateFields();
      setOrgSaving(true);
      await settingsApi.updateOrganization(values);
      message.success('Organization updated');
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err?.message || 'Failed to update organization');
    } finally {
      setOrgSaving(false);
    }
  };

  const handleChannelToggle = async (key: string, value: boolean) => {
    const updated = { ...channelSettings, [key]: value };
    setChannelSettings(updated);
    setChannelsSaving(true);
    try {
      await settingsApi.updateChannels(updated);
      message.success('Channel settings updated');
    } catch (err: any) {
      setChannelSettings(channelSettings);
      message.error(err?.message || 'Failed to update channel settings');
    } finally {
      setChannelsSaving(false);
    }
  };

  const handleSecuritySave = async () => {
    try {
      const values = await securityForm.validateFields();
      setSecuritySaving(true);
      if (values.currentPassword && values.newPassword) {
        const res = await fetch('/api/v1/auth/change-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            currentPassword: values.currentPassword,
            newPassword: values.newPassword,
          }),
        });
        if (!res.ok) {
          const data = await res.json();
          throw new Error(data.message || data.error || 'Failed to change password');
        }
      }
      await settingsApi.updateSecurity(values);
      message.success('Security settings updated');
      securityForm.resetFields(['currentPassword', 'newPassword', 'confirmPassword']);
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err?.message || 'Failed to update security');
    } finally {
      setSecuritySaving(false);
    }
  };

  const handleMfaToggle = async (checked: boolean) => {
    if (checked) {
      setMfaLoading(true);
      try {
        const res = await fetch('/api/v1/mfa/setup', { method: 'POST' });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Failed to setup MFA');
        setMfaQrCode(data.data?.qrCodeUri ? `https://api.qrserver.com/v1/create-qr-code/?data=${encodeURIComponent(data.data.qrCodeUri)}&size=200x200` : null);
        setMfaSecret(data.data?.secret);
      } catch (err: any) {
        message.error(err.message || 'Failed to setup MFA');
      } finally {
        setMfaLoading(false);
      }
    } else {
      setMfaLoading(true);
      try {
        const res = await fetch('/api/v1/mfa/disable', { method: 'POST' });
        if (!res.ok) throw new Error('Failed to disable MFA');
        setMfaEnabled(false);
        setMfaQrCode(null);
        setMfaSecret(null);
        message.success('MFA disabled');
      } catch (err: any) {
        message.error(err.message || 'Failed to disable MFA');
      } finally {
        setMfaLoading(false);
      }
    }
  };

  const handleMfaVerify = async () => {
    const code = securityForm.getFieldValue('mfaCode');
    if (!code || code.length !== 6) {
      message.error('Please enter a valid 6-digit code');
      return;
    }
    setMfaVerifying(true);
    try {
      const res = await fetch('/api/v1/mfa/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ code }),
      });
      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.error || 'Invalid code');
      }
      setMfaEnabled(true);
      setMfaQrCode(null);
      setMfaSecret(null);
      message.success('MFA enabled successfully');
    } catch (err: any) {
      message.error(err.message || 'Verification failed');
    } finally {
      setMfaVerifying(false);
    }
  };

  const tabItems = [
    {
      key: 'profile',
      label: <span><UserOutlined /> Profile</span>,
      children: (
        <Card>
          <Spin spinning={profileLoading}>
            <Title level={4}>Personal Information</Title>
            <Form form={profileForm} layout="vertical" style={{ maxWidth: 600 }}>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="firstName" label="First Name">
                    <Input />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="lastName" label="Last Name">
                    <Input />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item name="email" label="Email" rules={[{ type: 'email' }]}>
                <Input />
              </Form.Item>
              <Form.Item name="phone" label="Phone">
                <Input />
              </Form.Item>
              <Form.Item name="timezone" label="Timezone">
                <Select options={[
                  { value: 'America/New_York', label: 'Eastern Time' },
                  { value: 'America/Chicago', label: 'Central Time' },
                  { value: 'America/Denver', label: 'Mountain Time' },
                  { value: 'America/Los_Angeles', label: 'Pacific Time' },
                ]} />
              </Form.Item>
              <Button type="primary" onClick={handleProfileSave} loading={profileSaving}>Save Changes</Button>
            </Form>
          </Spin>
        </Card>
      ),
    },
    {
      key: 'organization',
      label: <span><TeamOutlined /> Organization</span>,
      children: (
        <Card>
          <Spin spinning={orgLoading}>
            <Title level={4}>Organization Settings</Title>
            <Form form={orgForm} layout="vertical" style={{ maxWidth: 600 }}>
              <Form.Item name="orgName" label="Organization Name">
                <Input />
              </Form.Item>
              <Form.Item name="industry" label="Industry">
                <Input />
              </Form.Item>
              <Form.Item name="maxAgents" label="Max Agents">
                <Input type="number" />
              </Form.Item>
              <Form.Item name="language" label="Default Language">
                <Select options={[
                  { value: 'en', label: 'English' },
                  { value: 'es', label: 'Spanish' },
                  { value: 'fr', label: 'French' },
                ]} />
              </Form.Item>
              <Button type="primary" onClick={handleOrgSave} loading={orgSaving}>Save Changes</Button>
            </Form>
          </Spin>
        </Card>
      ),
    },
    {
      key: 'channels',
      label: <span><ApiOutlined /> Channels</span>,
      children: (
        <Card>
          <Spin spinning={channelsLoading}>
            <Title level={4}>Communication Channels</Title>
            <Space direction="vertical" size="large" style={{ width: '100%', maxWidth: 600 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 500 }}>Voice Calls</div>
                  <div style={{ color: '#999', fontSize: 13 }}>Inbound and outbound voice calls</div>
                </div>
                <Switch checked={channelSettings.voice} onChange={(v) => handleChannelToggle('voice', v)} />
              </div>
              <Divider style={{ margin: 0 }} />
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 500 }}>SMS / Text</div>
                  <div style={{ color: '#999', fontSize: 13 }}>SMS messaging channel</div>
                </div>
                <Switch checked={channelSettings.sms} onChange={(v) => handleChannelToggle('sms', v)} />
              </div>
              <Divider style={{ margin: 0 }} />
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 500 }}>Email</div>
                  <div style={{ color: '#999', fontSize: 13 }}>Email ticketing integration</div>
                </div>
                <Switch checked={channelSettings.email} onChange={(v) => handleChannelToggle('email', v)} />
              </div>
              <Divider style={{ margin: 0 }} />
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 500 }}>Web Chat</div>
                  <div style={{ color: '#999', fontSize: 13 }}>Live website chat widget</div>
                </div>
                <Switch checked={channelSettings.webChat} onChange={(v) => handleChannelToggle('webChat', v)} />
              </div>
            </Space>
          </Spin>
        </Card>
      ),
    },
    {
      key: 'billing',
      label: <span><DollarOutlined /> Billing</span>,
      children: (
        <Card>
          <Spin spinning={billingLoading}>
            <Title level={4}>Subscription & Billing</Title>
            {billingLoadingError ? (
              <Alert type="error" message={billingLoadingError} showIcon />
            ) : (
            <div style={{ maxWidth: 600 }}>
              <div style={{
                background: '#f0f5ff',
                border: '1px solid #d6e4ff',
                borderRadius: 8,
                padding: 24,
                marginBottom: 24,
              }}>
                <div style={{ fontSize: 16, fontWeight: 600 }}>{billingInfo.planName || 'Enterprise Plan'}</div>
                <div style={{ fontSize: 32, fontWeight: 700, color: '#1677ff', margin: '8px 0' }}>
                  ${billingInfo.price || 299}<span style={{ fontSize: 16, fontWeight: 400, color: '#666' }}>/month</span>
                </div>
                <div style={{ color: '#666' }}>{billingInfo.description || '50 agents included • Unlimited calls • 24/7 support'}</div>
              </div>
              <Button type="primary">Upgrade Plan</Button>
              <Button style={{ marginLeft: 12 }}>View Invoices</Button>
            </div>
            )}
          </Spin>
        </Card>
      ),
    },
    {
      key: 'security',
      label: <span><SafetyOutlined /> Security</span>,
      children: (
        <Card>
          <Spin spinning={securityLoading}>
            <Title level={4}>Security Settings</Title>
            <Form form={securityForm} layout="vertical" style={{ maxWidth: 600 }}>
              <Title level={5}>Change Password</Title>
              <Form.Item name="currentPassword" label="Current Password"
                rules={[{ required: true, message: 'Current password required' }]}>
                <Input.Password />
              </Form.Item>
              <Form.Item name="newPassword" label="New Password"
                rules={[
                  { required: true, message: 'New password required' },
                  { min: 8, message: 'Min 8 characters' },
                ]}>
                <Input.Password />
              </Form.Item>
              <Form.Item name="confirmPassword" label="Confirm Password"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: 'Confirm your password' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('newPassword') === value) return Promise.resolve();
                      return Promise.reject(new Error('Passwords do not match'));
                    },
                  }),
                ]}>
                <Input.Password />
              </Form.Item>
              <Divider />
              <Title level={5}>Two-Factor Authentication</Title>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <div>
                  <div style={{ fontWeight: 500 }}>Authenticator App</div>
                  <div style={{ color: '#999', fontSize: 13 }}>Use Google Authenticator or similar TOTP app</div>
                </div>
                <Switch
                  checked={mfaEnabled}
                  loading={mfaLoading}
                  onChange={handleMfaToggle}
                />
              </div>
              {mfaQrCode && (
                <div style={{ textAlign: 'center', marginBottom: 16, padding: 16, background: '#f5f5f5', borderRadius: 8 }}>
                  <p style={{ marginBottom: 8 }}>Scan this QR code with your authenticator app:</p>
                  <img src={mfaQrCode} alt="MFA QR Code" style={{ width: 200, height: 200 }} />
                  <p style={{ color: '#999', fontSize: 12, marginTop: 8 }}>Or enter the secret manually: <code>{mfaSecret}</code></p>
                  <Form.Item
                    name="mfaCode"
                    label="Verify with 6-digit code"
                    rules={[{ required: true, message: 'Enter code to confirm' }, { len: 6, message: '6 digits required' }]}
                    style={{ maxWidth: 200, margin: '12px auto' }}
                  >
                    <Input placeholder="000000" maxLength={6} />
                  </Form.Item>
                  <Button type="primary" onClick={handleMfaVerify} loading={mfaVerifying}>Verify & Enable</Button>
                </div>
              )}
              <Divider />
              <Title level={5}>Session</Title>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <div>
                  <div style={{ fontWeight: 500 }}>Session Timeout</div>
                  <div style={{ color: '#999', fontSize: 13 }}>Auto logout after inactivity</div>
                </div>
                <Switch defaultChecked />
              </div>
              <Button type="primary" onClick={handleSecuritySave} loading={securitySaving}>Save Changes</Button>
            </Form>
          </Spin>
        </Card>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>Settings</Title>
      {error && <Alert type="error" message={error} showIcon closable style={{ marginBottom: 16 }} onClose={() => setError(null)} />}
      <Card style={{ padding: 0 }}>
        <Tabs tabPosition="left" items={tabItems} style={{ minHeight: 400 }} />
      </Card>
    </div>
  );
}
