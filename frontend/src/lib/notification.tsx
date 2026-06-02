import { notification } from 'antd';
import { CheckCircleOutlined, CloseCircleOutlined, InfoCircleOutlined, WarningOutlined } from '@ant-design/icons';

type NotificationType = 'success' | 'error' | 'info' | 'warning';

interface NotifyOptions {
  message: string;
  description?: string;
  duration?: number;
  placement?: 'topRight' | 'topLeft' | 'bottomRight' | 'bottomLeft';
}

const defaultConfig = {
  duration: 4,
  placement: 'topRight' as const,
};

export function showNotification(type: NotificationType, options: NotifyOptions) {
  const icons = {
    success: <CheckCircleOutlined style={{ color: '#52c41a' }} />,
    error: <CloseCircleOutlined style={{ color: '#ff4d4f' }} />,
    info: <InfoCircleOutlined style={{ color: '#1890ff' }} />,
    warning: <WarningOutlined style={{ color: '#faad14' }} />,
  };

  notification[type]({
    ...defaultConfig,
    ...options,
    icon: icons[type],
    message: options.message,
    description: options.description,
    duration: options.duration ?? defaultConfig.duration,
    placement: options.placement ?? defaultConfig.placement,
  });
}

// Convenience functions
export const notifySuccess = (msg: string, desc?: string) =>
  showNotification('success', { message: msg, description: desc });

export const notifyError = (msg: string, desc?: string) =>
  showNotification('error', { message: msg, description: desc });

export const notifyInfo = (msg: string, desc?: string) =>
  showNotification('info', { message: msg, description: desc });

export const notifyWarning = (msg: string, desc?: string) =>
  showNotification('warning', { message: msg, description: desc });

// Predefined messages for common operations (Vietnamese)
export const NOTIFY = {
  createSuccess: (entity: string) => notifySuccess(`Tạo ${entity} thành công`, `${entity} đã được tạo mới.`),
  updateSuccess: (entity: string) => notifySuccess(`Cập nhật ${entity} thành công`, `${entity} đã được cập nhật.`),
  deleteSuccess: (entity: string) => notifySuccess(`Xóa ${entity} thành công`, `${entity} đã được xóa.`),
  importSuccess: (entity: string, count: number) => notifySuccess(`Nhập ${entity} thành công`, `${count} ${entity} đã được nhập.`),
  exportSuccess: (entity: string) => notifySuccess(`Xuất ${entity} thành công`, `File ${entity} đã được tải xuống.`),
  createError: (entity: string, err?: string) => notifyError(`Lỗi tạo ${entity}`, err || `Không thể tạo ${entity}. Vui lòng thử lại.`),
  updateError: (entity: string, err?: string) => notifyError(`Lỗi cập nhật ${entity}`, err || `Không thể cập nhật ${entity}. Vui lòng thử lại.`),
  deleteError: (entity: string, err?: string) => notifyError(`Lỗi xóa ${entity}`, err || `Không thể xóa ${entity}. Vui lòng thử lại.`),
  networkError: () => notifyError('Lỗi kết nối', 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.'),
  permissionDenied: () => notifyWarning('Từ chối truy cập', 'Bạn không có quyền thực hiện thao tác này.'),
};
