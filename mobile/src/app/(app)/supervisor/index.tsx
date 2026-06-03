import { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl } from 'react-native';
import { Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import ScreenHeader from '../../../components/ScreenHeader';
import SearchBar from '../../../components/SearchBar';
import { SkeletonList } from '../../../components/SkeletonLoader';
import ErrorState from '../../../components/ErrorState';
import EmptyState from '../../../components/EmptyState';
import StatusBadge from '../../../components/StatusBadge';
import { supervisorApi } from '../../../lib/api';
import type { SupervisorAgent } from '../../../types';

export default function SupervisorScreen() {
  const [agents, setAgents] = useState<SupervisorAgent[]>([]);
  const [queueStats, setQueueStats] = useState<{ waiting: number; avgWait: number; longestWait: number } | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [showSearch, setShowSearch] = useState(false);

  const fetchData = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const [agentsRes, queueRes] = await Promise.all([
        supervisorApi.getAgents(),
        supervisorApi.getQueueStats(),
      ]);
      setAgents(agentsRes.data?.data?.content || agentsRes.data?.data || agentsRes.data || []);
      setQueueStats(queueRes.data?.data || queueRes.data || null);
    } catch (err: any) {
      setError(err?.message || 'Không thể tải dữ liệu giám sát');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const filtered = search
    ? agents.filter((a) => a.name.toLowerCase().includes(search.toLowerCase()))
    : agents;

  const renderItem = useCallback(({ item }: { item: SupervisorAgent }) => {
    const callColor = item.callsActive > 0 ? Colors.success : Colors.textSecondary;
    return (
      <View style={styles.agentCard}>
        <View style={styles.agentLeft}>
          <View style={styles.avatar}>
            <Ionicons name="person-circle" size={44} color={Colors.primary} />
            <View style={[styles.onlineDot, { backgroundColor: item.online ? Colors.online : Colors.offline }]} />
          </View>
          <View style={styles.agentInfo}>
            <Text style={styles.agentName}>{item.name}</Text>
            <Text style={styles.agentExt}>Máy nhánh: {item.extension}</Text>
            <StatusBadge status={item.status} />
          </View>
        </View>
        <View style={styles.agentStats}>
          <View style={styles.agentStat}>
            <Ionicons name="call-outline" size={14} color={callColor} />
            <Text style={[styles.agentStatValue, { color: callColor }]}>{item.callsActive}</Text>
          </View>
          <View style={styles.agentStat}>
            <Ionicons name="call-outline" size={14} color={Colors.textSecondary} />
            <Text style={styles.agentStatValue}>{item.callsToday}</Text>
          </View>
          <View style={styles.agentStat}>
            <Ionicons name="timer-outline" size={14} color={Colors.textSecondary} />
            <Text style={styles.agentStatValue}>{Math.floor(item.avgDuration / 60)}m</Text>
          </View>
        </View>
      </View>
    );
  }, []);

  if (loading && !refreshing) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Giám sát" showSearch={showSearch} onSearchToggle={() => setShowSearch(!showSearch)} />
        <SkeletonList count={6} />
      </View>
    );
  }

  if (error && agents.length === 0) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Giám sát" />
        <ErrorState message={error} onRetry={() => fetchData()} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <ScreenHeader title="Giám sát" showSearch={showSearch} onSearchToggle={() => setShowSearch(!showSearch)} />

      {showSearch && <SearchBar value={search} onChangeText={setSearch} placeholder="Tìm kiếm nhân viên..." />}

      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[styles.list, filtered.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchData(true)} colors={[Colors.primary]} />
        }
        ListHeaderComponent={() => queueStats ? (
          <View style={styles.queueBanner}>
            <View style={styles.queueItem}>
              <Text style={styles.queueValue}>{queueStats.waiting}</Text>
              <Text style={styles.queueLabel}>Đang chờ</Text>
            </View>
            <View style={styles.queueDivider} />
            <View style={styles.queueItem}>
              <Text style={styles.queueValue}>{queueStats.avgWait}s</Text>
              <Text style={styles.queueLabel}>TB chờ</Text>
            </View>
            <View style={styles.queueDivider} />
            <View style={styles.queueItem}>
              <Text style={styles.queueValue}>{queueStats.longestWait}s</Text>
              <Text style={styles.queueLabel}>Chờ lâu nhất</Text>
            </View>
          </View>
        ) : null}
        ListEmptyComponent={() => (
          search ? (
            <EmptyState icon="search-outline" title="Không tìm thấy nhân viên" subtitle="Thử tìm kiếm với từ khóa khác" />
          ) : (
            <EmptyState icon="people-outline" title="Không có nhân viên" subtitle="Danh sách nhân viên sẽ hiển thị ở đây" />
          )
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  list: { padding: 16, paddingBottom: 100 },
  listEmpty: { flexGrow: 1 },
  queueBanner: {
    flexDirection: 'row',
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    justifyContent: 'space-around',
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  queueItem: { alignItems: 'center' },
  queueValue: { fontSize: 20, fontWeight: '700', color: Colors.text },
  queueLabel: { fontSize: 11, color: Colors.textSecondary, marginTop: 2 },
  queueDivider: { width: 1, backgroundColor: Colors.border },
  agentCard: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 14,
    marginBottom: 8,
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  agentLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginBottom: 10,
  },
  avatar: {
    position: 'relative',
  },
  onlineDot: {
    position: 'absolute',
    bottom: 2,
    right: 2,
    width: 12,
    height: 12,
    borderRadius: 6,
    borderWidth: 2,
    borderColor: Colors.white,
  },
  agentInfo: { flex: 1 },
  agentName: { fontSize: 15, fontWeight: '600', color: Colors.text },
  agentExt: { fontSize: 12, color: Colors.textSecondary },
  agentStats: {
    flexDirection: 'row',
    gap: 24,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: Colors.border,
  },
  agentStat: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  agentStatValue: {
    fontSize: 13,
    fontWeight: '600',
    color: Colors.textSecondary,
  },
});
