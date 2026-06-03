import { useState, useCallback, useEffect } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, RefreshControl,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import { ticketsApi } from '../../../lib/api';
import type { Ticket, TicketStatus, TicketPriority } from '../../../types';
import SearchBar from '../../../components/SearchBar';
import EmptyView from '../../../components/EmptyView';

const FILTERS: { label: string; value: TicketStatus | 'all' }[] = [
  { label: 'Tất cả', value: 'all' },
  { label: 'Mở', value: 'open' },
  { label: 'Đang xử lý', value: 'in_progress' },
  { label: 'Đã giải quyết', value: 'resolved' },
  { label: 'Đã đóng', value: 'closed' },
];

const priorityColors: Record<TicketPriority, string> = {
  low: Colors.success,
  medium: Colors.warning,
  high: Colors.error,
  critical: '#ff0000',
};

const statusLabels: Record<TicketStatus, string> = {
  open: 'Mở',
  in_progress: 'Đang xử lý',
  resolved: 'Đã giải quyết',
  closed: 'Đã đóng',
};

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

export default function TicketsScreen() {
  const router = useRouter();
  const [activeFilter, setActiveFilter] = useState<TicketStatus | 'all'>('all');
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [showSearch, setShowSearch] = useState(false);

  const fetchTickets = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const params: Record<string, any> = {};
      if (activeFilter !== 'all') params.status = activeFilter;
      const res = await ticketsApi.getAll(params);
      setTickets(res.data?.data?.content || res.data?.data || res.data || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load tickets');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [activeFilter]);

  useEffect(() => { fetchTickets(); }, [fetchTickets]);

  const filteredByStatus = activeFilter === 'all'
    ? tickets
    : tickets.filter((t) => t.status === activeFilter);

  const filtered = search
    ? filteredByStatus.filter(
        (t) =>
          t.title.toLowerCase().includes(search.toLowerCase()) ||
          t.customerName.toLowerCase().includes(search.toLowerCase()),
      )
    : filteredByStatus;

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
        <Text style={styles.title}>Phiếu yêu cầu</Text>
        <TouchableOpacity style={styles.searchBtn} onPress={() => setShowSearch(!showSearch)} hitSlop={8}>
          <Ionicons name={showSearch ? 'close-outline' : 'search-outline'} size={22} color={Colors.text} />
        </TouchableOpacity>
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

      {showSearch && <SearchBar value={search} onChangeText={setSearch} placeholder="Tìm kiếm phiếu yêu cầu..." />}

      {loading ? (
        <ActivityIndicator size="large" color={Colors.primary} style={{ marginTop: 40 }} />
      ) : (
      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={renderTicket}
        contentContainerStyle={[styles.list, filtered.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={() => {
          if (error) return (
            <View style={styles.centerContainer}>
              <Ionicons name="bug-outline" size={48} color={Colors.textSecondary} />
              <Text style={styles.emptyText}>{error}</Text>
        <TouchableOpacity style={styles.retryBtn} onPress={() => fetchTickets()}>
          <Text style={styles.retryText}>Thử lại</Text>
        </TouchableOpacity>
            </View>
          );
          return (
            <EmptyView
              icon="ticket-outline"
        title={search ? 'Không tìm thấy phiếu yêu cầu' : 'Chưa có phiếu yêu cầu'}
        subtitle={search ? 'Thử tìm kiếm với từ khóa khác' : 'Tạo phiếu yêu cầu mới để bắt đầu'}
            />
          );
        }}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchTickets(true)} colors={[Colors.primary]} />
        }
      />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 60,
    paddingBottom: 12,
    backgroundColor: Colors.white,
  },
  searchBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: Colors.background,
    justifyContent: 'center',
    alignItems: 'center',
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
  centerContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingTop: 60,
    paddingHorizontal: 32,
  },
  emptyText: {
    fontSize: 16,
    color: Colors.textSecondary,
    marginTop: 12,
    textAlign: 'center',
  },
  emptySubtext: {
    fontSize: 13,
    color: Colors.textSecondary,
    marginTop: 4,
    textAlign: 'center',
    opacity: 0.7,
  },
  retryBtn: {
    marginTop: 16,
    paddingHorizontal: 24,
    paddingVertical: 10,
    backgroundColor: Colors.primary,
    borderRadius: 8,
  },
  retryText: {
    color: Colors.white,
    fontWeight: '600',
  },
  listEmpty: {
    flexGrow: 1,
  },
});
