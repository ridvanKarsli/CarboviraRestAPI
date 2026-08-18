import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyConversations } from '../api/conversations'
import Pagination from '../components/Pagination'
import ErrorBanner from '../components/ErrorBanner'

export default function ConversationsPage() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  function load(nextPage = page) {
    setLoading(true)
    getMyConversations({ page: nextPage })
      .then((data) => {
        setResult(data)
        setPage(nextPage)
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load(0)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div className="page">
      <h1>Görüşmelerim</h1>
      <ErrorBanner error={error} />
      {loading ? (
        <p className="hint">Yükleniyor...</p>
      ) : result?.content?.length ? (
        <>
          <div className="conversation-list">
            {result.content.map((conversation) => (
              <Link key={conversation.id} to={`/conversations/${conversation.id}`} className="conversation-row">
                <div>
                  <strong>{conversation.counterpartCompanyName}</strong>
                  <p className="hint">{conversation.listingTitle}</p>
                </div>
                <span className="hint">{new Date(conversation.createdAt).toLocaleDateString('tr-TR')}</span>
              </Link>
            ))}
          </div>
          <Pagination page={result.page} totalPages={result.totalPages} onChange={load} />
        </>
      ) : (
        <p className="hint">Henüz bir görüşmeniz yok.</p>
      )}
    </div>
  )
}
