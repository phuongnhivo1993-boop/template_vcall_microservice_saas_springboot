'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import {
  Card, Descriptions, Tag, Spin, Alert, Button, Space, Typography, Row, Col, Empty, Statistic
} from 'antd';
import {
  ArrowLeftOutlined, PhoneOutlined, ClockCircleOutlined, UserOutlined,
  AudioOutlined, PlayCircleOutlined, PauseCircleOutlined, StarOutlined
} from '@ant-design/icons';
import { callsApi } from '@/lib/api';

const { Title, Text } = Typography;

const statusColors: Record<string, string> = {
  completed: '#52c41a', ongoing: '#1677ff', missed: '#ff4d4f', failed: '#faad14',
};

const directionColors: Record<string, string> = {
  inbound: 'blue', outbound: 'purple', INBOUND: 'blue', OUTBOUND: 'purple',
};

export default function CallDetailPage() {
  const params = useParams();
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [call, setCall] = useState<any>(null);
  const [recording, setRecording] = useState<any>(null);
  const [recordingLoading, setRecordingLoading] = useState(false);
  const [playing, setPlaying] = useState(false);

  const fetchCall = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await callsApi.getById(params.id as string);
      setCall(res.data?.data || res.data);
    } catch (err: any) {
      setError(err?.response?.data?.message || err?.message || 'Failed to load call details');
    } finally {
      setLoading(false);
    }
  }, [params.id]);

  const fetchRecording = useCallback(async () => {
    setRecordingLoading(true);
    try {
      const res = await callsApi.getRecording(params.id as string);
      setRecording(res.data?.data || res.data);
    } catch {
      // no recording available
    } finally {
      setRecordingLoading(false);
    }
  }, [params.id]);

  useEffect(() => {
    fetchCall();
    fetchRecording();
  }, [fetchCall, fetchRecording]);

  const formatDuration = (secs: number) => {
    if (!secs) return '-';
    const mins = Math.floor(secs / 60);
    const s = secs % 60;
    return `${mins}:${s.toString().padStart(2, '0')}`;
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" tip="Loading call details..." /></div>;
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={fetchCall}>Retry</Button>} />;
  }

  if (!call) return <Empty description="Call not found" />;

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => router.back()}>Back</Button>
        <Title level={4} style={{ margin: 0 }}>Call Details</Title>
        <Tag color={statusColors[call.status] || 'default'}>{(call.status || 'UNKNOWN').toUpperCase()}</Tag>
        <Tag color={directionColors[call.direction] || 'default'}>{(call.direction || 'UNKNOWN').toUpperCase()}</Tag>
      </Space>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}>
          <Card><Statistic title="Duration" value={formatDuration(call.duration)} prefix={<ClockCircleOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="Direction" value={call.direction || '-'} prefix={<PhoneOutlined />} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="Status" value={(call.status || '-').toUpperCase()} valueStyle={{ color: statusColors[call.status] || undefined }} /></Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card><Statistic title="Agent" value={call.agent || call.agentName || '-'} prefix={<UserOutlined />} /></Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card title="Call Information">
            <Descriptions column={{ xs: 1, sm: 2 }} bordered size="small">
              <Descriptions.Item label="Call ID"><Text code>{call.id}</Text></Descriptions.Item>
              <Descriptions.Item label="Caller"><PhoneOutlined /> {call.caller || call.callerNumber || '-'}</Descriptions.Item>
              <Descriptions.Item label="Callee"><PhoneOutlined /> {call.callee || call.calleeNumber || '-'}</Descriptions.Item>
              <Descriptions.Item label="Direction">
                <Tag color={directionColors[call.direction] || 'default'}>{(call.direction || 'UNKNOWN').toUpperCase()}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={statusColors[call.status] || 'default'}>{(call.status || 'UNKNOWN').toUpperCase()}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Duration">{formatDuration(call.duration)}</Descriptions.Item>
              <Descriptions.Item label="Start Time">
                {call.startTime || call.time ? new Date(call.startTime || call.time).toLocaleString('vi-VN') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="End Time">
                {call.endTime ? new Date(call.endTime).toLocaleString('vi-VN') : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Queue">{call.queue || call.queueName || '-'}</Descriptions.Item>
              <Descriptions.Item label="Agent">{call.agent || call.agentName || '-'}</Descriptions.Item>
              <Descriptions.Item label="Disposition">{call.disposition || '-'}</Descriptions.Item>
              <Descriptions.Item label="Call Type">{call.callType || '-'}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title="Recording" style={{ marginBottom: 16 }}>
            {recordingLoading ? (
              <Spin size="small" />
            ) : recording?.url || recording?.recordingUrl || recording?.fileUrl ? (
              <div style={{ textAlign: 'center' }}>
                <audio
                  controls
                  style={{ width: '100%' }}
                  src={recording.url || recording.recordingUrl || recording.fileUrl}
                >
                  Your browser does not support the audio element.
                </audio>
                {recording.duration && (
                  <div style={{ marginTop: 8 }}>
                    <Text type="secondary">Duration: {formatDuration(recording.duration)}</Text>
                  </div>
                )}
              </div>
            ) : (
              <Empty description="No recording available" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>

          <Card title="Quality Score">
            {call.qualityScore != null ? (
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: 48, fontWeight: 'bold', color: call.qualityScore >= 80 ? '#52c41a' : call.qualityScore >= 50 ? '#faad14' : '#ff4d4f' }}>
                  {call.qualityScore}
                </div>
                <Text type="secondary">out of 100</Text>
              </div>
            ) : call.evaluation ? (
              <Descriptions column={1} size="small">
                <Descriptions.Item label="Score">
                  <StarOutlined style={{ color: '#faad14' }} /> {call.evaluation.score || call.evaluation.qualityScore || '-'}
                </Descriptions.Item>
                {call.evaluation.comments && (
                  <Descriptions.Item label="Comments">{call.evaluation.comments}</Descriptions.Item>
                )}
              </Descriptions>
            ) : (
              <Empty description="No quality score" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>

          <Card title="Call Metadata">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="Hangup Cause">{call.hangupCause || '-'}</Descriptions.Item>
              <Descriptions.Item label="SIP Code">{call.sipCode || '-'}</Descriptions.Item>
              <Descriptions.Item label="Billing">{call.billingDuration ? `${call.billingDuration}s` : '-'}</Descriptions.Item>
              <Descriptions.Item label="Tags">{call.tags?.join(', ') || '-'}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
