import { useCallback, useState, useEffect } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import CallCard from '../../../components/CallCard';
import SearchBar from '../../../components/SearchBar';
import EmptyView from '../../../components/EmptyView';
import { callsApi } from '../../../lib/api';
import type { Call } from '../../../types';

export default function CallsScreen() {
  const router = useRouter();
  const [calls, setCalls] = useState<Call[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const PAGE_SIZE = 20;

  const fetchCalls = async (pageNum: number, isRefresh = false) => {
    try {
      if (isRefresh) setRefreshing(true);
      else if (pageNum === 0) setLoading(true);
      setError(null);
      const params: Record<string, any> = { page: pageNum, size: PAGE_SIZE };
      if (search) params.q = search;
      const res = await callsApi.getHistory(params);
      const newCalls = res.data?.data?.content || res.data?.data || res.data || [];
      if (pageNum === 0) {
        setCalls(newCalls);
      } else {
        setCalls(prev => [...prev, ...newCalls]);
      }
      setHasMore(newCalls.length === PAGE_SIZE);
      setPage(pageNum);
    } catch (err: any) {
      setError(err?.message || 'Failed to load calls');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => { fetchCalls(0); }, [search]);

  const handleLoadMore = () => {
    if (!loading && hasMore) fetchCalls(page + 1);
  };

  const handleRefresh = () => fetchCalls(0, true);

  const filtered = search && calls.length > 0
    ? calls.filter(
        (c) =>
          (c.callerName || c.callerNumber || '').toLowerCase().includes(search.toLowerCase()) ||
          (c.calleeName || c.calleeNumber || '').toLowerCase().includes(search.toLowerCase()),
      )
    : calls;

  const renderItem = useCallback(({ item }: { item: Call }) => (
    <CallCard call={item} onPress={() => router.push(`/calls/${item.id}`)} />
  ), []);

  const renderEmpty = () => {
    if (loading) return null;
    if (error) return (
      <View style={styles.centerContainer}>
        <Ionicons name="cloud-offline-outline" size={48} color={Colors.textSecondary} />
        <Text style={styles.emptyText}>{error}</Text>
        <TouchableOpacity style={styles.retryBtn} onPress={() => fetchCalls(0)}>
          <Text style={styles.retryText}>Retry</Text>
        </TouchableOpacity>
      </View>
    );
    return (
      <EmptyView
        icon="call-outline"
        title={search ? 'No calls match your search' : 'No call history'}
        subtitle={search ? 'Try a different search term' : 'Make your first call to get started'}
      />
    );
  };

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <View style={styles.header}>
        <Text style={styles.title}>Call History</Text>
        <TouchableOpacity style={styles.dialBtn} onPress={() => router.push('/calls/dialer')}>
          <Ionicons name="dialpad" size={22} color={Colors.white} />
        </TouchableOpacity>
      </View>

      <SearchBar value={search} onChangeText={setSearch} placeholder="Search calls..." />

      {loading && calls.length === 0 ? (
        <ActivityIndicator size="large" color={Colors.primary} style={{ marginTop: 40 }} />
      ) : (
      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[styles.list, filtered.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={renderEmpty}
        onEndReached={handleLoadMore}
        onEndReachedThreshold={0.5}
        refreshing={refreshing}
        onRefresh={handleRefresh}
        ListFooterComponent={loading && calls.length > 0 ? <ActivityIndicator style={{ padding: 16 }} /> : null}
      />
      )}

      <TouchableOpacity
        style={styles.fab}
        onPress={() => router.push('/calls/dialer')}
      >
        <Ionicons name="call-outline" size={24} color={Colors.white} />
      </TouchableOpacity>
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
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: Colors.text,
  },
  dialBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
  },
  list: {
    paddingTop: 8,
    paddingBottom: 100,
  },
  fab: {
    position: 'absolute',
    bottom: 24,
    right: 20,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 8,
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
