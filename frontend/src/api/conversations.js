import { apiFetch } from './client'

export function startConversation(payload) {
  return apiFetch('/api/conversations', { method: 'POST', body: payload })
}

export function getMyConversations({ page = 0, size = 20 } = {}) {
  return apiFetch('/api/conversations', { params: { page, size } })
}

export function getMessages(conversationId, { page = 0, size = 50 } = {}) {
  return apiFetch(`/api/conversations/${conversationId}/messages`, { params: { page, size } })
}

export function sendMessage(conversationId, content) {
  return apiFetch(`/api/conversations/${conversationId}/messages`, { method: 'POST', body: { content } })
}
