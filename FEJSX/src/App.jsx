import { useCallback, useEffect, useState } from 'react'
import './App.css'
import { api } from './api/client'
import AuthModal from './components/AuthModal'
import Composer from './components/Composer'
import Mascot from './components/Mascot'
import MessageList from './components/MessageList'
import Modal from './components/Modal'
import Sidebar from './components/Sidebar'
import TopBar from './components/TopBar'
import { useAuth } from './hooks/authContext'
import { useTheme } from './hooks/useTheme'

// Fallback usato finché /api/models non risponde (BE spento, primo avvio...).
const FALLBACK_MODELS = [
  {
    id: 'nvidia/nemotron-3-nano-omni-30b-a3b-reasoning:free',
    label: 'Nemotron 3 Nano Omni (free)',
  },
]

export default function App() {
  const { theme, toggleTheme } = useTheme()
  const { user, isLoggedIn, loading: authLoading } = useAuth()

  const [chats, setChats] = useState([])
  const [activeChatId, setActiveChatId] = useState(null)
  const [messages, setMessages] = useState([])

  const [models, setModels] = useState(FALLBACK_MODELS)
  const [model, setModel] = useState(FALLBACK_MODELS[0].id)

  const [draft, setDraft] = useState('')
  const [pending, setPending] = useState(false)
  const [error, setError] = useState(null)
  const [modal, setModal] = useState(null) // 'auth' | 'settings' | null

  const refreshChats = useCallback(async () => {
    const list = await api.listChats()
    setChats(list)
    return list
  }, [])

  // I modelli sono pubblici: si caricano anche da sloggati.
  useEffect(() => {
    let cancelled = false

    api.listModels()
      .then((available) => {
        if (cancelled || available.length === 0) return
        setModels(available)
        setModel((current) =>
          available.some((item) => item.id === current) ? current : available[0].id,
        )
      })
      .catch((err) => {
        if (!cancelled) setError(`Backend non raggiungibile: ${err.message}`)
      })

    return () => {
      cancelled = true
    }
  }, [])

  // Le chat sono private: si caricano solo dopo il login.
  useEffect(() => {
    if (!isLoggedIn) return

    let cancelled = false

    async function load() {
      try {
        const list = await api.listChats()
        if (!cancelled) setChats(list)
      } catch (err) {
        if (!cancelled) setError(err.message)
      }
    }

    load()

    return () => {
      cancelled = true
    }
  }, [isLoggedIn])

  // Al logout non svuotiamo lo stato con un altro effetto: lo deriviamo.
  // Sloggati non si vede nulla, e al rientro le chat vengono comunque ricaricate.
  const visibleChats = isLoggedIn ? chats : []
  const visibleMessages = isLoggedIn ? messages : []
  const currentChatId = isLoggedIn ? activeChatId : null

  async function openChat(id) {
    setError(null)
    try {
      const chat = await api.getChat(id)
      setActiveChatId(chat.id)
      setMessages(chat.messages)
      setModel(chat.model)
    } catch (err) {
      setError(err.message)
    }
  }

  function startNewChat() {
    // Nessuna riga a DB finché non parte il primo messaggio.
    setActiveChatId(null)
    setMessages([])
    setDraft('')
    setError(null)
  }

  async function handleSend() {
    const content = draft.trim()
    if (!content || pending) return

    // Senza login non si può salvare nulla: chiediamo di accedere.
    if (!isLoggedIn) {
      setModal('auth')
      return
    }

    setError(null)
    setDraft('')
    setPending(true)

    // Eco ottimistico del messaggio utente, così la UI risponde subito.
    const optimistic = {
      id: `local-${Date.now()}`,
      role: 'USER',
      content,
    }
    setMessages((current) => [...current, optimistic])

    try {
      let chatId = activeChatId
      if (!chatId) {
        const created = await api.createChat(model)
        chatId = created.id
        setActiveChatId(chatId)
      }

      const reply = await api.sendMessage(chatId, content)
      // Il BE restituisce la coppia salvata (utente + assistente) con gli id veri.
      setMessages((current) => [
        ...current.filter((item) => item.id !== optimistic.id),
        reply.userMessage,
        reply.assistantMessage,
      ])
      await refreshChats()
    } catch (err) {
      setMessages((current) => current.filter((item) => item.id !== optimistic.id))
      setDraft(content)
      setError(err.message)
    } finally {
      setPending(false)
    }
  }

  async function handleToggleFavorite(chat) {
    // Aggiornamento ottimistico: la stella reagisce subito.
    setChats((current) =>
      current.map((item) =>
        item.id === chat.id ? { ...item, favorite: !item.favorite } : item,
      ),
    )
    try {
      await api.toggleFavorite(chat.id, !chat.favorite)
      await refreshChats()
    } catch (err) {
      setError(err.message)
      await refreshChats()
    }
  }

  async function handleDelete(chat) {
    try {
      await api.deleteChat(chat.id)
      if (chat.id === activeChatId) startNewChat()
      await refreshChats()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleModelChange(nextModel) {
    setModel(nextModel)
    // Una chat già esistente si aggiorna con PATCH: cambia un campo solo.
    if (!activeChatId || !isLoggedIn) return
    try {
      await api.changeModel(activeChatId, nextModel)
      await refreshChats()
    } catch (err) {
      setError(err.message)
    }
  }

  const isEmptyState = visibleMessages.length === 0

  if (authLoading) {
    return (
      <div className="app boot">
        <p className="boot-text">Caricamento...</p>
      </div>
    )
  }

  return (
    <div className="app">
      <TopBar
        theme={theme}
        onToggleTheme={toggleTheme}
        onOpenAuth={() => setModal('auth')}
        onOpenSettings={() => setModal('settings')}
      />

      <div className="app-body">
        <Sidebar
          chats={visibleChats}
          activeChatId={currentChatId}
          isLoggedIn={isLoggedIn}
          onNewChat={startNewChat}
          onSelect={openChat}
          onToggleFavorite={handleToggleFavorite}
          onDelete={handleDelete}
          onRequestLogin={() => setModal('auth')}
        />

        <main className={`workspace${isEmptyState ? ' is-empty' : ''}`}>
          {isEmptyState ? (
            <div className="greeting">
              <Mascot />
              <h1 className="greeting-title">
                {isLoggedIn ? `Hello, ${user.displayName}!` : 'Hello!'}
              </h1>
              <p className="greeting-sub">
                {isLoggedIn
                  ? 'Scrivi qui sotto per iniziare una nuova conversazione.'
                  : 'Accedi per salvare le tue conversazioni.'}
              </p>
            </div>
          ) : (
            <MessageList messages={visibleMessages} pending={pending} />
          )}

          {error && (
            <p className="error-banner" role="alert">
              {error}
            </p>
          )}

          <Composer
            value={draft}
            onChange={setDraft}
            onSubmit={handleSend}
            onNewChat={startNewChat}
            models={models}
            model={model}
            onModelChange={handleModelChange}
            disabled={pending}
          />
        </main>
      </div>

      {modal === 'auth' && <AuthModal onClose={() => setModal(null)} />}

      {modal === 'settings' && (
        <Modal title="Impostazioni" onClose={() => setModal(null)}>
          <label className="field">
            <span>Modello predefinito</span>
            <select value={model} onChange={(e) => handleModelChange(e.target.value)}>
              {models.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.label}
                </option>
              ))}
            </select>
          </label>
          <label className="field row">
            <span>Tema scuro</span>
            <input
              type="checkbox"
              checked={theme === 'dark'}
              onChange={toggleTheme}
            />
          </label>
          {isLoggedIn && (
            <p className="modal-note">
              Sei connesso come <strong>{user.email}</strong>.
            </p>
          )}
        </Modal>
      )}
    </div>
  )
}
