import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../constants/colors';
import type { Call } from '../types';

interface Props {
  call: Call;
  onPress?: () => void;
}

function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, '0')}`;
}

function formatTime(dateStr: string): string {
  const d = new Date(dateStr);
  const h = d.getHours().toString().padStart(2, '0');
  const m = d.getMinutes().toString().padStart(2, '0');
  return `${h}:${m}`;
}

export default function CallCard({ call, onPress }: Props) {
  const directionIcon = call.direction === 'incoming' ? 'call-received' : 'call-made';
  const directionColor = call.direction === 'incoming' ? Colors.incomingCall : Colors.outgoingCall;

  return (
    <TouchableOpacity style={styles.container} onPress={onPress} activeOpacity={0.7}>
      <View style={styles.left}>
        <Ionicons name="person-circle-outline" size={40} color={Colors.textSecondary} />
      </View>
      <View style={styles.center}>
        <Text style={styles.name}>{call.callerName || call.callerNumber}</Text>
        <View style={styles.meta}>
          <Ionicons name={directionIcon} size={14} color={directionColor} />
          <Text style={styles.number}>{call.calleeName || call.calleeNumber}</Text>
          <Text style={styles.time}>{formatTime(call.startedAt)}</Text>
        </View>
      </View>
      <View style={styles.right}>
        {call.status === 'missed' ? (
          <Text style={styles.missed}>Missed</Text>
        ) : (
          <Text style={styles.duration}>{formatDuration(call.duration)}</Text>
        )}
        {call.status === 'missed' && <Ionicons name="close-circle" size={16} color={Colors.error} />}
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.card,
    padding: 12,
    borderRadius: 12,
    marginHorizontal: 16,
    marginVertical: 4,
    shadowColor: '#000',
    shadowOpacity: 0.05,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  left: {
    marginRight: 12,
  },
  center: {
    flex: 1,
  },
  name: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
    marginBottom: 2,
  },
  meta: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  number: {
    fontSize: 13,
    color: Colors.textSecondary,
  },
  time: {
    fontSize: 12,
    color: Colors.textSecondary,
    marginLeft: 8,
  },
  right: {
    alignItems: 'flex-end',
    gap: 2,
  },
  duration: {
    fontSize: 13,
    color: Colors.textSecondary,
  },
  missed: {
    fontSize: 12,
    color: Colors.error,
    fontWeight: '500',
  },
});
