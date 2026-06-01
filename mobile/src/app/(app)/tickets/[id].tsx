import { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, FlatList,
  StyleSheet, ScrollView,
} from 'react-native';
import { useLocalSearchParams, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import type { Ticket, TicketPriority, TicketStatus, TicketComment } from '../../../types';

const MOCK_TICKET: Ticket = {
  id: '1',
  title: 'Cannot access email',
  description: 'User reports inability to log into email account. They have tried resetting their password multiple times but receive no reset email. Their account is active and they can access other services.',
  status: 'open',
  priority: 'high',
  customerId: 'c1',
  customerName: 'John Doe',
  slaDeadline: new Date(Date.now() + 7200000).toISOString(),
  createdAt: new Date(Date.now() - 3600000).toISOString(),
  updatedAt: new Date(Date.now() - 1800000).toISOString(),
  comments: [
    { id: 'cm1', ticketId: '1', authorId: 'c1', authorName: 'John Doe', authorType: 'customer', content: 'I haven\'t been able to log in since yesterday.', createdAt: new Date(Date.now() - 3600000).toISOString() },
    { id: 'cm2', ticketId: '1', authorId: 'a1', authorName: 'Agent Smith', authorType: 'agent', content: 'I can see your account is active. Let me check the email logs.', createdAt: new Date(Date.now() - 3000000).toISOString() },
  ],
};

const priorityColors: Record<TicketPriority, string> = {
  low: Colors.success,
  medium: Colors.warning,
  high: Colors.error,
  critical: '#ff0000',
};

const statusLabels: Record<TicketStatus, string> = {
  open: 'Open',
  in_progress: 'In Progress',
  resolved: 'Resolved',
  closed: 'Closed',
};

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function getSlaCountdown(deadline?: string): { label: string; color: string } | null {
  if (!deadline) return null;
  const diff = new Date(deadline).getTime() - Date.now();
  if (diff <= 0) return { label: 'SLA Breached', color: Colors.error };
  const hrs = Math.floor(diff / 3600000);
  const mins = Math.floor((diff % 3600000) / 60000);
  return { label: `${hrs}h ${mins}m remaining`, color: hrs < 1 ? Colors.error : hrs < 4 ? Colors.warning : Colors.success };
}

export default function TicketDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [ticket] = useState<Ticket>(MOCK_TICKET);
  const [comment, setComment] = useState('');

  const sla = getSlaCountdown(ticket.slaDeadline);

  const handleAddComment = () => {
    if (!comment.trim()) return;
    // In a real app, dispatch createComment thunk
    setComment('');
  };

  const renderComment = ({ item }: { item: TicketComment }) => {
    const isAgent = item.authorType === 'agent';
    return (
      <View style={[styles.commentBubble, isAgent ? styles.agentComment : styles.customerComment]}>
        <View style={styles.commentHeader}>
          <Text style={[styles.commentAuthor, isAgent && styles.agentCommentAuthor]}>{item.authorName}</Text>
          <Text style={styles.commentTime}>{formatDate(item.createdAt)}</Text>
        </View>
        <Text style={[styles.commentText, isAgent && styles.agentCommentText]}>{item.content}</Text>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <Stack.Screen
        options={{
          headerShown: true,
          headerTitle: `Ticket #${id}`,
          headerBackTitle: 'Back',
          headerStyle: { backgroundColor: Colors.white },
          headerTintColor: Colors.text,
        }}
      />

      <FlatList
        data={ticket.comments}
        keyExtractor={(item) => item.id}
        renderItem={renderComment}
        ListHeaderComponent={() => (
          <View style={styles.detailCard}>
            <View style={styles.badges}>
              <View style={[styles.badge, { backgroundColor: priorityColors[ticket.priority] }]}>
                <Text style={styles.badgeText}>{ticket.priority.toUpperCase()}</Text>
              </View>
              <View style={[styles.badge, { backgroundColor: Colors.primary }]}>
                <Text style={styles.badgeText}>{statusLabels[ticket.status]}</Text>
              </View>
            </View>

            <Text style={styles.ticketTitle}>{ticket.title}</Text>
            <Text style={styles.ticketDescription}>{ticket.description}</Text>

            <View style={styles.metaRow}>
              <Ionicons name="person-outline" size={16} color={Colors.textSecondary} />
              <Text style={styles.metaText}>{ticket.customerName}</Text>
            </View>

            {sla && (
              <View style={[styles.slaRow, { backgroundColor: sla.color + '15' }]}>
                <Ionicons name="timer-outline" size={16} color={sla.color} />
                <Text style={[styles.slaText, { color: sla.color }]}>{sla.label}</Text>
              </View>
            )}

            <View style={styles.metaRow}>
              <Ionicons name="calendar-outline" size={16} color={Colors.textSecondary} />
              <Text style={styles.metaText}>Created {formatDate(ticket.createdAt)}</Text>
            </View>

            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Comments ({ticket.comments.length})</Text>
            </View>
          </View>
        )}
        contentContainerStyle={styles.listContent}
        ListFooterComponent={() => (
          <View style={styles.commentInputRow}>
            <TextInput
              style={styles.commentInput}
              placeholder="Add a comment..."
              placeholderTextColor={Colors.textSecondary}
              value={comment}
              onChangeText={setComment}
              multiline
            />
            <TouchableOpacity
              style={[styles.commentSendBtn, !comment.trim() && styles.commentSendBtnDisabled]}
              onPress={handleAddComment}
              disabled={!comment.trim()}
            >
              <Ionicons name="send" size={18} color={Colors.white} />
            </TouchableOpacity>
          </View>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  listContent: {
    padding: 16,
  },
  detailCard: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
  },
  badges: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 12,
  },
  badge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 4,
  },
  badgeText: {
    color: Colors.white,
    fontSize: 11,
    fontWeight: '600',
  },
  ticketTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: Colors.text,
    marginBottom: 8,
  },
  ticketDescription: {
    fontSize: 14,
    color: Colors.textSecondary,
    lineHeight: 20,
    marginBottom: 16,
  },
  metaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 8,
  },
  metaText: {
    fontSize: 13,
    color: Colors.textSecondary,
  },
  slaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    padding: 8,
    borderRadius: 8,
    marginBottom: 8,
  },
  slaText: {
    fontSize: 13,
    fontWeight: '600',
  },
  sectionHeader: {
    marginTop: 8,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: Colors.border,
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
  },
  commentBubble: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 12,
    marginBottom: 8,
  },
  agentComment: {
    borderLeftWidth: 3,
    borderLeftColor: Colors.primary,
  },
  customerComment: {
    borderLeftWidth: 3,
    borderLeftColor: Colors.textSecondary,
  },
  commentHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  commentAuthor: {
    fontSize: 13,
    fontWeight: '600',
    color: Colors.text,
  },
  agentCommentAuthor: {
    color: Colors.primary,
  },
  commentTime: {
    fontSize: 11,
    color: Colors.textSecondary,
  },
  commentText: {
    fontSize: 14,
    color: Colors.text,
    lineHeight: 19,
  },
  agentCommentText: {
    color: Colors.text,
  },
  commentInputRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 8,
    gap: 8,
    marginTop: 8,
  },
  commentInput: {
    flex: 1,
    fontSize: 14,
    color: Colors.text,
    maxHeight: 80,
    paddingHorizontal: 8,
    paddingVertical: 6,
  },
  commentSendBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
  },
  commentSendBtnDisabled: {
    backgroundColor: Colors.border,
  },
});
