import { useState, useEffect } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet, ScrollView, Switch, ActivityIndicator,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { useSelector, useDispatch } from 'react-redux';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import StatusBadge from '../../../components/StatusBadge';
import { logoutUser } from '../../../store/slices/authSlice';
import { agentsApi } from '../../../lib/api';
import type { RootState, AppDispatch } from '../../../store';
import type { AgentStatus } from '../../../types';

export default function ProfileScreen() {
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();
  const user = useSelector((state: RootState) => state.auth.user);
  const [status, setStatus] = useState<AgentStatus>('online');
  const [isOnline, setIsOnline] = useState(true);
  const [stats, setStats] = useState<{ totalAgents?: number; onlineCount?: number; offlineCount?: number; busyCount?: number }>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    agentsApi.getStats()
      .then((res) => setStats(res.data?.data || {}))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleToggle = (value: boolean) => {
    setIsOnline(value);
    setStatus(value ? 'online' : 'offline');
    if (user?.agentId) {
      agentsApi.updateStatus(user.agentId, value ? 'ONLINE' : 'OFFLINE').catch(() => {});
    }
  };

  const handleLogout = async () => {
    await dispatch(logoutUser());
    router.replace('/(auth)/login');
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Stack.Screen options={{ headerShown: false }} />

      <View style={styles.profileHeader}>
        <View style={styles.avatar}>
          <Ionicons name="person-circle" size={80} color={Colors.primary} />
          <View style={[styles.statusDot, { backgroundColor: isOnline ? Colors.online : Colors.offline }]} />
        </View>
        <Text style={styles.name}>{user?.fullName || user?.name || 'Agent'}</Text>
        <Text style={styles.email}>{user?.email || ''}</Text>
      </View>

      <View style={styles.section}>
        <View style={styles.statusRow}>
          <View>
            <Text style={styles.sectionLabel}>Trạng thái</Text>
            <StatusBadge status={status} size="medium" />
          </View>
          <Switch
            value={isOnline}
            onValueChange={handleToggle}
            trackColor={{ false: Colors.border, true: Colors.primary + '60' }}
            thumbColor={isOnline ? Colors.primary : Colors.textSecondary}
          />
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Thống kê hôm nay</Text>
        {loading ? (
          <ActivityIndicator size="small" color={Colors.primary} />
        ) : (
        <View style={styles.statsGrid}>
          <View style={styles.statCard}>
            <Ionicons name="people-outline" size={24} color={Colors.primary} />
            <Text style={styles.statValue}>{stats.totalAgents ?? '-'}</Text>
            <Text style={styles.statLabel}>Tổng NV</Text>
          </View>
          <View style={styles.statCard}>
            <Ionicons name="checkmark-circle-outline" size={24} color={Colors.success} />
            <Text style={styles.statValue}>{stats.onlineCount ?? '-'}</Text>
            <Text style={styles.statLabel}>Trực tuyến</Text>
          </View>
          <View style={styles.statCard}>
            <Ionicons name="close-circle-outline" size={24} color={Colors.error} />
            <Text style={styles.statValue}>{stats.offlineCount ?? '-'}</Text>
            <Text style={styles.statLabel}>Ngoại tuyến</Text>
          </View>
          <View style={styles.statCard}>
            <Ionicons name="timer-outline" size={24} color={Colors.warning} />
            <Text style={styles.statValue}>{stats.busyCount ?? '-'}</Text>
            <Text style={styles.statLabel}>Bận</Text>
          </View>
        </View>
        )}
      </View>

      <View style={styles.section}>
        <TouchableOpacity style={styles.menuItem}>
          <Ionicons name="settings-outline" size={20} color={Colors.text} />
          <Text style={styles.menuText}>Cài đặt</Text>
          <Ionicons name="chevron-forward" size={18} color={Colors.textSecondary} />
        </TouchableOpacity>
        <TouchableOpacity style={styles.menuItem}>
          <Ionicons name="notifications-outline" size={20} color={Colors.text} />
          <Text style={styles.menuText}>Thông báo</Text>
          <Ionicons name="chevron-forward" size={18} color={Colors.textSecondary} />
        </TouchableOpacity>
        <TouchableOpacity style={styles.menuItem}>
          <Ionicons name="help-circle-outline" size={20} color={Colors.text} />
          <Text style={styles.menuText}>Trợ giúp & Hỗ trợ</Text>
          <Ionicons name="chevron-forward" size={18} color={Colors.textSecondary} />
        </TouchableOpacity>
      </View>

      <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout}>
        <Ionicons name="log-out-outline" size={20} color={Colors.error} />
        <Text style={styles.logoutText}>Đăng xuất</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  content: {
    paddingBottom: 40,
  },
  profileHeader: {
    alignItems: 'center',
    paddingTop: 80,
    paddingBottom: 24,
    backgroundColor: Colors.white,
  },
  avatar: {
    position: 'relative',
    marginBottom: 12,
  },
  statusDot: {
    position: 'absolute',
    bottom: 4,
    right: 4,
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 3,
    borderColor: Colors.white,
  },
  name: {
    fontSize: 20,
    fontWeight: '700',
    color: Colors.text,
  },
  email: {
    fontSize: 14,
    color: Colors.textSecondary,
    marginTop: 2,
  },
  extensionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    marginTop: 6,
  },
  extension: {
    fontSize: 14,
    color: Colors.textSecondary,
  },
  section: {
    backgroundColor: Colors.white,
    marginTop: 12,
    paddingHorizontal: 16,
    paddingVertical: 16,
    borderRadius: 12,
    marginHorizontal: 16,
  },
  statusRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  sectionLabel: {
    fontSize: 14,
    color: Colors.textSecondary,
    marginBottom: 4,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.text,
    marginBottom: 12,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  statCard: {
    width: '48%',
    backgroundColor: Colors.background,
    borderRadius: 10,
    padding: 14,
    alignItems: 'center',
    gap: 4,
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
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: Colors.border,
    gap: 12,
  },
  menuText: {
    flex: 1,
    fontSize: 15,
    color: Colors.text,
  },
  logoutBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginHorizontal: 16,
    marginTop: 24,
    paddingVertical: 14,
    backgroundColor: Colors.white,
    borderRadius: 12,
  },
  logoutText: {
    fontSize: 15,
    color: Colors.error,
    fontWeight: '600',
  },
});
