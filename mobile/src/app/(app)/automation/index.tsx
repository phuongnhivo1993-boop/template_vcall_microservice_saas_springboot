import { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl, Alert } from 'react-native';
import { Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import ScreenHeader from '../../../components/ScreenHeader';
import { SkeletonList } from '../../../components/SkeletonLoader';
import ErrorState from '../../../components/ErrorState';
import EmptyState from '../../../components/EmptyState';
import { automationApi } from '../../../lib/api';
import type { AutomationRule } from '../../../types';

const statusColors: Record<string, string> = {
  active: Colors.success,
  inactive: Colors.textSecondary,
};

const statusLabels: Record<string, string> = {
  active: 'Đang chạy',
  inactive: 'Tắt',
};

const triggerIcons: Record<string, keyof typeof Ionicons.glyphMap> = {
  ticket_created: 'ticket-outline',
  ticket_updated: 'create-outline',
  call_ended: 'call-outline',
  chat_received: 'chatbubble-outline',
  customer_created: 'person-add-outline',
  scheduled: 'alarm-outline',
};

function formatDate(dateStr?: string): string {
  if (!dateStr) return 'Chưa chạy';
  const d = new Date(dateStr);
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

export default function AutomationScreen() {
  const [rules, setRules] = useState<AutomationRule[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const fetchRules = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const res = await automationApi.getAll();
      setRules(res.data?.data?.content || res.data?.data || res.data || []);
    } catch (err: any) {
      setError(err?.message || 'Không thể tải quy tắc tự động hóa');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchRules(); }, [fetchRules]);

  const renderItem = useCallback(({ item }: { item: AutomationRule }) => (
    <View style={styles.ruleCard}>
      <View style={styles.ruleHeader}>
        <View style={[styles.ruleIcon, { backgroundColor: (statusColors[item.status] || Colors.textSecondary) + '15' }]}>
          <Ionicons
            name={triggerIcons[item.trigger] || 'flash-outline'}
            size={20}
            color={statusColors[item.status] || Colors.textSecondary}
          />
        </View>
        <View style={styles.ruleInfo}>
          <Text style={styles.ruleName}>{item.name}</Text>
          <Text style={styles.ruleTrigger}>Khi: {item.trigger}</Text>
        </View>
        <View style={[styles.statusBadge, { backgroundColor: (statusColors[item.status] || Colors.textSecondary) + '20' }]}>
          <Text style={[styles.statusText, { color: statusColors[item.status] || Colors.textSecondary }]}>
            {statusLabels[item.status] || item.status}
          </Text>
        </View>
      </View>
      <Text style={styles.ruleAction}>Hành động: {item.action}</Text>
      <Text style={styles.ruleMeta}>Lần chạy cuối: {formatDate(item.lastRun)}</Text>
    </View>
  ), []);

  if (loading && !refreshing) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Tự động hóa" />
        <SkeletonList count={5} />
      </View>
    );
  }

  if (error && rules.length === 0) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Tự động hóa" />
        <ErrorState message={error} onRetry={() => fetchRules()} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <ScreenHeader title="Tự động hóa" />

      <FlatList
        data={rules}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[styles.list, rules.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchRules(true)} colors={[Colors.primary]} />
        }
        ListEmptyComponent={() => (
          <EmptyState
            icon="flash-outline"
            title="Chưa có quy tắc tự động hóa"
            subtitle="Tạo quy tắc để tự động hóa quy trình làm việc"
            actionLabel="Tạo quy tắc"
            onAction={() => Alert.alert('Thông tin', 'Tính năng này khả dụng trên ứng dụng web')}
          />
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  list: { padding: 16, paddingBottom: 100 },
  listEmpty: { flexGrow: 1 },
  ruleCard: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 16,
    marginBottom: 10,
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  ruleHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginBottom: 8,
  },
  ruleIcon: {
    width: 40,
    height: 40,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  ruleInfo: { flex: 1 },
  ruleName: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
  },
  ruleTrigger: {
    fontSize: 12,
    color: Colors.textSecondary,
    marginTop: 1,
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 8,
  },
  statusText: {
    fontSize: 11,
    fontWeight: '600',
  },
  ruleAction: {
    fontSize: 13,
    color: Colors.textSecondary,
    marginBottom: 4,
  },
  ruleMeta: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
});
