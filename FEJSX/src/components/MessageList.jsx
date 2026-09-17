import { useEffect, useRef } from 'react'

function Bubble({ message }) {
  const isUser = message.role === 'USER'
  return (
    <div className={`bubble-row ${isUser ? 'from-user' : 'from-bot'}`}>
      <div className="bubble">
        {message.content.split('\n').map((line, index) => (
          <p key={index}>{line || ' '}</p>
        ))}
      </div>
    </div>
  )
}

export default function MessageList({ messages, pending }) {
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, pending])

  return (
    <div className="message-list">
      {messages.map((message) => (
        <Bubble key={message.id} message={message} />
      ))}

      {pending && (
        <div className="bubble-row from-bot">
          <div className="bubble typing" aria-label="Il modello sta scrivendo">
            <span />
            <span />
            <span />
          </div>
        </div>
      )}

      <div ref={bottomRef} />
    </div>
  )
}
