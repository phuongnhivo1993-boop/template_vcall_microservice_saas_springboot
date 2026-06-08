import { useCallback, useState, useEffect } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, RefreshControl,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import { chatApi } from '../../../lib/api';
import type { Conversation } from '../../../types';
import SearchBar from '../../../components/SearchBar';
import EmptyState from '../../../components/EmptyState';

function formatRelativeTime(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'now';
  if (mins < 60) return `${mins}m`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h`;
  const days = Math.floor(hrs / 24);
  return `${days}d`;
}

export default function ChatListScreen() {
  const router = useRouter();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [showSearch, setShowSearch] = useState(false);

  const fetchConversations = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const res = await chatApi.getConversations();
      setConversations(res.data?.data?.content || res.data?.data || res.data || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load conversations');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchConversations(); }, [fetchConversations]);

  const filtered = conversations.filter((c) =>
    c.customerName.toLowerCase().includes(search.toLowerCase()),
  );

  const renderItem = useCallback(({ item }: { item: Conversation }) => (
    <TouchableOpacity
      style={styles.conversationItem}
      onPress={() => router.push(`/chat/${item.id}`)}
      activeOpacity={0.7}
    >
      <View style={styles.avatar}>
        <Ionicons name="person-circle" size={48} color={Colors.textSecondary} />
        {item.unreadCount > 0 && (
          <View style={styles.unreadBadge}>
            <Text style={styles.unreadText}>{item.unreadCount}</Text>
          </View>
        )}
      </View>
      <View style={styles.conversationInfo}>
        <View style={styles.topRow}>
          <Text style={styles.customerName}>{item.customerName}</Text>
          {item.lastMessageAt && (
            <Text style={styles.timestamp}>{formatRelativeTime(item.lastMessageAt)}</Text>
          )}
        </View>
        <Text style={styles.lastMessage} numberOfLines={1}>
          {item.lastMessage}
        </Text>
      </View>
    </TouchableOpacity>
  ), []);

  const renderEmpty = () => {
    if (loading) return null;
    if (error) return (
      <View style={styles.centerContainer}>
        <Ionicons name="chatbox-ellipses-outline" size={48} color={Colors.textSecondary} />
        <Text style={styles.emptyText}>{error}</Text>
        <TouchableOpacity style={styles.retryBtn} onPress={() => fetchConversations()}>
          <Text style={styles.retryText}>Thử lại</Text>
        </TouchableOpacity>
      </View>
    );
    return (
      <EmptyState
        icon="chatbubbles-outline"
        title={search ? 'Không tìm thấy cuộc trò chuyện' : 'Chưa có cuộc trò chuyện'}
        subtitle={search ? 'Thử tìm kiếm với từ khóa khác' : 'Bắt đầu một cuộc trò chuyện mới'}
      />
    );
  };

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <View style={styles.header}>
        <Text style={styles.title}>Trò chuyện</Text>
        <TouchableOpacity style={styles.searchBtn} onPress={() => setShowSearch(!showSearch)} hitSlop={8}>
          <Ionicons name={showSearch ? 'close-outline' : 'search-outline'} size={22} color={Colors.text} />
        </TouchableOpacity>
      </View>

      {showSearch && <SearchBar value={search} onChangeText={setSearch} placeholder="Tìm kiếm cuộc trò chuyện..." />}

      {loading ? (
        <ActivityIndicator size="large" color={Colors.primary} style={{ marginTop: 40 }} />
      ) : (
      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[styles.list, filtered.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={renderEmpty}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchConversations(true)} colors={[Colors.primary]} />
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
    paddingBottom: 16,
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
  list: {
    paddingTop: 8,
    paddingBottom: 24,
  },
  conversationItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.card,
    padding: 14,
    marginHorizontal: 16,
    marginVertical: 4,
    borderRadius: 12,
    gap: 12,
  },
  avatar: {
    position: 'relative',
  },
  unreadBadge: {
    position: 'absolute',
    top: -2,
    right: -4,
    backgroundColor: Colors.error,
    borderRadius: 10,
    minWidth: 18,
    height: 18,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 4,
  },
  unreadText: {
    color: Colors.white,
    fontSize: 11,
    fontWeight: '700',
  },
  conversationInfo: {
    flex: 1,
  },
  topRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  customerName: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
  },
  timestamp: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
  lastMessage: {
    fontSize: 13,
    color: Colors.textSecondary,
    marginTop: 2,
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
