'use client';

import { useState, useEffect, useCallback, useMemo } from 'react';
import { Button, Tag, Space, Form, Input, Select, DatePicker, TimePicker, Switch, message, Typography, Tabs, Modal, Tooltip } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import CommonTable from '@/components/common/CommonTable';
import CommonForm from '@/components/common/CommonForm';
import CommonSearch from '@/components/common/CommonSearch';
import SavedFilters from '@/components/common/SavedFilters';
import { showDeleteConfirm } from '@/components/common/CommonConfirmDelete';
import { schedulingApi } from '@/lib/api';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { SorterResult } from 'antd/es/table/interface';
import dayjs from 'dayjs';

const { Title } = Typography;
const { TextArea } = Input;

interface Appointment {
  id: string;
  title: string;
  description: string;
  customerId: string;
  agentId: string;
  startTime: string;
  endTime: string;
  status: string;
  type: string;
  location: string;
  meetingLink: string;
  notes: string;
  reminderSent: boolean;
  recurrenceRule: string;
}

interface AvailabilitySlot {
  id: string;
  agentId: string;
  date: string;
  startTime: string;
  endTime: string;
  isBooked: boolean;
  status: string;
  notes: string;
}

interface ScheduleTemplate {
  id: string;
  name: string;
  description: string;
  agentId: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  isActive: boolean;
  type: string;
  effectiveFrom: string;
  effectiveTo: string;
}

const appointmentStatusColors: Record<string, string> = {
  SCHEDULED: 'blue',
  CONFIRMED: 'cyan',
  IN_PROGRESS: 'orange',
  COMPLETED: 'green',
  CANCELLED: 'red',
  NO_SHOW: 'default',
};

