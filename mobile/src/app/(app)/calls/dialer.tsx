import { useState, useCallback, useEffect } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, FlatList, StyleSheet, ActivityIndicator,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useDispatch } from 'react-redux';
import { Colors } from '../../../constants/colors';
import { customersApi, callsApi } from '../../../lib/api';
import { setActiveCall } from '../../../store/slices/callSlice';

const KEYS = [
  ['1', '', '2', 'ABC', '3', 'DEF'],
  ['4', 'GHI', '5', 'JKL', '6', 'MNO'],
  ['7', 'PQRS', '8', 'TUV', '9', 'WXYZ'],
  ['*', '', '0', '+', '#', ''],
];

export default function DialerScreen() {
  const router = useRouter();
  const dispatch = useDispatch();
  const [number, setNumber] = useState('');
  const [search, setSearch] = useState('');
  const [contacts, setContacts] = useState<any[]>([]);
  const [contactsLoading, setContactsLoading] = useState(false);
  const [calling, setCalling] = useState(false);

  const filtered = contacts.filter(
    (c: any) =>
      c.name?.toLowerCase().includes(search.toLowerCase()) ||
      (c.phone || '').includes(search),
  );

  const searchContacts = useCallback(async (keyword: string) => {
    if (!keyword.trim()) { setContacts([]); return; }
    setContactsLoading(true);
    try {
      const res = await customersApi.search(keyword);
      const data = res.data?.data || res.data || [];
      setContacts(Array.isArray(data) ? data : []);
    } catch {
      setContacts([]);
    } finally {
      setContactsLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => searchContacts(search), 300);
    return () => clearTimeout(timer);
  }, [search, searchContacts]);

  const handlePress = (digit: string) => {
    setNumber((prev) => prev + digit);
  };

  const handleDelete = () => {
    setNumber((prev) => prev.slice(0, -1));
  };

  const handleCall = async () => {
    if (!number.trim() || calling) return;
    setCalling(true);
    try {
      const res = await callsApi.startCall({
        callerNumber: number.trim(),
        calleeNumber: number.trim(),
        direction: 'OUTBOUND',
      });
      const callData = res.data?.data || res.data;
      if (callData?.id) {
        dispatch(setActiveCall({
          id: callData.id,
          channelId: callData.channelId || '',
          callerName: callData.callerName || '',
          callerNumber: callData.callerNumber || number.trim(),
          direction: 'outgoing',
          status: 'connected',
          startTime: Date.now(),
          isMuted: false,
          isSpeakerOn: false,
          isOnHold: false,
        }));
      }
      router.push('/calls/active');
    } catch {
      router.push('/calls/active');
    } finally {
      setCalling(false);
    }
  };

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()}>
          <Ionicons name="close" size={28} color={Colors.text} />
        </TouchableOpacity>
        <Text style={styles.title}>Quay số</Text>
        <View style={{ width: 28 }} />
      </View>

      <TextInput
        style={styles.numberInput}
        value={number}
        onChangeText={setNumber}
        placeholder="Nhập số điện thoại"
        placeholderTextColor={Colors.textSecondary}
        keyboardType="phone-pad"
      />

      <View style={styles.keypad}>
        {KEYS.map((row, ri) => (
          <View key={ri} style={styles.keypadRow}>
            {row.map((label, ci) => {
              const isDigit = /^[0-9*#]/.test(label);
              return (
                <TouchableOpacity
                  key={ci}
                  style={[styles.key, !label && styles.keyEmpty]}
                  onPress={() => label && isDigit && handlePress(label)}
                  disabled={!label}
                >
                  {label && (
                    <>
                      <Text style={styles.keyDigit}>{label[0]}</Text>
                      {label.length > 1 && <Text style={styles.keySub}>{label.slice(1)}</Text>}
                    </>
                  )}
                </TouchableOpacity>
              );
            })}
          </View>
        ))}
      </View>

      <View style={styles.bottomRow}>
        <TouchableOpacity onPress={handleDelete} style={styles.deleteBtn}>
          {number.length > 0 && <Ionicons name="backspace-outline" size={24} color={Colors.text} />}
        </TouchableOpacity>

        <TouchableOpacity style={styles.callBtn} onPress={handleCall}>
          <Ionicons name="call" size={28} color={Colors.white} />
        </TouchableOpacity>

        <View style={{ width: 40 }} />
      </View>

      <View style={styles.searchSection}>
        <TextInput
          style={styles.searchInput}
          placeholder="Tìm kiếm danh bạ..."
          placeholderTextColor={Colors.textSecondary}
          value={search}
          onChangeText={setSearch}
        />
        {contactsLoading && filtered.length === 0 ? (
          <ActivityIndicator size="small" color={Colors.primary} style={{ marginTop: 20 }} />
        ) : (
          <FlatList
            data={filtered}
            keyExtractor={(item) => String(item.id || item.name)}
            renderItem={({ item }) => (
              <TouchableOpacity
                style={styles.contactItem}
                onPress={() => setNumber(item.phone || item.phoneNumber || '')}
              >
                <Ionicons name="person-circle-outline" size={36} color={Colors.textSecondary} />
                <View style={styles.contactInfo}>
                  <Text style={styles.contactName}>{item.name}</Text>
                  <Text style={styles.contactNumber}>{item.phone || item.phoneNumber || ''}</Text>
                </View>
                <TouchableOpacity onPress={() => {
                  const phone = item.phone || item.phoneNumber || '';
                  setNumber(phone);
                  handleCall();
                }}>
                  <Ionicons name="call-outline" size={20} color={Colors.primary} />
                </TouchableOpacity>
              </TouchableOpacity>
            )}
            ListEmptyComponent={search.trim() ? (
              <Text style={{ textAlign: 'center', color: Colors.textSecondary, marginTop: 20 }}>No contacts found</Text>
            ) : null}
            style={styles.contactList}
          />
        )}
      </View>
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
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 60,
    paddingBottom: 12,
    backgroundColor: Colors.white,
  },
  title: {
    fontSize: 18,
    fontWeight: '600',
    color: Colors.text,
  },
  numberInput: {
    fontSize: 28,
    textAlign: 'center',
    paddingVertical: 16,
    color: Colors.text,
    backgroundColor: Colors.white,
    borderBottomWidth: 1,
    borderBottomColor: Colors.border,
  },
  keypad: {
    paddingVertical: 12,
    backgroundColor: Colors.white,
  },
  keypadRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: 8,
    marginVertical: 4,
  },
  key: {
    width: 80,
    height: 56,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 28,
    backgroundColor: Colors.background,
  },
  keyEmpty: {
    backgroundColor: 'transparent',
  },
  keyDigit: {
    fontSize: 24,
    fontWeight: '500',
    color: Colors.text,
  },
  keySub: {
    fontSize: 10,
    color: Colors.textSecondary,
    marginTop: -2,
  },
  bottomRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 40,
    paddingVertical: 16,
    backgroundColor: Colors.white,
  },
  deleteBtn: {
    width: 40,
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
  },
  callBtn: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: Colors.success,
    justifyContent: 'center',
    alignItems: 'center',
  },
  searchSection: {
    flex: 1,
    paddingHorizontal: 16,
    paddingTop: 8,
  },
  searchInput: {
    backgroundColor: Colors.white,
    borderRadius: 10,
    padding: 12,
    fontSize: 15,
    color: Colors.text,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: Colors.border,
  },
  contactList: {
    flex: 1,
  },
  contactItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.white,
    padding: 12,
    borderRadius: 10,
    marginVertical: 3,
    gap: 12,
  },
  contactInfo: {
    flex: 1,
  },
  contactName: {
    fontSize: 15,
    fontWeight: '500',
    color: Colors.text,
  },
  contactNumber: {
    fontSize: 13,
    color: Colors.textSecondary,
  },
});
