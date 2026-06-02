export const Permissions = {
  USER_VIEW: 'user:view',
  USER_CREATE: 'user:create',
  USER_EDIT: 'user:edit',
  USER_DELETE: 'user:delete',

  AGENT_VIEW: 'agent:view',
  AGENT_CREATE: 'agent:create',
  AGENT_EDIT: 'agent:edit',
  AGENT_DELETE: 'agent:delete',

  CUSTOMER_VIEW: 'customer:view',
  CUSTOMER_CREATE: 'customer:create',
  CUSTOMER_EDIT: 'customer:edit',
  CUSTOMER_DELETE: 'customer:delete',

  CRM_VIEW: 'crm:view',
  CRM_CREATE: 'crm:create',
  CRM_EDIT: 'crm:edit',
  CRM_DELETE: 'crm:delete',

  CALL_VIEW: 'call:view',
  CALL_MANAGE: 'call:manage',

  TICKET_VIEW: 'ticket:view',
  TICKET_CREATE: 'ticket:create',
  TICKET_EDIT: 'ticket:edit',
  TICKET_DELETE: 'ticket:delete',

  CAMPAIGN_VIEW: 'campaign:view',
  CAMPAIGN_CREATE: 'campaign:create',
  CAMPAIGN_EDIT: 'campaign:edit',
  CAMPAIGN_DELETE: 'campaign:delete',

  BILLING_VIEW: 'billing:view',
  BILLING_MANAGE: 'billing:manage',

  REPORTS_VIEW: 'report:view',
  REPORTS_EXPORT: 'report:export',

  NOTIFICATION_VIEW: 'notification:view',
  NOTIFICATION_SEND: 'notification:send',

  AUDIT_VIEW: 'audit:view',

  SETTINGS_VIEW: 'settings:view',
  SETTINGS_MANAGE: 'settings:manage',

  ADMIN_ACCESS: 'admin:access',

  KNOWLEDGE_VIEW: 'knowledge:view',
  KNOWLEDGE_CREATE: 'knowledge:create',
  KNOWLEDGE_EDIT: 'knowledge:edit',
  KNOWLEDGE_DELETE: 'knowledge:delete',

  INBOX_VIEW: 'inbox:view',

  WEBHOOK_VIEW: 'webhook:view',
  WEBHOOK_CREATE: 'webhook:create',
  WEBHOOK_EDIT: 'webhook:edit',
  WEBHOOK_DELETE: 'webhook:delete',
} as const;

export type Permission = typeof Permissions[keyof typeof Permissions];

export const RolePermissions: Record<string, Permission[]> = {
  SUPER_ADMIN: Object.values(Permissions),
  SUPERVISOR: [
    Permissions.USER_VIEW,
    Permissions.AGENT_VIEW, Permissions.AGENT_CREATE, Permissions.AGENT_EDIT,
    Permissions.CUSTOMER_VIEW, Permissions.CUSTOMER_CREATE, Permissions.CUSTOMER_EDIT,
    Permissions.CRM_VIEW,
    Permissions.CALL_VIEW, Permissions.CALL_MANAGE,
    Permissions.TICKET_VIEW, Permissions.TICKET_CREATE, Permissions.TICKET_EDIT,
    Permissions.CAMPAIGN_VIEW, Permissions.CAMPAIGN_CREATE, Permissions.CAMPAIGN_EDIT,
    Permissions.REPORTS_VIEW, Permissions.REPORTS_EXPORT,
    Permissions.NOTIFICATION_VIEW, Permissions.NOTIFICATION_SEND,
    Permissions.SETTINGS_VIEW,
    Permissions.KNOWLEDGE_VIEW, Permissions.KNOWLEDGE_CREATE, Permissions.KNOWLEDGE_EDIT,
    Permissions.INBOX_VIEW,
    Permissions.WEBHOOK_VIEW, Permissions.WEBHOOK_CREATE, Permissions.WEBHOOK_EDIT, Permissions.WEBHOOK_DELETE,
  ],
  AGENT: [
    Permissions.CUSTOMER_VIEW, Permissions.CUSTOMER_CREATE,
    Permissions.CRM_VIEW,
    Permissions.CALL_VIEW, Permissions.CALL_MANAGE,
    Permissions.TICKET_VIEW, Permissions.TICKET_CREATE,
    Permissions.NOTIFICATION_VIEW,
    Permissions.KNOWLEDGE_VIEW,
    Permissions.INBOX_VIEW,
  ],
  QA: [
    Permissions.CALL_VIEW,
    Permissions.TICKET_VIEW,
    Permissions.REPORTS_VIEW,
  ],
  FINANCE: [
    Permissions.BILLING_VIEW, Permissions.BILLING_MANAGE,
    Permissions.REPORTS_VIEW, Permissions.REPORTS_EXPORT,
  ],
  CUSTOMER: [
    Permissions.TICKET_VIEW, Permissions.TICKET_CREATE,
    Permissions.NOTIFICATION_VIEW,
  ],
};

export function hasPermission(userRole: string | undefined, permission: Permission): boolean {
  if (!userRole) return false;
  const rolePerms = RolePermissions[userRole.toUpperCase()];
  if (!rolePerms) return false;
  return rolePerms.includes(permission);
}

export function hasAnyPermission(userRole: string | undefined, permissions: Permission[]): boolean {
  return permissions.some(p => hasPermission(userRole, p));
}
