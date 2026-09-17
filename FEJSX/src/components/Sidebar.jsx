import { PlusIcon, StarIcon, TrashIcon } from './Icons'

function ChatRow({ chat, isActive, onSelect, onToggleFavorite, onDelete }) {
  return (
    <li className={`chat-row${isActive ? ' is-active' : ''}`}>
      <button
        type="button"
        className="chat-row-title"
        onClick={() => onSelect(chat.id)}
        title={chat.title}
      >
        {chat.title}
      </button>

      <span className="chat-row-tools">
        <button
          type="button"
          className="icon-button tiny"
          onClick={() => onToggleFavorite(chat)}
          aria-label={
            chat.favorite ? 'Togli dai preferiti' : 'Aggiungi ai preferiti'
          }
          title={chat.favorite ? 'Togli dai preferiti' : 'Aggiungi ai preferiti'}
        >
          <StarIcon
            width={15}
            height={15}
            filled={chat.favorite}
            className={chat.favorite ? 'is-favorite' : undefined}
          />
        </button>

        <button
          type="button"
          className="icon-button tiny"
          onClick={() => onDelete(chat)}
          aria-label="Elimina chat"
          title="Elimina chat"
        >
          <TrashIcon width={15} height={15} />
        </button>
      </span>
    </li>
  )
}

function Section({
  label,
  chats,
  emptyHint,
  activeChatId,
  onSelect,
  onToggleFavorite,
  onDelete,
}) {
  return (
    <div className="sidebar-section">
      <h2 className="sidebar-label">{label}</h2>
      {chats.length === 0 ? (
        <p className="sidebar-empty">{emptyHint}</p>
      ) : (
        <ul className="chat-list">
          {chats.map((chat) => (
            <ChatRow
              key={chat.id}
              chat={chat}
              isActive={chat.id === activeChatId}
              onSelect={onSelect}
              onToggleFavorite={onToggleFavorite}
              onDelete={onDelete}
            />
          ))}
        </ul>
      )}
    </div>
  )
}

export default function Sidebar({
  chats,
  activeChatId,
  isLoggedIn,
  onNewChat,
  onSelect,
  onToggleFavorite,
  onDelete,
  onRequestLogin,
}) {
  const favorites = chats.filter((chat) => chat.favorite)
  const previous = chats.filter((chat) => !chat.favorite)
  const shared = { activeChatId, onSelect, onToggleFavorite, onDelete }

  // Le chat sono per utente: da sloggati non c'e' nulla da mostrare.
  if (!isLoggedIn) {
    return (
      <aside className="sidebar">
        <button type="button" className="new-chat-button" onClick={onNewChat}>
          <PlusIcon width={17} height={17} />
          <span>Nuova chat</span>
        </button>

        <div className="sidebar-section">
          <h2 className="sidebar-label">Le tue chat</h2>
          <p className="sidebar-empty">
            Accedi per vedere e salvare le tue conversazioni.
          </p>
          <button type="button" className="link-button indent" onClick={onRequestLogin}>
            Accedi o registrati
          </button>
        </div>
      </aside>
    )
  }

  return (
    <aside className="sidebar">
      <button type="button" className="new-chat-button" onClick={onNewChat}>
        <PlusIcon width={17} height={17} />
        <span>Nuova chat</span>
      </button>

      <nav className="sidebar-scroll">
        <Section
          label="Preferite"
          chats={favorites}
          emptyHint="Clicca la stella per salvare una chat qui."
          {...shared}
        />
        <Section
          label="Chat precedenti"
          chats={previous}
          emptyHint="Nessuna conversazione ancora."
          {...shared}
        />
      </nav>
    </aside>
  )
}