const appointmentStatusOptions = [
  { value: 'SCHEDULED', label: 'Scheduled' },
  { value: 'CONFIRMED', label: 'Confirmed' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' },
  { value: 'NO_SHOW', label: 'No Show' },
];

const appointmentTypeOptions = [
  { value: 'CALL', label: 'Call' },
  { value: 'VIDEO', label: 'Video' },
  { value: 'IN_PERSON', label: 'In Person' },
  { value: 'OTHER', label: 'Other' },
];

const availabilityStatusColors: Record<string, string> = {
  AVAILABLE: 'green',
  BUSY: 'red',
  AWAY: 'orange',
  OFFLINE: 'default',
};

const availabilityStatusOptions = [
  { value: 'AVAILABLE', label: 'Available' },
  { value: 'BUSY', label: 'Busy' },
  { value: 'AWAY', label: 'Away' },
  { value: 'OFFLINE', label: 'Offline' },
];

const templateTypeOptions = [
  { value: 'WORKING', label: 'Working' },
  { value: 'BREAK', label: 'Break' },
  { value: 'MEETING', label: 'Meeting' },
  { value: 'TRAINING', label: 'Training' },
];

const dayOfWeekOptions = [
  { value: 'MONDAY', label: 'Monday' },
  { value: 'TUESDAY', label: 'Tuesday' },
  { value: 'WEDNESDAY', label: 'Wednesday' },
  { value: 'THURSDAY', label: 'Thursday' },
  { value: 'FRIDAY', label: 'Friday' },
  { value: 'SATURDAY', label: 'Saturday' },
  { value: 'SUNDAY', label: 'Sunday' },
];

export default function SchedulingPage() {
  const [activeTab, setActiveTab] = useState('appointments');

  // Appointments state
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [appointmentsLoading, setAppointmentsLoading] = useState(false);
  const [appointmentsError, setAppointmentsError] = useState<string | null>(null);
  const [appointmentPagination, setAppointmentPagination] = useState<TablePaginationConfig>({
    current: 1, pageSize: 10, total: 0,
  });
  const [appointmentModalOpen, setAppointmentModalOpen] = useState(false);
  const [editingAppointment, setEditingAppointment] = useState<Appointment | null>(null);
  const [selectedAppointmentKeys, setSelectedAppointmentKeys] = useState<string[]>([]);

  // Availability state
  const [availability, setAvailability] = useState<AvailabilitySlot[]>([]);
  const [availabilityLoading, setAvailabilityLoading] = useState(false);
  const [availabilityError, setAvailabilityError] = useState<string | null>(null);
  const [availabilityPagination, setAvailabilityPagination] = useState<TablePaginationConfig>({
    current: 1, pageSize: 10, total: 0,
  });
  const [availabilityModalOpen, setAvailabilityModalOpen] = useState(false);
  const [editingAvailability, setEditingAvailability] = useState<AvailabilitySlot | null>(null);
  const [selectedAvailabilityKeys, setSelectedAvailabilityKeys] = useState<string[]>([]);

  // Templates state
  const [templates, setTemplates] = useState<ScheduleTemplate[]>([]);
  const [templatesLoading, setTemplatesLoading] = useState(false);
  const [templatesError, setTemplatesError] = useState<string | null>(null);
  const [templatePagination, setTemplatePagination] = useState<TablePaginationConfig>({
    current: 1, pageSize: 10, total: 0,
  });
  const [templateModalOpen, setTemplateModalOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<ScheduleTemplate | null>(null);
  const [selectedTemplateKeys, setSelectedTemplateKeys] = useState<string[]>([]);

  // SavedFilters state
  const [appointmentFilters, setAppointmentFilters] = useState<Record<string, any>>({});
  const [availabilityFilters, setAvailabilityFilters] = useState<Record<string, any>>({});
  const [templateFilters, setTemplateFilters] = useState<Record<string, any>>({});

  // ---- Appointments ----
  const fetchAppointments = useCallback(async (page = 1, size = 10, params?: Record<string, any>) => {
    setAppointmentsLoading(true);
    setAppointmentsError(null);
    try {
      const res = await schedulingApi.getAppointments({ page: page - 1, size, ...params });
      const data = res.data;
      if (data.content) {
        setAppointments(data.content);
        setAppointmentPagination((prev) => ({
          ...prev, current: data.page + 1, pageSize: data.size, total: data.totalElements,
        }));
      } else if (Array.isArray(data)) {
        setAppointments(data);
      } else if (data.data) {
        setAppointments(Array.isArray(data.data) ? data.data : []);
      }
    } catch (err: any) {
      setAppointmentsError(err?.response?.data?.message || err?.message || 'Failed to load appointments');
      setAppointments([]);
    } finally {
      setAppointmentsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'appointments') {
      fetchAppointments(appointmentPagination.current, appointmentPagination.pageSize);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const handleAppointmentTableChange = (
    pag: TablePaginationConfig,
    _filters: any,
    _sorter: SorterResult<Appointment> | SorterResult<Appointment>[],
  ) => {
    setAppointmentPagination((prev) => ({ ...prev, current: pag.current, pageSize: pag.pageSize }));
    fetchAppointments(pag.current, pag.pageSize);
  };

  const handleAppointmentSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') cleaned[key] = val;
    });
    setAppointmentFilters(cleaned);
    setAppointmentPagination((prev) => ({ ...prev, current: 1 }));
    fetchAppointments(1, appointmentPagination.pageSize, cleaned);
  };

  const handleAppointmentReset = () => {
    setAppointmentFilters({});
    setAppointmentPagination((prev) => ({ ...prev, current: 1 }));
    fetchAppointments(1, appointmentPagination.pageSize);
  };

  const handleCreateAppointment = () => {
    setEditingAppointment(null);
    setAppointmentModalOpen(true);
  };

  const handleEditAppointment = (record: Appointment) => {
    setEditingAppointment(record);
    setAppointmentModalOpen(true);
  };

  const handleDeleteAppointment = (record: Appointment) => {
    showDeleteConfirm({
      title: 'Delete Appointment',
      content: `Are you sure you want to delete "${record.title}"? This action cannot be undone.`,
      onOk: async () => {
        await schedulingApi.deleteAppointment(record.id);
        fetchAppointments(appointmentPagination.current, appointmentPagination.pageSize);
      },
    });
  };

  const handleAppointmentFormSubmit = async (values: any) => {
    try {
      const payload = {
        ...values,
        startTime: values.startTime?.toISOString?.() || values.startTime,
        endTime: values.endTime?.toISOString?.() || values.endTime,
      };
      if (editingAppointment?.id) {
        await schedulingApi.updateAppointment(editingAppointment.id, payload);
        message.success('Appointment updated successfully');
      } else {
        await schedulingApi.createAppointment(payload);
        message.success('Appointment created successfully');
      }
      setAppointmentModalOpen(false);
      fetchAppointments(appointmentPagination.current, appointmentPagination.pageSize);
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Failed to save appointment');
    }
  };

  const handleBulkDeleteAppointments = () => {
    Modal.confirm({
      title: 'Delete Selected Appointments',
      content: `Are you sure you want to delete ${selectedAppointmentKeys.length} selected appointments?`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await schedulingApi.bulkDeleteAppointments(selectedAppointmentKeys);
          message.success(`Deleted ${selectedAppointmentKeys.length} appointments`);
          setSelectedAppointmentKeys([]);
          fetchAppointments(appointmentPagination.current, appointmentPagination.pageSize);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Delete failed');
        }
      },
    });
  };

  const handleExportAppointmentsCsv = async () => {
    try {
      const res = await schedulingApi.exportAppointmentsCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `appointments_${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Appointments exported');
    } catch {
      message.error('Export failed');
    }
  };

  const handleExportAppointmentsExcel = async () => {
    try {
      const res = await schedulingApi.exportAppointmentsExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `appointments_${new Date().toISOString().slice(0, 10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Appointments exported');
    } catch {
      message.error('Export failed');
    }
  };

  const appointmentColumns: ColumnsType<Appointment> = [
    {
      title: 'Title',
      dataIndex: 'title',
      key: 'title',
      sorter: true,
      render: (title: string) => <span style={{ fontWeight: 500 }}>{title}</span>,
    },
    { title: 'Customer ID', dataIndex: 'customerId', key: 'customerId', ellipsis: true },
    { title: 'Agent ID', dataIndex: 'agentId', key: 'agentId', ellipsis: true },
    {
      title: 'Start Time',
      dataIndex: 'startTime',
      key: 'startTime',
      render: (val: string) => val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: 'End Time',
      dataIndex: 'endTime',
      key: 'endTime',
      render: (val: string) => val ? dayjs(val).format('YYYY-MM-DD HH:mm') : '-',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => <Tag color={appointmentStatusColors[status] || 'default'}>{status}</Tag>,
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
    },
    { title: 'Location', dataIndex: 'location', key: 'location', ellipsis: true },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: Appointment) => (
        <Space>
          <Tooltip title="Edit"><Button type="link" icon={<EditOutlined />} onClick={() => handleEditAppointment(record)}>
            Edit
          </Button></Tooltip>
          <Tooltip title="Delete"><Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDeleteAppointment(record)}>
            Delete
          </Button></Tooltip>
        </Space>
      ),
    },
  ];

  const appointmentSearchFields = [
    { name: 'keyword', label: 'Keyword', type: 'input' as const, placeholder: 'Search by title or description' },
    {
      name: 'status',
      label: 'Status',
      type: 'select' as const,
      placeholder: 'Filter by status',
      options: appointmentStatusOptions,
    },
    {
      name: 'type',
      label: 'Type',
      type: 'select' as const,
      placeholder: 'Filter by type',
      options: appointmentTypeOptions,
    },
  ];

  // ---- Availability ----
  const fetchAvailability = useCallback(async (page = 1, size = 10, params?: Record<string, any>) => {
    setAvailabilityLoading(true);
    setAvailabilityError(null);
    try {
      const res = await schedulingApi.getAvailability({ page: page - 1, size, ...params });
      const data = res.data;
      if (data.content) {
        setAvailability(data.content);
        setAvailabilityPagination((prev) => ({
          ...prev, current: data.page + 1, pageSize: data.size, total: data.totalElements,
        }));
      } else if (Array.isArray(data)) {
        setAvailability(data);
      } else if (data.data) {
        setAvailability(Array.isArray(data.data) ? data.data : []);
      }
    } catch (err: any) {
      setAvailabilityError(err?.response?.data?.message || err?.message || 'Failed to load availability');
      setAvailability([]);
    } finally {
      setAvailabilityLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'availability') {
      fetchAvailability(availabilityPagination.current, availabilityPagination.pageSize);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const handleAvailabilityTableChange = (
    pag: TablePaginationConfig,
    _filters: any,
    _sorter: SorterResult<AvailabilitySlot> | SorterResult<AvailabilitySlot>[],
  ) => {
    setAvailabilityPagination((prev) => ({ ...prev, current: pag.current, pageSize: pag.pageSize }));
    fetchAvailability(pag.current, pag.pageSize);
  };

  const handleAvailabilitySearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') cleaned[key] = val;
    });
    setAvailabilityFilters(cleaned);
    setAvailabilityPagination((prev) => ({ ...prev, current: 1 }));
    fetchAvailability(1, availabilityPagination.pageSize, cleaned);
  };

  const handleAvailabilityReset = () => {
    setAvailabilityFilters({});
    setAvailabilityPagination((prev) => ({ ...prev, current: 1 }));
    fetchAvailability(1, availabilityPagination.pageSize);
  };

  const handleCreateAvailability = () => {
    setEditingAvailability(null);
    setAvailabilityModalOpen(true);
  };

  const handleEditAvailability = (record: AvailabilitySlot) => {
    setEditingAvailability(record);
    setAvailabilityModalOpen(true);
  };

  const handleBulkDeleteAvailability = () => {
    Modal.confirm({
      title: 'Delete Selected Availability',
      content: `Are you sure you want to delete ${selectedAvailabilityKeys.length} selected availability slots?`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await schedulingApi.bulkDeleteAvailability(selectedAvailabilityKeys);
          message.success(`Deleted ${selectedAvailabilityKeys.length} availability slots`);
          setSelectedAvailabilityKeys([]);
          fetchAvailability(availabilityPagination.current, availabilityPagination.pageSize);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Delete failed');
        }
      },
    });
  };

  const handleDeleteAvailability = (record: AvailabilitySlot) => {
    showDeleteConfirm({
      title: 'Delete Availability',
      content: 'Are you sure you want to delete this availability slot? This action cannot be undone.',
      onOk: async () => {
        await schedulingApi.deleteAvailability(record.id);
        fetchAvailability(availabilityPagination.current, availabilityPagination.pageSize);
      },
    });
  };

  const handleAvailabilityFormSubmit = async (values: any) => {
    try {
      const payload = {
        ...values,
        date: values.date?.format?.('YYYY-MM-DD') || values.date,
        startTime: values.startTime?.format?.('HH:mm:ss') || values.startTime,
        endTime: values.endTime?.format?.('HH:mm:ss') || values.endTime,
      };
      if (editingAvailability?.id) {
        await schedulingApi.deleteAvailability(editingAvailability.id);
        await schedulingApi.createAvailability(payload);
        message.success('Availability updated successfully');
      } else {
        await schedulingApi.createAvailability(payload);
        message.success('Availability created successfully');
      }
      setAvailabilityModalOpen(false);
      fetchAvailability(availabilityPagination.current, availabilityPagination.pageSize);
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Failed to save availability');
    }
  };

  const handleExportAvailabilityCsv = async () => {
    try {
      const res = await schedulingApi.exportAvailabilityCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `availability_${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Availability exported');
    } catch {
      message.error('Export failed');
    }
  };

  const handleExportAvailabilityExcel = async () => {
    try {
      const res = await schedulingApi.exportAvailabilityExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `availability_${new Date().toISOString().slice(0, 10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Availability exported');
    } catch {
      message.error('Export failed');
    }
  };

  const availabilityColumns: ColumnsType<AvailabilitySlot> = [
    { title: 'Agent ID', dataIndex: 'agentId', key: 'agentId', ellipsis: true },
    {
      title: 'Date',
      dataIndex: 'date',
      key: 'date',
      render: (val: string) => val ? dayjs(val).format('YYYY-MM-DD') : '-',
    },
    {
      title: 'Start Time',
      dataIndex: 'startTime',
      key: 'startTime',
    },
    {
      title: 'End Time',
      dataIndex: 'endTime',
      key: 'endTime',
    },
    {
      title: 'Booked',
      dataIndex: 'isBooked',
      key: 'isBooked',
      render: (val: boolean) => <Tag color={val ? 'red' : 'green'}>{val ? 'Yes' : 'No'}</Tag>,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => <Tag color={availabilityStatusColors[status] || 'default'}>{status}</Tag>,
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: AvailabilitySlot) => (
        <Space>
          <Tooltip title="Edit"><Button type="link" icon={<EditOutlined />} onClick={() => handleEditAvailability(record)}>
            Edit
          </Button></Tooltip>
          <Tooltip title="Delete"><Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDeleteAvailability(record)}>
            Delete
          </Button></Tooltip>
        </Space>
      ),
    },
  ];

  const availabilitySearchFields = [
    { name: 'agentId', label: 'Agent ID', type: 'input' as const, placeholder: 'Filter by agent ID' },
    {
      name: 'status',
      label: 'Status',
      type: 'select' as const,
      placeholder: 'Filter by status',
      options: availabilityStatusOptions,
    },
    { name: 'date', label: 'Date', type: 'input' as const, placeholder: 'YYYY-MM-DD' },
  ];

  // ---- Templates ----
  const fetchTemplates = useCallback(async (page = 1, size = 10, params?: Record<string, any>) => {
    setTemplatesLoading(true);
    setTemplatesError(null);
    try {
      const res = await schedulingApi.getTemplates({ page: page - 1, size, ...params });
      const data = res.data;
      if (data.content) {
        setTemplates(data.content);
        setTemplatePagination((prev) => ({
          ...prev, current: data.page + 1, pageSize: data.size, total: data.totalElements,
        }));
      } else if (Array.isArray(data)) {
        setTemplates(data);
      } else if (data.data) {
        setTemplates(Array.isArray(data.data) ? data.data : []);
      }
    } catch (err: any) {
      setTemplatesError(err?.response?.data?.message || err?.message || 'Failed to load templates');
      setTemplates([]);
    } finally {
      setTemplatesLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeTab === 'templates') {
      fetchTemplates(templatePagination.current, templatePagination.pageSize);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const handleTemplateTableChange = (
    pag: TablePaginationConfig,
    _filters: any,
    _sorter: SorterResult<ScheduleTemplate> | SorterResult<ScheduleTemplate>[],
  ) => {
    setTemplatePagination((prev) => ({ ...prev, current: pag.current, pageSize: pag.pageSize }));
    fetchTemplates(pag.current, pag.pageSize);
  };

  const handleTemplateSearch = (values: any) => {
    const cleaned: Record<string, any> = {};
    Object.entries(values).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') cleaned[key] = val;
    });
    setTemplateFilters(cleaned);
    setTemplatePagination((prev) => ({ ...prev, current: 1 }));
    fetchTemplates(1, templatePagination.pageSize, cleaned);
  };

  const handleTemplateReset = () => {
    setTemplateFilters({});
    setTemplatePagination((prev) => ({ ...prev, current: 1 }));
    fetchTemplates(1, templatePagination.pageSize);
  };

  const handleCreateTemplate = () => {
    setEditingTemplate(null);
    setTemplateModalOpen(true);
  };

  const handleEditTemplate = (record: ScheduleTemplate) => {
    setEditingTemplate(record);
    setTemplateModalOpen(true);
  };

  const handleDeleteTemplate = (record: ScheduleTemplate) => {
    showDeleteConfirm({
      title: 'Delete Template',
      content: `Are you sure you want to delete "${record.name}"? This action cannot be undone.`,
      onOk: async () => {
        await schedulingApi.deleteTemplate(record.id);
        fetchTemplates(templatePagination.current, templatePagination.pageSize);
      },
    });
  };

  const handleTemplateFormSubmit = async (values: any) => {
    try {
      const payload = {
        ...values,
        startTime: values.startTime?.format?.('HH:mm:ss') || values.startTime,
        endTime: values.endTime?.format?.('HH:mm:ss') || values.endTime,
        effectiveFrom: values.effectiveFrom?.format?.('YYYY-MM-DD') || values.effectiveFrom || null,
        effectiveTo: values.effectiveTo?.format?.('YYYY-MM-DD') || values.effectiveTo || null,
      };
      if (editingTemplate?.id) {
        await schedulingApi.updateTemplate(editingTemplate.id, payload);
        message.success('Template updated successfully');
      } else {
        await schedulingApi.createTemplate(payload);
        message.success('Template created successfully');
      }
      setTemplateModalOpen(false);
      fetchTemplates(templatePagination.current, templatePagination.pageSize);
    } catch (err: any) {
      message.error(err?.response?.data?.message || err?.message || 'Failed to save template');
    }
  };

  const handleBulkDeleteTemplates = () => {
    Modal.confirm({
      title: 'Delete Selected Templates',
      content: `Are you sure you want to delete ${selectedTemplateKeys.length} selected templates?`,
      okText: 'Delete',
      okType: 'danger',
      cancelText: 'Cancel',
      onOk: async () => {
        try {
          await schedulingApi.bulkDeleteTemplates(selectedTemplateKeys);
          message.success(`Deleted ${selectedTemplateKeys.length} templates`);
          setSelectedTemplateKeys([]);
          fetchTemplates(templatePagination.current, templatePagination.pageSize);
        } catch (err: any) {
          message.error(err?.response?.data?.message || 'Delete failed');
        }
      },
    });
  };

  const handleExportTemplatesCsv = async () => {
    try {
      const res = await schedulingApi.exportTemplatesCsv();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `schedule_templates_${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Templates exported');
    } catch {
      message.error('Export failed');
    }
  };

  const handleExportTemplatesExcel = async () => {
    try {
      const res = await schedulingApi.exportTemplatesExcel();
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `schedule_templates_${new Date().toISOString().slice(0, 10)}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('Templates exported');
    } catch {
      message.error('Export failed');
    }
  };

  const templateColumns: ColumnsType<ScheduleTemplate> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
      render: (name: string) => <span style={{ fontWeight: 500 }}>{name}</span>,
    },
    { title: 'Agent ID', dataIndex: 'agentId', key: 'agentId', ellipsis: true },
    {
      title: 'Day',
      dataIndex: 'dayOfWeek',
      key: 'dayOfWeek',
    },
    {
      title: 'Start Time',
      dataIndex: 'startTime',
      key: 'startTime',
    },
    {
      title: 'End Time',
      dataIndex: 'endTime',
      key: 'endTime',
    },
    {
      title: 'Type',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => <Tag>{type}</Tag>,
    },
    {
      title: 'Active',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (val: boolean) => <Tag color={val ? 'green' : 'default'}>{val ? 'Yes' : 'No'}</Tag>,
    },
    {
      title: 'Effective From',
      dataIndex: 'effectiveFrom',
      key: 'effectiveFrom',
      render: (val: string) => val ? dayjs(val).format('YYYY-MM-DD') : '-',
    },
    {
      title: 'Effective To',
      dataIndex: 'effectiveTo',
      key: 'effectiveTo',
      render: (val: string) => val ? dayjs(val).format('YYYY-MM-DD') : '-',
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_: unknown, record: ScheduleTemplate) => (
        <Space>
          <Tooltip title="Edit"><Button type="link" icon={<EditOutlined />} onClick={() => handleEditTemplate(record)}>
            Edit
          </Button></Tooltip>
          <Tooltip title="Delete"><Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDeleteTemplate(record)}>
            Delete
          </Button></Tooltip>
        </Space>
      ),
    },
  ];

  const templateSearchFields = [
    { name: 'keyword', label: 'Keyword', type: 'input' as const, placeholder: 'Search by name' },
    { name: 'agentId', label: 'Agent ID', type: 'input' as const, placeholder: 'Filter by agent ID' },
    {
      name: 'dayOfWeek',
      label: 'Day',
      type: 'select' as const,
      placeholder: 'Filter by day',
      options: dayOfWeekOptions,
    },
    {
      name: 'type',
      label: 'Type',
      type: 'select' as const,
      placeholder: 'Filter by type',
      options: templateTypeOptions,
    },
  ];

  // ---- Tab Items ----
  const tabItems = [
    {
      key: 'appointments',
      label: 'Appointments',
      children: (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
            <CommonSearch
              fields={appointmentSearchFields}
              onSearch={handleAppointmentSearch}
              onReset={handleAppointmentReset}
              loading={appointmentsLoading}
            />
            <SavedFilters
              currentValues={appointmentFilters}
              onApply={(v) => { setAppointmentFilters(v); handleAppointmentSearch(v); }}
              storageKey="vcall-saved-filters-scheduling-appointments"
            />
          </div>
          {selectedAppointmentKeys.length > 0 && (
            <Button danger onClick={handleBulkDeleteAppointments} style={{ marginBottom: 16 }}>
              Delete Selected ({selectedAppointmentKeys.length})
            </Button>
          )}
          <CommonTable<Appointment>
            rowSelection={{ selectedRowKeys: selectedAppointmentKeys, onChange: (keys: React.Key[]) => setSelectedAppointmentKeys(keys as string[]) }}
            columns={appointmentColumns}
            dataSource={appointments}
            loading={appointmentsLoading}
            error={appointmentsError}
            rowKey="id"
            pagination={appointmentPagination}
            onRefresh={() => { setSelectedAppointmentKeys([]); fetchAppointments(appointmentPagination.current, appointmentPagination.pageSize); }}
            onExportCsv={handleExportAppointmentsCsv}
            onExportExcel={handleExportAppointmentsExcel}
            onTableChange={handleAppointmentTableChange}
          />
          <CommonForm
            open={appointmentModalOpen}
            title={editingAppointment?.id ? 'Edit Appointment' : 'New Appointment'}
            onClose={() => { setAppointmentModalOpen(false); setEditingAppointment(null); }}
            onSubmit={handleAppointmentFormSubmit}
            initialValues={editingAppointment ? {
              ...editingAppointment,
              startTime: editingAppointment.startTime ? dayjs(editingAppointment.startTime) : undefined,
              endTime: editingAppointment.endTime ? dayjs(editingAppointment.endTime) : undefined,
            } : undefined}
            width={640}
          >
            <Form.Item name="title" label="Title" rules={[{ required: true, message: 'Please enter title' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="description" label="Description">
              <TextArea rows={3} />
            </Form.Item>
            <Form.Item name="customerId" label="Customer ID" rules={[{ required: true, message: 'Please enter customer ID' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="agentId" label="Agent ID" rules={[{ required: true, message: 'Please enter agent ID' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="startTime" label="Start Time" rules={[{ required: true, message: 'Please select start time' }]}>
              <DatePicker showTime format="YYYY-MM-DD HH:mm" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="endTime" label="End Time" rules={[{ required: true, message: 'Please select end time' }]}>
              <DatePicker showTime format="YYYY-MM-DD HH:mm" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="type" label="Type" rules={[{ required: true, message: 'Please select type' }]}>
              <Select options={appointmentTypeOptions} />
            </Form.Item>
            <Form.Item name="status" label="Status" initialValue="SCHEDULED">
              <Select options={appointmentStatusOptions} />
            </Form.Item>
            <Form.Item name="location" label="Location">
              <Input />
            </Form.Item>
            <Form.Item name="meetingLink" label="Meeting Link">
              <Input />
            </Form.Item>
            <Form.Item name="notes" label="Notes">
              <TextArea rows={2} />
            </Form.Item>
          </CommonForm>
        </>
      ),
    },
    {
      key: 'availability',
      label: 'Availability',
      children: (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
            <CommonSearch
              fields={availabilitySearchFields}
              onSearch={handleAvailabilitySearch}
              onReset={handleAvailabilityReset}
              loading={availabilityLoading}
            />
            <SavedFilters
              currentValues={availabilityFilters}
              onApply={(v) => { setAvailabilityFilters(v); handleAvailabilitySearch(v); }}
              storageKey="vcall-saved-filters-scheduling-availability"
            />
          </div>
          {selectedAvailabilityKeys.length > 0 && (
            <Button danger onClick={handleBulkDeleteAvailability} style={{ marginBottom: 16 }}>
              Delete Selected ({selectedAvailabilityKeys.length})
            </Button>
          )}
          <CommonTable<AvailabilitySlot>
            rowSelection={{ selectedRowKeys: selectedAvailabilityKeys, onChange: (keys: React.Key[]) => setSelectedAvailabilityKeys(keys as string[]) }}
            columns={availabilityColumns}
            dataSource={availability}
            loading={availabilityLoading}
            error={availabilityError}
            rowKey="id"
            pagination={availabilityPagination}
            onRefresh={() => { setSelectedAvailabilityKeys([]); fetchAvailability(availabilityPagination.current, availabilityPagination.pageSize); }}
            onExportCsv={handleExportAvailabilityCsv}
            onExportExcel={handleExportAvailabilityExcel}
            onTableChange={handleAvailabilityTableChange}
          />
          <CommonForm
            open={availabilityModalOpen}
            title={editingAvailability?.id ? 'Edit Availability' : 'New Availability'}
            onClose={() => { setAvailabilityModalOpen(false); setEditingAvailability(null); }}
            onSubmit={handleAvailabilityFormSubmit}
            initialValues={editingAvailability ? {
              ...editingAvailability,
              date: editingAvailability.date ? dayjs(editingAvailability.date) : undefined,
            } : undefined}
            width={540}
          >
            <Form.Item name="agentId" label="Agent ID" rules={[{ required: true, message: 'Please enter agent ID' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="date" label="Date" rules={[{ required: true, message: 'Please select date' }]}>
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="startTime" label="Start Time" rules={[{ required: true, message: 'Please select start time' }]}>
              <TimePicker format="HH:mm" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="endTime" label="End Time" rules={[{ required: true, message: 'Please select end time' }]}>
              <TimePicker format="HH:mm" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="status" label="Status" initialValue="AVAILABLE">
              <Select options={availabilityStatusOptions} />
            </Form.Item>
            <Form.Item name="notes" label="Notes">
              <TextArea rows={2} />
            </Form.Item>
          </CommonForm>
        </>
      ),
    },
    {
      key: 'templates',
      label: 'Schedule Templates',
      children: (
        <>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
            <CommonSearch
              fields={templateSearchFields}
              onSearch={handleTemplateSearch}
              onReset={handleTemplateReset}
              loading={templatesLoading}
            />
            <SavedFilters
              currentValues={templateFilters}
              onApply={(v) => { setTemplateFilters(v); handleTemplateSearch(v); }}
              storageKey="vcall-saved-filters-scheduling-templates"
            />
          </div>
          {selectedTemplateKeys.length > 0 && (
            <Button danger onClick={handleBulkDeleteTemplates} style={{ marginBottom: 16 }}>
              Delete Selected ({selectedTemplateKeys.length})
            </Button>
          )}
          <CommonTable<ScheduleTemplate>
            rowSelection={{ selectedRowKeys: selectedTemplateKeys, onChange: (keys: React.Key[]) => setSelectedTemplateKeys(keys as string[]) }}
            columns={templateColumns}
            dataSource={templates}
            loading={templatesLoading}
            error={templatesError}
            rowKey="id"
            pagination={templatePagination}
            onRefresh={() => { setSelectedTemplateKeys([]); fetchTemplates(templatePagination.current, templatePagination.pageSize); }}
            onExportCsv={handleExportTemplatesCsv}
            onExportExcel={handleExportTemplatesExcel}
            onTableChange={handleTemplateTableChange}
          />
          <CommonForm
            open={templateModalOpen}
            title={editingTemplate?.id ? 'Edit Template' : 'New Template'}
            onClose={() => { setTemplateModalOpen(false); setEditingTemplate(null); }}
            onSubmit={handleTemplateFormSubmit}
            initialValues={editingTemplate ? {
              ...editingTemplate,
              effectiveFrom: editingTemplate.effectiveFrom ? dayjs(editingTemplate.effectiveFrom) : undefined,
              effectiveTo: editingTemplate.effectiveTo ? dayjs(editingTemplate.effectiveTo) : undefined,
            } : undefined}
            width={600}
          >
            <Form.Item name="name" label="Name" rules={[{ required: true, message: 'Please enter name' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="description" label="Description">
              <TextArea rows={2} />
            </Form.Item>
            <Form.Item name="agentId" label="Agent ID" rules={[{ required: true, message: 'Please enter agent ID' }]}>
              <Input />
            </Form.Item>
            <Form.Item name="dayOfWeek" label="Day of Week" rules={[{ required: true, message: 'Please select day' }]}>
              <Select options={dayOfWeekOptions} />
            </Form.Item>
            <Form.Item name="startTime" label="Start Time" rules={[{ required: true, message: 'Please select start time' }]}>
              <TimePicker format="HH:mm" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="endTime" label="End Time" rules={[{ required: true, message: 'Please select end time' }]}>
              <TimePicker format="HH:mm" style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="type" label="Type" initialValue="WORKING">
              <Select options={templateTypeOptions} />
            </Form.Item>
            <Form.Item name="isActive" label="Active" valuePropName="checked" initialValue={true}>
              <Switch />
            </Form.Item>
            <Form.Item name="effectiveFrom" label="Effective From">
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="effectiveTo" label="Effective To">
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
          </CommonForm>
        </>
      ),
    },
  ];

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>Scheduling</Title>
        {activeTab === 'appointments' && (
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateAppointment}>
            New Appointment
          </Button>
        )}
        {activeTab === 'availability' && (
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateAvailability}>
            New Availability
          </Button>
        )}
        {activeTab === 'templates' && (
          <Button type="primary" icon={<PlusOutlined />} onClick={handleCreateTemplate}>
            New Template
          </Button>
        )}
      </div>
      <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />
    </div>
  );
}
