export interface User {
  id: string;
  email: string;
  name: string;
  avatar?: string;
  role: 'agent' | 'admin' | 'supervisor';
}

export interface Agent {
  id: string;
  userId: string;
  name: string;
  email: string;
  extension: string;
  avatar?: string;
  status: AgentStatus;
  callsToday: number;
  avgDuration: number;
  online: boolean;
}

export type AgentStatus = 'online' | 'offline' | 'away' | 'busy';

export interface Call {
  id: string;
  callerId: string;
  callerName: string;
  callerNumber: string;
  calleeId?: string;
  calleeName: string;
  calleeNumber: string;
  direction: 'incoming' | 'outgoing';
  status: CallStatus;
  duration: number;
  startedAt: string;
  endedAt?: string;
  recording?: string;
}

export type CallStatus = 'connected' | 'ringing' | 'ended' | 'missed' | 'voicemail';

export interface ActiveCall {
  id: string;
  channelId: string;
  callerName: string;
  callerNumber: string;
  direction: 'incoming' | 'outgoing';
  status: CallStatus;
  startTime: number;
  isMuted: boolean;
  isSpeakerOn: boolean;
  isOnHold: boolean;
}

export interface ChatMessage {
  id: string;
  conversationId: string;
  senderId: string;
  senderType: 'agent' | 'customer';
  content: string;
  type: 'text' | 'image' | 'file';
  fileUrl?: string;
  fileName?: string;
  createdAt: string;
  read: boolean;
}

export interface Conversation {
  id: string;
  customerId: string;
  customerName: string;
  customerAvatar?: string;
  lastMessage?: string;
  lastMessageAt?: string;
  unreadCount: number;
  status: 'active' | 'resolved' | 'pending';
}

export interface Ticket {
  id: string;
  title: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  customerId: string;
  customerName: string;
  assignedTo?: string;
  assignedToName?: string;
  slaDeadline?: string;
  createdAt: string;
  updatedAt: string;
  comments: TicketComment[];
}

export type TicketStatus = 'open' | 'in_progress' | 'resolved' | 'closed';

export type TicketPriority = 'low' | 'medium' | 'high' | 'critical';

export interface TicketComment {
  id: string;
  ticketId: string;
  authorId: string;
  authorName: string;
  authorType: 'agent' | 'customer';
  content: string;
  createdAt: string;
}

export interface DashboardStats {
  totalCalls: number;
  totalTickets: number;
  totalConversations: number;
  totalCustomers: number;
  missedCalls: number;
  avgWaitTime: number;
  satisfaction: number;
}

export interface Activity {
  id: string;
  type: 'call' | 'ticket' | 'chat' | 'system';
  message: string;
  timestamp: string;
}

export interface Customer {
  id: string;
  name: string;
  email: string;
  phone: string;
  company?: string;
  totalTickets: number;
  lastContact: string;
  status: 'active' | 'inactive' | 'lead';
}

export interface Campaign {
  id: string;
  name: string;
  description: string;
  type: 'email' | 'sms' | 'call' | 'social';
  status: 'draft' | 'active' | 'paused' | 'completed';
  sentCount: number;
  openRate: number;
  clickRate: number;
  startDate: string;
  endDate?: string;
  createdAt: string;
}

export interface Invoice {
  id: string;
  amount: number;
  currency: string;
  status: 'paid' | 'pending' | 'overdue' | 'cancelled';
  description: string;
  issuedAt: string;
  paidAt?: string;
  dueDate: string;
}

export interface Report {
  id: string;
  name: string;
  type: string;
  period: string;
  createdAt: string;
  format: 'pdf' | 'csv' | 'excel';
}

export interface SupervisorAgent {
  id: string;
  name: string;
  email: string;
  extension: string;
  status: AgentStatus;
  callsActive: number;
  callsToday: number;
  avgDuration: number;
  online: boolean;
}

export interface Webhook {
  id: string;
  name: string;
  url: string;
  events: string[];
  status: 'active' | 'inactive' | 'failed';
  lastTriggered?: string;
  createdAt: string;
}

export interface KnowledgeArticle {
  id: string;
  title: string;
  content: string;
  category: string;
  tags: string[];
  views: number;
  helpful: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AutomationRule {
  id: string;
  name: string;
  description: string;
  trigger: string;
  action: string;
  status: 'active' | 'inactive';
  lastRun?: string;
  createdAt: string;
}

export interface AppNotification {
  id: string;
  title: string;
  message: string;
  type: 'info' | 'success' | 'warning' | 'error';
  read: boolean;
  createdAt: string;
}

export interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
  loading: boolean;
}

export interface CallState {
  activeCall: ActiveCall | null;
  callHistory: Call[];
  callStatus: 'idle' | 'ringing' | 'connected' | 'dialing';
}
