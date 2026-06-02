'use client';

import { useState } from 'react';
import { Modal, Card, Row, Col, Statistic, Tag, Space, Typography, Button, Slider, message } from 'antd';
import { PlayCircleOutlined, PauseCircleOutlined, DownloadOutlined, SoundOutlined,
         PhoneOutlined, ClockCircleOutlined, UserOutlined, FileTextOutlined } from '@ant-design/icons';

const { Text, Title } = Typography;

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
  onClose: () => void;
}

export default function RecordingPlayer({ visible, recording, onClose }: RecordingPlayerProps) {
  const [isPlaying, setIsPlaying] = useState(false);
  const [progress, setProgress] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);

  if (!recording) return null;

  const formatDuration = (seconds: number) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const togglePlay = () => {
    setIsPlaying(!isPlaying);
    if (!isPlaying) {
      const interval = setInterval(() => {
        setProgress(prev => {
          if (prev >= 100) {
            clearInterval(interval);
            setIsPlaying(false);
            return 100;
          }
          setCurrentTime(prev => prev + 1);
          return prev + 1;
        });
      }, recording.duration * 10);
    }
  };

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
              <Text type="secondary">Start: {new Date(recording.startTime).toLocaleString('vi-VN')}</Text>
              <Text><ClockCircleOutlined /> {formatDuration(recording.duration)}</Text>
              <Text><SoundOutlined /> {formatFileSize(recording.fileSize)}</Text>
              <Tag color={recording.status === 'COMPLETED' ? 'green' : 'orange'}>{recording.status}</Tag>
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
        <Text>{formatDuration(currentTime)} / {formatDuration(recording.duration)}</Text>
        <Slider
          value={progress}
          onChange={(val) => {
            setProgress(val);
            setCurrentTime(Math.floor(val * recording.duration / 100));
          }}
          tooltip={{ formatter: (val) => formatDuration(Math.floor((val || 0) * recording.duration / 100)) }}
        />
      </div>

      <div style={{ height: 80, background: '#f0f0f0', borderRadius: 8, marginBottom: 16, display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
        <div style={{ display: 'flex', gap: 2, alignItems: 'center', height: '100%', width: '100%', padding: '0 8px' }}>
          {Array.from({ length: 80 }).map((_, i) => (
            <div key={i} style={{
              width: 4,
              height: `${Math.random() * 60 + 10}%`,
              background: i / 80 * 100 <= progress ? '#1890ff' : '#d9d9d9',
              borderRadius: 2,
              transition: 'height 0.3s',
              flexShrink: 0,
            }} />
          ))}
        </div>
      </div>

      {recording.transcript && (
        <Card title={<Space><FileTextOutlined /> Transcript</Space>} size="small">
          <Text style={{ whiteSpace: 'pre-wrap' }}>{recording.transcript}</Text>
        </Card>
      )}
    </Modal>
  );
}
