'use client';

import { useState, useEffect } from 'react';
import { Button, Space, Modal, Input, message, Tooltip, Popover, Tag, Typography } from 'antd';
import { SaveOutlined, DeleteOutlined, FilterOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface SavedFilter {
  id: string;
  name: string;
  values: Record<string, any>;
}

interface SavedFiltersProps {
  currentValues: Record<string, any>;
  onApply: (values: Record<string, any>) => void;
  storageKey?: string;
}

export default function SavedFilters({ currentValues, onApply, storageKey = 'vcall-saved-filters' }: SavedFiltersProps) {
  const [filters, setFilters] = useState<SavedFilter[]>([]);
  const [saveModalOpen, setSaveModalOpen] = useState(false);
  const [filterName, setFilterName] = useState('');

  useEffect(() => {
    try {
      const saved = localStorage.getItem(storageKey);
      if (saved) setFilters(JSON.parse(saved));
    } catch {}
  }, [storageKey]);

  const persistFilters = (newFilters: SavedFilter[]) => {
    setFilters(newFilters);
    localStorage.setItem(storageKey, JSON.stringify(newFilters));
  };

  const handleSave = () => {
    if (!filterName.trim()) {
      message.warning('Please enter a filter name');
      return;
    }
    const newFilter: SavedFilter = {
      id: Date.now().toString(),
      name: filterName.trim(),
      values: { ...currentValues },
    };
    persistFilters([...filters, newFilter]);
    setFilterName('');
    setSaveModalOpen(false);
    message.success(`Filter "${filterName}" saved`);
  };

  const handleApply = (filter: SavedFilter) => {
    onApply(filter.values);
  };

  const handleDelete = (id: string) => {
    persistFilters(filters.filter(f => f.id !== id));
    message.success('Filter deleted');
  };

  const filterContent = (
    <div style={{ width: 250 }}>
      {filters.length === 0 ? (
        <Text type="secondary" style={{ display: 'block', textAlign: 'center', padding: 16 }}>
          No saved filters yet
        </Text>
      ) : (
        filters.map((filter) => (
          <div key={filter.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '4px 0' }}>
            <Button type="link" onClick={() => handleApply(filter)} style={{ padding: 0 }}>
              <Tag color="blue">{filter.name}</Tag>
            </Button>
            <Tooltip title="Delete filter">
              <Button type="text" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(filter.id)} />
            </Tooltip>
          </div>
        ))
      )}
    </div>
  );

  return (
    <Space>
      <Popover content={filterContent} title="Saved Filters" trigger="click">
        <Button icon={<FilterOutlined />}>
          Saved Filters ({filters.length})
        </Button>
      </Popover>
      <Tooltip title="Save current search as filter">
        <Button icon={<SaveOutlined />} onClick={() => setSaveModalOpen(true)}>
          Save Search
        </Button>
      </Tooltip>
      <Modal
        title="Save Search Filter"
        open={saveModalOpen}
        onOk={handleSave}
        onCancel={() => setSaveModalOpen(false)}
        okText="Save"
      >
        <Input
          placeholder="Enter filter name (e.g., 'High Priority Tickets')"
          value={filterName}
          onChange={(e) => setFilterName(e.target.value)}
          onPressEnter={handleSave}
          autoFocus
        />
      </Modal>
    </Space>
  );
}
