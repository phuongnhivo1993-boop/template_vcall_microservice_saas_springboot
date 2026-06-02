'use client';

import { useState, useEffect, useCallback } from 'react';
import { Card, Input, List, Typography, Space, Tag, Empty, Spin, Alert, Button, Row, Col, Breadcrumb, Divider, Rate, message } from 'antd';
import { SearchOutlined, BookOutlined, FolderOutlined, ClockCircleOutlined, EyeOutlined, LikeOutlined, PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { Can } from '@/components/common/Can';
import { Permissions } from '@/lib/permissions';

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

const MOCK_ARTICLES: Article[] = [
  { id: '1', title: 'Hướng dẫn đăng nhập hệ thống lần đầu', content: 'Chi tiết các bước đăng nhập hệ thống VCall Contact Center lần đầu tiên...', category: 'getting-started', tags: ['login', 'setup'], author: 'Admin', views: 245, likes: 12, createdAt: '2026-05-01', updatedAt: '2026-05-15', status: 'published' },
  { id: '2', title: 'Cách tạo ticket hỗ trợ khách hàng', content: 'Hướng dẫn chi tiết cách tạo và quản lý ticket hỗ trợ...', category: 'features', tags: ['ticket', 'support'], author: 'Admin', views: 189, likes: 8, createdAt: '2026-05-03', updatedAt: '2026-05-10', status: 'published' },
  { id: '3', title: 'Khắc phục lỗi không nghe được âm thanh cuộc gọi', content: 'Các bước kiểm tra và khắc phục lỗi âm thanh khi thực hiện cuộc gọi...', category: 'troubleshooting', tags: ['audio', 'call'], author: 'Tech Support', views: 312, likes: 25, createdAt: '2026-04-20', updatedAt: '2026-05-18', status: 'published' },
  { id: '4', title: 'Bảng giá dịch vụ và cách thanh toán', content: 'Chi tiết các gói dịch vụ và phương thức thanh toán...', category: 'account', tags: ['pricing', 'payment'], author: 'Finance', views: 156, likes: 5, createdAt: '2026-04-15', updatedAt: '2026-05-01', status: 'published' },
  { id: '5', title: 'Hướng dẫn sử dụng tính năng chuyển cuộc gọi', content: 'Cách sử dụng tính năng chuyển cuộc gọi giữa các agent...', category: 'features', tags: ['call', 'transfer'], author: 'Admin', views: 98, likes: 3, createdAt: '2026-05-10', updatedAt: '2026-05-12', status: 'published' },
  { id: '6', title: 'Câu hỏi thường gặp về gói cước', content: 'Các câu hỏi thường gặp về gói cước và dịch vụ...', category: 'faq', tags: ['faq', 'pricing'], author: 'Admin', views: 67, likes: 2, createdAt: '2026-05-05', updatedAt: '2026-05-08', status: 'published' },
];

export default function KnowledgeBasePage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchText, setSearchText] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [articles, setArticles] = useState<Article[]>([]);
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [editingArticle, setEditingArticle] = useState<Article | null>(null);

  useEffect(() => {
    loadArticles();
  }, []);

  const loadArticles = useCallback(() => {
    setLoading(true);
    setError(null);
    try {
      setArticles(MOCK_ARTICLES);
    } catch (err) {
      setError('Failed to load articles');
    } finally {
      setLoading(false);
    }
  }, []);

  const filteredArticles = articles.filter(article => {
    if (selectedCategory !== 'all' && article.category !== selectedCategory) return false;
    if (searchText) {
      const q = searchText.toLowerCase();
      return article.title.toLowerCase().includes(q) ||
             article.content.toLowerCase().includes(q) ||
             article.tags.some(t => t.toLowerCase().includes(q));
    }
    return true;
  });

  const handleSaveArticle = async (values: any) => {
    if (editingArticle) {
      setArticles(prev => prev.map(a => a.id === editingArticle.id ? { ...a, ...values } : a));
      message.success('Article updated');
    } else {
      const newArticle: Article = {
        id: Date.now().toString(),
        ...values,
        author: 'Admin',
        views: 0,
        likes: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        status: 'published',
      };
      setArticles(prev => [newArticle, ...prev]);
      message.success('Article created');
    }
    setFormOpen(false);
    setEditingArticle(null);
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" tip="Loading knowledge base..." /></div>;
  }

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon action={<Button onClick={loadArticles}>Retry</Button>} />;
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
            <Text type="secondary"><ClockCircleOutlined /> Updated: {new Date(selectedArticle.updatedAt).toLocaleDateString('vi-VN')}</Text>
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

  const articlesColumns = [
    { title: 'Title', dataIndex: 'title', key: 'title', render: (t: string) => <a onClick={() => setSelectedArticle(articles.find(a => a.title === t) || null)}>{t}</a> },
    { title: 'Category', dataIndex: 'category', key: 'category', render: (c: string) => <Tag>{CATEGORIES.find(cat => cat.key === c)?.label || c}</Tag> },
    { title: 'Views', dataIndex: 'views', key: 'views' },
    { title: 'Likes', dataIndex: 'likes', key: 'likes' },
    { title: 'Updated', dataIndex: 'updatedAt', key: 'updatedAt', render: (d: string) => new Date(d).toLocaleDateString('vi-VN') },
    { title: 'Status', dataIndex: 'status', key: 'status', render: (s: string) => <Tag color={s === 'published' ? 'green' : 'orange'}>{s}</Tag> },
    {
      title: 'Actions', key: 'actions',
      render: (_: any, record: Article) => (
        <Space>
          <Button size="small" onClick={() => setSelectedArticle(record)}>View</Button>
          <Can I={Permissions.KNOWLEDGE_EDIT}>
            <Button size="small" icon={<EditOutlined />} onClick={() => { setEditingArticle(record); setFormOpen(true); }}>Edit</Button>
          </Can>
          <Can I={Permissions.KNOWLEDGE_DELETE}>
            <Button size="small" danger icon={<DeleteOutlined />} onClick={() => showDeleteConfirm({ onOk: async () => { setArticles(prev => prev.filter(a => a.id !== record.id)); message.success('Deleted'); } })}>Delete</Button>
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
                  onClick={() => setSelectedCategory(cat.key)}
                  style={{ cursor: 'pointer', background: selectedCategory === cat.key ? '#e6f7ff' : 'transparent', padding: '4px 8px', borderRadius: 4 }}
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
              <Space>
                <Input.Search
                  placeholder="Search articles..."
                  value={searchText}
                  onChange={(e) => setSearchText(e.target.value)}
                  style={{ width: 300 }}
                  allowClear
                />
                <Can I={Permissions.KNOWLEDGE_CREATE}>
                  <Button type="primary" icon={<PlusOutlined />} onClick={() => { setEditingArticle(null); setFormOpen(true); }}>New Article</Button>
                </Can>
              </Space>
            }
          >
            <CommonTable
              columns={articlesColumns}
              dataSource={filteredArticles}
              rowKey="id"
              loading={false}
              pagination={{ pageSize: 10 }}
            />
          </Card>
        </Col>
      </Row>

      <CommonForm
        open={formOpen}
        title={editingArticle ? 'Edit Article' : 'New Article'}
        onClose={() => { setFormOpen(false); setEditingArticle(null); }}
        onSubmit={handleSaveArticle}
        initialValues={editingArticle || { title: '', content: '', category: 'faq', tags: [] }}
      >
        <div style={{ padding: 8 }}>
          <div style={{ marginBottom: 16 }}>
            <label>Title *</label>
            <Input name="title" placeholder="Article title" />
          </div>
          <div style={{ marginBottom: 16 }}>
            <label>Category *</label>
            <select name="category" style={{ width: '100%', padding: 8, border: '1px solid #d9d9d9', borderRadius: 6 }}>
              {CATEGORIES.filter(c => c.key !== 'all').map(c => <option key={c.key} value={c.key}>{c.label}</option>)}
            </select>
          </div>
          <div style={{ marginBottom: 16 }}>
            <label>Content *</label>
            <textarea name="content" rows={8} style={{ width: '100%', padding: 8, border: '1px solid #d9d9d9', borderRadius: 6 }} placeholder="Article content..." />
          </div>
          <div style={{ marginBottom: 16 }}>
            <label>Tags (comma separated)</label>
            <Input name="tags" placeholder="tag1, tag2, tag3" />
          </div>
        </div>
      </CommonForm>
    </div>
  );
}
