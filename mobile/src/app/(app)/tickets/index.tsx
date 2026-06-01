import { useState, useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import type { Ticket, TicketStatus, TicketPriority } from '../../../types';

const MOCK_TICKETS: Ticket[] = [
  { id: '1', title: 'Cannot access email', description: 'User reports inability to log into email account', status: 'open', priority: 'high', customerId: 'c1', customerName: 'John Doe', createdAt: new Date(Date.now() - 3600000).toISOString(), updatedAt: new Date(Date.now() - 3600000).toISOString(), comments: [] },
  { id: '2', title: 'Billing discrepancy', description: 'Customer was charged incorrect amount', status: 'in_progress', priority: 'critical', customerId: 'c2', customerName: 'Jane Roe', createdAt: new Date(Date.now() - 7200000).toISOString(), updatedAt: new Date(Date.now() - 1800000).toISOString(), comments: [] },
  { id: '3', title: 'Feature request: Dark mode', description: 'Customer would like dark mode support', status: 'open', priority: 'low', customerId: 'c3', customerName: 'Bob Wilson', createdAt: new Date(Date.now() - 86400000).toISOString(), updatedAt: new Date(Date.now() - 86400000).toISOString(), comments: [] },
  { id: '4', title: 'Account verification pending', description: 'Unable to verify identity documents', status: 'in_progress', priority: 'medium', customerId: 'c4', customerName: 'Alice Brown', createdAt: new Date(Date.now() - 172800000).toISOString(), updatedAt: new Date(Date.now() - 43200000).toISOString(), comments: [] },
  { id: '5', title: 'Refund request', description: 'Customer requesting full refund', status: 'resolved', priority: 'high', customerId: 'c5', customerName: 'Charlie Davis', createdAt: new Date(Date.now() - 259200000).toISOString(), updatedAt: new Date(Date.now() - 86400000).toISOString(), comments: [] },
];

const FILTERS: { label: string; value: TicketStatus | 'all' }[] = [
  { label: 'All', value: 'all' },
  { label: 'Open', value: 'open' },
  { label: 'In Progress', value: 'in_progress' },
  { label: 'Resolved', value: 'resolved' },
  { label: 'Closed', value: 'closed' },
];

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
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

export default function TicketsScreen() {
  const router = useRouter();
  const [activeFilter, setActiveFilter] = useState<TicketStatus | 'all'>('all');

  const filtered = activeFilter === 'all'
    ? MOCK_TICKETS
    : MOCK_TICKETS.filter((t) => t.status === activeFilter);

  const renderTicket = useCallback(({ item }: { item: Ticket }) => (
    <TouchableOpacity
      style={styles.ticketCard}
      onPress={() => router.push(`/tickets/${item.id}`)}
      activeOpacity={0.7}
    >
      <View style={styles.ticketHeader}>
        <View style={[styles.priorityBadge, { backgroundColor: priorityColors[item.priority] }]}>
          <Text style={styles.priorityText}>{item.priority}</Text>
        </View>
        <Text style={styles.ticketStatus}>{statusLabels[item.status]}</Text>
      </View>
      <Text style={styles.ticketTitle} numberOfLines={2}>{item.title}</Text>
      <Text style={styles.customerName}>{item.customerName}</Text>
      <Text style={styles.ticketDate}>{formatDate(item.createdAt)}</Text>
    </TouchableOpacity>
  ), []);

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <View style={styles.header}>
        <Text style={styles.title}>Tickets</Text>
      </View>

      <FlatList
        horizontal
        data={FILTERS}
        keyExtractor={(item) => item.value}
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.filters}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={[styles.filterChip, activeFilter === item.value && styles.filterChipActive]}
            onPress={() => setActiveFilter(item.value)}
          >
            <Text style={[styles.filterText, activeFilter === item.value && styles.filterTextActive]}>
              {item.label}
            </Text>
          </TouchableOpacity>
        )}
      />

      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={renderTicket}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  header: {
    paddingHorizontal: 20,
    paddingTop: 60,
    paddingBottom: 12,
    backgroundColor: Colors.white,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: Colors.text,
  },
  filters: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    backgroundColor: Colors.white,
    gap: 8,
  },
  filterChip: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: Colors.background,
    marginRight: 8,
  },
  filterChipActive: {
    backgroundColor: Colors.primary,
  },
  filterText: {
    fontSize: 13,
    color: Colors.textSecondary,
    fontWeight: '500',
  },
  filterTextActive: {
    color: Colors.white,
  },
  list: {
    padding: 16,
    paddingBottom: 100,
  },
  ticketCard: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 16,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOpacity: 0.04,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  ticketHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  priorityBadge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 4,
  },
  priorityText: {
    color: Colors.white,
    fontSize: 11,
    fontWeight: '600',
    textTransform: 'uppercase',
  },
  ticketStatus: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
  ticketTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
    marginBottom: 4,
  },
  customerName: {
    fontSize: 13,
    color: Colors.textSecondary,
    marginBottom: 4,
  },
  ticketDate: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
});
