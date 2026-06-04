'use client';

import { useState } from 'react';
import { Form, Input, Button, Card, Typography, message, Result } from 'antd';
import { MailOutlined } from '@ant-design/icons';
import Link from 'next/link';

const { Title, Text } = Typography;

export default function ForgotPasswordPage() {
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const onFinish = async (values: { email: string }) => {
    setLoading(true);
    try {
      const res = await fetch('/api/v1/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: values.email }),
      });
      setSent(true);
    } catch {
      message.error('An error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <div className="login-container">
        <Card className="login-card">
          <Result
            status="success"
            title="Reset Link Sent"
            subTitle="If an account exists with that email, you will receive a password reset link within a few minutes. The link expires in 30 minutes."
            extra={[
              <Link key="login" href="/auth/login">
                <Button type="primary">Back to Login</Button>
              </Link>,
            ]}
          />
        </Card>
      </div>
    );
  }

  return (
    <div className="login-container">
      <Card className="login-card">
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={2} style={{ margin: 0 }}>Forgot Password</Title>
          <Text type="secondary">Enter your email to receive a reset link</Text>
        </div>
        <Form
          name="forgot-password"
          onFinish={onFinish}
          layout="vertical"
          size="large"
        >
          <Form.Item
            name="email"
            rules={[
              { required: true, message: 'Please enter your email' },
              { type: 'email', message: 'Please enter a valid email' },
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="Email address" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              Send Reset Link
            </Button>
          </Form.Item>
          <div style={{ textAlign: 'center' }}>
            <Link href="/auth/login">Back to Login</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
}
