'use client';

import { Modal, Form, Spin, message } from 'antd';
import { ReactNode, useEffect, useState } from 'react';

interface CommonFormProps {
  open: boolean;
  title: string;
  onClose: () => void;
  onSubmit: (values: any) => Promise<void>;
  children: ReactNode;
  loading?: boolean;
  initialValues?: any;
  width?: number;
}

export default function CommonForm({
  open,
  title,
  onClose,
  onSubmit,
  children,
  loading = false,
  initialValues,
  width = 720,
}: CommonFormProps) {
  const [form] = Form.useForm();
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (open) {
      form.resetFields();
      if (initialValues) {
        form.setFieldsValue(initialValues);
      }
    }
  }, [open, initialValues, form]);

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      await onSubmit(values);
      message.success(initialValues ? 'Updated successfully' : 'Created successfully');
      onClose();
    } catch (error: any) {
      if (error?.errorFields) return;
      message.error(error?.message || 'Operation failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      title={title}
      open={open}
      onCancel={onClose}
      onOk={handleOk}
      confirmLoading={submitting}
      width={width}
      destroyOnClose
    >
      <Spin spinning={loading}>
        <Form form={form} layout="vertical" initialValues={initialValues}>
          {children}
        </Form>
      </Spin>
    </Modal>
  );
}
