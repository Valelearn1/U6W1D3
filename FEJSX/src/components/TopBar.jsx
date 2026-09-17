import { useAuth } from '../hooks/authContext'
import { LogoutIcon, MoonIcon, SettingsIcon, SunIcon, UserIcon } from './Icons'

export default function TopBar({ theme, onToggleTheme, onOpenAuth, onOpenSettings }) {
  const { user, isLoggedIn, logout } = useAuth()
  const isDark = theme === 'dark'

  return (
    <header className="topbar">
      <div className="topbar-brand">
        <span className="brand-dot" aria-hidden="true" />
        <span className="brand-name">ChatBot</span>
      </div>

      <div className="topbar-actions">
        {isLoggedIn ? (
          <>
            <span className="user-chip" title={user.email}>
              <span className="user-avatar" aria-hidden="true">
                {user.displayName.charAt(0).toUpperCase()}
              </span>
              <span className="user-name">{user.displayName}</span>
            </span>
            <button
              type="button"
              className="icon-button"
              onClick={logout}
              aria-label="Esci"
              title="Esci"
            >
              <LogoutIcon />
            </button>
          </>
        ) : (
          <button
            type="button"
            className="ghost-button auth-button"
            onClick={onOpenAuth}
          >
            <UserIcon width={18} height={18} />
            <span>Login / Signup</span>
          </button>
        )}

        <button
          type="button"
          className="icon-button"
          onClick={onOpenSettings}
          aria-label="Impostazioni"
          title="Impostazioni"
        >
          <SettingsIcon />
        </button>

        <button
          type="button"
          className="icon-button theme-toggle"
          onClick={onToggleTheme}
          aria-label={isDark ? 'Passa al tema chiaro' : 'Passa al tema scuro'}
          title={isDark ? 'Tema chiaro' : 'Tema scuro'}
        >
          {isDark ? <SunIcon /> : <MoonIcon />}
        </button>
      </div>
    </header>
  )
}
