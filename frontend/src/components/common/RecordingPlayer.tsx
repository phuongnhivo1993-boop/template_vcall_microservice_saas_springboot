'use client';

import { useState, useRef, useEffect } from 'react';
import { Modal, Card, Row, Col, Space, Typography, Button, Slider } from 'antd';
import {
  PlayCircleOutlined, PauseCircleOutlined, DownloadOutlined, SoundOutlined,
  PhoneOutlined, ClockCircleOutlined, UserOutlined, FileTextOutlined,
} from '@ant-design/icons';

const { Text } = Typography;

interface Recording {
  id: string;
  callId: string;
  fileName: string;
  duration: number;
  format: string;
  fileSize: number;
  callerNumber: string;
  calleeNumber: string;
  agentName: string;
  customerName: string;
  startTime: string;
  status: string;
  transcript?: string;
}

interface RecordingPlayerProps {
  visible: boolean;
  recording: Recording | null;
  recordingUrl?: string;
  onClose: () => void;
}

export default function RecordingPlayer({ visible, recording, recordingUrl, onClose }: RecordingPlayerProps) {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!visible) {
      setIsPlaying(false);
      setCurrentTime(0);
      setReady(false);
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current.currentTime = 0;
      }
    }
  }, [visible]);

  useEffect(() => {
    if (visible && recordingUrl && audioRef.current) {
      audioRef.current.src = recordingUrl;
      audioRef.current.load();
    }
  }, [visible, recordingUrl]);

  const handleLoadedMetadata = () => {
    if (audioRef.current) {
      setDuration(audioRef.current.duration);
      setReady(true);
    }
  };

  const handleTimeUpdate = () => {
    if (audioRef.current) {
      setCurrentTime(audioRef.current.currentTime);
    }
  };

  const handleEnded = () => {
    setIsPlaying(false);
    setCurrentTime(0);
  };

  const togglePlay = () => {
    if (!audioRef.current || !ready) return;
    if (isPlaying) {
      audioRef.current.pause();
    } else {
      audioRef.current.play();
    }
    setIsPlaying(!isPlaying);
  };

  const handleSeek = (value: number) => {
    if (!audioRef.current || !ready) return;
    const time = (value / 100) * duration;
    audioRef.current.currentTime = time;
    setCurrentTime(time);
  };

  const formatTime = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = Math.floor(seconds % 60);
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  if (!recording) return null;

  const progress = duration > 0 ? (currentTime / duration) * 100 : 0;

  return (
    <Modal
      title={<Space><PhoneOutlined /> Call Recording Player</Space>}
      open={visible}
      onCancel={onClose}
      width={600}
      footer={[
        <Button key="close" onClick={onClose}>Close</Button>,
        <Button key="download" icon={<DownloadOutlined />}>Download Recording</Button>,
      ]}
    >
      <audio
        ref={audioRef}
        onLoadedMetadata={handleLoadedMetadata}
        onTimeUpdate={handleTimeUpdate}
        onEnded={handleEnded}
      />

      <Card size="small" style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col span={12}>
            <Space direction="vertical" size={2}>
              <Text type="secondary">Call ID: {recording.callId}</Text>
              <Text><PhoneOutlined /> {recording.callerNumber} → {recording.calleeNumber}</Text>
              <Text><UserOutlined /> Agent: {recording.agentName}</Text>
              <Text><UserOutlined /> Customer: {recording.customerName}</Text>
            </Space>
          </Col>
          <Col span={12}>
            <Space direction="vertical" size={2}>
              <Text type="secondary">Start: {new Date(recording.startTime).toLocaleString()}</Text>
              <Text><ClockCircleOutlined /> {formatTime(recording.duration)}</Text>
              <Text><SoundOutlined /> {formatFileSize(recording.fileSize)}</Text>
            </Space>
          </Col>
        </Row>
      </Card>

      <div style={{ textAlign: 'center', padding: '20px 0' }}>
        <div style={{ fontSize: 48, marginBottom: 16 }}>
          {isPlaying ? (
            <PauseCircleOutlined onClick={togglePlay} style={{ cursor: 'pointer', color: '#1890ff' }} />
          ) : (
            <PlayCircleOutlined onClick={togglePlay} style={{ cursor: 'pointer', color: '#52c41a' }} />
          )}
        </div>
        <Text>{formatTime(currentTime)} / {formatTime(duration)}</Text>
        <Slider
          value={progress}
          onChange={handleSeek}
          tooltip={{ formatter: (val) => formatTime(((val || 0) / 100) * duration) }}
          style={{ marginTop: 8 }}
        />
      </div>

      {recording.transcript && (
        <Card title={<Space><FileTextOutlined /> Transcript</Space>} size="small" style={{ marginTop: 16 }}>
          <Text style={{ whiteSpace: 'pre-wrap' }}>{recording.transcript}</Text>
        </Card>
      )}
    </Modal>
  );
}
