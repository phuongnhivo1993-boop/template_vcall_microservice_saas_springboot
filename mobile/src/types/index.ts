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
