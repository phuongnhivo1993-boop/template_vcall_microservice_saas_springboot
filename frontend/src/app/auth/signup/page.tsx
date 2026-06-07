'use client';

import { useState } from 'react';
import { Form, Input, Button, Card, Typography, message, Steps, Select, Result } from 'antd';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import dayjs from 'dayjs';

const { Title, Text } = Typography;

const PLANS = [
  { value: 'TRIAL', label: 'Trial', desc: '14 days free, 5 agents, 500 calls/mo' },
  { value: 'BASIC', label: 'Basic', desc: '$99/mo, 10 agents, 2000 calls/mo' },
  { value: 'PRO', label: 'Professional', desc: '$299/mo, 50 agents, 10000 calls/mo' },
  { value: 'ENTERPRISE', label: 'Enterprise', desc: 'Custom pricing, unlimited agents' },
];

export default function SignupPage() {
  const [step, setStep] = useState(0);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [tenantInfo, setTenantInfo] = useState<any>(null);
  const [formData, setFormData] = useState({
    companyName: '',
    adminEmail: '',
    adminName: '',
    password: '',
    phone: '',
    plan: 'TRIAL',
  });
  const router = useRouter();

  const handlePlanSelect = (plan: string) => {
    setFormData((prev) => ({ ...prev, plan }));
  };

  const handleSubmit = async () => {
    setLoading(true);
    try {
      const res = await fetch('/api/v1/tenants/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });
      const data = await res.json();
      if (!res.ok) {
        message.error(data.message || data.error || 'Registration failed');
        return;
      }
      setTenantInfo(data.data);
      setSuccess(true);
    } catch {
      message.error('An error occurred during registration');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="login-container">
        <Card className="login-card" style={{ maxWidth: 560 }}>
          <Result
            status="success"
            title="Welcome to VCall!"
            subTitle={
              <div style={{ textAlign: 'left' }}>
                <p>Your account has been created successfully.</p>
                {tenantInfo && (
                  <div style={{ background: '#f6ffed', padding: 12, borderRadius: 6, margin: '12px 0' }}>
                    <p><strong>Tenant ID:</strong> {tenantInfo.tenantId}</p>
                    <p><strong>Plan:</strong> {tenantInfo.plan}</p>
                    <p><strong>Trial ends:</strong> {dayjs(tenantInfo.trialEndDate).format('DD/MM/YYYY')}</p>
                    <p><strong>Max Agents:</strong> {tenantInfo.maxAgents}</p>
                    <p><strong>Max Users:</strong> {tenantInfo.maxUsers}</p>
                  </div>
                )}
              </div>
            }
            extra={[
              <Link key="login" href="/auth/login">
                <Button type="primary" size="large">Go to Login</Button>
              </Link>,
            ]}
          />
        </Card>
      </div>
    );
  }

  return (
    <div className="login-container">
      <Card className="login-card" style={{ maxWidth: 560 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <img src="/logo.svg" alt="VCall" style={{ height: 40, marginBottom: 12 }} />
          <Title level={2} style={{ margin: 0 }}>Create Your Account</Title>
          <Text type="secondary">Start your 14-day free trial</Text>
        </div>

        <Steps
          current={step}
          size="small"
          items={[
            { title: 'Company' },
            { title: 'Admin' },
            { title: 'Plan' },
          ]}
          style={{ marginBottom: 32 }}
        />

        {step === 0 && (
          <Form layout="vertical" size="large" onFinish={() => setStep(1)}>
            <Form.Item
              name="companyName"
              label="Company Name"
              rules={[{ required: true, message: 'Company name is required' }, { min: 2, message: 'Min 2 characters' }]}
            >
              <Input placeholder="Your Healthcare Organization" onChange={(e) => setFormData((p) => ({ ...p, companyName: e.target.value }))} />
            </Form.Item>
            <Form.Item
              name="phone"
              label="Phone Number"
            >
              <Input placeholder="+84 123 456 789" onChange={(e) => setFormData((p) => ({ ...p, phone: e.target.value }))} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block>Next: Admin Account</Button>
            </Form.Item>
          </Form>
        )}

        {step === 1 && (
          <Form layout="vertical" size="large" onFinish={handleSubmit}>
            <Form.Item
              name="adminName"
              label="Your Name"
              rules={[{ required: true, message: 'Name is required' }]}
            >
              <Input placeholder="John Doe" onChange={(e) => setFormData((p) => ({ ...p, adminName: e.target.value }))} />
            </Form.Item>
            <Form.Item
              name="adminEmail"
              label="Email Address"
              rules={[
                { required: true, message: 'Email is required' },
                { type: 'email', message: 'Valid email required' },
              ]}
            >
              <Input placeholder="admin@clinic.com" onChange={(e) => setFormData((p) => ({ ...p, adminEmail: e.target.value }))} />
            </Form.Item>
            <Form.Item
              name="password"
              label="Password"
              rules={[
                { required: true, message: 'Password is required' },
                { min: 8, message: 'Min 8 characters' },
              ]}
            >
              <Input.Password placeholder="Min 8 characters" onChange={(e) => setFormData((p) => ({ ...p, password: e.target.value }))} />
            </Form.Item>
            <div style={{ display: 'flex', gap: 12 }}>
              <Button onClick={() => setStep(0)}>Back</Button>
              <Button type="primary" htmlType="submit" block>Next: Choose Plan</Button>
            </div>
          </Form>
        )}

        {step === 2 && (
          <div>
            <div style={{ display: 'grid', gap: 12, marginBottom: 24 }}>
              {PLANS.map((plan) => (
                <Card
                  key={plan.value}
                  hoverable
                  size="small"
                  style={{
                    border: formData.plan === plan.value ? '2px solid #1677ff' : '1px solid #d9d9d9',
                    cursor: 'pointer',
                  }}
                  onClick={() => handlePlanSelect(plan.value)}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 600 }}>{plan.label}</div>
                      <div style={{ color: '#666', fontSize: 13 }}>{plan.desc}</div>
                    </div>
                    <div>
                      {formData.plan === plan.value && <span style={{ color: '#1677ff', fontSize: 20 }}>✓</span>}
                    </div>
                  </div>
                </Card>
              ))}
            </div>
            <div style={{ display: 'flex', gap: 12 }}>
              <Button onClick={() => setStep(1)}>Back</Button>
              <Button type="primary" onClick={handleSubmit} loading={loading} block>
                Create Account
              </Button>
            </div>
          </div>
        )}

        <div style={{ textAlign: 'center', marginTop: 24 }}>
          <Text type="secondary">
            Already have an account? <Link href="/auth/login">Sign In</Link>
          </Text>
        </div>
      </Card>
    </div>
  );
}
