import { useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import CallCard from '../../../components/CallCard';
import type { Call } from '../../../types';

const MOCK_CALLS: Call[] = [
  {
    id: '1', callerId: 'c1', callerName: 'John Doe', callerNumber: '+1 555-0101',
    calleeName: 'Agent Smith', calleeNumber: '+1 555-1001', direction: 'incoming',
    status: 'ended', duration: 245, startedAt: new Date(Date.now() - 3600000).toISOString(),
  },
  {
    id: '2', callerId: 'c2', callerName: 'Jane Roe', callerNumber: '+1 555-0102',
    calleeName: 'Agent Smith', calleeNumber: '+1 555-1001', direction: 'outgoing',
    status: 'ended', duration: 120, startedAt: new Date(Date.now() - 7200000).toISOString(),
  },
  {
    id: '3', callerId: 'c3', callerName: 'Bob Wilson', callerNumber: '+1 555-0103',
    calleeName: 'Agent Smith', calleeNumber: '+1 555-1001', direction: 'incoming',
    status: 'missed', duration: 0, startedAt: new Date(Date.now() - 10800000).toISOString(),
  },
  {
    id: '4', callerId: 'c4', callerName: 'Alice Brown', callerNumber: '+1 555-0104',
    calleeName: 'Agent Smith', calleeNumber: '+1 555-1001', direction: 'incoming',
    status: 'ended', duration: 480, startedAt: new Date(Date.now() - 14400000).toISOString(),
  },
  {
    id: '5', callerId: 'c5', callerName: 'Charlie Davis', callerNumber: '+1 555-0105',
    calleeName: 'Agent Smith', calleeNumber: '+1 555-1001', direction: 'outgoing',
    status: 'voicemail', duration: 45, startedAt: new Date(Date.now() - 18000000).toISOString(),
  },
];

export default function CallsScreen() {
  const router = useRouter();

  const renderItem = useCallback(({ item }: { item: Call }) => (
    <CallCard call={item} onPress={() => router.push(`/calls/${item.id}`)} />
  ), []);

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <View style={styles.header}>
        <Text style={styles.title}>Call History</Text>
        <TouchableOpacity style={styles.dialBtn} onPress={() => router.push('/calls/dialer')}>
          <Ionicons name="dialpad" size={22} color={Colors.white} />
        </TouchableOpacity>
      </View>

      <FlatList
        data={MOCK_CALLS}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
      />

      <TouchableOpacity
        style={styles.fab}
        onPress={() => router.push('/calls/dialer')}
      >
        <Ionicons name="call-outline" size={24} color={Colors.white} />
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 60,
    paddingBottom: 16,
    backgroundColor: Colors.white,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: Colors.text,
  },
  dialBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
  },
  list: {
    paddingTop: 8,
    paddingBottom: 100,
  },
  fab: {
    position: 'absolute',
    bottom: 24,
    right: 20,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 8,
  },
});
