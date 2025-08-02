// ========================================
// DFRM - Centraliserad API-klient
// ========================================

import { toast } from '@/components/ui/use-toast';

// API-konfiguration
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

// Feltyper
export interface ApiError {
  message: string;
  status: number;
  code?: string;
}

export interface ValidationError {
  field: string;
  message: string;
}

export interface BusinessError {
  code: string;
  message: string;
}

export interface NetworkError {
  message: string;
  status: number;
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
}

// HTTP-metoder
class ApiClient {
  private baseURL: string;

  constructor(baseURL: string = API_BASE_URL) {
    this.baseURL = baseURL;
  }

  // Hjälpmetod för att hantera cookies
  private getAuthToken(): string | null {
    if (typeof document !== 'undefined') {
      return document.cookie
        .split('; ')
        .find(row => row.startsWith('auth_token='))
        ?.split('=')[1] || null;
    }
    return null;
  }

  // Hjälpmetod för att skapa headers
  private getHeaders(): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    const token = this.getAuthToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
  }

  // Centraliserad felhantering
  private async handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
      let error: ApiError;

      try {
        const errorData = await response.json();
        error = {
          message: errorData.message || 'Ett fel uppstod',
          status: response.status,
          code: errorData.code,
        };
      } catch {
        error = {
          message: 'Ett nätverksfel uppstod',
          status: response.status,
        };
      }

      // Visa felmeddelande för användaren
      toast({
        title: 'Fel',
        description: error.message,
        variant: 'destructive',
      });

      throw error;
    }

    return response.json();
  }

  // GET-request
  async get<T>(endpoint: string, params?: Record<string, any>): Promise<T> {
    const url = new URL(`${this.baseURL}${endpoint}`);
    
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          url.searchParams.append(key, String(value));
        }
      });
    }

    const response = await fetch(url.toString(), {
      method: 'GET',
      headers: this.getHeaders(),
    });

    return this.handleResponse<T>(response);
  }

  // POST-request
  async post<T>(endpoint: string, data?: any): Promise<T> {
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: data ? JSON.stringify(data) : undefined,
    });

    return this.handleResponse<T>(response);
  }

  // PUT-request
  async put<T>(endpoint: string, data?: any): Promise<T> {
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: data ? JSON.stringify(data) : undefined,
    });

    return this.handleResponse<T>(response);
  }

  // DELETE-request
  async delete<T>(endpoint: string): Promise<T> {
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: 'DELETE',
      headers: this.getHeaders(),
    });

    return this.handleResponse<T>(response);
  }

  // PATCH-request
  async patch<T>(endpoint: string, data?: any): Promise<T> {
    const response = await fetch(`${this.baseURL}${endpoint}`, {
      method: 'PATCH',
      headers: this.getHeaders(),
      body: data ? JSON.stringify(data) : undefined,
    });

    return this.handleResponse<T>(response);
  }
}

// Skapa singleton-instans
export const apiClient = new ApiClient();

