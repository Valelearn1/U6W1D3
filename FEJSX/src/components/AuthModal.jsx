import { useState } from 'react'
import { useAuth } from '../hooks/authContext'
import Modal from './Modal'

export default function AuthModal({ onClose }) {
  const { login, register } = useAuth()

  const [mode, setMode] = useState('login') // 'login' | 'signup'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const isSignup = mode === 'signup'

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setBusy(true)
    try {
      if (isSignup) {
        await register(email.trim(), password, displayName.trim())
      } else {
        await login(email.trim(), password)
      }
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  function switchMode() {
    setMode(isSignup ? 'login' : 'signup')
    setError(null)
  }

  return (
    <Modal title={isSignup ? 'Crea un account' : 'Accedi'} onClose={onClose}>
      <form className="auth-form" onSubmit={handleSubmit}>
        {isSignup && (
          <label className="field">
            <span>Nome</span>
            <input
              type="text"
              value={displayName}
              onChange={(e) => setDisplayName(e.target.value)}
              placeholder="Come ti chiami"
              autoComplete="name"
              required
            />
          </label>
        )}

        <label className="field">
          <span>Email</span>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="tu@esempio.it"
            autoComplete="email"
            required
          />
        </label>

        <label className="field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder={isSignup ? 'Almeno 8 caratteri' : '••••••••'}
            autoComplete={isSignup ? 'new-password' : 'current-password'}
            minLength={isSignup ? 8 : undefined}
            required
          />
        </label>

        {error && (
          <p className="form-error" role="alert">
            {error}
          </p>
        )}

        <button type="submit" className="primary-button full" disabled={busy}>
          {busy ? 'Attendi...' : isSignup ? 'Crea account' : 'Accedi'}
        </button>

        <p className="auth-switch">
          {isSignup ? 'Hai gia un account?' : 'Non hai un account?'}{' '}
          <button type="button" className="link-button" onClick={switchMode}>
            {isSignup ? 'Accedi' : 'Registrati'}
          </button>
        </p>
      </form>
    </Modal>
  )
}
