import { useState, useEffect } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet, ActivityIndicator, ScrollView,
} from 'react-native';
import { useLocalSearchParams, Stack, useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import { callsApi } from '../../../lib/api';
import type { Call } from '../../../types';

function formatDuration(seconds: number): string {
  if (!seconds) return '00:00';
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

function formatDateTime(dateStr: string): string {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

const statusColors: Record<string, string> = {
  connected: Colors.success,
  ringing: Colors.warning,
  ended: Colors.textSecondary,
  missed: Colors.error,
  voicemail: Colors.primary,
};

const statusLabels: Record<string, string> = {
  connected: 'Đã kết nối',
  ringing: 'Đang đổ chuông',
  ended: 'Đã kết thúc',
  missed: 'Nhỡ cuộc gọi',
  voicemail: 'Thư thoại',
};

export default function CallDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const [call, setCall] = useState<Call | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);
    callsApi.getCall(id)
      .then((res) => setCall(res.data?.data || res.data))
      .catch((err) => setError(err?.message || 'Failed to load call details'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={Colors.primary} />
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.center}>
        <Ionicons name="alert-circle-outline" size={48} color={Colors.error} />
        <Text style={[styles.centerText, { color: Colors.error, marginTop: 12 }]}>{error}</Text>
        <TouchableOpacity style={styles.retryBtn} onPress={() => {
          setLoading(true); setError(null);
          callsApi.getCall(id).then((res) => setCall(res.data?.data || res.data))
            .catch((e) => setError(e?.message || 'Failed'))
            .finally(() => setLoading(false));
        }}>
          <Text style={styles.retryText}>Thử lại</Text>
        </TouchableOpacity>
      </View>
    );
  }

  if (!call) {
    return (
      <View style={styles.center}>
        <Ionicons name="call-outline" size={48} color={Colors.textSecondary} />
        <Text style={styles.centerText}>Không tìm thấy cuộc gọi</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen
        options={{
          headerShown: true,
          headerTitle: 'Chi tiết cuộc gọi',
          headerBackTitle: 'Quay lại',
          headerStyle: { backgroundColor: Colors.white },
          headerTintColor: Colors.text,
        }}
      />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.statusSection}>
          <View style={[styles.statusBadge, { backgroundColor: (statusColors[call.status] || Colors.textSecondary) + '20' }]}>
            <Ionicons
              name={call.status === 'missed' ? 'call-missed' : call.direction === 'incoming' ? 'call-incoming' : 'call-outgoing'}
              size={40}
              color={statusColors[call.status] || Colors.textSecondary}
            />
          </View>
          <Text style={[styles.statusLabel, { color: statusColors[call.status] || Colors.textSecondary }]}>
            {statusLabels[call.status] || call.status}
          </Text>
          {call.duration > 0 && (
            <Text style={styles.duration}>{formatDuration(call.duration)}</Text>
          )}
        </View>

        <View style={styles.detailCard}>
          <View style={styles.detailRow}>
            <Ionicons name="person-outline" size={18} color={Colors.textSecondary} />
            <View style={styles.detailContent}>
              <Text style={styles.detailLabel}>{call.direction === 'incoming' ? 'Người gọi' : 'Người nhận'}</Text>
              <Text style={styles.detailValue}>{call.callerName || call.callerNumber || 'Unknown'}</Text>
            </View>
          </View>

          <View style={styles.divider} />

          <View style={styles.detailRow}>
            <Ionicons name="call-outline" size={18} color={Colors.textSecondary} />
            <View style={styles.detailContent}>
              <Text style={styles.detailLabel}>Số điện thoại</Text>
              <Text style={styles.detailValue}>{call.callerNumber || call.calleeNumber || 'N/A'}</Text>
            </View>
          </View>

          <View style={styles.divider} />

          <View style={styles.detailRow}>
            <Ionicons name="arrow-forward-outline" size={18} color={Colors.textSecondary} />
            <View style={styles.detailContent}>
              <Text style={styles.detailLabel}>{call.direction === 'incoming' ? 'Đến số' : 'Từ số'}</Text>
              <Text style={styles.detailValue}>{call.calleeNumber || call.callerNumber || 'N/A'}</Text>
            </View>
          </View>

          <View style={styles.divider} />

          <View style={styles.detailRow}>
            <Ionicons name="calendar-outline" size={18} color={Colors.textSecondary} />
            <View style={styles.detailContent}>
              <Text style={styles.detailLabel}>Thời gian</Text>
              <Text style={styles.detailValue}>{formatDateTime(call.startedAt)}</Text>
            </View>
          </View>

          {call.endedAt && (
            <>
              <View style={styles.divider} />
              <View style={styles.detailRow}>
                <Ionicons name="time-outline" size={18} color={Colors.textSecondary} />
                <View style={styles.detailContent}>
                  <Text style={styles.detailLabel}>Kết thúc</Text>
                  <Text style={styles.detailValue}>{formatDateTime(call.endedAt)}</Text>
                </View>
              </View>
            </>
          )}
        </View>

        {call.recording && (
          <TouchableOpacity style={styles.recordingBtn}>
            <Ionicons name="musical-notes-outline" size={20} color={Colors.white} />
            <Text style={styles.recordingBtnText}>Nghe ghi âm</Text>
          </TouchableOpacity>
        )}

        <View style={styles.actionRow}>
          <TouchableOpacity style={styles.actionBtn} onPress={() => router.push('/calls/dialer')}>
            <Ionicons name="call-outline" size={22} color={Colors.primary} />
            <Text style={styles.actionLabel}>Gọi lại</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.actionBtn}>
            <Ionicons name="chatbubble-ellipses-outline" size={22} color={Colors.primary} />
            <Text style={styles.actionLabel}>Nhắn tin</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  scrollContent: { padding: 16 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: Colors.background, padding: 24 },
  centerText: { fontSize: 15, color: Colors.textSecondary, marginTop: 8, textAlign: 'center' },
  retryBtn: { marginTop: 16, paddingHorizontal: 24, paddingVertical: 10, borderRadius: 8, backgroundColor: Colors.primary },
  retryText: { color: Colors.white, fontSize: 14, fontWeight: '600' },

  statusSection: { alignItems: 'center', paddingVertical: 24 },
  statusBadge: { width: 80, height: 80, borderRadius: 40, justifyContent: 'center', alignItems: 'center', marginBottom: 12 },
  statusLabel: { fontSize: 18, fontWeight: '700', marginBottom: 4 },
  duration: { fontSize: 32, fontWeight: '300', color: Colors.text, marginTop: 4 },

  detailCard: { backgroundColor: Colors.card, borderRadius: 12, padding: 16, marginBottom: 16 },
  detailRow: { flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 8 },
  detailContent: { flex: 1 },
  detailLabel: { fontSize: 12, color: Colors.textSecondary, marginBottom: 2 },
  detailValue: { fontSize: 15, color: Colors.text, fontWeight: '500' },
  divider: { height: 1, backgroundColor: Colors.border, marginLeft: 30 },

  recordingBtn: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    backgroundColor: Colors.primary, borderRadius: 12, padding: 14, gap: 8, marginBottom: 16,
  },
  recordingBtnText: { color: Colors.white, fontSize: 15, fontWeight: '600' },

  actionRow: { flexDirection: 'row', gap: 12, marginTop: 8 },
  actionBtn: {
    flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
    backgroundColor: Colors.card, borderRadius: 12, padding: 14, gap: 8,
    borderWidth: 1, borderColor: Colors.border,
  },
  actionLabel: { fontSize: 14, color: Colors.primary, fontWeight: '500' },
});
