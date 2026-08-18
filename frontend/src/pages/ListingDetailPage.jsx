import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getListing } from '../api/listings'
import { startConversation } from '../api/conversations'
import { useAuth } from '../context/AuthContext'
import StatusBadge from '../components/StatusBadge'
import ErrorBanner from '../components/ErrorBanner'

const TYPE_LABELS = {
  WASTE: 'Atık',
  RAW_MATERIAL: 'Hammadde Talebi',
}

export default function ListingDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [listing, setListing] = useState(null)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)

  useEffect(() => {
    getListing(id).then(setListing).catch(setError)
  }, [id])

  async function handleSendMessage(e) {
    e.preventDefault()
    setSending(true)
    setError(null)
    try {
      const conversation = await startConversation({ listingId: Number(id), message })
      navigate(`/conversations/${conversation.id}`)
    } catch (err) {
      setError(err)
    } finally {
      setSending(false)
    }
  }

  if (error && !listing) {
    return <ErrorBanner error={error} />
  }

  if (!listing) {
    return <p className="hint">Yükleniyor...</p>
  }

  const isOwner = user?.companyId != null && user.companyId === listing.companyId

  return (
    <div className="page">
      <div className="card listing-detail">
        <div className="listing-card-header">
          <span className="listing-type">{TYPE_LABELS[listing.type] ?? listing.type}</span>
          <StatusBadge status={listing.status} />
        </div>
        <h1>{listing.title}</h1>
        <p className="listing-meta">
          {listing.category} · {listing.quantity} {listing.unit} · {listing.city}
        </p>
        {listing.price != null && <p className="listing-price">{listing.price} ₺</p>}
        <p>{listing.description}</p>

        {listing.specSheetUrl && (
          <p>
            <a href={listing.specSheetUrl} target="_blank" rel="noreferrer">
              Malzeme belgesi / bilgi formu
            </a>
          </p>
        )}

        {Object.keys(listing.attributes || {}).length > 0 && (
          <>
            <h3>Spesifikasyonlar</h3>
            <ul className="attribute-list">
              {Object.entries(listing.attributes).map(([key, value]) => (
                <li key={key}>
                  <strong>{key}:</strong> {value}
                </li>
              ))}
            </ul>
          </>
        )}

        <p className="listing-company">İlan sahibi: {listing.companyName}</p>

        {isOwner ? (
          <div className="listing-actions">
            <Link className="btn btn-primary" to={`/listings/${listing.id}/edit`}>
              İlanı Düzenle
            </Link>
            <Link className="btn btn-ghost" to="/listings/mine">
              Tüm İlanlarım
            </Link>
          </div>
        ) : (
          <form className="message-form" onSubmit={handleSendMessage}>
            <h3>İlan sahibine mesaj gönder</h3>
            <ErrorBanner error={error} />
            <textarea
              rows={3}
              placeholder="Merhaba, bu ilan hakkında..."
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              required
            />
            <button className="btn btn-primary" type="submit" disabled={sending}>
              {sending ? 'Gönderiliyor...' : 'Mesaj Gönder'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}
