import { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity, ScrollView,
  StyleSheet, ActivityIndicator, Alert, KeyboardAvoidingView, Platform,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import { ticketsApi } from '../../../lib/api';

const PRIORITIES = [
  { label: 'Thấp', value: 'low' },
  { label: 'Trung bình', value: 'medium' },
  { label: 'Cao', value: 'high' },
  { label: 'Khẩn cấp', value: 'critical' },
];

const CATEGORIES = [
  { label: 'Kỹ thuật', value: 'technical' },
  { label: 'Thanh toán', value: 'billing' },
  { label: 'Tài khoản', value: 'account' },
  { label: 'Khác', value: 'other' },
];

export default function CreateTicketScreen() {
  const router = useRouter();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState('medium');
  const [category, setCategory] = useState('other');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async () => {
    if (!title.trim()) {
      Alert.alert('Xác thực', 'Vui lòng nhập tiêu đề phiếu yêu cầu');
      return;
    }
    if (!description.trim()) {
      Alert.alert('Xác thực', 'Vui lòng nhập mô tả');
      return;
    }
    setSubmitting(true);
    try {
      await ticketsApi.create({
        title: title.trim(),
        description: description.trim(),
        priority,
        category,
      });
      Alert.alert('Thành công', 'Phiếu yêu cầu đã được tạo', [
        { text: 'OK', onPress: () => router.back() },
      ]);
    } catch (err: any) {
      Alert.alert('Lỗi', err?.response?.data?.message || err?.message || 'Không thể tạo phiếu yêu cầu');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <Stack.Screen
        options={{
          headerShown: true,
          headerTitle: 'Tạo phiếu yêu cầu',
          headerBackTitle: 'Quay lại',
          headerStyle: { backgroundColor: Colors.white },
          headerTintColor: Colors.text,
        }}
      />
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <View style={styles.section}>
          <Text style={styles.label}>Tiêu đề *</Text>
          <TextInput
            style={styles.input}
            placeholder="Nhập tiêu đề phiếu yêu cầu"
            placeholderTextColor={Colors.textSecondary}
            value={title}
            onChangeText={setTitle}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>Mô tả *</Text>
          <TextInput
            style={[styles.input, styles.textArea]}
            placeholder="Nhập mô tả chi tiết"
            placeholderTextColor={Colors.textSecondary}
            value={description}
            onChangeText={setDescription}
            multiline
            textAlignVertical="top"
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>Mức độ ưu tiên</Text>
          <View style={styles.optionsRow}>
            {PRIORITIES.map((p) => (
              <TouchableOpacity
                key={p.value}
                style={[styles.optionChip, priority === p.value && styles.optionChipActive]}
                onPress={() => setPriority(p.value)}
                activeOpacity={0.7}
              >
                <Text style={[styles.optionText, priority === p.value && styles.optionTextActive]}>
                  {p.label}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.label}>Danh mục</Text>
          <View style={styles.optionsRow}>
            {CATEGORIES.map((c) => (
              <TouchableOpacity
                key={c.value}
                style={[styles.optionChip, category === c.value && styles.optionChipActive]}
                onPress={() => setCategory(c.value)}
                activeOpacity={0.7}
              >
                <Text style={[styles.optionText, category === c.value && styles.optionTextActive]}>
                  {c.label}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <TouchableOpacity
          style={[styles.submitBtn, submitting && styles.submitBtnDisabled]}
          onPress={handleSubmit}
          disabled={submitting}
          activeOpacity={0.7}
        >
          {submitting ? (
            <ActivityIndicator color={Colors.white} />
          ) : (
            <>
              <Ionicons name="checkmark-circle-outline" size={20} color={Colors.white} />
              <Text style={styles.submitText}>Tạo phiếu yêu cầu</Text>
            </>
          )}
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  content: {
    padding: 16,
    paddingBottom: 40,
  },
  section: {
    marginBottom: 20,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.text,
    marginBottom: 8,
  },
  input: {
    backgroundColor: Colors.white,
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    fontSize: 15,
    color: Colors.text,
    borderWidth: 1,
    borderColor: Colors.border,
  },
  textArea: {
    minHeight: 120,
    paddingTop: 12,
  },
  optionsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  optionChip: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: Colors.white,
    borderWidth: 1,
    borderColor: Colors.border,
  },
  optionChipActive: {
    backgroundColor: Colors.primary,
    borderColor: Colors.primary,
  },
  optionText: {
    fontSize: 13,
    color: Colors.textSecondary,
    fontWeight: '500',
  },
  optionTextActive: {
    color: Colors.white,
  },
  submitBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    backgroundColor: Colors.primary,
    borderRadius: 12,
    paddingVertical: 14,
    marginTop: 8,
  },
  submitBtnDisabled: {
    opacity: 0.7,
  },
  submitText: {
    color: Colors.white,
    fontSize: 16,
    fontWeight: '600',
  },
});
