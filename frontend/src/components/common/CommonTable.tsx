'use client';

import { Table, Card, Space, Button, Typography, Alert } from 'antd';
import { DownloadOutlined, FileExcelOutlined, ReloadOutlined, PlusOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { SorterResult } from 'antd/es/table/interface';
import { Can } from './Can';
import type { Permission } from '@/lib/permissions';

interface CommonTableProps<T> {
  columns: ColumnsType<T>;
  dataSource: T[];
  loading?: boolean;
  error?: string | null;
  rowKey?: string | ((record: T) => string);
  pagination?: TablePaginationConfig | false;
  onRefresh?: () => void;
  onExportCsv?: () => void;
  onExportExcel?: () => void;
  title?: string;
  extra?: React.ReactNode;
  onTableChange?: (pagination: TablePaginationConfig, filters: any, sorter: SorterResult<T> | SorterResult<T>[]) => void;
  rowSelection?: object;
  scroll?: { x?: number | string; y?: number | string };
  onCreateNew?: () => void;
  createLabel?: string;
  createPermission?: Permission;
}

export default function CommonTable<T extends object>({
  columns,
  dataSource,
  loading = false,
  error = null,
  rowKey = 'id',
  pagination,
  onRefresh,
  onExportCsv,
  onExportExcel,
  title,
  extra,
  onTableChange,
  rowSelection,
  scroll = { x: 'max-content' },
  onCreateNew,
  createLabel = 'Add New',
  createPermission,
}: CommonTableProps<T>) {
  if (error) {
    return (
      <Card>
        <Alert
          message="Error loading data"
          description={error}
          type="error"
          showIcon
          action={
            onRefresh && (
              <Button onClick={onRefresh} icon={<ReloadOutlined />} size="small">
                Retry
              </Button>
            )
          }
        />
      </Card>
    );
  }

  const createButton = onCreateNew ? (
    <Can I={createPermission!} fallback={null}>
      <Button type="primary" icon={<PlusOutlined />} onClick={onCreateNew}>
        {createLabel}
      </Button>
    </Can>
  ) : null;

  return (
    <Card
      title={title}
      extra={
        <Space>
          {createButton}
          {extra}
          {onExportCsv && (
            <Button icon={<DownloadOutlined />} onClick={onExportCsv}>
              Export CSV
            </Button>
          )}
          {onExportExcel && (
            <Button icon={<FileExcelOutlined />} onClick={onExportExcel}>
              Export Excel
            </Button>
          )}
          {onRefresh && (
            <Button icon={<ReloadOutlined />} onClick={onRefresh}>
              Refresh
            </Button>
          )}
        </Space>
      }
    >
      <Table<T>
        columns={columns}
        dataSource={dataSource}
        rowKey={rowKey}
        loading={loading}
        pagination={pagination !== false ? { showSizeChanger: true, showTotal: (total) => `Total ${total} items`, ...pagination } : false}
        onChange={onTableChange}
        rowSelection={rowSelection}
        scroll={scroll}
        locale={{
          emptyText: <Typography.Text type="secondary">No data available</Typography.Text>,
        }}
      />
    </Card>
  );
}
