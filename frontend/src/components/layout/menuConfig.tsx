import {
  DashboardOutlined, TeamOutlined, UserOutlined, ContactsOutlined,
  PhoneOutlined, FileTextOutlined, ApiOutlined,
  BarChartOutlined, DollarOutlined, BellOutlined,
  SafetyOutlined, SettingOutlined, CustomerServiceOutlined,
  MessageOutlined, BookOutlined, ThunderboltOutlined,
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
  { key: 'dashboard', label: 'Dashboard', icon: <DashboardOutlined />, path: '/dashboard' },
  { key: 'agents', label: 'Agents', icon: <TeamOutlined />, path: '/agents', permission: Permissions.AGENT_VIEW },
  { key: 'customers', label: 'Customers', icon: <UserOutlined />, path: '/customers', permission: Permissions.CUSTOMER_VIEW },
  { key: 'crm', label: 'CRM', icon: <ContactsOutlined />, path: '/crm', permission: Permissions.CRM_VIEW },
  { key: 'calls', label: 'Calls', icon: <PhoneOutlined />, path: '/calls', permission: Permissions.CALL_VIEW },
  { key: 'tickets', label: 'Tickets', icon: <FileTextOutlined />, path: '/tickets', permission: Permissions.TICKET_VIEW },
  { key: 'campaigns', label: 'Campaigns', icon: <ApiOutlined />, path: '/campaigns', permission: Permissions.CAMPAIGN_VIEW },
  { key: 'billing', label: 'Billing', icon: <DollarOutlined />, path: '/billing', permission: Permissions.BILLING_VIEW },
  { key: 'reports', label: 'Reports', icon: <BarChartOutlined />, path: '/reports', permission: Permissions.REPORTS_VIEW },
  { key: 'notifications', label: 'Notifications', icon: <BellOutlined />, path: '/notifications', permission: Permissions.NOTIFICATION_VIEW },
  { key: 'audit', label: 'Audit', icon: <SafetyOutlined />, path: '/audit', permission: Permissions.AUDIT_VIEW },
  { key: 'settings', label: 'Settings', icon: <SettingOutlined />, path: '/settings', permission: Permissions.SETTINGS_VIEW },
  { key: 'supervisor', label: 'Supervisor', icon: <CustomerServiceOutlined />, path: '/supervisor', permission: Permissions.ADMIN_ACCESS },
  { key: 'inbox', label: 'Inbox', icon: <MessageOutlined />, path: '/inbox', permission: Permissions.CALL_MANAGE },
  { key: 'workspace', label: 'Workspace', icon: <CustomerServiceOutlined />, path: '/workspace', permission: Permissions.CALL_MANAGE },
  { key: 'wallboard', label: 'Wallboard', icon: <BarChartOutlined />, path: '/wallboard', permission: Permissions.ADMIN_ACCESS },
  { key: 'knowledge-base', label: 'Knowledge Base', icon: <BookOutlined />, path: '/knowledge-base', permission: Permissions.KNOWLEDGE_VIEW },
  { key: 'automation', label: 'Automation', icon: <ThunderboltOutlined />, path: '/automation', permission: Permissions.ADMIN_ACCESS },
];
