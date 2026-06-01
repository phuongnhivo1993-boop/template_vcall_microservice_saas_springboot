import { useCallback } from 'react';
import {
  View, Text, FlatList, TouchableOpacity, StyleSheet,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import type { Conversation } from '../../../types';

const MOCK_CONVERSATIONS: Conversation[] = [
  { id: '1', customerId: 'c1', customerName: 'John Doe', lastMessage: 'Thanks for your help!', lastMessageAt: new Date(Date.now() - 600000).toISOString(), unreadCount: 0, status: 'active' },
  { id: '2', customerId: 'c2', customerName: 'Jane Roe', lastMessage: 'I still have an issue with my account', lastMessageAt: new Date(Date.now() - 1800000).toISOString(), unreadCount: 2, status: 'active' },
  { id: '3', customerId: 'c3', customerName: 'Bob Wilson', lastMessage: 'When will the technician arrive?', lastMessageAt: new Date(Date.now() - 3600000).toISOString(), unreadCount: 1, status: 'active' },
  { id: '4', customerId: 'c4', customerName: 'Alice Brown', lastMessage: 'Perfect, thank you!', lastMessageAt: new Date(Date.now() - 7200000).toISOString(), unreadCount: 0, status: 'resolved' },
  { id: '5', customerId: 'c5', customerName: 'Charlie Davis', lastMessage: 'I need a refund', lastMessageAt: new Date(Date.now() - 14400000).toISOString(), unreadCount: 0, status: 'pending' },
];

function formatRelativeTime(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'now';
  if (mins < 60) return `${mins}m`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h`;
  const days = Math.floor(hrs / 24);
  return `${days}d`;
}

export default function ChatListScreen() {
  const router = useRouter();

  const renderItem = useCallback(({ item }: { item: Conversation }) => (
    <TouchableOpacity
      style={styles.conversationItem}
      onPress={() => router.push(`/chat/${item.id}`)}
      activeOpacity={0.7}
    >
      <View style={styles.avatar}>
        <Ionicons name="person-circle" size={48} color={Colors.textSecondary} />
        {item.unreadCount > 0 && (
          <View style={styles.unreadBadge}>
            <Text style={styles.unreadText}>{item.unreadCount}</Text>
          </View>
        )}
      </View>
      <View style={styles.conversationInfo}>
        <View style={styles.topRow}>
          <Text style={styles.customerName}>{item.customerName}</Text>
          {item.lastMessageAt && (
            <Text style={styles.timestamp}>{formatRelativeTime(item.lastMessageAt)}</Text>
          )}
        </View>
        <Text style={styles.lastMessage} numberOfLines={1}>
          {item.lastMessage}
        </Text>
      </View>
    </TouchableOpacity>
  ), []);

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <View style={styles.header}>
        <Text style={styles.title}>Chat</Text>
      </View>

      <FlatList
        data={MOCK_CONVERSATIONS}
        keyExtractor={(item) => item.id}
        renderItem={renderItem}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  header: {
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
  list: {
    paddingTop: 8,
    paddingBottom: 24,
  },
  conversationItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.card,
    padding: 14,
    marginHorizontal: 16,
    marginVertical: 4,
    borderRadius: 12,
    gap: 12,
  },
  avatar: {
    position: 'relative',
  },
  unreadBadge: {
    position: 'absolute',
    top: -2,
    right: -4,
    backgroundColor: Colors.error,
    borderRadius: 10,
    minWidth: 18,
    height: 18,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 4,
  },
  unreadText: {
    color: Colors.white,
    fontSize: 11,
    fontWeight: '700',
  },
  conversationInfo: {
    flex: 1,
  },
  topRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  customerName: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.text,
  },
  timestamp: {
    fontSize: 12,
    color: Colors.textSecondary,
  },
  lastMessage: {
    fontSize: 13,
    color: Colors.textSecondary,
    marginTop: 2,
  },
});
