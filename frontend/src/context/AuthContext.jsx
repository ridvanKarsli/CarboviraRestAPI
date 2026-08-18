import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import * as authApi from '../api/auth'

const TOKEN_KEY = 'carbovira_token'
const USER_KEY = 'carbovira_user'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  })

  // AuthResponse'daki token'ı ayrı, kalanını (userId, email, fullName, role, companyId, companyName)
  // "user" olarak saklıyoruz. companyId PLATFORM_ADMIN için null gelebilir.
  const persist = useCallback((authResponse) => {
    const { token, ...rest } = authResponse
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(rest))
    setUser(rest)
  }, [])

  const login = useCallback(
    async (email, password) => {
      const response = await authApi.login({ email, password })
      persist(response)
      return response
    },
    [persist],
  )

  const register = useCallback(
    async (payload) => {
      const response = await authApi.register(payload)
      persist(response)
      return response
    },
    [persist],
  )

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }, [])

  const value = useMemo(() => ({ user, login, register, logout }), [user, login, register, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth, AuthProvider içinde kullanılmalı')
  }
  return ctx
}
