// API Client with automatic JWT injection, error handling, and CSV download support

const RAW_URL = (import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '').trim();
const FORMATTED_URL = RAW_URL && !RAW_URL.startsWith('http://') && !RAW_URL.startsWith('https://')
  ? `https://${RAW_URL}`
  : RAW_URL;
const BASE_URL = FORMATTED_URL.replace(/\/+$/, '');

export async function apiRequest(endpoint, options = {}) {
  const token = localStorage.getItem('cpts_token');

  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    // If unauthorized, clear stored credentials
    localStorage.removeItem('cpts_token');
    localStorage.removeItem('cpts_user');
    window.dispatchEvent(new Event('cpts-auth-expired'));
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('text/csv')) {
    const blob = await response.blob();
    return blob;
  }

  let data;
  try {
    data = await response.json();
  } catch (err) {
    data = { message: response.statusText };
  }

  if (!response.ok) {
    const errorMsg = data?.message || data?.error || `Request failed with status ${response.status}`;
    const error = new Error(errorMsg);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

export const api = {
  get: (url, params) => {
    let finalUrl = url;
    if (params) {
      const searchParams = new URLSearchParams();
      Object.entries(params).forEach(([key, val]) => {
        if (val !== undefined && val !== null && val !== '') {
          searchParams.append(key, val);
        }
      });
      const qs = searchParams.toString();
      if (qs) finalUrl += `?${qs}`;
    }
    return apiRequest(finalUrl, { method: 'GET' });
  },

  post: (url, body) =>
    apiRequest(url, {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  put: (url, body, params) => {
    let finalUrl = url;
    if (params) {
      const searchParams = new URLSearchParams();
      Object.entries(params).forEach(([key, val]) => {
        if (val !== undefined && val !== null && val !== '') {
          searchParams.append(key, val);
        }
      });
      const qs = searchParams.toString();
      if (qs) finalUrl += `?${qs}`;
    }
    return apiRequest(finalUrl, {
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined,
    });
  },

  delete: (url) => apiRequest(url, { method: 'DELETE' }),

  downloadCsv: async (url, params, defaultFilename = 'transactions.csv') => {
    let finalUrl = url;
    if (params) {
      const searchParams = new URLSearchParams();
      Object.entries(params).forEach(([key, val]) => {
        if (val !== undefined && val !== null && val !== '') {
          searchParams.append(key, val);
        }
      });
      const qs = searchParams.toString();
      if (qs) finalUrl += `?${qs}`;
    }
    const token = localStorage.getItem('cpts_token');
    const response = await fetch(`${BASE_URL}${finalUrl}`, {
      method: 'GET',
      headers: {
        Authorization: token ? `Bearer ${token}` : '',
      },
    });
    if (!response.ok) {
      throw new Error('Failed to download CSV');
    }
    const blob = await response.blob();
    const blobUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = defaultFilename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(blobUrl);
  },
};
