import { View, ActivityIndicator, Text, StyleSheet } from 'react-native';
import { Colors } from '../constants/colors';

interface Props {
  message?: string;
  fullScreen?: boolean;
}

export default function LoadingScreen({ message = 'Đang tải...', fullScreen = true }: Props) {
  return (
    <View style={[styles.container, !fullScreen && styles.inline]}>
      <ActivityIndicator size="large" color={Colors.primary} />
      <Text style={styles.text}>{message}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.background,
    paddingHorizontal: 32,
  },
  inline: {
    flex: 0,
    paddingVertical: 60,
  },
  text: {
    marginTop: 16,
    fontSize: 15,
    color: Colors.textSecondary,
    textAlign: 'center',
  },
});
