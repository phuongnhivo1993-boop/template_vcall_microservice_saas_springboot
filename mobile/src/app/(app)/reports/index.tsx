import { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl, Dimensions } from 'react-native';
import { Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import ScreenHeader from '../../../components/ScreenHeader';
import { SkeletonList } from '../../../components/SkeletonLoader';
import ErrorState from '../../../components/ErrorState';
import EmptyState from '../../../components/EmptyState';
import { reportsApi } from '../../../lib/api';
import type { Report } from '../../../types';

const formatIcons: Record<string, keyof typeof Ionicons.glyphMap> = {
  pdf: 'document-outline',
  csv: 'grid-outline',
  excel: 'grid-outline',
};

const typeLabels: Record<string, string> = {
  call_volume: 'Lưu lượng cuộc gọi',
  ticket_summary: 'Tổng quan phiếu yêu cầu',
  agent_performance: 'Hiệu suất nhân viên',
  customer_satisfaction: 'Hài lòng khách hàng',
  billing: 'Báo cáo hóa đơn',
};

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

const CHART_PLACEHOLDER_HEIGHT = 160;

function ChartPlaceholder({ label, value, color }: { label: string; value: number; color: string }) {
  const width = Dimensions.get('window').width - 64;
  const barWidth = Math.max(4, (value / 100) * width);

  return (
    <View style={styles.chartRow}>
      <Text style={styles.chartLabel}>{label}</Text>
      <View style={[styles.chartBar, { width: barWidth, backgroundColor: color + '30' }]}>
        <View style={[styles.chartFill, { width: `${value}%`, backgroundColor: color }]} />
      </View>
      <Text style={styles.chartValue}>{value}%</Text>
    </View>
  );
}

export default function ReportsScreen() {
  const [reports, setReports] = useState<Report[]>([]);
  const [analytics, setAnalytics] = useState<any>({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const fetchReports = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const [reportsRes, analyticsRes] = await Promise.allSettled([
        reportsApi.getAll(),
        reportsApi.getAnalytics({ period: 'MONTHLY' }),
      ]);
      if (reportsRes.status === 'fulfilled') {
        const res = reportsRes.value;
        setReports(res.data?.data?.content || res.data?.data || res.data || []);
      }
      if (analyticsRes.status === 'fulfilled') {
        const res = analyticsRes.value;
        setAnalytics(res.data?.data || res.data || {});
      }
    } catch (err: any) {
      setError(err?.message || 'Không thể tải báo cáo');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchReports(); }, [fetchReports]);

  const renderItem = useCallback(({ item }: { item: Report }) => (
    <TouchableOpacity style={styles.reportCard} activeOpacity={0.7}>
      <View style={styles.reportRow}>
        <View style={[styles.reportIcon, { backgroundColor: Colors.primary + '15' }]}>
          <Ionicons name={formatIcons[item.format] || 'document-outline'} size={20} color={Colors.primary} />
        </View>
        <View style={styles.reportInfo}>
          <Text style={styles.reportName}>{item.name}</Text>
          <Text style={styles.reportMeta}>
            {typeLabels[item.type] || item.type} • {formatDate(item.createdAt)}
          </Text>
        </View>
        <Ionicons name="download-outline" size={20} color={Colors.textSecondary} />
      </View>
    </TouchableOpacity>
  ), []);

  if (loading && !refreshing) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Báo cáo" />
        <SkeletonList count={5} />
      </View>
    );
  }

  if (error && reports.length === 0) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Báo cáo" />
        <ErrorState message={error} onRetry={() => fetchReports()} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <ScreenHeader title="Báo cáo" />

      <FlatList
        data={reports}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={[styles.list, reports.length === 0 && styles.listEmpty]}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchReports(true)} colors={[Colors.primary]} />
        }
        ListHeaderComponent={() => (
          <View style={styles.chartSection}>
            <Text style={styles.sectionTitle}>Hiệu suất tháng này</Text>
            <View style={styles.chartCard}>
              <ChartPlaceholder label="Cuộc gọi" value={analytics.callVolume ?? 75} color={Colors.primary} />
              <ChartPlaceholder label="Phiếu yêu cầu" value={analytics.tickets ?? 60} color={Colors.warning} />
              <ChartPlaceholder label="Hài lòng KH" value={analytics.csat ?? 88} color={Colors.success} />
              <ChartPlaceholder label="Phản hồi nhanh" value={analytics.responseRate ?? 92} color="#722ed1" />
            </View>
          </View>
        )}
        ListEmptyComponent={() => (
          <EmptyState
            icon="bar-chart-outline"
            title="Không có báo cáo"
            subtitle="Các báo cáo sẽ hiển thị ở đây"
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
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.text,
    marginBottom: 12,
  },
  chartSection: {
    marginBottom: 16,
  },
  chartCard: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    padding: 16,
    gap: 12,
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  chartRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  chartLabel: {
    fontSize: 12,
    color: Colors.textSecondary,
    width: 80,
  },
  chartBar: {
    height: 20,
    borderRadius: 10,
    overflow: 'hidden',
    flex: 1,
  },
  chartFill: {
    height: '100%',
    borderRadius: 10,
  },
  chartValue: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.text,
    width: 36,
    textAlign: 'right',
  },
  reportCard: {
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
  reportRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  reportIcon: {
    width: 40,
    height: 40,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  reportInfo: {
    flex: 1,
  },
  reportName: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
  },
  reportMeta: {
    fontSize: 12,
    color: Colors.textSecondary,
    marginTop: 1,
  },
});
