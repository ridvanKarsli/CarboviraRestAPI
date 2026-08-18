import { useEffect, useState } from 'react'
import { getMyCompany, updateMyCompany } from '../api/companies'
import ErrorBanner from '../components/ErrorBanner'

export default function CompanyProfilePage() {
  const [form, setForm] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    getMyCompany()
      .then((company) =>
        setForm({
          name: company.name,
          sector: company.sector || '',
          city: company.city || '',
          address: company.address || '',
          description: company.description || '',
          latitude: company.latitude ?? '',
          longitude: company.longitude ?? '',
          taxNumber: company.taxNumber,
          verified: company.verified,
        }),
      )
      .catch(setError)
      .finally(() => setLoading(false))
  }, [])

  function update(field, value) {
    setSaved(false)
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  function useCurrentLocation() {
    if (!navigator.geolocation) {
      setError(new Error('Tarayıcınız konum bilgisini desteklemiyor.'))
      return
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        update('latitude', position.coords.latitude)
        update('longitude', position.coords.longitude)
      },
      () => setError(new Error('Konum alınamadı, izin verdiğinizden emin olun.')),
    )
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      const payload = {
        name: form.name,
        sector: form.sector,
        city: form.city,
        address: form.address,
        description: form.description,
        latitude: form.latitude === '' ? null : Number(form.latitude),
        longitude: form.longitude === '' ? null : Number(form.longitude),
      }
      await updateMyCompany(payload)
      setSaved(true)
    } catch (err) {
      setError(err)
    } finally {
      setSaving(false)
    }
  }

  if (loading || !form) {
    return <p className="hint">Yükleniyor...</p>
  }

  return (
    <div className="page">
      <h1>Firma Profilim</h1>
      <form className="card form-card" onSubmit={handleSubmit}>
        <ErrorBanner error={error} />
        {saved && <p className="success-banner">Kaydedildi.</p>}
        <div className="form-grid">
          <label>
            Vergi numarası
            <input value={form.taxNumber} disabled />
          </label>
          <label>
            Onay durumu
            <input value={form.verified ? 'Onaylı' : 'Onay bekliyor'} disabled />
          </label>
          <label className="span-2">
            Firma adı
            <input value={form.name} onChange={(e) => update('name', e.target.value)} required />
          </label>
          <label>
            Sektör
            <input value={form.sector} onChange={(e) => update('sector', e.target.value)} />
          </label>
          <label>
            Şehir
            <input value={form.city} onChange={(e) => update('city', e.target.value)} />
          </label>
          <label className="span-2">
            Adres
            <input value={form.address} onChange={(e) => update('address', e.target.value)} />
          </label>
          <label className="span-2">
            Açıklama
            <textarea rows={3} value={form.description} onChange={(e) => update('description', e.target.value)} />
          </label>
          <label>
            Enlem
            <input type="number" step="0.0001" value={form.latitude} onChange={(e) => update('latitude', e.target.value)} />
          </label>
          <label>
            Boylam
            <input
              type="number"
              step="0.0001"
              value={form.longitude}
              onChange={(e) => update('longitude', e.target.value)}
            />
          </label>
        </div>
        <button type="button" className="btn btn-ghost btn-small" onClick={useCurrentLocation}>
          Konumumu kullan
        </button>
        <p className="hint">
          Konum bilgisi yalnızca "Yakınımdaki İlanlar" aramasında kullanılıyor, isteğe bağlıdır.
        </p>
        <button className="btn btn-primary" type="submit" disabled={saving}>
          {saving ? 'Kaydediliyor...' : 'Kaydet'}
        </button>
      </form>
    </div>
  )
}
