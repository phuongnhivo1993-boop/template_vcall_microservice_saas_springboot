import { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl } from 'react-native';
import { Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import ScreenHeader from '../../../components/ScreenHeader';
import { SkeletonList } from '../../../components/SkeletonLoader';
import ErrorState from '../../../components/ErrorState';
import EmptyState from '../../../components/EmptyState';
import { webhooksApi } from '../../../lib/api';
import type { Webhook } from '../../../types';

const statusColors: Record<string, string> = {
  active: Colors.success,
  inactive: Colors.textSecondary,
  failed: Colors.error,
};

const statusLabels: Record<string, string> = {
  active: 'Hoạt động',
  inactive: 'Tắt',
  failed: 'Lỗi',
};

function formatDate(dateStr?: string): string {
  if (!dateStr) return 'Chưa kích hoạt';
  const d = new Date(dateStr);
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

export default function WebhooksScreen() {
  const [webhooks, setWebhooks] = useState<Webhook[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const fetchWebhooks = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const res = await webhooksApi.getAll();
      setWebhooks(res.data?.data?.content || res.data?.data || res.data || []);
    } catch (err: any) {
      setError(err?.message || 'Không thể tải webhook');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchWebhooks(); }, [fetchWebhooks]);

  const renderItem = useCallback(({ item }: { item: Webhook }) => (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <View style={styles.cardLeft}>
          <View style={[styles.statusDot, { backgroundColor: statusColors[item.status] }]} />
          <View>
            <Text style={styles.webhookName}>{item.name}</Text>
            <Text style={styles.webhookUrl} numberOfLines={1}>{item.url}</Text>
          </View>
        </View>
        <View style={[styles.statusBadge, { backgroundColor: statusColors[item.status] + '20' }]}>
          <Text style={[styles.statusText, { color: statusColors[item.status] }]}>
            {statusLabels[item.status]}
          </Text>
        </View>
      </View>
      {item.events.length > 0 && (
        <View style={styles.eventsRow}>
          {item.events.slice(0, 3).map((evt, i) => (
            <View key={i} style={styles.eventChip}>
              <Text style={styles.eventText}>{evt}</Text>
            </View>
          ))}
          {item.events.length > 3 && (
            <Text style={styles.moreEvents}>+{item.events.length - 3}</Text>
          )}
        </View>
      )}
      <Text style={styles.lastTriggered}>Kích hoạt lần cuối: {formatDate(item.lastTriggered)}</Text>
    </View>
  ), []);

  if (loading && !refreshing) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Webhook" />
        <SkeletonList count={5} />
      </View>
    );
  }

  if (error && webhooks.length === 0) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Webhook" />
        <ErrorState message={error} onRetry={() => fetchWebhooks()} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <ScreenHeader title="Webhook" />

      <FlatList
        data={webhooks}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[styles.list, webhooks.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchWebhooks(true)} colors={[Colors.primary]} />
        }
        ListEmptyComponent={() => (
          <EmptyState
            icon="git-merge-outline"
            title="Chưa có webhook"
            subtitle="Thêm webhook để tích hợp với hệ thống khác"
            actionLabel="Thêm webhook"
            onAction={() => {}}
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
  card: {
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
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 10,
  },
  cardLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    flex: 1,
  },
  statusDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
  },
  webhookName: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
  },
  webhookUrl: {
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
  eventsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
    marginBottom: 8,
  },
  eventChip: {
    backgroundColor: Colors.background,
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
  },
  eventText: {
    fontSize: 11,
    color: Colors.textSecondary,
  },
  moreEvents: {
    fontSize: 11,
    color: Colors.textSecondary,
    alignSelf: 'center',
  },
  lastTriggered: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
});
