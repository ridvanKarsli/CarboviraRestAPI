import { apiFetch } from './client'

export function searchListings({ type, category, city, q, page = 0, size = 20 } = {}) {
  return apiFetch('/api/listings', { params: { type, category, city, q, page, size } })
}

export function getNearbyListings({ radiusKm, page = 0, size = 20 }) {
  return apiFetch('/api/listings/nearby', { params: { radiusKm, page, size } })
}

export function getMyListings({ page = 0, size = 20 } = {}) {
  return apiFetch('/api/listings/mine', { params: { page, size } })
}

export function getListing(id) {
  return apiFetch(`/api/listings/${id}`)
}

export function createListing(payload) {
  return apiFetch('/api/listings', { method: 'POST', body: payload })
}

export function updateListing(id, payload) {
  return apiFetch(`/api/listings/${id}`, { method: 'PUT', body: payload })
}

export function updateListingStatus(id, status) {
  return apiFetch(`/api/listings/${id}/status`, { method: 'PATCH', body: { status } })
}

export function deleteListing(id) {
  return apiFetch(`/api/listings/${id}`, { method: 'DELETE' })
}
