import { apiFetch } from './client'

export function register(payload) {
  return apiFetch('/api/auth/register', { method: 'POST', body: payload, auth: false })
}

export function login(payload) {
  return apiFetch('/api/auth/login', { method: 'POST', body: payload, auth: false })
}
