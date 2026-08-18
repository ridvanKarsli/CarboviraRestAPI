import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { deleteListing, getMyListings, updateListingStatus } from '../api/listings'
import StatusBadge from '../components/StatusBadge'
import Pagination from '../components/Pagination'
import ErrorBanner from '../components/ErrorBanner'

export default function MyListingsPage() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  function load(nextPage = page) {
    setLoading(true)
    getMyListings({ page: nextPage })
      .then((data) => {
        setResult(data)
        setPage(nextPage)
        setError(null)
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load(0)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  async function handleStatusChange(id, status) {
    try {
      await updateListingStatus(id, status)
      load(page)
    } catch (err) {
      setError(err)
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('İlanı silmek istediğinize emin misiniz?')) {
      return
    }
    try {
      await deleteListing(id)
      load(page)
    } catch (err) {
      setError(err)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>İlanlarım</h1>
        <Link to="/listings/new" className="btn btn-primary">
          Yeni İlan
        </Link>
      </div>

      <ErrorBanner error={error} />

      {loading ? (
        <p className="hint">Yükleniyor...</p>
      ) : result?.content?.length ? (
        <>
          <table className="table">
            <thead>
              <tr>
                <th>Başlık</th>
                <th>Kategori</th>
                <th>Miktar</th>
                <th>Durum</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {result.content.map((listing) => (
                <tr key={listing.id}>
                  <td>
                    <Link to={`/listings/${listing.id}`}>{listing.title}</Link>
                  </td>
                  <td>{listing.category}</td>
                  <td>
                    {listing.quantity} {listing.unit}
                  </td>
                  <td>
                    <StatusBadge status={listing.status} />
                  </td>
                  <td className="table-actions">
                    <Link className="btn btn-ghost btn-small" to={`/listings/${listing.id}/edit`}>
                      Düzenle
                    </Link>
                    {listing.status !== 'ACTIVE' && (
                      <button className="btn btn-ghost btn-small" onClick={() => handleStatusChange(listing.id, 'ACTIVE')}>
                        Aktifleştir
                      </button>
                    )}
                    {listing.status === 'ACTIVE' && (
                      <button className="btn btn-ghost btn-small" onClick={() => handleStatusChange(listing.id, 'PASSIVE')}>
                        Pasifleştir
                      </button>
                    )}
                    {listing.status !== 'ARCHIVED' && (
                      <button className="btn btn-ghost btn-small" onClick={() => handleStatusChange(listing.id, 'ARCHIVED')}>
                        Arşivle
                      </button>
                    )}
                    <button className="btn btn-danger btn-small" onClick={() => handleDelete(listing.id)}>
                      Sil
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination page={result.page} totalPages={result.totalPages} onChange={load} />
        </>
      ) : (
        <p className="hint">Henüz bir ilanınız yok.</p>
      )}
    </div>
  )
}
