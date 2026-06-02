'use client';

import { Button, Space, Typography, Breadcrumb } from 'antd';
import { ArrowLeftOutlined, PlusOutlined, DownloadOutlined, FileExcelOutlined } from '@ant-design/icons';
import { Can } from './Can';
import type { Permission } from '@/lib/permissions';

const { Title } = Typography;

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  onBack?: () => void;
  onCreateNew?: () => void;
  createLabel?: string;
  createPermission?: Permission;
  onExportCsv?: () => void;
  onExportExcel?: () => void;
  extra?: React.ReactNode;
  breadcrumb?: { title: string; href?: string }[];
}

export default function PageHeader({
  title, subtitle, onBack, onCreateNew, createLabel = 'Create New',
  createPermission, onExportCsv, onExportExcel, extra, breadcrumb,
}: PageHeaderProps) {
  return (
    <div style={{ marginBottom: 24 }}>
      {breadcrumb && breadcrumb.length > 0 && (
        <Breadcrumb style={{ marginBottom: 8 }} items={breadcrumb.map(b => ({ title: b.href ? <a href={b.href}>{b.title}</a> : b.title }))} />
      )}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <Space>
          {onBack && <Button icon={<ArrowLeftOutlined />} onClick={onBack}>Back</Button>}
          <div>
            <Title level={4} style={{ margin: 0 }}>{title}</Title>
            {subtitle && <span style={{ color: '#888', fontSize: 13 }}>{subtitle}</span>}
          </div>
        </Space>
        <Space>
          {extra}
          {onExportCsv && <Button icon={<DownloadOutlined />} onClick={onExportCsv}>Export CSV</Button>}
          {onExportExcel && <Button icon={<FileExcelOutlined />} onClick={onExportExcel}>Export Excel</Button>}
          {onCreateNew && (
            <Can I={createPermission!} fallback={null}>
              <Button type="primary" icon={<PlusOutlined />} onClick={onCreateNew}>{createLabel}</Button>
            </Can>
          )}
        </Space>
      </div>
    </div>
  );
}
