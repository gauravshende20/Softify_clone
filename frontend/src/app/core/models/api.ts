export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiErrorBody {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  traceId?: string;
  fieldErrors?: { field: string; message: string; rejectedValue?: unknown }[];
}

export const API_BASE = '/api/v1';

export function emptyPage<T>(size = 20): PageResponse<T> {
  return {
    content: [],
    page: 0,
    size,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };
}

export function asList<T>(value: T[] | PageResponse<T> | null | undefined): T[] {
  if (!value) {
    return [];
  }
  if (Array.isArray(value)) {
    return value;
  }
  return value.content ?? [];
}

export function asPage<T>(value: T[] | PageResponse<T> | null | undefined): PageResponse<T> {
  if (!value) {
    return emptyPage<T>();
  }
  if (Array.isArray(value)) {
    return {
      content: value,
      page: 0,
      size: value.length,
      totalElements: value.length,
      totalPages: value.length ? 1 : 0,
      first: true,
      last: true,
    };
  }
  return {
    ...emptyPage<T>(),
    ...value,
    content: value.content ?? [],
  };
}
