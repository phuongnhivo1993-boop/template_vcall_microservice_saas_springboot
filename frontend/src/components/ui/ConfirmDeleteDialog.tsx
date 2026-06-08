'use client';

import { Modal, Typography } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface ConfirmDeleteDialogProps {
  open: boolean;
  title?: string;
  message?: string;
  itemName?: string;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  confirmText?: string;
  cancelText?: string;
  danger?: boolean;
}

export default function ConfirmDeleteDialog({
  open,
  title = 'Confirm Delete',
  message,
  itemName,
  loading = false,
  onConfirm,
  onCancel,
  confirmText = 'Delete',
  cancelText = 'Cancel',
  danger = true,
}: ConfirmDeleteDialogProps) {
  return (
    <Modal
      open={open}
      title={
        <span>
          <ExclamationCircleOutlined style={{ color: '#ff4d4f', marginRight: 8 }} />
          {title}
        </span>
      }
      onOk={onConfirm}
      onCancel={onCancel}
      confirmLoading={loading}
      okText={confirmText}
      cancelText={cancelText}
      okButtonProps={{ danger }}
      destroyOnClose
      centered
    >
      <div style={{ padding: '16px 0' }}>
        {message ? (
          <Text>{message}</Text>
        ) : (
          <Text>
            Are you sure you want to delete
            {itemName ? <Text strong> {itemName}</Text> : ' this item'}?
            This action cannot be undone.
          </Text>
        )}
      </div>
    </Modal>
  );
}
