import { useState } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet, ScrollView, Switch,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { useSelector, useDispatch } from 'react-redux';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import StatusBadge from '../../../components/StatusBadge';
import { clearAuth } from '../../../store/slices/authSlice';
import type { RootState, AppDispatch } from '../../../store';
import type { AgentStatus } from '../../../types';

const MOCK_AGENT = {
  name: 'Agent Smith',
  email: 'agent.smith@vcall.com',
  extension: '101',
  avatar: undefined,
  callsToday: 24,
  avgDuration: 245,
};

export default function ProfileScreen() {
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();
  const user = useSelector((state: RootState) => state.auth.user);
  const [status, setStatus] = useState<AgentStatus>('online');
  const [isOnline, setIsOnline] = useState(true);

  const handleToggle = (value: boolean) => {
    setIsOnline(value);
    setStatus(value ? 'online' : 'offline');
  };

  const handleLogout = () => {
    dispatch(clearAuth());
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
        <Text style={styles.name}>{user?.name || MOCK_AGENT.name}</Text>
        <Text style={styles.email}>{user?.email || MOCK_AGENT.email}</Text>
        <View style={styles.extensionRow}>
          <Ionicons name="call-outline" size={16} color={Colors.textSecondary} />
          <Text style={styles.extension}>Ext. {MOCK_AGENT.extension}</Text>
        </View>
      </View>

      <View style={styles.section}>
        <View style={styles.statusRow}>
          <View>
            <Text style={styles.sectionLabel}>Status</Text>
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
        <Text style={styles.sectionTitle}>Today's Stats</Text>
        <View style={styles.statsGrid}>
          <View style={styles.statCard}>
            <Ionicons name="call-outline" size={24} color={Colors.primary} />
            <Text style={styles.statValue}>{MOCK_AGENT.callsToday}</Text>
            <Text style={styles.statLabel}>Calls Today</Text>
          </View>
          <View style={styles.statCard}>
            <Ionicons name="timer-outline" size={24} color={Colors.success} />
            <Text style={styles.statValue}>{Math.floor(MOCK_AGENT.avgDuration / 60)}m</Text>
            <Text style={styles.statLabel}>Avg Duration</Text>
          </View>
          <View style={styles.statCard}>
            <Ionicons name="chatbubbles-outline" size={24} color={Colors.warning} />
            <Text style={styles.statValue}>12</Text>
            <Text style={styles.statLabel}>Chats</Text>
          </View>
          <View style={styles.statCard}>
            <Ionicons name="ticket-outline" size={24} color={Colors.error} />
            <Text style={styles.statValue}>5</Text>
            <Text style={styles.statLabel}>Tickets</Text>
          </View>
        </View>
      </View>

      <View style={styles.section}>
        <TouchableOpacity style={styles.menuItem}>
          <Ionicons name="settings-outline" size={20} color={Colors.text} />
          <Text style={styles.menuText}>Settings</Text>
          <Ionicons name="chevron-forward" size={18} color={Colors.textSecondary} />
        </TouchableOpacity>
        <TouchableOpacity style={styles.menuItem}>
          <Ionicons name="notifications-outline" size={20} color={Colors.text} />
          <Text style={styles.menuText}>Notifications</Text>
          <Ionicons name="chevron-forward" size={18} color={Colors.textSecondary} />
        </TouchableOpacity>
        <TouchableOpacity style={styles.menuItem}>
          <Ionicons name="help-circle-outline" size={20} color={Colors.text} />
          <Text style={styles.menuText}>Help & Support</Text>
          <Ionicons name="chevron-forward" size={18} color={Colors.textSecondary} />
        </TouchableOpacity>
      </View>

      <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout}>
        <Ionicons name="log-out-outline" size={20} color={Colors.error} />
        <Text style={styles.logoutText}>Sign Out</Text>
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
