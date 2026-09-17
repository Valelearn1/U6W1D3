import { useCallback, useEffect, useState } from 'react'
import {
  api,
  getToken,
  setToken,
  setUnauthorizedHandler,
} from '../api/client'
import { AuthContext } from './authContext'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  // Se c'è un token salvato partiamo in "loading": va prima verificato.
  // Se non c'è, non c'è nulla da aspettare e si parte già pronti.
  const [loading, setLoading] = useState(() => Boolean(getToken()))

  const logout = useCallback(() => {
    setToken(null)
    setUser(null)
  }, [])

  // Se una qualsiasi chiamata riceve 401, il token è scaduto: si esce.
  useEffect(() => {
    setUnauthorizedHandler(() => setUser(null))
  }, [])

  // Al caricamento della pagina il token è in localStorage ma non sappiamo
  // se è ancora valido: lo chiediamo al backend con /auth/me.
  useEffect(() => {
    if (!getToken()) return

    let cancelled = false
    api.me()
      .then((me) => {
        if (!cancelled) setUser(me)
      })
      .catch(() => {
        if (!cancelled) logout()
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [logout])

  async function login(email, password) {
    const response = await api.login(email, password)
    setToken(response.token)
    setUser(response.user)
  }

  async function register(email, password, displayName) {
    const response = await api.register(email, password, displayName)
    setToken(response.token)
    setUser(response.user)
  }

  return (
    <AuthContext.Provider
      value={{ user, loading, login, register, logout, isLoggedIn: Boolean(user) }}
    >
      {children}
    </AuthContext.Provider>
  )
}
