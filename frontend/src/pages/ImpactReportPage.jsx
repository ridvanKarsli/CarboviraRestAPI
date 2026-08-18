import { useEffect, useState } from 'react'
import { getImpactReport } from '../api/companies'
import ErrorBanner from '../components/ErrorBanner'

function toInstant(dateString, endOfDay) {
  if (!dateString) {
    return undefined
  }
  return `${dateString}T${endOfDay ? '23:59:59' : '00:00:00'}Z`
}

function presetRange(months) {
  const to = new Date()
  const from = new Date()
  from.setMonth(from.getMonth() - months)
  return { from: from.toISOString().slice(0, 10), to: to.toISOString().slice(0, 10) }
}

export default function ImpactReportPage() {
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')
  const [report, setReport] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  function load(from, to) {
    setLoading(true)
    getImpactReport({ from: toInstant(from), to: toInstant(to, true) })
      .then(setReport)
      .catch(setError)
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load(fromDate, toDate)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function applyPreset(months) {
    const range = presetRange(months)
    setFromDate(range.from)
    setToDate(range.to)
    load(range.from, range.to)
  }

  function clearPeriod() {
    setFromDate('')
    setToDate('')
    load('', '')
  }

  return (
    <div className="page">
      <h1>Etki ve Sürdürülebilirlik Raporu</h1>
      <p className="hint">
        Bu rapor kesin bir ölçüm değil; kategori bazlı yayınlanmış ortalamalara göre kaba bir tahmin sunar.
      </p>

      <div className="filter-bar">
        <label className="inline-label">
          Başlangıç
          <input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} />
        </label>
        <label className="inline-label">
          Bitiş
          <input type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} />
        </label>
        <button className="btn btn-primary btn-small" onClick={() => load(fromDate, toDate)}>
          Uygula
        </button>
        <button className="btn btn-ghost btn-small" onClick={() => applyPreset(1)}>
          Son 1 ay
        </button>
        <button className="btn btn-ghost btn-small" onClick={() => applyPreset(12)}>
          Son 1 yıl
        </button>
        <button className="btn btn-ghost btn-small" onClick={clearPeriod}>
          Tüm zamanlar
        </button>
      </div>

      <ErrorBanner error={error} />

      {loading ? (
        <p className="hint">Yükleniyor...</p>
      ) : (
        report && (
          <>
            <div className="stat-grid">
              <div className="stat-card">
                <span className="stat-label">Toplam İlan</span>
                <span className="stat-value">{report.totalListings}</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">Aktif İlan</span>
                <span className="stat-value">{report.activeListings}</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">Arşivlenmiş İlan</span>
                <span className="stat-value">{report.archivedListings}</span>
              </div>
              <div className="stat-card">
                <span className="stat-label">Toplam Görüşme</span>
                <span className="stat-value">{report.totalConversations}</span>
              </div>
            </div>

            <h3>Birim Bazlı Toplam Miktar</h3>
            {Object.keys(report.totalQuantityByUnit || {}).length > 0 ? (
              <ul className="attribute-list">
                {Object.entries(report.totalQuantityByUnit).map(([unit, amount]) => (
                  <li key={unit}>
                    {amount} {unit}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="hint">Kayıtlı miktar yok.</p>
            )}

            <h3>Karbon Ayak İzi (seçilen dönem)</h3>
            <div className="stat-grid">
              <div className="stat-card stat-card-highlight">
                <span className="stat-label">Satıştan önlenen CO2</span>
                <span className="stat-value">{report.co2SavedKgFromSelling} kg</span>
              </div>
              <div className="stat-card stat-card-highlight">
                <span className="stat-label">Satın almadan önlenen CO2</span>
                <span className="stat-value">{report.co2SavedKgFromBuying} kg</span>
              </div>
              <div className="stat-card stat-card-highlight">
                <span className="stat-label">Toplam</span>
                <span className="stat-value">{report.co2SavedKgTotal} kg</span>
              </div>
            </div>
          </>
        )
      )}
    </div>
  )
}
