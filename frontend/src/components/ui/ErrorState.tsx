'use client';

import { Typography, Button, Result } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface ErrorStateProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
  retryText?: string;
  fullPage?: boolean;
}

export default function ErrorState({
  title = 'Something went wrong',
  message,
  onRetry,
  retryText = 'Try Again',
  fullPage = false,
}: ErrorStateProps) {
  if (fullPage) {
    return (
      <Result
        status="error"
        title={title}
        subTitle={message}
        extra={
          onRetry && (
            <Button type="primary" icon={<ReloadOutlined />} onClick={onRetry}>
              {retryText}
            </Button>
          )
        }
      />
    );
  }

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 48,
        textAlign: 'center',
      }}
    >
      <div
        style={{
          width: 48,
          height: 48,
          borderRadius: '50%',
          backgroundColor: '#fff2f0',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 16,
        }}
      >
        <span style={{ fontSize: 24, color: '#ff4d4f' }}>!</span>
      </div>
      <Text strong style={{ fontSize: 16, marginBottom: 8 }}>
        {title}
      </Text>
      {message && (
        <Text type="secondary" style={{ fontSize: 14, maxWidth: 400, marginBottom: 16 }}>
          {message}
        </Text>
      )}
      {onRetry && (
        <Button icon={<ReloadOutlined />} onClick={onRetry}>
          {retryText}
        </Button>
      )}
    </div>
  );
}
