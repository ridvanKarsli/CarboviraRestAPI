const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

const TOKEN_KEY = 'carbovira_token'

function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

// Backend'in ApiError şeklini (message, violations vs.) taşıyabilmesi için ayrı bir tip.
// Sayfalar bunu yakalayıp error.body.message'ı direkt kullanıcıya gösteriyor.
export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message || 'Bilinmeyen bir hata oluştu')
    this.status = status
    this.body = body
  }
}

export async function apiFetch(path, { method = 'GET', body, auth = true, params } = {}) {
  const url = new URL(path, BASE_URL)
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, value)
      }
    })
  }

  const headers = { 'Content-Type': 'application/json' }
  if (auth) {
    const token = getToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }
  }

  const response = await fetch(url, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (response.status === 204) {
    return null
  }

  const text = await response.text()
  const data = text ? JSON.parse(text) : null

  if (!response.ok) {
    throw new ApiError(response.status, data)
  }

  return data
}
