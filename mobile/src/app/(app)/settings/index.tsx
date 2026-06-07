import { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, Switch, StyleSheet, ScrollView } from 'react-native';
import { Stack, useRouter } from 'expo-router';
import { useSelector, useDispatch } from 'react-redux';
import { Ionicons } from '@expo/vector-icons';
import * as SecureStore from 'expo-secure-store';
import { Colors } from '../../../constants/colors';
import { logoutUser } from '../../../store/slices/authSlice';
import type { RootState, AppDispatch } from '../../../store';

interface SettingItem {
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  type: 'toggle' | 'nav' | 'info';
  value?: boolean;
  onToggle?: (val: boolean) => void;
  onPress?: () => void;
  info?: string;
}

const SETTINGS_KEYS = {
  notifications: 'settings_notifications',
  soundEnabled: 'settings_sound',
  vibrationEnabled: 'settings_vibration',
  autoAnswer: 'settings_auto_answer',
};

export default function SettingsScreen() {
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();
  const user = useSelector((state: RootState) => state.auth.user);

  const [notifications, setNotifications] = useState(true);
  const [soundEnabled, setSoundEnabled] = useState(true);
  const [vibrationEnabled, setVibrationEnabled] = useState(true);
  const [autoAnswer, setAutoAnswer] = useState(false);

  useEffect(() => {
    const loadSettings = async () => {
      const n = await SecureStore.getItemAsync(SETTINGS_KEYS.notifications);
      const s = await SecureStore.getItemAsync(SETTINGS_KEYS.soundEnabled);
      const v = await SecureStore.getItemAsync(SETTINGS_KEYS.vibrationEnabled);
      const a = await SecureStore.getItemAsync(SETTINGS_KEYS.autoAnswer);
      if (n !== null) setNotifications(n === 'true');
      if (s !== null) setSoundEnabled(s === 'true');
      if (v !== null) setVibrationEnabled(v === 'true');
      if (a !== null) setAutoAnswer(a === 'true');
    };
    loadSettings();
  }, []);

  const persistSetting = async (key: string, value: boolean) => {
    await SecureStore.setItemAsync(key, String(value));
  };

  const handleNotifications = (val: boolean) => {
    setNotifications(val);
    persistSetting(SETTINGS_KEYS.notifications, val);
  };

  const handleSoundEnabled = (val: boolean) => {
    setSoundEnabled(val);
    persistSetting(SETTINGS_KEYS.soundEnabled, val);
  };

  const handleVibrationEnabled = (val: boolean) => {
    setVibrationEnabled(val);
    persistSetting(SETTINGS_KEYS.vibrationEnabled, val);
  };

  const handleAutoAnswer = (val: boolean) => {
    setAutoAnswer(val);
    persistSetting(SETTINGS_KEYS.autoAnswer, val);
  };

  const handleLogout = async () => {
    await dispatch(logoutUser());
    router.replace('/(auth)/login');
  };

  const sections: { title: string; items: SettingItem[] }[] = [
    {
      title: 'Thông báo',
      items: [
        { icon: 'notifications-outline', label: 'Thông báo đẩy', type: 'toggle', value: notifications, onToggle: handleNotifications },
        { icon: 'volume-high-outline', label: 'Âm thanh', type: 'toggle', value: soundEnabled, onToggle: handleSoundEnabled },
        { icon: 'phone-portrait-outline', label: 'Rung', type: 'toggle', value: vibrationEnabled, onToggle: handleVibrationEnabled },
      ],
    },
    {
      title: 'Cuộc gọi',
      items: [
        { icon: 'call-outline', label: 'Tự động trả lời', type: 'toggle', value: autoAnswer, onToggle: handleAutoAnswer },
        { icon: 'mic-outline', label: 'Âm lượng mic', type: 'nav', info: '80%' },
        { icon: 'volume-high-outline', label: 'Âm lượng loa', type: 'nav', info: '70%' },
      ],
    },
    {
      title: 'Tài khoản',
      items: [
        { icon: 'person-outline', label: 'Hồ sơ', type: 'nav', onPress: () => router.push('/profile') },
        { icon: 'lock-closed-outline', label: 'Đổi mật khẩu', type: 'nav' },
        { icon: 'language-outline', label: 'Ngôn ngữ', type: 'info', info: 'Tiếng Việt' },
      ],
    },
    {
      title: 'Khác',
      items: [
        { icon: 'information-circle-outline', label: 'Phiên bản', type: 'info', info: '1.0.0' },
        { icon: 'help-circle-outline', label: 'Trợ giúp', type: 'nav' },
        { icon: 'shield-outline', label: 'Điều khoản sử dụng', type: 'nav' },
      ],
    },
  ];

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Stack.Screen options={{ headerShown: false }} />
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} hitSlop={8}>
          <Ionicons name="arrow-back" size={24} color={Colors.text} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Cài đặt</Text>
        <View style={{ width: 24 }} />
      </View>

      {sections.map((section, sIdx) => (
        <View key={sIdx} style={styles.section}>
          <Text style={styles.sectionTitle}>{section.title}</Text>
          <View style={styles.sectionCard}>
            {section.items.map((item, iIdx) => (
              <TouchableOpacity
                key={iIdx}
                style={[styles.settingItem, iIdx < section.items.length - 1 && styles.settingBorder]}
                onPress={item.onPress}
                disabled={item.type === 'toggle' || item.type === 'info'}
                activeOpacity={item.type === 'nav' ? 0.7 : 1}
              >
                <View style={styles.settingLeft}>
                  <Ionicons name={item.icon} size={20} color={Colors.text} />
                  <Text style={styles.settingLabel}>{item.label}</Text>
                </View>
                <View style={styles.settingRight}>
                  {item.type === 'toggle' && item.onToggle && (
                    <Switch
                      value={item.value}
                      onValueChange={item.onToggle}
                      trackColor={{ false: Colors.border, true: Colors.primary + '60' }}
                      thumbColor={item.value ? Colors.primary : Colors.textSecondary}
                    />
                  )}
                  {item.type === 'info' && item.info && (
                    <Text style={styles.settingInfo}>{item.info}</Text>
                  )}
                  {item.type === 'nav' && (
                    <Ionicons name="chevron-forward" size={18} color={Colors.textSecondary} />
                  )}
                </View>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      ))}

      <TouchableOpacity style={styles.logoutBtn} onPress={handleLogout}>
        <Ionicons name="log-out-outline" size={20} color={Colors.error} />
        <Text style={styles.logoutText}>Đăng xuất</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  content: { paddingBottom: 40 },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingTop: 60,
    paddingBottom: 16,
    backgroundColor: Colors.white,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: Colors.text,
  },
  section: {
    marginTop: 16,
    paddingHorizontal: 16,
  },
  sectionTitle: {
    fontSize: 13,
    color: Colors.textSecondary,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: 8,
    fontWeight: '600',
  },
  sectionCard: {
    backgroundColor: Colors.card,
    borderRadius: 12,
    overflow: 'hidden',
  },
  settingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  settingBorder: {
    borderBottomWidth: 1,
    borderBottomColor: Colors.border,
  },
  settingLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  settingLabel: {
    fontSize: 15,
    color: Colors.text,
  },
  settingRight: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  settingInfo: {
    fontSize: 14,
    color: Colors.textSecondary,
  },
  logoutBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginHorizontal: 16,
    marginTop: 24,
    paddingVertical: 14,
    backgroundColor: Colors.white,
    borderRadius: 12,
  },
  logoutText: {
    fontSize: 15,
    color: Colors.error,
    fontWeight: '600',
  },
});
