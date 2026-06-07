import { useState, useCallback, useEffect } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, RefreshControl, Alert } from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import ScreenHeader from '../../../components/ScreenHeader';
import SearchBar from '../../../components/SearchBar';
import { SkeletonList } from '../../../components/SkeletonLoader';
import ErrorState from '../../../components/ErrorState';
import EmptyState from '../../../components/EmptyState';
import { crmApi } from '../../../lib/api';
import type { Customer } from '../../../types';

export default function CRMScreen() {
  const router = useRouter();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [showSearch, setShowSearch] = useState(false);

  const fetchCustomers = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    setError(null);
    try {
      const params: Record<string, any> = {};
      if (search) params.search = search;
      const res = await crmApi.getAll(params);
      setCustomers(res.data?.data?.content || res.data?.data || res.data || []);
    } catch (err: any) {
      setError(err?.message || 'Không thể tải danh sách khách hàng');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [search]);

  useEffect(() => { fetchCustomers(); }, [fetchCustomers]);

  const statusColor = (status: string) => {
    switch (status) {
      case 'active': return Colors.success;
      case 'inactive': return Colors.textSecondary;
      case 'lead': return Colors.warning;
      default: return Colors.textSecondary;
    }
  };

  const statusLabel = (status: string) => {
    switch (status) {
      case 'active': return 'Hoạt động';
      case 'inactive': return 'Không hoạt động';
      case 'lead': return 'Tiềm năng';
      default: return status;
    }
  };

  const renderItem = useCallback(({ item }: { item: Customer }) => (
    <TouchableOpacity style={styles.customerCard} activeOpacity={0.7}>
      <View style={styles.avatar}>
        <Ionicons name="person-circle" size={44} color={Colors.primary} />
      </View>
      <View style={styles.info}>
        <Text style={styles.name}>{item.name}</Text>
        <Text style={styles.email}>{item.email}</Text>
        {item.phone && <Text style={styles.phone}>{item.phone}</Text>}
      </View>
      <View style={styles.right}>
        <View style={[styles.statusDot, { backgroundColor: statusColor(item.status) }]} />
        <Text style={styles.statusText}>{statusLabel(item.status)}</Text>
        <Text style={styles.ticketCount}>{item.totalTickets} vé</Text>
      </View>
    </TouchableOpacity>
  ), []);

  if (loading && !refreshing) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader
          title="Quản lý khách hàng"
          showSearch={showSearch}
          onSearchToggle={() => setShowSearch(!showSearch)}
        />
        <SkeletonList count={8} />
      </View>
    );
  }

  if (error && customers.length === 0) {
    return (
      <View style={styles.container}>
        <Stack.Screen options={{ headerShown: false }} />
        <ScreenHeader title="Quản lý khách hàng" />
        <ErrorState message={error} onRetry={() => fetchCustomers()} />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <ScreenHeader
        title="Quản lý khách hàng"
        showSearch={showSearch}
        onSearchToggle={() => setShowSearch(!showSearch)}
      />

      {showSearch && <SearchBar value={search} onChangeText={setSearch} placeholder="Tìm kiếm khách hàng..." />}

      <FlatList
        data={customers}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={[styles.list, customers.length === 0 && styles.listEmpty]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => fetchCustomers(true)} colors={[Colors.primary]} />
        }
        ListEmptyComponent={() => (
          search ? (
            <EmptyState
              icon="search-outline"
              title="Không tìm thấy khách hàng"
              subtitle="Thử tìm kiếm với từ khóa khác"
            />
          ) : (
            <EmptyState
              icon="people-outline"
              title="Chưa có khách hàng"
              subtitle="Thêm khách hàng mới để bắt đầu"
              actionLabel="Thêm khách hàng"
              onAction={() => Alert.alert('Thông tin', 'Tính năng này khả dụng trên ứng dụng web')}
            />
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
  customerCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.card,
    padding: 14,
    borderRadius: 12,
    marginBottom: 8,
    gap: 12,
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  avatar: {
    position: 'relative',
  },
  info: { flex: 1 },
  name: { fontSize: 15, fontWeight: '600', color: Colors.text },
  email: { fontSize: 13, color: Colors.textSecondary, marginTop: 1 },
  phone: { fontSize: 12, color: Colors.textSecondary, marginTop: 1 },
  right: { alignItems: 'flex-end', gap: 2 },
  statusDot: { width: 8, height: 8, borderRadius: 4 },
  statusText: { fontSize: 11, color: Colors.textSecondary },
  ticketCount: { fontSize: 11, color: Colors.textSecondary },
});
