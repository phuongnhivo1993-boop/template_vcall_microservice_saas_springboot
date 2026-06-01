import { useEffect, useState, useRef } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet, Vibration, Platform,
} from 'react-native';
import { useRouter, Stack } from 'expo-router';
import { useSelector, useDispatch } from 'react-redux';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '../../../constants/colors';
import { toggleMute, toggleSpeaker, endActiveCall } from '../../../store/slices/callSlice';
import type { RootState, AppDispatch } from '../../../store';

function formatTimer(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
}

export default function ActiveCallScreen() {
  const router = useRouter();
  const dispatch = useDispatch<AppDispatch>();
  const activeCall = useSelector((state: RootState) => state.call.activeCall);
  const [timer, setTimer] = useState(0);
  const [showKeypad, setShowKeypad] = useState(false);
  const [dtmfBuffer, setDtmfBuffer] = useState('');
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (activeCall?.startTime) {
      const elapsed = Math.floor((Date.now() - activeCall.startTime) / 1000);
      setTimer(elapsed);
    }
    intervalRef.current = setInterval(() => {
      setTimer((t) => t + 1);
    }, 1000);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, []);

  const handleEndCall = () => {
    dispatch(endActiveCall());
    router.back();
  };

  const handleDtmf = (digit: string) => {
    setDtmfBuffer((prev) => prev + digit);
    Vibration.vibrate(50);
  };

  if (!activeCall) {
    return (
      <View style={styles.container}>
        <Text style={styles.noCallText}>No active call</Text>
        <TouchableOpacity style={styles.endBtn} onPress={() => router.back()}>
          <Text style={styles.endBtnText}>Go Back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ headerShown: false }} />

      <View style={styles.header}>
        <Text style={styles.callerName}>{activeCall.callerName}</Text>
        <Text style={styles.callerNumber}>{activeCall.callerNumber}</Text>
        <Text style={styles.timer}>{formatTimer(timer)}</Text>
        <View style={styles.statusBadge}>
          <View style={styles.statusDot} />
          <Text style={styles.statusText}>
            {activeCall.direction === 'incoming' ? 'Incoming Call' : 'Outgoing Call'}
          </Text>
        </View>
      </View>

      {showKeypad && (
        <View style={styles.dtmfContainer}>
          <Text style={styles.dtmfBuffer}>{dtmfBuffer}</Text>
          <View style={styles.keypad}>
            {['1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '0', '#'].map((digit) => (
              <TouchableOpacity
                key={digit}
                style={styles.dtmfKey}
                onPress={() => handleDtmf(digit)}
              >
                <Text style={styles.dtmfKeyText}>{digit}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>
      )}

      <View style={styles.controls}>
        <ControlButton icon="mic-off-outline" label="Mute" active={activeCall.isMuted} onPress={() => dispatch(toggleMute())} />
        <ControlButton icon="volume-high-outline" label="Speaker" active={activeCall.isSpeakerOn} onPress={() => dispatch(toggleSpeaker())} />
        <ControlButton
          icon="keypad-outline"
          label="Keypad"
          active={showKeypad}
          onPress={() => setShowKeypad(!showKeypad)}
        />
      </View>

      <TouchableOpacity style={styles.endBtn} onPress={handleEndCall}>
        <Ionicons name="call" size={28} color={Colors.white} />
      </TouchableOpacity>
    </View>
  );
}

function ControlButton({
  icon, label, active, onPress,
}: {
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  active: boolean;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity
      style={[styles.controlBtn, active && styles.controlBtnActive]}
      onPress={onPress}
    >
      <Ionicons name={icon} size={24} color={active ? Colors.white : Colors.text} />
      <Text style={[styles.controlLabel, active && styles.controlLabelActive]}>{label}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
    justifyContent: 'space-between',
    paddingTop: 60,
    paddingBottom: 50,
    alignItems: 'center',
  },
  header: {
    alignItems: 'center',
    marginTop: 40,
  },
  callerName: {
    fontSize: 26,
    fontWeight: '700',
    color: Colors.white,
  },
  callerNumber: {
    fontSize: 16,
    color: '#aaa',
    marginTop: 4,
  },
  timer: {
    fontSize: 48,
    fontWeight: '200',
    color: Colors.white,
    marginTop: 24,
    fontVariant: ['tabular-nums'],
  },
  statusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
    gap: 6,
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: Colors.success,
  },
  statusText: {
    color: Colors.success,
    fontSize: 14,
  },
  noCallText: {
    color: Colors.white,
    fontSize: 18,
  },
  dtmfContainer: {
    alignItems: 'center',
  },
  dtmfBuffer: {
    color: Colors.white,
    fontSize: 20,
    letterSpacing: 4,
    marginBottom: 16,
  },
  keypad: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    width: 220,
    gap: 12,
  },
  dtmfKey: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: 'rgba(255,255,255,0.1)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  dtmfKeyText: {
    color: Colors.white,
    fontSize: 22,
    fontWeight: '500',
  },
  controls: {
    flexDirection: 'row',
    gap: 24,
  },
  controlBtn: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: 'rgba(255,255,255,0.1)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  controlBtnActive: {
    backgroundColor: Colors.primary,
  },
  controlLabel: {
    color: '#aaa',
    fontSize: 11,
    marginTop: 4,
  },
  controlLabelActive: {
    color: Colors.white,
  },
  endBtn: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: Colors.error,
    justifyContent: 'center',
    alignItems: 'center',
    transform: [{ rotate: '135deg' }],
  },
  endBtnText: {
    color: Colors.white,
    fontSize: 16,
    fontWeight: '600',
  },
});
