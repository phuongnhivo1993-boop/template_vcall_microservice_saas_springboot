'use client';

import { useState, useCallback } from 'react';
import { Upload, Button, message, Typography, Space, Progress, List } from 'antd';
import { InboxOutlined, UploadOutlined, FileTextOutlined, CloseCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';
import type { UploadProps, UploadFile } from 'antd/es/upload/interface';

const { Dragger } = Upload;
const { Text } = Typography;

interface FileUploadProps {
  accept?: string;
  multiple?: boolean;
  maxSize?: number;
  maxFiles?: number;
  onFilesSelected?: (files: File[]) => void;
  uploadUrl?: string;
  title?: string;
  description?: string;
}

const ACCEPT_DESCRIPTIONS: Record<string, string> = {
  '.csv': 'CSV files',
  '.xlsx,.xls': 'Excel files',
  '.jpg,.jpeg,.png,.gif': 'Image files',
  '.pdf': 'PDF files',
  '.mp3,.wav': 'Audio files',
  '.mp4,.mov': 'Video files',
};

export default function FileUpload({
  accept = '.csv,.xlsx,.xls,.pdf,.jpg,.jpeg,.png',
  multiple = true,
  maxSize = 10,
  maxFiles = 10,
  onFilesSelected,
  uploadUrl,
  title = 'Kéo thả file vào đây',
  description,
}: FileUploadProps) {
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [uploading, setUploading] = useState(false);

  const acceptDescription = description || Object.entries(ACCEPT_DESCRIPTIONS)
    .filter(([key]) => accept.includes(key))
    .map(([, desc]) => desc)
    .join(', ') || 'files';

  const beforeUpload = useCallback((file: File) => {
    const isLt = file.size / 1024 / 1024 < maxSize;
    if (!isLt) {
      message.error(`File ${file.name} exceeds ${maxSize}MB limit`);
      return Upload.LIST_IGNORE;
    }

    const ext = '.' + file.name.split('.').pop()?.toLowerCase();
    const acceptedTypes = accept.split(',');
    if (!acceptedTypes.some(t => t.trim().toLowerCase() === ext || t.trim() === '*')) {
      message.error(`File type ${ext} is not supported`);
      return Upload.LIST_IGNORE;
    }

    return false;
  }, [accept, maxSize]);

  const handleChange: UploadProps['onChange'] = ({ fileList: newFileList }) => {
    setFileList(newFileList.slice(-maxFiles));
    if (onFilesSelected) {
      onFilesSelected(newFileList.filter(f => f.originFileObj).map(f => f.originFileObj as File));
    }
  };

  const handleUpload = async () => {
    if (!uploadUrl || fileList.length === 0) return;
    setUploading(true);
    const formData = new FormData();
    fileList.forEach(file => {
      if (file.originFileObj) {
        formData.append('files', file.originFileObj);
      }
    });
    try {
      const response = await fetch(uploadUrl, { method: 'POST', body: formData });
      if (response.ok) {
        message.success(`${fileList.length} file(s) uploaded successfully`);
        setFileList([]);
      } else {
        message.error('Upload failed');
      }
    } catch (error) {
      message.error('Upload failed: network error');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div>
      <Dragger
        multiple={multiple}
        beforeUpload={beforeUpload}
        onChange={handleChange}
        fileList={fileList}
        onRemove={(file) => {
          setFileList(prev => prev.filter(f => f.uid !== file.uid));
        }}
        accept={accept}
        showUploadList={false}
      >
        <p className="ant-upload-drag-icon"><InboxOutlined /></p>
        <p className="ant-upload-text">{title}</p>
        <p className="ant-upload-hint">
          Hỗ trợ: {acceptDescription}. Tối đa {maxFiles} files, mỗi file {maxSize}MB.
        </p>
      </Dragger>

      {fileList.length > 0 && (
        <List
          size="small"
          style={{ marginTop: 16 }}
          dataSource={fileList}
          renderItem={(file) => (
            <List.Item
              actions={[
                <Button key="remove" type="text" size="small" danger
                  icon={<CloseCircleOutlined />}
                  onClick={() => setFileList(prev => prev.filter(f => f.uid !== file.uid))}
                />,
              ]}
            >
              <List.Item.Meta
                avatar={<FileTextOutlined style={{ fontSize: 20, color: '#1890ff' }} />}
                title={file.name}
                description={`${(file.size || 0) / 1024 < 1024 ? ((file.size || 0) / 1024).toFixed(1) + ' KB' : ((file.size || 0) / 1024 / 1024).toFixed(1) + ' MB'}`}
              />
            </List.Item>
          )}
        />
      )}

      {uploadUrl && fileList.length > 0 && (
        <Button
          type="primary"
          icon={<UploadOutlined />}
          onClick={handleUpload}
          loading={uploading}
          style={{ marginTop: 16 }}
          block
        >
          {uploading ? 'Uploading...' : `Upload ${fileList.length} file(s)`}
        </Button>
      )}
    </div>
  );
}
