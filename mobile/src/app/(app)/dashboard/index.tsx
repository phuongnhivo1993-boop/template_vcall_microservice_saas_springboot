import { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl, ScrollView } from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import ScreenHeader from '../../../components/ScreenHeader';
import { SkeletonList } from '../../../components/SkeletonLoader';
import ErrorState from '../../../components/ErrorState';
import EmptyState from '../../../components/EmptyState';
import { dashboardApi } from '../../../lib/api';
import type { DashboardStats, Activity } from '../../../types';

export default function DashboardScreen() {
  const router = useRouter();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const fetchData = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const [statsRes, actRes] = await Promise.all([
        dashboardApi.getStats(),
        dashboardApi.getActivities(),
      ]);
      setStats(statsRes.data?.data || statsRes.data || null);
      setActivities(actRes.data?.data?.content || actRes.data?.data || actRes.data || []);
    } catch (err: any) {
      setError(err?.message || 'Không thể tải dữ liệu');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchData(); }, [fetchData]);

  const statCards = stats ? [
    { label: 'Cuộc gọi', value: stats.totalCalls, icon: 'call-outline' as const, color: Colors.primary },
    { label: 'Phiếu yêu cầu', value: stats.totalTickets, icon: 'ticket-outline' as const, color: Colors.warning },
    { label: 'Trò chuyện', value: stats.totalConversations, icon: 'chatbubbles-outline' as const, color: Colors.success },
    { label: 'Khách hàng', value: stats.totalCustomers, icon: 'people-outline' as const, color: '#722ed1' },
  ] : [];

  const quickActions = [
    { label: 'Cuộc gọi', icon: 'call-outline' as const, route: '/calls/dialer' as const, color: Colors.success },
    { label: 'Phiếu mới', icon: 'ticket-outline' as const, route: '/tickets' as const, color: Colors.warning },
    { label: 'Nhắn tin', icon: 'chatbubbles-outline' as const, route: '/chat' as const, color: Colors.primary },
    { label: 'Thêm KH', icon: 'person-add-outline' as const, route: '/crm' as const, color: '#722ed1' },
  ];

  const formatTime = (dateStr: string) => {
    const d = new Date(dateStr);
    const h = d.getHours().toString().padStart(2, '0');
    const m = d.getMinutes().toString().padStart(2, '0');
    return `${h}:${m}`;
  };

  if (loading && !refreshing) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Tổng quan" subtitle="Chào mừng trở lại" />
        <SkeletonList count={6} />
      </View>
    );
  }

  if (error && !stats) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Tổng quan" />
        <ErrorState message={error} onRetry={() => fetchData()} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <ScreenHeader title="Tổng quan" subtitle="Chào mừng trở lại" />

      <FlatList
        data={activities}
        keyExtractor={(item) => item.id}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchData(true)} colors={[Colors.primary]} />
        }
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.listContent}
        ListHeaderComponent={() => (
          <View>
            <View style={styles.statsGrid}>
              {statCards.map((card, i) => (
                <View key={i} style={styles.statCard}>
                  <View style={[styles.statIcon, { backgroundColor: card.color + '15' }]}>
                    <Ionicons name={card.icon} size={22} color={card.color} />
                  </View>
                  <Text style={styles.statValue}>{card.value}</Text>
                  <Text style={styles.statLabel}>{card.label}</Text>
                </View>
              ))}
            </View>

            <View style={styles.quickActions}>
              <Text style={styles.sectionTitle}>Thao tác nhanh</Text>
              <View style={styles.quickGrid}>
                {quickActions.map((action, i) => (
                  <TouchableOpacity
                    key={i}
                    style={styles.quickBtn}
                    onPress={() => router.push(action.route)}
                    activeOpacity={0.7}
                  >
                    <View style={[styles.quickIcon, { backgroundColor: action.color + '15' }]}>
                      <Ionicons name={action.icon} size={20} color={action.color} />
                    </View>
                    <Text style={styles.quickLabel}>{action.label}</Text>
                  </TouchableOpacity>
                ))}
              </View>
            </View>

            {stats && (
              <View style={styles.extraStats}>
                <View style={styles.extraStatItem}>
                  <Ionicons name="close-circle-outline" size={16} color={Colors.error} />
                  <Text style={styles.extraStatText}>Cuộc gọi nhỡ: {stats.missedCalls}</Text>
                </View>
                <View style={styles.extraStatItem}>
                  <Ionicons name="timer-outline" size={16} color={Colors.warning} />
                  <Text style={styles.extraStatText}>TB chờ: {stats.avgWaitTime}s</Text>
                </View>
                <View style={styles.extraStatItem}>
                  <Ionicons name="happy-outline" size={16} color={Colors.success} />
                  <Text style={styles.extraStatText}>Hài lòng: {stats.satisfaction}%</Text>
                </View>
              </View>
            )}

            <View style={styles.activityHeader}>
              <Text style={styles.sectionTitle}>Hoạt động gần đây</Text>
            </View>
          </View>
        )}
        ListEmptyComponent={() => (
          <EmptyState
            icon="timer-outline"
            title="Chưa có hoạt động nào"
            subtitle="Các hoạt động gần đây sẽ hiển thị ở đây"
          />
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  listContent: { paddingBottom: 24 },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    padding: 12,
    gap: 8,
  },
  statCard: {
    width: '48%',
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    gap: 4,
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  statIcon: {
    width: 44,
    height: 44,
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 4,
  },
  statValue: {
    fontSize: 22,
    fontWeight: '700',
    color: Colors.text,
  },
  statLabel: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
  quickActions: {
    paddingHorizontal: 16,
    marginTop: 8,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.text,
    marginBottom: 12,
  },
  quickGrid: {
    flexDirection: 'row',
    gap: 8,
  },
  quickBtn: {
    flex: 1,
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 14,
    alignItems: 'center',
    gap: 8,
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  quickIcon: {
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  quickLabel: {
    fontSize: 12,
    fontWeight: '500',
    color: Colors.text,
  },
  extraStats: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    gap: 12,
    marginTop: 12,
    marginBottom: 8,
  },
  extraStatItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: Colors.card,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
  },
  extraStatText: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
  activityHeader: {
    paddingHorizontal: 16,
    paddingTop: 12,
  },
});
