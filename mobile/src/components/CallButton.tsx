import { TouchableOpacity, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useSelector } from 'react-redux';
import { Colors } from '../constants/colors';
import type { RootState } from '../store';

export default function CallButton() {
  const router = useRouter();
  const activeCall = useSelector((state: RootState) => state.call.activeCall);

  if (!activeCall) return null;

  return (
    <TouchableOpacity
      style={styles.fab}
      onPress={() => router.push('/calls/active')}
      activeOpacity={0.8}
    >
      <Ionicons name="call" size={24} color={Colors.white} />
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  fab: {
    position: 'absolute',
    bottom: 100,
    right: 20,
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: Colors.success,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 8,
    zIndex: 999,
  },
});
