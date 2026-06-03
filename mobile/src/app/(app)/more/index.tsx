import { View, Text, FlatList, TouchableOpacity, StyleSheet } from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';

interface MoreItem {
  label: string;
  icon: keyof typeof Ionicons.glyphMap;
  route: any;
  color: string;
}

const MORE_ITEMS: MoreItem[] = [
  { label: 'Quản lý khách hàng', icon: 'people-outline', route: '/crm', color: '#722ed1' },
  { label: 'Chiến dịch', icon: 'megaphone-outline', route: '/campaigns', color: Colors.warning },
  { label: 'Hóa đơn', icon: 'receipt-outline', route: '/billing', color: '#13c2c2' },
  { label: 'Báo cáo', icon: 'bar-chart-outline', route: '/reports', color: Colors.primary },
  { label: 'Giám sát', icon: 'pulse-outline', route: '/supervisor', color: '#eb2f96' },
  { label: 'Webhook', icon: 'git-merge-outline', route: '/webhooks', color: '#fa8c16' },
  { label: 'Kiến thức', icon: 'book-outline', route: '/knowledge-base', color: '#52c41a' },
  { label: 'Tự động hóa', icon: 'flash-outline', route: '/automation', color: '#1677ff' },
  { label: 'Thông báo', icon: 'notifications-outline', route: '/notifications', color: Colors.error },
  { label: 'Cài đặt', icon: 'settings-outline', route: '/settings', color: Colors.textSecondary },
  { label: 'Hồ sơ', icon: 'person-outline', route: '/profile', color: Colors.primary },
];

export default function MoreScreen() {
  const router = useRouter();

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />
      <View style={styles.header}>
        <Text style={styles.title}>Thêm</Text>
      </View>

      <FlatList
        data={MORE_ITEMS}
        keyExtractor={(item) => item.route}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={styles.menuItem}
            onPress={() => router.push(item.route)}
            activeOpacity={0.7}
          >
            <View style={[styles.menuIcon, { backgroundColor: item.color + '15' }]}>
              <Ionicons name={item.icon} size={20} color={item.color} />
            </View>
            <Text style={styles.menuLabel}>{item.label}</Text>
            <Ionicons name="chevron-forward" size={18} color={Colors.textSecondary} />
          </TouchableOpacity>
        )}
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
    padding: 16,
    paddingBottom: 100,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.card,
    padding: 14,
    borderRadius: 12,
    marginBottom: 8,
    gap: 14,
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowOffset: { width: 0, height: 2 },
    shadowRadius: 4,
    elevation: 2,
  },
  menuIcon: {
    width: 42,
    height: 42,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  menuLabel: {
    flex: 1,
    fontSize: 15,
    fontWeight: '500',
    color: Colors.text,
  },
});
