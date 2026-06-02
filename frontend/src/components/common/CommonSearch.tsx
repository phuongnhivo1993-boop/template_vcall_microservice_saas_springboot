'use client';

import { Input, Select, DatePicker, Space, Button, Form } from 'antd';
import { SearchOutlined, ClearOutlined } from '@ant-design/icons';
import { ReactNode } from 'react';

const { RangePicker } = DatePicker;

interface FilterField {
  name: string;
  label: string;
  type: 'input' | 'select' | 'dateRange' | 'custom';
  placeholder?: string;
  options?: { value: string; label: string }[];
  render?: ReactNode;
}

interface CommonSearchProps {
  fields: FilterField[];
  onSearch: (values: any) => void;
  onReset: () => void;
  loading?: boolean;
}

export default function CommonSearch({ fields, onSearch, onReset, loading }: CommonSearchProps) {
  const [form] = Form.useForm();

  const handleSearch = () => {
    const values = form.getFieldsValue();
    onSearch(values);
  };

  const handleReset = () => {
    form.resetFields();
    onReset();
  };

  return (
    <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
      {fields.map((field) => (
        <Form.Item key={field.name} name={field.name}>
          {field.type === 'input' && (
            <Input placeholder={field.placeholder || `Search by ${field.label}`} style={{ width: 200 }} allowClear />
          )}
          {field.type === 'select' && (
            <Select
              placeholder={field.placeholder || `Filter by ${field.label}`}
              style={{ width: 160 }}
              allowClear
              options={field.options}
            />
          )}
          {field.type === 'dateRange' && (
            <RangePicker style={{ width: 260 }} />
          )}
          {field.type === 'custom' && field.render}
        </Form.Item>
      ))}
      <Form.Item>
        <Space>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch} loading={loading}>
            Search
          </Button>
          <Button icon={<ClearOutlined />} onClick={handleReset}>
            Reset
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
}
