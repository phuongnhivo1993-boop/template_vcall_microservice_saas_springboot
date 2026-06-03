import { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl } from 'react-native';
import { Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import ScreenHeader from '../../../components/ScreenHeader';
import { SkeletonList } from '../../../components/SkeletonLoader';
import ErrorState from '../../../components/ErrorState';
import EmptyState from '../../../components/EmptyState';
import { billingApi } from '../../../lib/api';
import type { Invoice } from '../../../types';

const statusColors: Record<string, string> = {
  paid: Colors.success,
  pending: Colors.warning,
  overdue: Colors.error,
  cancelled: Colors.textSecondary,
};

const statusLabels: Record<string, string> = {
  paid: 'Đã thanh toán',
  pending: 'Chờ thanh toán',
  overdue: 'Quá hạn',
  cancelled: 'Đã hủy',
};

function formatCurrency(amount: number, currency: string): string {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: currency || 'VND' }).format(amount);
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export default function BillingScreen() {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const fetchInvoices = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const res = await billingApi.getInvoices();
      setInvoices(res.data?.data?.content || res.data?.data || res.data || []);
    } catch (err: any) {
      setError(err?.message || 'Không thể tải hóa đơn');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchInvoices(); }, [fetchInvoices]);

  const totalPending = invoices
    .filter((inv) => inv.status === 'pending' || inv.status === 'overdue')
    .reduce((sum, inv) => sum + inv.amount, 0);

  const renderItem = useCallback(({ item }: { item: Invoice }) => (
    <View style={styles.invoiceCard}>
      <View style={styles.invoiceHeader}>
        <View style={styles.invoiceLeft}>
          <Ionicons
            name={item.status === 'paid' ? 'checkmark-circle' : 'time-outline'}
            size={20}
            color={statusColors[item.status]}
          />
          <Text style={styles.invoiceDesc} numberOfLines={1}>{item.description}</Text>
        </View>
        <View style={[styles.statusBadge, { backgroundColor: statusColors[item.status] + '20' }]}>
          <Text style={[styles.statusText, { color: statusColors[item.status] }]}>
            {statusLabels[item.status]}
          </Text>
        </View>
      </View>
      <Text style={styles.amount}>{formatCurrency(item.amount, item.currency)}</Text>
      <View style={styles.invoiceMeta}>
        <Text style={styles.metaText}>Ngày phát hành: {formatDate(item.issuedAt)}</Text>
        <Text style={styles.metaText}>Hạn thanh toán: {formatDate(item.dueDate)}</Text>
      </View>
    </View>
  ), []);

  if (loading && !refreshing) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Hóa đơn" />
        <SkeletonList count={5} />
      </View>
    );
  }

  if (error && invoices.length === 0) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Hóa đơn" />
        <ErrorState message={error} onRetry={() => fetchInvoices()} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <ScreenHeader title="Hóa đơn" />

      {totalPending > 0 && (
        <View style={styles.summaryBanner}>
          <Ionicons name="wallet-outline" size={20} color={Colors.white} />
          <Text style={styles.summaryText}>
            Tổng tiền chưa thanh toán: {formatCurrency(totalPending, 'VND')}
          </Text>
        </View>
      )}

      <FlatList
        data={invoices}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[styles.list, invoices.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchInvoices(true)} colors={[Colors.primary]} />
        }
        ListEmptyComponent={() => (
          <EmptyState
            icon="receipt-outline"
            title="Không có hóa đơn"
            subtitle="Các hóa đơn sẽ hiển thị ở đây"
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
  summaryBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    backgroundColor: Colors.error,
    paddingHorizontal: 16,
    paddingVertical: 12,
    marginHorizontal: 16,
    borderRadius: 10,
    marginBottom: 8,
  },
  summaryText: {
    color: Colors.white,
    fontSize: 14,
    fontWeight: '600',
  },
  invoiceCard: {
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
  invoiceHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  invoiceLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    flex: 1,
  },
  invoiceDesc: {
    fontSize: 14,
    color: Colors.text,
    fontWeight: '500',
    flex: 1,
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
  amount: {
    fontSize: 20,
    fontWeight: '700',
    color: Colors.text,
    marginBottom: 8,
  },
  invoiceMeta: {
    gap: 2,
  },
  metaText: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
});
