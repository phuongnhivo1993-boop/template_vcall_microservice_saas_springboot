import { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl } from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import ScreenHeader from '../../../components/ScreenHeader';
import SearchBar from '../../../components/SearchBar';
import { SkeletonList } from '../../../components/SkeletonLoader';
import ErrorState from '../../../components/ErrorState';
import EmptyState from '../../../components/EmptyState';
import { campaignsApi } from '../../../lib/api';
import type { Campaign } from '../../../types';

const statusColors: Record<string, string> = {
  draft: Colors.textSecondary,
  active: Colors.success,
  paused: Colors.warning,
  completed: Colors.primary,
};

const statusLabels: Record<string, string> = {
  draft: 'Bản nháp',
  active: 'Đang chạy',
  paused: 'Tạm dừng',
  completed: 'Hoàn thành',
};

const typeIcons: Record<string, keyof typeof Ionicons.glyphMap> = {
  email: 'mail-outline',
  sms: 'chatbubble-outline',
  call: 'call-outline',
  social: 'globe-outline',
};

const typeLabels: Record<string, string> = {
  email: 'Email',
  sms: 'SMS',
  call: 'Cuộc gọi',
  social: 'Mạng xã hội',
};

export default function CampaignsScreen() {
  const router = useRouter();
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [showSearch, setShowSearch] = useState(false);

  const fetchCampaigns = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const res = await campaignsApi.getAll();
      setCampaigns(res.data?.data?.content || res.data?.data || res.data || []);
    } catch (err: any) {
      setError(err?.message || 'Không thể tải chiến dịch');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => { fetchCampaigns(); }, [fetchCampaigns]);

  const filtered = search
    ? campaigns.filter((c) => c.name.toLowerCase().includes(search.toLowerCase()))
    : campaigns;

  const renderItem = useCallback(({ item }: { item: Campaign }) => (
    <View style={styles.campaignCard}>
      <View style={styles.cardHeader}>
        <View style={[styles.typeBadge, { backgroundColor: Colors.primary + '15' }]}>
          <Ionicons name={typeIcons[item.type] || 'megaphone-outline'} size={18} color={Colors.primary} />
        </View>
        <View style={[styles.statusBadge, { backgroundColor: (statusColors[item.status] || Colors.textSecondary) + '20' }]}>
          <Text style={[styles.statusText, { color: statusColors[item.status] || Colors.textSecondary }]}>
            {statusLabels[item.status] || item.status}
          </Text>
        </View>
      </View>
      <Text style={styles.campaignName}>{item.name}</Text>
      <Text style={styles.campaignDesc} numberOfLines={2}>{item.description}</Text>
      <View style={styles.statsRow}>
        <View style={styles.stat}>
          <Text style={styles.statValue}>{item.sentCount}</Text>
          <Text style={styles.statLabel}>Đã gửi</Text>
        </View>
        <View style={styles.stat}>
          <Text style={styles.statValue}>{item.openRate}%</Text>
          <Text style={styles.statLabel}>Mở</Text>
        </View>
        <View style={styles.stat}>
          <Text style={styles.statValue}>{item.clickRate}%</Text>
          <Text style={styles.statLabel}>Nhấp</Text>
        </View>
      </View>
    </View>
  ), []);

  if (loading && !refreshing) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Chiến dịch" showSearch={showSearch} onSearchToggle={() => setShowSearch(!showSearch)} />
        <SkeletonList count={5} />
      </View>
    );
  }

  if (error && campaigns.length === 0) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Chiến dịch" />
        <ErrorState message={error} onRetry={() => fetchCampaigns()} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <ScreenHeader title="Chiến dịch" showSearch={showSearch} onSearchToggle={() => setShowSearch(!showSearch)} />

      {showSearch && <SearchBar value={search} onChangeText={setSearch} placeholder="Tìm kiếm chiến dịch..." />}

      <FlatList
        data={filtered}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[styles.list, filtered.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchCampaigns(true)} colors={[Colors.primary]} />
        }
        ListEmptyComponent={() => (
          search ? (
            <EmptyState icon="search-outline" title="Không tìm thấy chiến dịch" subtitle="Thử tìm kiếm với từ khóa khác" />
          ) : (
            <EmptyState icon="megaphone-outline" title="Chưa có chiến dịch" subtitle="Tạo chiến dịch mới để bắt đầu" actionLabel="Tạo chiến dịch" onAction={() => {}} />
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
  campaignCard: {
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
    alignItems: 'center',
    marginBottom: 10,
  },
  typeBadge: {
    width: 36,
    height: 36,
    borderRadius: 18,
    justifyContent: 'center',
    alignItems: 'center',
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    fontSize: 11,
    fontWeight: '600',
  },
  campaignName: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.text,
    marginBottom: 4,
  },
  campaignDesc: {
    fontSize: 13,
    color: Colors.textSecondary,
    lineHeight: 18,
    marginBottom: 12,
  },
  statsRow: {
    flexDirection: 'row',
    gap: 16,
  },
  stat: {
    alignItems: 'center',
  },
  statValue: {
    fontSize: 16,
    fontWeight: '700',
    color: Colors.text,
  },
  statLabel: {
    fontSize: 11,
    color: Colors.textSecondary,
    marginTop: 1,
  },
});
