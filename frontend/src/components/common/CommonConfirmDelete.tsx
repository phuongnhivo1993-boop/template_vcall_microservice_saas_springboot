'use client';

import { Modal, message } from 'antd';
import { ExclamationCircleOutlined } from '@ant-design/icons';

interface ConfirmDeleteOptions {
  title?: string;
  content?: string;
  okText?: string;
  okType?: 'primary' | 'danger' | 'dashed' | 'default';
  cancelText?: string;
  onOk: () => Promise<void>;
  onCancel?: () => void;
}

export function showDeleteConfirm({ title = 'Confirm Delete', content = 'Are you sure you want to delete this item? This action cannot be undone.', okText = 'Delete', okType = 'danger', cancelText = 'Cancel', onOk, onCancel }: ConfirmDeleteOptions) {
  Modal.confirm({
    title,
    icon: <ExclamationCircleOutlined />,
    content,
    okText,
    okType,
    cancelText,
    onOk: async () => {
      try {
        await onOk();
        message.success('Deleted successfully');
      } catch (error: any) {
        message.error(error?.message || 'Delete failed');
      }
    },
    onCancel,
  });
}
