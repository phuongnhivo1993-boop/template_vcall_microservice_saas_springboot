'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Input, List, Typography, Space, Tag, Empty, Spin, Alert, Button, Row, Col, Breadcrumb, Divider, Rate, message, Select, Form, Modal } from 'antd';
import { SearchOutlined, BookOutlined, FolderOutlined, ClockCircleOutlined, EyeOutlined, LikeOutlined, PlusOutlined, EditOutlined, DeleteOutlined, CopyOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { Can } from '@/components/common/Can';
import { Permissions } from '@/lib/permissions';
import { knowledgeBaseApi } from '@/lib/api';
import dayjs from 'dayjs';
import type { TablePaginationConfig } from 'antd/es/table';

const { Title, Text, Paragraph } = Typography;

interface Article {
  id: string;
  title: string;
  content: string;
  category: string;
  tags: string[];
  author: string;
  views: number;
  likes: number;
  createdAt: string;
  updatedAt: string;
  status: 'published' | 'draft';
}

const CATEGORIES = [
  { key: 'all', label: 'All Categories' },
  { key: 'getting-started', label: 'Getting Started' },
  { key: 'account', label: 'Account & Billing' },
  { key: 'troubleshooting', label: 'Troubleshooting' },
  { key: 'features', label: 'Features Guide' },
  { key: 'faq', label: 'FAQ' },
];

export default function KnowledgeBasePage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [articles, setArticles] = useState<Article[]>([]);
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [editingArticle, setEditingArticle] = useState<Article | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<string[]>([]);
  const [filters, setFilters] = useState<Record<string, any>>({});
  const [pagination, setPagination] = useState<TablePaginationConfig>({
    current: 1, pageSize: 10, total: 0,
  });

  const loadArticles = useCallback(async (page = 1, size = 10, searchParams?: Record<string, any>) => {
    setLoading(true);
    setError(null);
    try {
      const res = await knowledgeBaseApi.list({ page: page - 1, size, ...searchParams });
      const data = res.data?.data || res.data;
      if (data.content) {
        setArticles(data.content);
        setPagination((prev) => ({
          ...prev,
          current: (data.page ?? page - 1) + 1,
          pageSize: data.size ?? size,
          total: data.totalElements ?? 0,
        }));
      } else if (Array.isArray(data)) {
        setArticles(data);
      } else {
        setArticles(data.content || []);
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to load articles');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadArticles(pagination.current, pagination.pageSize);
  }, [loadArticles]);

  const filteredArticles = articles;

  const handleSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') cleaned[key] = val;
    });
    setFilters(cleaned);
    setPagination((prev) => ({ ...prev, current: 1 }));
    loadArticles(1, pagination.pageSize, cleaned);
  };

  const handleReset = () => {
    setFilters({});
    setPagination((prev) => ({ ...prev, current: 1 }));
    loadArticles(1, pagination.pageSize);
  };

  const handleTableChange = (pag: TablePaginationConfig) => {
    setPagination((prev) => ({ ...prev, current: pag.current, pageSize: pag.pageSize }));
    loadArticles(pag.current, pag.pageSize, filters);
  };

  const handleSaveArticle = async (values: any) => {
    try {
      if (editingArticle?.id) {
        await knowledgeBaseApi.update(editingArticle.id, values);
        message.success('Article updated');
      } else {
        await knowledgeBaseApi.create(values);
        message.success('Article created');
      }
      setFormOpen(false);
      setEditingArticle(null);
      loadArticles(pagination.current, pagination.pageSize, filters);
    } catch (err: any) {
      message.error(err?.message || 'Failed to save article');
    }
  };

  const handleDuplicate = (record: Article) => {
    setEditingArticle({ ...record, id: '' } as Article);
    setFormOpen(true);
  };

  const handleBulkDelete = () => {
    Modal.confirm({
      title: 'Xóa nhiều bài viết',
      content: `Bạn có chắc chắn muốn xóa ${selectedRowKeys.length} bài viết đã chọn?`,
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: async () => {
        try {
          await knowledgeBaseApi.bulkDelete(selectedRowKeys);
          message.success(`Đã xóa ${selectedRowKeys.length} bài viết`);
          setSelectedRowKeys([]);
          loadArticles(pagination.current, pagination.pageSize, filters);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Xóa thất bại');
        }
      },
    });
  };

  const handleDeleteArticle = async (id: string) => {
    showDeleteConfirm({
      title: 'Delete Article',
      content: 'Are you sure you want to delete this article?',
      onOk: async () => {
        await knowledgeBaseApi.delete(id);
        message.success('Article deleted');
        loadArticles(pagination.current, pagination.pageSize, filters);
      },
    });
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" tip="Loading knowledge base..." /></div>;
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={() => loadArticles(pagination.current, pagination.pageSize)}>Retry</Button>} />;
  }

  if (selectedArticle) {
    return (
      <div>
        <Breadcrumb style={{ marginBottom: 16 }} items={[
          { title: <a onClick={() => setSelectedArticle(null)}>Knowledge Base</a> },
          { title: selectedArticle.title },
        ]} />
        <Card>
          <Title level={3}>{selectedArticle.title}</Title>
          <Space style={{ marginBottom: 16 }}>
            <Tag color="blue">{CATEGORIES.find(c => c.key === selectedArticle.category)?.label}</Tag>
            {selectedArticle.tags.map(tag => <Tag key={tag}>{tag}</Tag>)}
            <Text type="secondary"><ClockCircleOutlined /> Updated: {dayjs(selectedArticle.updatedAt).format('DD/MM/YYYY')}</Text>
            <Text type="secondary"><EyeOutlined /> {selectedArticle.views} views</Text>
            <Text type="secondary"><LikeOutlined /> {selectedArticle.likes} likes</Text>
          </Space>
          <Divider />
          <Paragraph style={{ whiteSpace: 'pre-wrap', fontSize: 15, lineHeight: 1.8 }}>
            {selectedArticle.content}
          </Paragraph>
          <Divider />
          <Space>
            <Button icon={<LikeOutlined />}>Was this helpful? ({selectedArticle.likes})</Button>
            <Rate disabled value={selectedArticle.likes > 10 ? 5 : selectedArticle.likes > 5 ? 4 : 3} />
          </Space>
        </Card>
      </div>
    );
  }

  const searchFields = [
    { name: 'title', label: 'Title', type: 'input' as const, placeholder: 'Search articles by title' },
    {
      name: 'category',
      label: 'Category',
      type: 'select' as const,
      placeholder: 'Filter by category',
      options: CATEGORIES.filter(c => c.key !== 'all').map(c => ({ value: c.key, label: c.label })),
    },
    {
      name: 'status',
      label: 'Status',
      type: 'select' as const,
      placeholder: 'Filter by status',
      options: [
        { value: 'published', label: 'Published' },
        { value: 'draft', label: 'Draft' },
      ],
    },
  ];

  const articlesColumns = [
    { title: 'Title', dataIndex: 'title', key: 'title', render: (t: string) => <a onClick={() => setSelectedArticle(articles.find(a => a.title === t) || null)}>{t}</a> },
    { title: 'Category', dataIndex: 'category', key: 'category', render: (c: string) => <Tag>{CATEGORIES.find(cat => cat.key === c)?.label || c}</Tag> },
    { title: 'Views', dataIndex: 'views', key: 'views' },
    { title: 'Likes', dataIndex: 'likes', key: 'likes' },
    { title: 'Updated', dataIndex: 'updatedAt', key: 'updatedAt', render: (d: string) => dayjs(d).format('DD/MM/YYYY') },
    { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={s === 'published' ? 'green' : 'orange'}>{s}</Tag> },
    {
      title: 'Actions', key: 'actions',
      render: (_: any, record: Article) => (
        <Space>
          <Button size="small" onClick={() => setSelectedArticle(record)}>View</Button>
          <Can I={Permissions.KNOWLEDGE_EDIT}>
            <Button size="small" icon={<EditOutlined />} onClick={() => { setEditingArticle(record); setFormOpen(true); }}>Edit</Button>
          </Can>
          <Button size="small" icon={<CopyOutlined />} onClick={() => handleDuplicate(record)}>Nhân bản</Button>
          <Can I={Permissions.KNOWLEDGE_DELETE}>
            <Button size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteArticle(record.id)}>Delete</Button>
          </Can>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} md={6}>
          <Card size="small" title="Categories">
            <List
              size="small"
              dataSource={CATEGORIES}
              renderItem={(cat) => (
                <List.Item
                  onClick={() => { setFilters((prev) => ({ ...prev, category: cat.key })); loadArticles(1, pagination.pageSize, { ...filters, category: cat.key }); }}
                  style={{ cursor: 'pointer', background: filters.category === cat.key || (!filters.category && cat.key === 'all') ? '#e6f7ff' : 'transparent', padding: '4px 8px', borderRadius: 4 }}
                >
                  <Space><FolderOutlined />{cat.label}</Space>
                  <Tag>{cat.key === 'all' ? articles.length : articles.filter(a => a.category === cat.key).length}</Tag>
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col xs={24} md={18}>
          <Card
            title={<Space><BookOutlined /> Knowledge Base</Space>}
            extra={
              <Can I={Permissions.KNOWLEDGE_CREATE}>
                <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingArticle(null); setFormOpen(true); }}>New Article</Button>
              </Can>
            }
          >
            <CommonSearch
              fields={searchFields}
              onSearch={handleSearch}
              onReset={handleReset}
              loading={loading}
              initialValues={filters}
            />
            <SavedFilters currentValues={filters} onApply={(v) => { setFilters(v); loadArticles(1, pagination.pageSize, v); }} storageKey="vcall-saved-filters-kb" />
            {selectedRowKeys.length > 0 && (
              <Button danger onClick={handleBulkDelete} style={{ marginBottom: 16 }}>
                Xóa đã chọn ({selectedRowKeys.length})
              </Button>
            )}
            <CommonTable
              rowSelection={{ selectedRowKeys, onChange: (keys: React.Key[]) => setSelectedRowKeys(keys as string[]) }}
              columns={articlesColumns}
              dataSource={filteredArticles}
              rowKey="id"
              loading={loading}
              pagination={pagination}
              onRefresh={() => { setSelectedRowKeys([]); loadArticles(pagination.current, pagination.pageSize, filters); }}
              onTableChange={handleTableChange}
            />
          </Card>
        </Col>
      </Row>

      <CommonForm
        open={formOpen}
        title={editingArticle?.id ? 'Edit Article' : 'New Article'}
        onClose={() => { setFormOpen(false); setEditingArticle(null); }}
        onSubmit={handleSaveArticle}
        initialValues={editingArticle || { title: '', content: '', category: 'faq', tags: [] }}
      >
        <Form.Item name="title" label="Title *" rules={[{ required: true, message: 'Please enter title' }]}>
          <Input placeholder="Article title" />
        </Form.Item>
        <Form.Item name="category" label="Category *" rules={[{ required: true, message: 'Please select category' }]}>
          <Select options={CATEGORIES.filter(c => c.key !== 'all').map(c => ({ label: c.label, value: c.key }))} />
        </Form.Item>
        <Form.Item name="content" label="Content *" rules={[{ required: true, message: 'Please enter content' }]}>
          <Input.TextArea rows={8} placeholder="Article content..." />
        </Form.Item>
        <Form.Item name="tags" label="Tags (comma separated)">
          <Input placeholder="tag1, tag2, tag3" />
        </Form.Item>
      </CommonForm>
    </div>
  );
}
