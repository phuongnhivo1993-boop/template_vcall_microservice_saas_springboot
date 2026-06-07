'use client';

import { useState, useEffect, useCallback, useMemo } from 'react';
import { useUrlState } from '@/lib/hooks/useUrlState';
import { Button, Tag, Space, Typography, Form, Input, Select, Switch, message, Modal, Tabs, Table, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { SorterResult } from 'antd/es/table/interface';
import dayjs from 'dayjs';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { Can } from '@/components/common/Can';
import { Permissions } from '@/lib/permissions';
import { surveyApi } from '@/lib/api';

const { Title } = Typography;

interface Survey {
  id: string;
  title: string;
  description: string;
  type: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface SurveyTemplate {
  id: string;
  name: string;
  description: string;
  trigger: string;
  surveyIds: string;
  isActive: boolean;
  createdAt: string;
}

interface SurveyQuestion {
  id: string;
  surveyId: string;
  questionText: string;
  questionType: string;
  orderIndex: number;
  isRequired: boolean;
  options: string;
}

interface SurveyAnswer {
  id: string;
  surveyId: string;
  questionId: string;
  customerId: string;
  callId: string;
  ticketId: string;
  answer: string;
  rating: number;
  submittedAt: string;
}

const surveyTypeOptions = [
  { value: 'CSAT', label: 'CSAT' },
  { value: 'NPS', label: 'NPS' },
  { value: 'CUSTOM', label: 'CUSTOM' },
];

const triggerOptions = [
  { value: 'POST_CALL', label: 'Post Call' },
  { value: 'POST_TICKET', label: 'Post Ticket' },
  { value: 'POST_CHAT', label: 'Post Chat' },
  { value: 'SCHEDULED', label: 'Scheduled' },
];

const questionTypeOptions = [
  { value: 'TEXT', label: 'Text' },
  { value: 'RATING', label: 'Rating' },
  { value: 'SINGLE_CHOICE', label: 'Single Choice' },
  { value: 'MULTIPLE_CHOICE', label: 'Multiple Choice' },
  { value: 'CSAT', label: 'CSAT' },
  { value: 'NPS', label: 'NPS' },
];

const typeColors: Record<string, string> = {
  CSAT: 'blue',
  NPS: 'purple',
  CUSTOM: 'green',
};

const triggerColors: Record<string, string> = {
  POST_CALL: 'blue',
  POST_TICKET: 'orange',
  POST_CHAT: 'cyan',
  SCHEDULED: 'purple',
};

export default function SurveysPage() {
  const [activeTab, setActiveTab] = useState('surveys');

  const [surveys, setSurveys] = useState<Survey[]>([]);
  const [surveyLoading, setSurveyLoading] = useState(false);
  const [surveyError, setSurveyError] = useState<string | null>(null);
  const [surveyUrlParams, setSurveyUrlParams] = useUrlState({
    title: '', type: '', page: '1', pageSize: '10',
  });
  const [surveyPagination, setSurveyPagination] = useState<TablePaginationConfig>({
    current: parseInt(surveyUrlParams.page),
    pageSize: parseInt(surveyUrlParams.pageSize),
    total: 0,
  });
  const [surveyModalOpen, setSurveyModalOpen] = useState(false);
  const [editingSurvey, setEditingSurvey] = useState<Survey | null>(null);
  const [selectedSurveyKeys, setSelectedSurveyKeys] = useState<string[]>([]);

  const [templates, setTemplates] = useState<SurveyTemplate[]>([]);
  const [templateLoading, setTemplateLoading] = useState(false);
  const [templateError, setTemplateError] = useState<string | null>(null);
  const [templateUrlParams, setTemplateUrlParams] = useUrlState({
    name: '', trigger: '', page: '1', pageSize: '10',
  });
  const [templatePagination, setTemplatePagination] = useState<TablePaginationConfig>({
    current: parseInt(templateUrlParams.page),
    pageSize: parseInt(templateUrlParams.pageSize),
    total: 0,
  });
  const [templateModalOpen, setTemplateModalOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<SurveyTemplate | null>(null);
  const [selectedTemplateKeys, setSelectedTemplateKeys] = useState<string[]>([]);

  const [responses, setResponses] = useState<SurveyAnswer[]>([]);
  const [responseLoading, setResponseLoading] = useState(false);
  const [responsePagination, setResponsePagination] = useState<TablePaginationConfig>({
    current: 1, pageSize: 10, total: 0,
  });

  const [questions, setQuestions] = useState<SurveyQuestion[]>([]);
  const [questionModalOpen, setQuestionModalOpen] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState<SurveyQuestion | null>(null);
  const [questionSurveyId, setQuestionSurveyId] = useState<string>('');

  const fetchSurveys = useCallback(async (page = 1, size = 10, params?: Record<string, any>) => {
    setSurveyLoading(true);
    setSurveyError(null);
    try {
      const res = await surveyApi.list({ page: page - 1, size, ...params });
      const data = res.data;
      if (data.content) {
        setSurveys(data.content);
        setSurveyPagination((prev) => ({
          ...prev,
          current: data.page + 1,
          pageSize: data.size,
          total: data.totalElements,
        }));
      } else if (Array.isArray(data)) {
        setSurveys(data);
      } else if (data.data) {
        setSurveys(Array.isArray(data.data) ? data.data : []);
      }
    } catch (err: any) {
      setSurveyError(err?.response?.data?.message || err?.message || 'Failed to load surveys');
      setSurveys([]);
    } finally {
      setSurveyLoading(false);
    }
  }, []);

  const fetchTemplates = useCallback(async (page = 1, size = 10, params?: Record<string, any>) => {
    setTemplateLoading(true);
    setTemplateError(null);
    try {
      const res = await surveyApi.getTemplates({ page: page - 1, size, ...params });
      const data = res.data;
      if (data.content) {
        setTemplates(data.content);
        setTemplatePagination((prev) => ({
          ...prev,
          current: data.page + 1,
          pageSize: data.size,
          total: data.totalElements,
        }));
      } else if (Array.isArray(data)) {
        setTemplates(data);
      } else if (data.data) {
        setTemplates(Array.isArray(data.data) ? data.data : []);
      }
    } catch (err: any) {
      setTemplateError(err?.response?.data?.message || err?.message || 'Failed to load templates');
      setTemplates([]);
    } finally {
      setTemplateLoading(false);
    }
  }, []);

  const fetchResponses = useCallback(async (page = 1, size = 10) => {
    setResponseLoading(true);
    try {
      const res = await surveyApi.getResponses({ page: page - 1, size });
      const data = res.data;
      if (data.content) {
        setResponses(data.content);
        setResponsePagination((prev) => ({
          ...prev,
          current: data.page + 1,
          pageSize: data.size,
          total: data.totalElements,
        }));
      } else if (Array.isArray(data)) {
        setResponses(data);
      } else if (data.data) {
        setResponses(Array.isArray(data.data) ? data.data : []);
      }
    } catch {
      setResponses([]);
    } finally {
      setResponseLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'surveys') {
      fetchSurveys(parseInt(surveyUrlParams.page), parseInt(surveyUrlParams.pageSize));
    } else if (activeTab === 'templates') {
      fetchTemplates(parseInt(templateUrlParams.page), parseInt(templateUrlParams.pageSize));
    } else if (activeTab === 'responses') {
      fetchResponses(1, 10);
    }
  }, [activeTab]);

  const handleSurveySearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        cleaned[key] = val;
      }
    });
    setSurveyUrlParams({ title: cleaned.title || '', type: cleaned.type || '', page: '1', pageSize: surveyUrlParams.pageSize });
    fetchSurveys(1, surveyPagination.pageSize, cleaned);
  };

  const handleSurveyReset = () => {
    setSurveyUrlParams({ title: '', type: '', page: '1', pageSize: surveyUrlParams.pageSize });
    fetchSurveys(1, surveyPagination.pageSize);
  };

  const handleSurveyTableChange = (pag: TablePaginationConfig) => {
    setSurveyUrlParams({ page: String(pag.current), pageSize: String(pag.pageSize) });
    fetchSurveys(pag.current, pag.pageSize);
  };

  const handleTemplateSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        cleaned[key] = val;
      }
    });
    setTemplateUrlParams({ name: cleaned.name || '', trigger: cleaned.trigger || '', page: '1', pageSize: templateUrlParams.pageSize });
    fetchTemplates(1, templatePagination.pageSize, cleaned);
  };

  const handleTemplateReset = () => {
    setTemplateUrlParams({ name: '', trigger: '', page: '1', pageSize: templateUrlParams.pageSize });
    fetchTemplates(1, templatePagination.pageSize);
  };

  const handleTemplateTableChange = (pag: TablePaginationConfig) => {
    setTemplateUrlParams({ page: String(pag.current), pageSize: String(pag.pageSize) });
    fetchTemplates(pag.current, pag.pageSize);
  };

  const handleDeleteSurvey = (survey: Survey) => {
    showDeleteConfirm({
      title: 'Delete Survey',
      content: `Are you sure you want to delete survey "${survey.title}"? This action cannot be undone.`,
      onOk: async () => {
        await surveyApi.delete(survey.id);
        fetchSurveys(surveyPagination.current, surveyPagination.pageSize);
      },
    });
  };

  const handleDeleteTemplate = (template: SurveyTemplate) => {
    showDeleteConfirm({
      title: 'Delete Template',
      content: `Are you sure you want to delete template "${template.name}"? This action cannot be undone.`,
      onOk: async () => {
        await surveyApi.deleteTemplate(template.id);
        fetchTemplates(templatePagination.current, templatePagination.pageSize);
      },
    });
  };

  const handleBulkDeleteSurveys = () => {
    Modal.confirm({
      title: 'Bulk Delete Surveys',
      content: `Are you sure you want to delete ${selectedSurveyKeys.length} selected surveys?`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await surveyApi.bulkDelete(selectedSurveyKeys);
          message.success(`Deleted ${selectedSurveyKeys.length} surveys`);
          setSelectedSurveyKeys([]);
          fetchSurveys(surveyPagination.current, surveyPagination.pageSize);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Delete failed');
        }
      },
    });
  };

  const handleBulkDeleteTemplates = () => {
    Modal.confirm({
      title: 'Bulk Delete Templates',
      content: `Are you sure you want to delete ${selectedTemplateKeys.length} selected templates?`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await surveyApi.bulkDeleteTemplates(selectedTemplateKeys);
          message.success(`Deleted ${selectedTemplateKeys.length} templates`);
          setSelectedTemplateKeys([]);
          fetchTemplates(templatePagination.current, templatePagination.pageSize);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Delete failed');
        }
      },
    });
  };

  const handleSurveyFormSubmit = async (values: any) => {
    if (editingSurvey?.id) {
      await surveyApi.update(editingSurvey.id, values);
    } else {
      await surveyApi.create(values);
    }
    fetchSurveys(surveyPagination.current, surveyPagination.pageSize);
  };

  const handleTemplateFormSubmit = async (values: any) => {
    if (editingTemplate?.id) {
      await surveyApi.updateTemplate(editingTemplate.id, values);
    } else {
      await surveyApi.createTemplate(values);
    }
    fetchTemplates(templatePagination.current, templatePagination.pageSize);
  };

  const handleQuestionFormSubmit = async (values: any) => {
    if (editingQuestion?.id) {
      await surveyApi.updateQuestion(editingQuestion.id, values);
    } else {
      await surveyApi.createQuestion({ ...values, surveyId: questionSurveyId });
    }
    fetchQuestions(questionSurveyId);
  };

  const fetchQuestions = async (surveyId: string) => {
    try {
      const res = await surveyApi.getQuestions(surveyId);
      const data = res.data;
      if (Array.isArray(data)) {
        setQuestions(data);
      } else if (data.data) {
        setQuestions(Array.isArray(data.data) ? data.data : []);
      } else {
        setQuestions([]);
      }
    } catch {
      setQuestions([]);
    }
  };

  const handleExportCsv = async () => {
    try {
      const res = await surveyApi.exportCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `surveys_${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Surveys exported');
    } catch {
      message.error('Export failed');
    }
  };

  const handleExportExcel = async () => {
    try {
      const res = await surveyApi.exportExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `surveys_${new Date().toISOString().slice(0, 10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Surveys exported');
    } catch {
      message.error('Export failed');
    }
  };

  const surveyColumns: ColumnsType<Survey> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      render: (id: string) => <span style={{ fontWeight: 500 }}>{id.slice(0, 8)}...</span>,
    },
    { title: 'Title', dataIndex: 'title', key: 'title' },
    { title: 'Description', dataIndex: 'description', key: 'description', ellipsis: true },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => <Tag color={typeColors[type] || 'default'}>{type}</Tag>,
    },
    {
      title: 'Active',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (active: boolean) => <Tag color={active ? 'green' : 'default'}>{active ? 'Yes' : 'No'}</Tag>,
    },
    { title: 'Created', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '' },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Survey) => (
        <Space>
          <Can I={Permissions.SURVEY_EDIT}>
            <Button type="link" icon={<EditOutlined />} onClick={() => { setEditingSurvey(record); setSurveyModalOpen(true); }}>
              Edit
            </Button>
          </Can>
          <Button type="link" onClick={() => { setQuestionSurveyId(record.id); fetchQuestions(record.id); setQuestionModalOpen(true); }}>
            Questions
          </Button>
          <Can I={Permissions.SURVEY_DELETE}>
            <Popconfirm title="Are you sure?" onConfirm={() => handleDeleteSurvey(record)}>
              <Button type="link" danger icon={<DeleteOutlined />}>Delete</Button>
            </Popconfirm>
          </Can>
        </Space>
      ),
    },
  ];

  const templateColumns: ColumnsType<SurveyTemplate> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      render: (id: string) => <span style={{ fontWeight: 500 }}>{id.slice(0, 8)}...</span>,
    },
    { title: 'Name', dataIndex: 'name', key: 'name' },
    { title: 'Description', dataIndex: 'description', key: 'description', ellipsis: true },
    {
      title: 'Trigger',
      dataIndex: 'trigger',
      key: 'trigger',
      render: (trigger: string) => <Tag color={triggerColors[trigger] || 'default'}>{trigger}</Tag>,
    },
    { title: 'Survey IDs', dataIndex: 'surveyIds', key: 'surveyIds', ellipsis: true },
    {
      title: 'Active',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (active: boolean) => <Tag color={active ? 'green' : 'default'}>{active ? 'Yes' : 'No'}</Tag>,
    },
    { title: 'Created', dataIndex: 'createdAt', key: 'createdAt', render: (v: string) => v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '' },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: SurveyTemplate) => (
        <Space>
          <Can I={Permissions.SURVEY_EDIT}>
            <Button type="link" icon={<EditOutlined />} onClick={() => { setEditingTemplate(record); setTemplateModalOpen(true); }}>
              Edit
            </Button>
          </Can>
          <Can I={Permissions.SURVEY_DELETE}>
            <Popconfirm title="Are you sure?" onConfirm={() => handleDeleteTemplate(record)}>
              <Button type="link" danger icon={<DeleteOutlined />}>Delete</Button>
            </Popconfirm>
          </Can>
        </Space>
      ),
    },
  ];

  const responseColumns: ColumnsType<SurveyAnswer> = [
    { title: 'ID', dataIndex: 'id', key: 'id', render: (id: string) => <span style={{ fontWeight: 500 }}>{id.slice(0, 8)}...</span> },
    { title: 'Survey ID', dataIndex: 'surveyId', key: 'surveyId', render: (v: string) => v?.slice(0, 8) + '...' },
    { title: 'Question ID', dataIndex: 'questionId', key: 'questionId', render: (v: string) => v?.slice(0, 8) + '...' },
    { title: 'Customer ID', dataIndex: 'customerId', key: 'customerId', render: (v: string) => v?.slice(0, 8) + '...' },
    { title: 'Call ID', dataIndex: 'callId', key: 'callId', render: (v: string) => v?.slice(0, 8) + '...' },
    { title: 'Ticket ID', dataIndex: 'ticketId', key: 'ticketId', render: (v: string) => v?.slice(0, 8) + '...' },
    { title: 'Answer', dataIndex: 'answer', key: 'answer', ellipsis: true },
    {
      title: 'Rating',
      dataIndex: 'rating',
      key: 'rating',
      render: (r: number) => r !== null && r !== undefined ? <Tag color="blue">{r}/10</Tag> : '-',
    },
    { title: 'Submitted At', dataIndex: 'submittedAt', key: 'submittedAt', render: (v: string) => v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '' },
  ];

  const questionColumns: ColumnsType<SurveyQuestion> = [
    { title: 'Order', dataIndex: 'orderIndex', key: 'orderIndex', width: 80 },
    { title: 'Question', dataIndex: 'questionText', key: 'questionText' },
    { title: 'Type', dataIndex: 'questionType', key: 'questionType', render: (t: string) => <Tag>{t}</Tag> },
    {
      title: 'Required',
      dataIndex: 'isRequired',
      key: 'isRequired',
      render: (r: boolean) => <Tag color={r ? 'red' : 'default'}>{r ? 'Yes' : 'No'}</Tag>,
    },
    { title: 'Options', dataIndex: 'options', key: 'options', ellipsis: true },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: SurveyQuestion) => (
        <Space>
          <Can I={Permissions.SURVEY_EDIT}>
            <Button type="link" icon={<EditOutlined />} onClick={() => { setEditingQuestion(record); setQuestionModalOpen(true); }}>
              Edit
            </Button>
          </Can>
          <Can I={Permissions.SURVEY_DELETE}>
            <Popconfirm title="Are you sure?" onConfirm={async () => { await surveyApi.deleteQuestion(record.id); fetchQuestions(questionSurveyId); }}>
              <Button type="link" danger icon={<DeleteOutlined />}>Delete</Button>
            </Popconfirm>
          </Can>
        </Space>
      ),
    },
  ];

  const surveySearchFields = [
    { name: 'title', label: 'Title', type: 'input' as const, placeholder: 'Search by title' },
    { name: 'type', label: 'Type', type: 'select' as const, placeholder: 'Filter by type', options: surveyTypeOptions },
  ];

  const templateSearchFields = [
    { name: 'name', label: 'Name', type: 'input' as const, placeholder: 'Search by name' },
    { name: 'trigger', label: 'Trigger', type: 'select' as const, placeholder: 'Filter by trigger', options: triggerOptions },
  ];

  const tabItems = [
    {
      key: 'surveys',
      label: 'Surveys',
      children: (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
            <CommonSearch
              fields={surveySearchFields}
              onSearch={handleSurveySearch}
              onReset={handleSurveyReset}
              loading={surveyLoading}
              initialValues={{ title: surveyUrlParams.title, type: surveyUrlParams.type }}
            />
          </div>
          {selectedSurveyKeys.length > 0 && (
            <Button danger onClick={handleBulkDeleteSurveys} style={{ marginBottom: 16 }}>
              Delete Selected ({selectedSurveyKeys.length})
            </Button>
          )}
          <CommonTable<Survey>
            rowSelection={{ selectedRowKeys: selectedSurveyKeys, onChange: (keys: React.Key[]) => setSelectedSurveyKeys(keys as string[]) }}
            columns={surveyColumns}
            dataSource={surveys}
            loading={surveyLoading}
            error={surveyError}
            rowKey="id"
            pagination={surveyPagination}
            onRefresh={() => { setSelectedSurveyKeys([]); fetchSurveys(surveyPagination.current, surveyPagination.pageSize); }}
            onExportCsv={handleExportCsv}
            onExportExcel={handleExportExcel}
            onTableChange={handleSurveyTableChange}
          />
        </div>
      ),
    },
    {
      key: 'templates',
      label: 'Templates',
      children: (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
            <CommonSearch
              fields={templateSearchFields}
              onSearch={handleTemplateSearch}
              onReset={handleTemplateReset}
              loading={templateLoading}
              initialValues={{ name: templateUrlParams.name, trigger: templateUrlParams.trigger }}
            />
          </div>
          {selectedTemplateKeys.length > 0 && (
            <Button danger onClick={handleBulkDeleteTemplates} style={{ marginBottom: 16 }}>
              Delete Selected ({selectedTemplateKeys.length})
            </Button>
          )}
          <CommonTable<SurveyTemplate>
            rowSelection={{ selectedRowKeys: selectedTemplateKeys, onChange: (keys: React.Key[]) => setSelectedTemplateKeys(keys as string[]) }}
            columns={templateColumns}
            dataSource={templates}
            loading={templateLoading}
            error={templateError}
            rowKey="id"
            pagination={templatePagination}
            onRefresh={() => { setSelectedTemplateKeys([]); fetchTemplates(templatePagination.current, templatePagination.pageSize); }}
            onTableChange={handleTemplateTableChange}
          />
        </div>
      ),
    },
    {
      key: 'responses',
      label: 'Responses',
      children: (
        <div>
          <CommonTable<SurveyAnswer>
            columns={responseColumns}
            dataSource={responses}
            loading={responseLoading}
            rowKey="id"
            pagination={responsePagination}
            onRefresh={() => fetchResponses(responsePagination.current, responsePagination.pageSize)}
            onExportCsv={async () => {
              try {
                const res = await surveyApi.exportResponsesCsv();
                const blob = new Blob([res.data], { type: 'text/csv' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `survey_responses_${new Date().toISOString().slice(0, 10)}.csv`;
                a.click();
                URL.revokeObjectURL(url);
                message.success('Responses exported');
              } catch {
                message.error('Export failed');
              }
            }}
            onExportExcel={async () => {
              try {
                const res = await surveyApi.exportResponsesExcel();
                const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `survey_responses_${new Date().toISOString().slice(0, 10)}.xlsx`;
                a.click();
                URL.revokeObjectURL(url);
                message.success('Responses exported');
              } catch {
                message.error('Export failed');
              }
            }}
            onTableChange={(pag) => {
              setResponsePagination(pag);
              fetchResponses(pag.current, pag.pageSize);
            }}
          />
        </div>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>Surveys</Title>
        <Space>
          <Can I={Permissions.SURVEY_CREATE}>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => {
                if (activeTab === 'surveys') {
                  setEditingSurvey(null);
                  setSurveyModalOpen(true);
                } else if (activeTab === 'templates') {
                  setEditingTemplate(null);
                  setTemplateModalOpen(true);
                }
              }}
            >
              Create {activeTab === 'surveys' ? 'Survey' : 'Template'}
            </Button>
          </Can>
        </Space>
      </div>

      <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />

      <CommonForm
        open={surveyModalOpen}
        title={editingSurvey?.id ? 'Edit Survey' : 'Create Survey'}
        onClose={() => { setSurveyModalOpen(false); setEditingSurvey(null); }}
        onSubmit={handleSurveyFormSubmit}
        initialValues={editingSurvey}
        width={600}
      >
        <Form.Item name="title" label="Title" rules={[{ required: true, message: 'Please enter title' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
        <Form.Item name="type" label="Type" rules={[{ required: true, message: 'Please select type' }]}>
          <Select options={surveyTypeOptions} />
        </Form.Item>
        <Form.Item name="isActive" label="Active" valuePropName="checked" initialValue={true}>
          <Switch />
        </Form.Item>
      </CommonForm>

      <CommonForm
        open={templateModalOpen}
        title={editingTemplate?.id ? 'Edit Template' : 'Create Template'}
        onClose={() => { setTemplateModalOpen(false); setEditingTemplate(null); }}
        onSubmit={handleTemplateFormSubmit}
        initialValues={editingTemplate}
        width={600}
      >
        <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter name' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="description" label="Description">
          <Input.TextArea rows={3} />
        </Form.Item>
        <Form.Item name="trigger" label="Trigger" rules={[{ required: true, message: 'Please select trigger' }]}>
          <Select options={triggerOptions} />
        </Form.Item>
        <Form.Item name="surveyIds" label="Survey IDs">
          <Input placeholder="Comma-separated survey IDs" />
        </Form.Item>
        <Form.Item name="isActive" label="Active" valuePropName="checked" initialValue={true}>
          <Switch />
        </Form.Item>
      </CommonForm>

      <CommonForm
        open={questionModalOpen}
        title={editingQuestion?.id ? 'Edit Question' : 'Create Question'}
        onClose={() => { setQuestionModalOpen(false); setEditingQuestion(null); }}
        onSubmit={handleQuestionFormSubmit}
        initialValues={editingQuestion}
        width={600}
      >
        <Form.Item name="questionText" label="Question Text" rules={[{ required: true, message: 'Please enter question text' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="questionType" label="Question Type" rules={[{ required: true, message: 'Please select question type' }]}>
          <Select options={questionTypeOptions} />
        </Form.Item>
        <Form.Item name="orderIndex" label="Order" rules={[{ required: true, message: 'Please enter order' }]}>
          <Input type="number" />
        </Form.Item>
        <Form.Item name="isRequired" label="Required" valuePropName="checked" initialValue={false}>
          <Switch />
        </Form.Item>
        <Form.Item name="options" label="Options">
          <Input.TextArea rows={3} placeholder="JSON options array or comma-separated values" />
        </Form.Item>
      </CommonForm>
    </div>
  );
}
