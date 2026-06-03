import { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, FlatList, StyleSheet,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';

const CONTACTS = [
  { id: '1', name: 'John Doe', number: '+1 555-0101' },
  { id: '2', name: 'Jane Roe', number: '+1 555-0102' },
  { id: '3', name: 'Bob Wilson', number: '+1 555-0103' },
  { id: '4', name: 'Alice Brown', number: '+1 555-0104' },
  { id: '5', name: 'Charlie Davis', number: '+1 555-0105' },
];

const KEYS = [
  ['1', '', '2', 'ABC', '3', 'DEF'],
  ['4', 'GHI', '5', 'JKL', '6', 'MNO'],
  ['7', 'PQRS', '8', 'TUV', '9', 'WXYZ'],
  ['*', '', '0', '+', '#', ''],
];

export default function DialerScreen() {
  const router = useRouter();
  const [number, setNumber] = useState('');
  const [search, setSearch] = useState('');

  const filtered = CONTACTS.filter(
    (c) =>
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      c.number.includes(search),
  );

  const handlePress = (digit: string) => {
    setNumber((prev) => prev + digit);
  };

  const handleDelete = () => {
    setNumber((prev) => prev.slice(0, -1));
  };

  const handleCall = () => {
    if (number.trim()) {
      router.push('/calls/active');
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
        <FlatList
          data={filtered}
          keyExtractor={(item) => item.id}
          renderItem={({ item }) => (
            <TouchableOpacity
              style={styles.contactItem}
              onPress={() => setNumber(item.number)}
            >
              <Ionicons name="person-circle-outline" size={36} color={Colors.textSecondary} />
              <View style={styles.contactInfo}>
                <Text style={styles.contactName}>{item.name}</Text>
                <Text style={styles.contactNumber}>{item.number}</Text>
              </View>
              <TouchableOpacity onPress={() => { setNumber(item.number); handleCall(); }}>
                <Ionicons name="call-outline" size={20} color={Colors.primary} />
              </TouchableOpacity>
            </TouchableOpacity>
          )}
          style={styles.contactList}
        />
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
