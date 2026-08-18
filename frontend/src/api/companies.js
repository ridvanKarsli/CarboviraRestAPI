import { apiFetch } from './client'

export function getMyCompany() {
  return apiFetch('/api/companies/me')
}

export function updateMyCompany(payload) {
  return apiFetch('/api/companies/me', { method: 'PUT', body: payload })
}

export function getCompany(id) {
  return apiFetch(`/api/companies/${id}`)
}

// from/to opsiyonel, ISO Instant string olarak gönderiliyor (ör. 2026-01-01T00:00:00Z).
export function getImpactReport({ from, to } = {}) {
  return apiFetch('/api/companies/me/impact-report', { params: { from, to } })
}
