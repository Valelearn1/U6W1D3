// Unico punto di contatto col backend Spring Boot.
// La API key di OpenRouter NON passa mai da qui: sta solo lato server.

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api'
const TOKEN_KEY = 'chatbot-token'

// ---- gestione del token ----

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

/** Chiamata quando il backend risponde 401: il token non vale piu'. */
let onUnauthorized = () => {}
export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

async function request(path, options = {}) {
  const token = getToken()

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      // Ecco dove il token entra in ogni richiesta protetta.
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (res.status === 401) {
    setToken(null)
    onUnauthorized()
    throw new ApiError('Sessione scaduta: accedi di nuovo.', 401)
  }

  if (!res.ok) {
    let detail = `HTTP ${res.status}`
    try {
      const body = await res.json()
      detail = body.message ?? body.error ?? detail
    } catch {
      // risposta senza corpo JSON: teniamo il codice di stato
    }
    throw new ApiError(detail, res.status)
  }

  return res.status === 204 ? null : res.json()
}

export const api = {
  // ---- autenticazione ----
  register: (email, password, displayName) =>
    request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password, displayName }),
    }),

  login: (email, password) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  me: () => request('/auth/me'),

  // ---- chat ----
  listChats: () => request('/chats'),
  getChat: (id) => request(`/chats/${id}`),

  createChat: (model) =>
    request('/chats', {
      method: 'POST',
      body: JSON.stringify({ model }),
    }),

  sendMessage: (chatId, content) =>
    request(`/chats/${chatId}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),

  // PUT: sostituzione completa dei campi editabili della chat
  updateChat: (id, { title, model, favorite }) =>
    request(`/chats/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ title, model, favorite }),
    }),

  // PATCH: modifica di un singolo campo (vedi nota sul PATCH nel README)
  toggleFavorite: (id, favorite) =>
    request(`/chats/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({ favorite }),
    }),

  changeModel: (id, model) =>
    request(`/chats/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({ model }),
    }),

  renameChat: (id, title) =>
    request(`/chats/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({ title }),
    }),

  deleteChat: (id) => request(`/chats/${id}`, { method: 'DELETE' }),

  listModels: () => request('/models'),
}
