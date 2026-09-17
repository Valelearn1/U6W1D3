import { useEffect, useRef } from 'react'
import { ChevronDownIcon, PlusIcon, SendIcon } from './Icons'

export default function Composer({
  value,
  onChange,
  onSubmit,
  onNewChat,
  models,
  model,
  onModelChange,
  disabled,
}) {
  const textareaRef = useRef(null)

  // La textarea cresce col testo invece di scrollare.
  useEffect(() => {
    const el = textareaRef.current
    if (!el) return
    el.style.height = 'auto'
    el.style.height = `${Math.min(el.scrollHeight, 200)}px`
  }, [value])

  function handleKeyDown(event) {
    // Invio manda, Shift+Invio va a capo.
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      onSubmit()
    }
  }

  return (
    <form
      className="composer"
      onSubmit={(event) => {
        event.preventDefault()
        onSubmit()
      }}
    >
      <button
        type="button"
        className="icon-button composer-new"
        onClick={onNewChat}
        aria-label="Nuova chat"
        title="Nuova chat"
      >
        <PlusIcon />
      </button>

      <textarea
        ref={textareaRef}
        className="composer-input"
        placeholder="How can I help you?"
        rows={1}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        onKeyDown={handleKeyDown}
      />

      <div className="composer-right">
        <label className="model-picker">
          <select
            value={model}
            onChange={(event) => onModelChange(event.target.value)}
            aria-label="Scegli il modello"
          >
            {models.map((item) => (
              <option key={item.id} value={item.id}>
                {item.label}
              </option>
            ))}
          </select>
          <ChevronDownIcon width={15} height={15} />
        </label>

        <button
          type="submit"
          className="send-button"
          disabled={disabled || value.trim().length === 0}
          aria-label="Invia messaggio"
          title="Invia messaggio"
        >
          <SendIcon width={18} height={18} />
        </button>
      </div>
    </form>
  )
}
