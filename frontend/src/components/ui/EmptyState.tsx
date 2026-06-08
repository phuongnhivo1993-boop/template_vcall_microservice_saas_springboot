'use client';

import { Typography, Button, Empty } from 'antd';
import type { ButtonProps } from 'antd';

const { Title, Text } = Typography;

interface EmptyStateProps {
  title?: string;
  description?: string;
  actionText?: string;
  actionProps?: ButtonProps;
  onAction?: () => void;
  image?: React.ReactNode;
}

export default function EmptyState({
  title = 'No data available',
  description,
  actionText,
  actionProps,
  onAction,
  image,
}: EmptyStateProps) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '48px 24px',
        textAlign: 'center',
      }}
    >
      {image || <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={false} />}
      <Title level={4} style={{ marginTop: 16, marginBottom: 8 }}>
        {title}
      </Title>
      {description && (
        <Text type="secondary" style={{ fontSize: 14, maxWidth: 400, marginBottom: 16 }}>
          {description}
        </Text>
      )}
      {actionText && onAction && (
        <Button type="primary" onClick={onAction} {...actionProps}>
          {actionText}
        </Button>
      )}
    </div>
  );
}
