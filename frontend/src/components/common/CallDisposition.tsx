'use client';

import { useState } from 'react';
import { Modal, Form, Select, Input, Rate, Tag, Space, Button, Typography, Divider, message } from 'antd';
import { PhoneOutlined, StopOutlined } from '@ant-design/icons';

const { Text, Title } = Typography;
const { TextArea } = Input;

const DISPOSITION_CODES = [
  { value: 'RESOLVED', label: 'Resolved', color: 'green', description: 'Issue was resolved' },
  { value: 'CALLBACK', label: 'Callback Required', color: 'orange', description: 'Customer needs follow-up' },
  { value: 'TRANSFERRED', label: 'Transferred', color: 'blue', description: 'Call was transferred to another agent' },
  { value: 'VOICEMAIL', label: 'Left Voicemail', color: 'purple', description: 'Sent to voicemail' },
  { value: 'NO_ANSWER', label: 'No Answer', color: 'red', description: 'Customer did not answer' },
  { value: 'BUSY', label: 'Busy Line', color: 'red', description: 'Line was busy' },
  { value: 'WRONG_NUMBER', label: 'Wrong Number', color: 'red', description: 'Wrong number dialed' },
  { value: 'ABANDONED', label: 'Abandoned', color: 'orange', description: 'Customer hung up before answer' },
  { value: 'SALES_CLOSED', label: 'Sale Closed', color: 'green', description: 'Sale was completed' },
  { value: 'FOLLOW_UP', label: 'Follow-up Scheduled', color: 'blue', description: 'Follow-up appointment made' },
];

interface CallDispositionProps {
  visible: boolean;
  callInfo: { id: string; callerName: string; callerNumber: string; duration: number } | null;
  onClose: () => void;
  onSubmit: (data: { dispositionCode: string; notes: string; rating: number; callbackScheduled?: boolean }) => void;
}

export default function CallDisposition({ visible, callInfo, onClose, onSubmit }: CallDispositionProps) {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);
  const selectedCode = Form.useWatch('dispositionCode', form);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      onSubmit(values);
      message.success('Call disposition saved');
      form.resetFields();
      onClose();
    } catch (err: any) {
      if (err.errorFields) return;
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title={<Space><PhoneOutlined /> Call Disposition</Space>}
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={submitting}
      width={520}
    >
      {callInfo && (
        <div style={{ marginBottom: 16, padding: 12, background: '#f5f5f5', borderRadius: 8 }}>
          <Space direction="vertical" size={2}>
            <Text strong>{callInfo.callerName || 'Unknown'}</Text>
            <Text type="secondary">{callInfo.callerNumber}</Text>
            <Text type="secondary">Duration: {Math.floor(callInfo.duration / 60)}m {callInfo.duration % 60}s</Text>
          </Space>
        </div>
      )}
      <Form form={form} layout="vertical">
        <Form.Item name="dispositionCode" label="Disposition Code" rules={[{ required: true, message: 'Please select a disposition' }]}>
          <Select
            placeholder="Select call outcome"
            options={DISPOSITION_CODES.map(dc => ({
              value: dc.value,
              label: <Space><Tag color={dc.color}>{dc.label}</Tag><Text type="secondary">{dc.description}</Text></Space>,
            }))}
          />
        </Form.Item>

        {selectedCode && (
          <div style={{ marginBottom: 16 }}>
            <Tag color={DISPOSITION_CODES.find(d => d.value === selectedCode)?.color} style={{ fontSize: 14, padding: '4px 12px' }}>
              {DISPOSITION_CODES.find(d => d.value === selectedCode)?.description}
            </Tag>
          </div>
        )}

        <Form.Item name="rating" label="Customer Satisfaction">
          <Rate />
        </Form.Item>

        <Form.Item name="notes" label="Call Notes">
          <TextArea rows={4} placeholder="Enter call notes, summary, or follow-up actions..." />
        </Form.Item>

        <Divider />
        <Text type="secondary" style={{ fontSize: 12 }}>
          * Disposition codes help track call outcomes and improve reporting accuracy
        </Text>
      </Form>
    </Modal>
  );
}
