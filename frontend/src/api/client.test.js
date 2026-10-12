import { describe, it, expect, vi, beforeEach } from 'vitest';
import { apiRequest } from './client';

// Mock browser globals for Node test environment
const mockStorage = (() => {
  let store = {};
  return {
    getItem: (key) => store[key] || null,
    setItem: (key, value) => { store[key] = String(value); },
    removeItem: (key) => { delete store[key]; },
    clear: () => { store = {}; },
  };
})();

globalThis.localStorage = mockStorage;
globalThis.window = {
  dispatchEvent: vi.fn(),
  Event: class { constructor(name) { this.name = name; } },
};

describe('Frontend API Client (client.js)', () => {
  beforeEach(() => {
    mockStorage.clear();
    vi.restoreAllMocks();
  });

  it('attaches Authorization header when token is stored in localStorage', async () => {
    mockStorage.setItem('cpts_token', 'test-sample-jwt-token-123');

    const mockFetch = vi.fn().mockResolvedValue({
      status: 200,
      ok: true,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({ success: true }),
    });
    globalThis.fetch = mockFetch;

    await apiRequest('/api/test');

    expect(mockFetch).toHaveBeenCalledTimes(1);
    const [url, options] = mockFetch.mock.calls[0];
    expect(url).toBe('/api/test');
    expect(options.headers['Authorization']).toBe('Bearer test-sample-jwt-token-123');
    expect(options.headers['Content-Type']).toBe('application/json');
  });

  it('handles 401 Unauthorized by clearing credentials from localStorage', async () => {
    mockStorage.setItem('cpts_token', 'expired-token');
    mockStorage.setItem('cpts_user', JSON.stringify({ id: 1, name: 'Alice' }));

    const mockFetch = vi.fn().mockResolvedValue({
      status: 401,
      ok: false,
      headers: new Headers({ 'content-type': 'application/json' }),
      json: async () => ({ message: 'Token expired' }),
    });
    globalThis.fetch = mockFetch;

    await expect(apiRequest('/api/protected')).rejects.toThrow();

    expect(mockStorage.getItem('cpts_token')).toBeNull();
    expect(mockStorage.getItem('cpts_user')).toBeNull();
  });

  it('handles 204 No Content without attempting to parse JSON', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      status: 204,
      ok: true,
      headers: new Headers(),
    });
    globalThis.fetch = mockFetch;

    const result = await apiRequest('/api/delete/1', { method: 'DELETE' });
    expect(result).toBeNull();
  });
});
