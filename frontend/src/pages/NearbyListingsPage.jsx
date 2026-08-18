import { useEffect, useState } from 'react'
import { getNearbyListings } from '../api/listings'
import ListingCard from '../components/ListingCard'
import Pagination from '../components/Pagination'
import ErrorBanner from '../components/ErrorBanner'

export default function NearbyListingsPage() {
  const [radiusKm, setRadiusKm] = useState(50)
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  function runSearch(nextPage = page) {
    setLoading(true)
    setError(null)
    getNearbyListings({ radiusKm, page: nextPage })
      .then((data) => {
        setResult(data)
        setPage(nextPage)
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    runSearch(0)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div className="page">
      <div className="page-header">
        <h1>Yakınımdaki İlanlar</h1>
      </div>
      <p className="hint">
        Firma profilinize konum eklediyseniz, belirttiğiniz yarıçap içindeki aktif ilanları en yakından en uzağa
        sıralar. Atık taşımacılığı belli bir mesafeden sonra ekonomik olmaktan çıktığı için bu arama öncelik sırasını
        gösteriyor.
      </p>

      <div className="filter-bar">
        <label className="inline-label">
          Yarıçap (km)
          <input
            type="number"
            min="1"
            value={radiusKm}
            onChange={(e) => setRadiusKm(Number(e.target.value))}
          />
        </label>
        <button className="btn btn-primary btn-small" onClick={() => runSearch(0)}>
          Ara
        </button>
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
          <Pagination page={result.page} totalPages={result.totalPages} onChange={runSearch} />
        </>
      ) : (
        !error && <p className="hint">Bu yarıçapta ilan bulunamadı.</p>
      )}
    </div>
  )
}
