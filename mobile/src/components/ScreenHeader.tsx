import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../constants/colors';

interface Props {
  title: string;
  subtitle?: string;
  showSearch?: boolean;
  onSearchToggle?: () => void;
  rightAction?: {
    icon: keyof typeof Ionicons.glyphMap;
    onPress: () => void;
  };
}

export default function ScreenHeader({ title, subtitle, showSearch, onSearchToggle, rightAction }: Props) {
  return (
    <View style={styles.container}>
      <View style={styles.left}>
        <Text style={styles.title}>{title}</Text>
        {subtitle && <Text style={styles.subtitle}>{subtitle}</Text>}
      </View>
      <View style={styles.right}>
        {onSearchToggle && (
          <TouchableOpacity style={styles.iconBtn} onPress={onSearchToggle} hitSlop={8}>
            <Ionicons
              name={showSearch ? 'close-outline' : 'search-outline'}
              size={22}
              color={Colors.text}
            />
          </TouchableOpacity>
        )}
        {rightAction && (
          <TouchableOpacity style={styles.iconBtn} onPress={rightAction.onPress} hitSlop={8}>
            <Ionicons name={rightAction.icon} size={22} color={Colors.text} />
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 60,
    paddingBottom: 16,
    backgroundColor: Colors.white,
  },
  left: {
    flex: 1,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: Colors.text,
  },
  subtitle: {
    fontSize: 13,
    color: Colors.textSecondary,
    marginTop: 2,
  },
  right: {
    flexDirection: 'row',
    gap: 8,
  },
  iconBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: Colors.background,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
