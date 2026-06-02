// Vietnamese Phone Number Validation
const VIETNAMESE_PHONE_PATTERNS = [
  { prefix: '03', operator: 'Viettel Mobile', length: 10 },
  { prefix: '05', operator: 'Vietnamobile', length: 10 },
  { prefix: '07', operator: 'Mobifone', length: 10 },
  { prefix: '08', operator: 'Vinaphone', length: 10 },
  { prefix: '09', operator: 'Viettel/Vinaphone/Mobifone', length: 10 },
  { prefix: '01', operator: 'Fixed line/VoIP', length: 11 },
  { prefix: '02', operator: 'Fixed line', length: 11 },
];

export function isValidVietnamesePhone(phone: string): boolean {
  const cleaned = phone.replace(/\D/g, '');

  const normalized = cleaned.startsWith('84')
    ? '0' + cleaned.slice(2)
    : cleaned;

  if (normalized.length < 10 || normalized.length > 11) return false;

  return VIETNAMESE_PHONE_PATTERNS.some(pattern =>
    normalized.startsWith(pattern.prefix)
  );
}

export function formatVietnamesePhone(phone: string): string {
  const cleaned = phone.replace(/\D/g, '');
  const normalized = cleaned.startsWith('84')
    ? '0' + cleaned.slice(2)
    : cleaned;

  if (normalized.length === 10) {
    return `${normalized.slice(0, 4)} ${normalized.slice(4, 7)} ${normalized.slice(7)}`;
  }
  if (normalized.length === 11) {
    return `${normalized.slice(0, 4)} ${normalized.slice(4, 8)} ${normalized.slice(8)}`;
  }
  return phone;
}

export function getPhoneOperator(phone: string): string {
  const cleaned = phone.replace(/\D/g, '');
  const normalized = cleaned.startsWith('84') ? '0' + cleaned.slice(2) : cleaned;

  const match = VIETNAMESE_PHONE_PATTERNS.find(pattern =>
    normalized.startsWith(pattern.prefix)
  );
  return match?.operator || 'Unknown';
}

// Vietnamese ID Card (CMND/CCCD) Validation
export function isValidVietnameseId(id: string): boolean {
  const cleaned = id.replace(/\s/g, '');
  return /^\d{9}$/.test(cleaned) || /^\d{12}$/.test(cleaned);
}

// Vietnamese Tax Code Validation
export function isValidVietnameseTaxCode(code: string): boolean {
  const cleaned = code.replace(/[\s-]/g, '');
  return /^\d{10}$/.test(cleaned) || /^\d{13}$/.test(cleaned);
}

// Format Vietnamese date
export function formatVnDate(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
}

export function formatVnDateTime(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

// Format Vietnamese currency
export function formatVnCurrency(amount: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
}

// Generate Vietnamese channel list for omnichannel inbox
export const VIETNAMESE_CHANNELS = [
  { key: 'all', label: 'Tất cả kênh', icon: 'global' },
  { key: 'chat', label: 'Chat trực tuyến', icon: 'message', color: '#52c41a' },
  { key: 'email', label: 'Email', icon: 'mail', color: '#722ed1' },
  { key: 'sms', label: 'SMS', icon: 'sms', color: '#1890ff' },
  { key: 'facebook', label: 'Facebook Messenger', icon: 'facebook', color: '#1877F2' },
  { key: 'zalo', label: 'Zalo', icon: 'zalo', color: '#0068FF' },
  { key: 'call', label: 'Ghi chú cuộc gọi', icon: 'phone', color: '#faad14' },
];

// Vietnamese province/city list (top 20)
export const VIETNAM_PROVINCES = [
  'Hồ Chí Minh', 'Hà Nội', 'Đà Nẵng', 'Hải Phòng', 'Cần Thơ',
  'An Giang', 'Bà Rịa - Vũng Tàu', 'Bắc Giang', 'Bắc Kạn', 'Bạc Liêu',
  'Bắc Ninh', 'Bến Tre', 'Bình Định', 'Bình Dương', 'Bình Phước',
  'Bình Thuận', 'Cà Mau', 'Cao Bằng', 'Đắk Lắk', 'Đắk Nông',
];
