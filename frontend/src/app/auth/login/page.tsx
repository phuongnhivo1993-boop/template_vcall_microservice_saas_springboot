'use client';

import { useState } from 'react';
import { Form, Input, Button, Card, Typography, message } from 'antd';
import { UserOutlined, LockOutlined, SafetyOutlined } from '@ant-design/icons';
import { signIn } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { connectSocket } from '@/lib/socket';

const { Title, Text } = Typography;

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const [mfaRequired, setMfaRequired] = useState(false);
  const [mfaToken, setMfaToken] = useState('');
  const [mfaLoading, setMfaLoading] = useState(false);
  const router = useRouter();

  const onFinish = async (values: { username: string; password: string; company?: string }) => {
    setLoading(true);
    try {
      const res = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: values.username, password: values.password, tenantId: values.company }),
      });
      const data = await res.json();

      if (!res.ok) {
        if (res.status === 423) {
          message.error(`Account locked. Try again in ${Math.ceil((data.retryAfterSeconds || 1740) / 60)} minutes.`);
        } else if (res.status === 503) {
          message.error('Service temporarily unavailable. Please ensure all backend services are running and try again.');
        } else {
          message.error(data.message || data.error || 'Invalid credentials');
        }
        return;
      }

      if (data.mfaRequired) {
        setMfaToken(data.mfaToken);
        setMfaRequired(true);
        return;
      }

      const loginData = data.data || data;

      const result = await signIn('credentials', {
        username: loginData.user?.username || '',
        password: '',
        accessToken: loginData.accessToken,
        refreshToken: loginData.refreshToken,
        userRole: loginData.user?.role || 'AGENT',
        userId: loginData.user?.id || '',
        userEmail: loginData.user?.email || '',
        redirect: false,
      });

      if (result?.error) {
        message.error('Login failed');
      } else {
        message.success('Login successful');
        connectSocket(loginData.accessToken);
        router.push('/dashboard');
      }
    } catch {
      message.error('An error occurred during login');
    } finally {
      setLoading(false);
    }
  };

  const handleMfaVerify = async (values: { code: string }) => {
    setMfaLoading(true);
    try {
      const res = await fetch('/api/v1/auth/mfa-challenge', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ mfaToken, code: values.code }),
      });
      const data = await res.json();

      if (!res.ok) {
        message.error(data.error || 'Invalid verification code');
        return;
      }

      const mfaLoginData = data.data || data;

      const result = await signIn('credentials', {
        username: mfaLoginData.user?.username || '',
        password: '',
        accessToken: mfaLoginData.accessToken,
        refreshToken: mfaLoginData.refreshToken,
        userRole: mfaLoginData.user?.role || 'AGENT',
        userId: mfaLoginData.user?.id || '',
        userEmail: mfaLoginData.user?.email || '',
        redirect: false,
      });

      if (result?.error) {
        message.error('MFA verification failed');
      } else {
        message.success('Login successful');
        router.push('/dashboard');
      }
    } catch {
      message.error('An error occurred during verification');
    } finally {
      setMfaLoading(false);
    }
  };

  if (mfaRequired) {
    return (
      <div className="login-container">
        <Card className="login-card">
          <div style={{ textAlign: 'center', marginBottom: 32 }}>
            <SafetyOutlined style={{ fontSize: 48, color: '#1677ff', marginBottom: 16 }} />
            <Title level={3} style={{ margin: 0 }}>Two-Factor Authentication</Title>
            <Text type="secondary">Enter the 6-digit code from your authenticator app</Text>
          </div>
          <Form name="mfa" onFinish={handleMfaVerify} layout="vertical" size="large">
            <Form.Item
              name="code"
              rules={[
                { required: true, message: 'Please enter verification code' },
                { len: 6, message: 'Code must be 6 digits' },
                { pattern: /^\d{6}$/, message: 'Code must contain only digits' },
              ]}
            >
              <Input.OTP length={6} />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={mfaLoading} block size="large">
                Verify
              </Button>
            </Form.Item>
            <div style={{ textAlign: 'center' }}>
              <Button type="link" onClick={() => setMfaRequired(false)}>
                Back to Login
              </Button>
            </div>
          </Form>
        </Card>
      </div>
    );
  }

  return (
    <div className="login-container">
      <Card className="login-card">
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <img src="/logo.svg" alt="VCall" style={{ height: 48, marginBottom: 16 }} />
          <Title level={2} style={{ margin: 0 }}>VCall</Title>
          <Text type="secondary">Contact Center Platform</Text>
        </div>
        <Form
          name="login"
          onFinish={onFinish}
          layout="vertical"
          size="large"
          autoComplete="off"
          initialValues={{ username: 'admin', password: 'admin@123', company: 'vcall' }}
        >
          <Form.Item name="company" label="Company">
            <Input prefix={<UserOutlined />} placeholder="your-company" />
          </Form.Item>
          <Form.Item
            name="username"
            rules={[{ required: true, message: 'Please enter your username' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="Username" />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please enter your password' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="Password" />
          </Form.Item>
          <div style={{ textAlign: 'right', marginBottom: 16 }}>
            <Link href="/auth/forgot-password">Forgot Password?</Link>
          </div>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              Sign In
            </Button>
          </Form.Item>
        </Form>
        <div style={{ textAlign: 'center' }}>
          <Text type="secondary">
            New to VCall? <Link href="/auth/signup">Create Account</Link>
          </Text>
        </div>
      </Card>
    </div>
  );
}
