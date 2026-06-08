'use client';

import { Spin, Typography } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';

interface LoadingSpinnerProps {
  tip?: string;
  size?: 'small' | 'default' | 'large';
  fullPage?: boolean;
  minHeight?: number | string;
}

export default function LoadingSpinner({
  tip = 'Loading...',
  size = 'large',
  fullPage = false,
  minHeight,
}: LoadingSpinnerProps) {
  const indicator = <LoadingOutlined spin />;

  if (fullPage) {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: minHeight || '60vh',
          flexDirection: 'column',
          gap: 16,
        }}
      >
        <Spin indicator={indicator} size={size} />
        <Typography.Text type="secondary">{tip}</Typography.Text>
      </div>
    );
  }

  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        padding: 48,
        flexDirection: 'column',
        gap: 16,
        minHeight: minHeight || 200,
      }}
    >
      <Spin indicator={indicator} size={size} />
      <Typography.Text type="secondary">{tip}</Typography.Text>
    </div>
  );
}
