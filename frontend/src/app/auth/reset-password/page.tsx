'use client';

import { Suspense, useState } from 'react';
import { Form, Input, Button, Card, Typography, message, Result, Spin } from 'antd';
import { LockOutlined } from '@ant-design/icons';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';

const { Title, Text } = Typography;

function ResetPasswordForm() {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get('token');

  const onFinish = async (values: { newPassword: string }) => {
    if (!token) {
      message.error('Invalid or missing reset token');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('/api/v1/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword: values.newPassword }),
      });

      if (!res.ok) {
        const data = await res.json();
        message.error(data.message || data.error || 'Reset failed. Token may be expired.');
        return;
      }

      setSuccess(true);
      setTimeout(() => router.push('/auth/login'), 3000);
    } catch {
      message.error('An error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <Card className="login-card">
        <Result
          status="warning"
          title="Invalid Reset Link"
          subTitle="This password reset link is missing or invalid. Please request a new one."
          extra={[
            <Link key="forgot" href="/auth/forgot-password">
              <Button type="primary">Request New Link</Button>
            </Link>,
          ]}
        />
      </Card>
    );
  }

  if (success) {
    return (
      <Card className="login-card">
        <Result
          status="success"
          title="Password Reset Successful"
          subTitle="Your password has been updated. Redirecting to login..."
          extra={[
            <Link key="login" href="/auth/login">
              <Button type="primary">Go to Login</Button>
            </Link>,
          ]}
        />
      </Card>
    );
  }

  return (
    <Card className="login-card">
      <div style={{ textAlign: 'center', marginBottom: 32 }}>
        <Title level={2} style={{ margin: 0 }}>Reset Password</Title>
        <Text type="secondary">Enter your new password</Text>
      </div>
      <Form
        name="reset-password"
        onFinish={onFinish}
        layout="vertical"
        size="large"
      >
        <Form.Item
          name="newPassword"
          label="New Password"
          rules={[
            { required: true, message: 'Please enter a new password' },
            { min: 8, message: 'Password must be at least 8 characters' },
          ]}
        >
          <Input.Password prefix={<LockOutlined />} placeholder="New password" />
        </Form.Item>
        <Form.Item
          name="confirmPassword"
          label="Confirm Password"
          dependencies={['newPassword']}
          rules={[
            { required: true, message: 'Please confirm your password' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('newPassword') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('Passwords do not match'));
              },
            }),
          ]}
        >
          <Input.Password prefix={<LockOutlined />} placeholder="Confirm password" />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading} block>
            Reset Password
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
}

export default function ResetPasswordPage() {
  return (
    <div className="login-container">
      <Suspense fallback={
        <Card className="login-card" style={{ textAlign: 'center' }}>
          <Spin size="large" />
        </Card>
      }>
        <ResetPasswordForm />
      </Suspense>
    </div>
  );
}