// Typade API-metoder för specifika endpoints
export const api = {
  // Autentisering
  auth: {
    login: (credentials: { email: string; password: string }) =>
      apiClient.post<{ token: string; user: any }>('/auth/login', credentials),
    refresh: () => apiClient.post<{ token: string }>('/auth/refresh'),
    logout: () => apiClient.post('/auth/logout'),
  },

  // Lägenheter
  apartments: {
    getAll: (params?: { page?: number; size?: number; search?: string }) =>
      apiClient.get<{ apartments: any[]; total: number }>('/apartments', params),
    getById: (id: string) => apiClient.get<any>(`/apartments/${id}`),
    create: (data: any) => apiClient.post<any>('/apartments', data),
    update: (id: string, data: any) => apiClient.put<any>(`/apartments/${id}`, data),
    delete: (id: string) => apiClient.delete(`/apartments/${id}`),
    getTenant: (id: string) => apiClient.get<any>(`/apartments/${id}/tenant`),
  },

  // Hyresgäster
  tenants: {
    getAll: (params?: { page?: number; size?: number; status?: string }) =>
      apiClient.get<{ tenants: any[]; total: number }>('/tenants', params),
    getById: (id: string) => apiClient.get<any>(`/tenants/${id}`),
    create: (data: any) => apiClient.post<any>('/tenants', data),
    update: (id: string, data: any) => apiClient.put<any>(`/tenants/${id}`, data),
    delete: (id: string) => apiClient.delete(`/tenants/${id}`),
    terminate: (id: string, data: any) => apiClient.post<any>(`/tenants/${id}/terminate`, data),
    moveOut: (id: string) => apiClient.post<any>(`/tenants/${id}/move-out`),
  },

  // Intresseanmälningar
  interests: {
    getAll: (params?: { page?: number; size?: number; status?: string }) =>
      apiClient.get<{ interests: any[]; total: number }>('/interests', params),
    getById: (id: string) => apiClient.get<any>(`/interests/${id}`),
    create: (data: any) => apiClient.post<any>('/interests', data),
    update: (id: string, data: any) => apiClient.put<any>(`/interests/${id}`, data),
    delete: (id: string) => apiClient.delete(`/interests/${id}`),
    bookViewing: (id: string, data: any) => apiClient.post<any>(`/interests/${id}/book-viewing`, data),
    sendViewingEmail: (id: string) => apiClient.post<any>(`/interests/${id}/send-viewing-email`),
    checkEmails: () => apiClient.post<any>('/interests/check-emails'),
    getReport: (params?: any) => apiClient.get<any>('/interests/report/filtered', params),
  },

  // Felanmälningar
  issues: {
    getAll: (params?: { page?: number; size?: number; status?: string }) =>
      apiClient.get<{ issues: any[]; total: number }>('/issues', params),
    getById: (id: string) => apiClient.get<any>(`/issues/${id}`),
    create: (data: any) => apiClient.post<any>('/issues', data),
    update: (id: string, data: any) => apiClient.put<any>(`/issues/${id}`, data),
    delete: (id: string) => apiClient.delete(`/issues/${id}`),
    approve: (id: string, data: any) => apiClient.post<any>(`/issues/${id}/approve`, data),
    reject: (id: string, data: any) => apiClient.post<any>(`/issues/${id}/reject`, data),
    checkEmails: () => apiClient.post<any>('/issues/check-emails'),
    getNew: () => apiClient.get<any[]>('/issues/new'),
    getByStatus: (status: string) => apiClient.get<any[]>(`/issues/status/${status}`),
  },

  // Uppgifter
  tasks: {
    getAll: (params?: { page?: number; size?: number; status?: string }) =>
      apiClient.get<{ tasks: any[]; total: number }>('/tasks', params),
    getById: (id: string) => apiClient.get<any>(`/tasks/${id}`),
    create: (data: any) => apiClient.post<any>('/tasks', data),
    update: (id: string, data: any) => apiClient.put<any>(`/tasks/${id}`, data),
    delete: (id: string) => apiClient.delete(`/tasks/${id}`),
    getByStatus: (status: string) => apiClient.get<any[]>(`/tasks/status/${status}`),
    getByUser: (userId: string) => apiClient.get<any[]>(`/tasks/user/${userId}`),
    sendEmail: (id: string) => apiClient.post<any>(`/tasks/${id}/send-email`),
  },

  // Uppgiftskommentarer
  taskComments: {
    getAll: (params?: { taskId?: string }) =>
      apiClient.get<{ comments: any[]; total: number }>('/task-comments', params),
    getById: (id: string) => apiClient.get<any>(`/task-comments/${id}`),
    create: (data: any) => apiClient.post<any>('/task-comments', data),
    update: (id: string, data: any) => apiClient.put<any>(`/task-comments/${id}`, data),
    delete: (id: string) => apiClient.delete(`/task-comments/${id}`),
  },

  // Nycklar
  keys: {
    getAll: (params?: { apartmentId?: string }) =>
      apiClient.get<{ keys: any[]; total: number }>('/keys', params),
    getById: (id: string) => apiClient.get<any>(`/keys/${id}`),
    create: (data: any) => apiClient.post<any>('/keys', data),
    update: (id: string, data: any) => apiClient.put<any>(`/keys/${id}`, data),
    delete: (id: string) => apiClient.delete(`/keys/${id}`),
  },

  // Användare
  users: {
    getAll: (params?: { page?: number; size?: number }) =>
      apiClient.get<{ users: any[]; total: number }>('/users', params),
    getById: (id: string) => apiClient.get<any>(`/users/${id}`),
    create: (data: any) => apiClient.post<any>('/users', data),
    update: (id: string, data: any) => apiClient.put<any>(`/users/${id}`, data),
    delete: (id: string) => apiClient.delete(`/users/${id}`),
  },
};

export default apiClient; 