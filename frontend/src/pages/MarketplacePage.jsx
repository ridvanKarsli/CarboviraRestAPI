import { useEffect, useState } from 'react'
import { searchListings } from '../api/listings'
import ListingCard from '../components/ListingCard'
import Pagination from '../components/Pagination'
import ErrorBanner from '../components/ErrorBanner'

const INITIAL_FILTERS = { type: '', category: '', city: '', q: '' }

export default function MarketplacePage() {
  const [filters, setFilters] = useState(INITIAL_FILTERS)
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    setLoading(true)
    searchListings({ ...filters, page })
      .then((data) => {
        if (active) {
          setResult(data)
          setError(null)
        }
      })
      .catch((err) => active && setError(err))
      .finally(() => active && setLoading(false))
    return () => {
      active = false
    }
  }, [filters, page])

  function handleFilterChange(field, value) {
    setPage(0)
    setFilters((prev) => ({ ...prev, [field]: value }))
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>İlanlar</h1>
      </div>

      <div className="filter-bar">
        <select value={filters.type} onChange={(e) => handleFilterChange('type', e.target.value)}>
          <option value="">Tüm tipler</option>
          <option value="WASTE">Atık</option>
          <option value="RAW_MATERIAL">Hammadde talebi</option>
        </select>
        <input
          placeholder="Kategori (ör. Plastik)"
          value={filters.category}
          onChange={(e) => handleFilterChange('category', e.target.value)}
        />
        <input
          placeholder="Şehir (ör. İstanbul)"
          value={filters.city}
          onChange={(e) => handleFilterChange('city', e.target.value)}
        />
        <input
          placeholder="Ara (başlık/açıklama)"
          value={filters.q}
          onChange={(e) => handleFilterChange('q', e.target.value)}
        />
      </div>

      <ErrorBanner error={error} />

      {loading ? (
        <p className="hint">Yükleniyor...</p>
      ) : result?.content?.length ? (
        <>
          <div className="listing-grid">
            {result.content.map((listing) => (
              <ListingCard key={listing.id} listing={listing} />
            ))}
          </div>
          <Pagination page={result.page} totalPages={result.totalPages} onChange={setPage} />
        </>
      ) : (
        <p className="hint">Filtrelere uyan ilan bulunamadı.</p>
      )}
    </div>
  )
}
