import { useState, useRef, useCallback } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, FlatList,
  StyleSheet, KeyboardAvoidingView, Platform,
} from 'react-native';
import { useLocalSearchParams, Stack, useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import type { ChatMessage } from '../../../types';

const MOCK_MESSAGES: ChatMessage[] = [
  { id: 'm1', conversationId: '1', senderId: 'customer', senderType: 'customer', content: 'Hello, I need help with my account', type: 'text', createdAt: new Date(Date.now() - 1200000).toISOString(), read: true },
  { id: 'm2', conversationId: '1', senderId: 'agent', senderType: 'agent', content: 'Hi John! I\'d be happy to help. What seems to be the issue?', type: 'text', createdAt: new Date(Date.now() - 1100000).toISOString(), read: true },
  { id: 'm3', conversationId: '1', senderId: 'customer', senderType: 'customer', content: 'I can\'t log into my account. It says invalid password.', type: 'text', createdAt: new Date(Date.now() - 1000000).toISOString(), read: true },
  { id: 'm4', conversationId: '1', senderId: 'agent', senderType: 'agent', content: 'Let me check your account. Can you provide your email address?', type: 'text', createdAt: new Date(Date.now() - 900000).toISOString(), read: true },
  { id: 'm5', conversationId: '1', senderId: 'customer', senderType: 'customer', content: 'john.doe@email.com', type: 'text', createdAt: new Date(Date.now() - 800000).toISOString(), read: true },
  { id: 'm6', conversationId: '1', senderId: 'agent', senderType: 'agent', content: 'Thanks! I\'ve reset your password. Please check your email for the temporary password.', type: 'text', createdAt: new Date(Date.now() - 700000).toISOString(), read: true },
  { id: 'm7', conversationId: '1', senderId: 'customer', senderType: 'customer', content: 'Thanks for your help!', type: 'text', createdAt: new Date(Date.now() - 600000).toISOString(), read: true },
];

export default function ChatDetailScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const router = useRouter();
  const [messages] = useState<ChatMessage[]>(MOCK_MESSAGES);
  const [inputText, setInputText] = useState('');
  const flatListRef = useRef<FlatList>(null);

  const renderMessage = useCallback(({ item }: { item: ChatMessage }) => {
    const isAgent = item.senderType === 'agent';
    return (
      <View style={[styles.messageRow, isAgent ? styles.agentRow : styles.customerRow]}>
        <View style={[styles.messageBubble, isAgent ? styles.agentBubble : styles.customerBubble]}>
          <Text style={[styles.messageText, isAgent && styles.agentMessageText]}>
            {item.content}
          </Text>
          <Text style={[styles.messageTime, isAgent && styles.agentMessageTime]}>
            {new Date(item.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
          </Text>
        </View>
      </View>
    );
  }, []);

  const handleSend = () => {
    if (!inputText.trim()) return;
    // In a real app, dispatch sendMessage thunk
    setInputText('');
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 90 : 0}
    >
      <Stack.Screen
        options={{
          headerShown: true,
          headerTitle: `Trò chuyện`,
          headerBackTitle: 'Quay lại',
          headerStyle: { backgroundColor: Colors.white },
          headerTintColor: Colors.text,
        }}
      />

      <FlatList
        ref={flatListRef}
        data={messages}
        keyExtractor={(item) => item.id}
        renderItem={renderMessage}
        contentContainerStyle={styles.messagesList}
        showsVerticalScrollIndicator={false}
        inverted={false}
        onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: false })}
      />

      <View style={styles.inputBar}>
        <TouchableOpacity style={styles.attachBtn}>
          <Ionicons name="attach-outline" size={22} color={Colors.textSecondary} />
        </TouchableOpacity>
        <TextInput
          style={styles.textInput}
          placeholder="Nhập tin nhắn..."
          placeholderTextColor={Colors.textSecondary}
          value={inputText}
          onChangeText={setInputText}
          multiline
          maxLength={1000}
        />
        <TouchableOpacity
          style={[styles.sendBtn, !inputText.trim() && styles.sendBtnDisabled]}
          onPress={handleSend}
          disabled={!inputText.trim()}
        >
          <Ionicons name="send" size={20} color={Colors.white} />
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  messagesList: {
    padding: 16,
    paddingBottom: 8,
  },
  messageRow: {
    marginVertical: 3,
    maxWidth: '80%',
  },
  agentRow: {
    alignSelf: 'flex-end',
  },
  customerRow: {
    alignSelf: 'flex-start',
  },
  messageBubble: {
    borderRadius: 16,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  agentBubble: {
    backgroundColor: Colors.chatBubbleAgent,
    borderBottomRightRadius: 4,
  },
  customerBubble: {
    backgroundColor: Colors.chatBubbleCustomer,
    borderBottomLeftRadius: 4,
  },
  messageText: {
    fontSize: 15,
    color: Colors.text,
    lineHeight: 20,
  },
  agentMessageText: {
    color: Colors.white,
  },
  messageTime: {
    fontSize: 11,
    color: Colors.textSecondary,
    marginTop: 4,
    alignSelf: 'flex-end',
  },
  agentMessageTime: {
    color: 'rgba(255,255,255,0.7)',
  },
  inputBar: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    backgroundColor: Colors.white,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderTopWidth: 1,
    borderTopColor: Colors.border,
    gap: 8,
  },
  attachBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: Colors.background,
    justifyContent: 'center',
    alignItems: 'center',
  },
  textInput: {
    flex: 1,
    backgroundColor: Colors.background,
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 10,
    fontSize: 15,
    color: Colors.text,
    maxHeight: 100,
  },
  sendBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: Colors.primary,
    justifyContent: 'center',
    alignItems: 'center',
  },
  sendBtnDisabled: {
    backgroundColor: Colors.border,
  },
});
