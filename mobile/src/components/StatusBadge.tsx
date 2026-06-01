import { View, Text, StyleSheet } from 'react-native';
import { Colors } from '../constants/colors';
import type { AgentStatus } from '../types';

interface Props {
  status: AgentStatus;
  size?: 'small' | 'medium';
}

const statusColors: Record<AgentStatus, string> = {
  online: Colors.online,
  offline: Colors.offline,
  away: Colors.away,
  busy: Colors.busy,
};

const statusLabels: Record<AgentStatus, string> = {
  online: 'Online',
  offline: 'Offline',
  away: 'Away',
  busy: 'Busy',
};

export default function StatusBadge({ status, size = 'small' }: Props) {
  const dotSize = size === 'medium' ? 12 : 8;
  return (
    <View style={styles.container}>
      <View style={[styles.dot, { width: dotSize, height: dotSize, backgroundColor: statusColors[status] }]} />
      <Text style={[styles.label, size === 'medium' && styles.labelMedium]}>{statusLabels[status]}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  dot: {
    borderRadius: 999,
  },
  label: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
  labelMedium: {
    fontSize: 14,
  },
});
