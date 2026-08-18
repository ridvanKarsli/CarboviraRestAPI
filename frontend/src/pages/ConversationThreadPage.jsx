import { useEffect, useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getMessages, sendMessage } from '../api/conversations'
import { useAuth } from '../context/AuthContext'
import ErrorBanner from '../components/ErrorBanner'

const POLL_INTERVAL_MS = 5000

export default function ConversationThreadPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const [messages, setMessages] = useState([])
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [content, setContent] = useState('')
  const [sending, setSending] = useState(false)
  const bottomRef = useRef(null)

  function loadMessages() {
    getMessages(id, { size: 100 })
      .then((data) => {
        setMessages(data.content)
        setError(null)
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadMessages()
    const interval = setInterval(loadMessages, POLL_INTERVAL_MS)
    return () => clearInterval(interval)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  async function handleSend(e) {
    e.preventDefault()
    if (!content.trim()) {
      return
    }
    setSending(true)
    try {
      await sendMessage(id, content)
      setContent('')
      loadMessages()
    } catch (err) {
      setError(err)
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="page">
      <Link to="/conversations" className="back-link">
        ← Görüşmelerim
      </Link>
      <div className="card thread-card">
        {loading ? (
          <p className="hint">Yükleniyor...</p>
        ) : (
          <div className="thread-messages">
            {messages.map((message) => (
              <div
                key={message.id}
                className={`thread-message ${message.senderCompanyId === user?.companyId ? 'mine' : ''}`}
              >
                <div className="thread-message-meta">
                  {message.senderName} · {message.senderCompanyName}
                </div>
                <div className="thread-message-content">{message.content}</div>
                <div className="thread-message-time">{new Date(message.sentAt).toLocaleString('tr-TR')}</div>
              </div>
            ))}
            <div ref={bottomRef} />
          </div>
        )}

        <ErrorBanner error={error} />

        <form className="thread-composer" onSubmit={handleSend}>
          <textarea
            rows={2}
            placeholder="Mesajınızı yazın..."
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
          <button className="btn btn-primary" type="submit" disabled={sending}>
            Gönder
          </button>
        </form>
      </div>
    </div>
  )
}
