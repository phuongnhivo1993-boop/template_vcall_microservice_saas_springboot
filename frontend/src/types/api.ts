export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errors: string[];
  pagination?: Pagination;
}

export interface Pagination {
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
