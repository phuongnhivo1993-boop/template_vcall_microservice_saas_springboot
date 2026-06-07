import {
  DashboardOutlined, TeamOutlined, UserOutlined, ContactsOutlined,
  PhoneOutlined, FileTextOutlined, ApiOutlined,
  BarChartOutlined, DollarOutlined, BellOutlined,
  SafetyOutlined, SettingOutlined, CustomerServiceOutlined,
  MessageOutlined, BookOutlined, ThunderboltOutlined,
  VideoCameraOutlined, ExperimentOutlined, BuildOutlined,
  AppstoreOutlined, LineChartOutlined, FormOutlined, CalendarOutlined,
} from '@ant-design/icons';
import { Permissions } from '@/lib/permissions';

export interface MenuItem {
  key: string;
  label: string;
  icon: React.ReactNode;
  path?: string;
  permission?: string;
  children?: MenuItem[];
}

export const menuItems: MenuItem[] = [
  { key: 'dashboard', label: 'Tổng quan', icon: <DashboardOutlined />, path: '/dashboard' },
  { key: 'inbox', label: 'Hộp thư đến', icon: <MessageOutlined />, path: '/inbox', permission: Permissions.CALL_MANAGE },
  {
    key: 'operations',
    label: 'Hỗ trợ',
    icon: <PhoneOutlined />,
    permission: Permissions.CALL_VIEW,
    children: [
      { key: 'calls', label: 'Cuộc gọi', icon: <PhoneOutlined />, path: '/calls', permission: Permissions.CALL_VIEW },
      { key: 'tickets', label: 'Yêu cầu', icon: <FileTextOutlined />, path: '/tickets', permission: Permissions.TICKET_VIEW },
      { key: 'surveys', label: 'Khảo sát', icon: <FormOutlined />, path: '/surveys', permission: Permissions.SURVEY_VIEW },
      { key: 'scheduling', label: 'Lịch hẹn', icon: <CalendarOutlined />, path: '/scheduling', permission: Permissions.AGENT_VIEW },
    ],
  },
  {
    key: 'people',
    label: 'Khách hàng & Nhân viên',
    icon: <TeamOutlined />,
    permission: Permissions.AGENT_VIEW,
    children: [
      { key: 'agents', label: 'Nhân viên', icon: <TeamOutlined />, path: '/agents', permission: Permissions.AGENT_VIEW },
      { key: 'customers', label: 'Khách hàng', icon: <UserOutlined />, path: '/customers', permission: Permissions.CUSTOMER_VIEW },
      { key: 'crm', label: 'CRM', icon: <ContactsOutlined />, path: '/crm', permission: Permissions.CRM_VIEW },
    ],
  },
  { key: 'campaigns', label: 'Chiến dịch', icon: <ApiOutlined />, path: '/campaigns', permission: Permissions.CAMPAIGN_VIEW },
  {
    key: 'xr',
    label: 'Thực tế ảo (XR)',
    icon: <VideoCameraOutlined />,
    permission: Permissions.ADMIN_ACCESS,
    children: [
      { key: 'video', label: 'Video 360°', icon: <VideoCameraOutlined />, path: '/video', permission: Permissions.ADMIN_ACCESS },
      { key: 'tours', label: 'Tour VR', icon: <AppstoreOutlined />, path: '/tours', permission: Permissions.ADMIN_ACCESS },
      { key: 'scenes', label: 'Cảnh VR', icon: <ExperimentOutlined />, path: '/scenes', permission: Permissions.ADMIN_ACCESS },
      { key: 'assets', label: 'Tài nguyên', icon: <BuildOutlined />, path: '/assets', permission: Permissions.ADMIN_ACCESS },
      { key: 'analytics', label: 'Phân tích XR', icon: <LineChartOutlined />, path: '/analytics', permission: Permissions.ADMIN_ACCESS },
      { key: 'collaboration', label: 'Hợp tác VR', icon: <CustomerServiceOutlined />, path: '/collaboration', permission: Permissions.ADMIN_ACCESS },
    ],
  },
  {
    key: 'monitoring',
    label: 'Giám sát & Làm việc',
    icon: <BarChartOutlined />,
    permission: Permissions.ADMIN_ACCESS,
    children: [
      { key: 'supervisor', label: 'Giám sát', icon: <CustomerServiceOutlined />, path: '/supervisor', permission: Permissions.ADMIN_ACCESS },
      { key: 'wallboard', label: 'Bảng điều khiển', icon: <BarChartOutlined />, path: '/wallboard', permission: Permissions.ADMIN_ACCESS },
      { key: 'workspace', label: 'Làm việc', icon: <CustomerServiceOutlined />, path: '/workspace', permission: Permissions.CALL_MANAGE },
    ],
  },
  {
    key: 'knowledge',
    label: 'Kiến thức & Tự động',
    icon: <BookOutlined />,
    permission: Permissions.KNOWLEDGE_VIEW,
    children: [
      { key: 'knowledge-base', label: 'Kiến thức', icon: <BookOutlined />, path: '/knowledge-base', permission: Permissions.KNOWLEDGE_VIEW },
      { key: 'automation', label: 'Tự động hóa', icon: <ThunderboltOutlined />, path: '/automation', permission: Permissions.ADMIN_ACCESS },
      { key: 'webhooks', label: 'Webhooks', icon: <ApiOutlined />, path: '/webhooks', permission: Permissions.WEBHOOK_VIEW },
    ],
  },
  { key: 'reports', label: 'Báo cáo', icon: <BarChartOutlined />, path: '/reports', permission: Permissions.REPORTS_VIEW },
  { key: 'billing', label: 'Hóa đơn', icon: <DollarOutlined />, path: '/billing', permission: Permissions.BILLING_VIEW },
  { key: 'notifications', label: 'Thông báo', icon: <BellOutlined />, path: '/notifications', permission: Permissions.NOTIFICATION_VIEW },
  { key: 'audit', label: 'Kiểm tra', icon: <SafetyOutlined />, path: '/audit', permission: Permissions.AUDIT_VIEW },
  { key: 'portal', label: 'Cổng thông tin', icon: <SafetyOutlined />, path: '/portal', permission: Permissions.CUSTOMER_VIEW },
  { key: 'settings', label: 'Cài đặt', icon: <SettingOutlined />, path: '/settings', permission: Permissions.SETTINGS_VIEW },
];
