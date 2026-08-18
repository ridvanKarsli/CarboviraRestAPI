import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createListing, getListing, updateListing } from '../api/listings'
import AttributesEditor from '../components/AttributesEditor'
import ErrorBanner from '../components/ErrorBanner'

const EMPTY_FORM = {
  type: 'WASTE',
  title: '',
  category: '',
  description: '',
  quantity: '',
  unit: 'kg',
  city: '',
  price: '',
  specSheetUrl: '',
  attributes: {},
}

export default function ListingFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const [form, setForm] = useState(EMPTY_FORM)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!isEdit) {
      return
    }
    getListing(id)
      .then((listing) =>
        setForm({
          type: listing.type,
          title: listing.title,
          category: listing.category,
          description: listing.description || '',
          quantity: listing.quantity,
          unit: listing.unit,
          city: listing.city,
          price: listing.price ?? '',
          specSheetUrl: listing.specSheetUrl || '',
          attributes: listing.attributes || {},
        }),
      )
      .catch(setError)
      .finally(() => setLoading(false))
  }, [id, isEdit])

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)

    const payload = {
      ...form,
      quantity: form.quantity === '' ? null : Number(form.quantity),
      price: form.price === '' ? null : Number(form.price),
    }

    try {
      if (isEdit) {
        // type güncelleme uçtan değiştirilemiyor, sadece isteğe eklemiyoruz.
        const { type, ...updatePayload } = payload
        await updateListing(id, updatePayload)
        navigate(`/listings/${id}`)
      } else {
        const created = await createListing(payload)
        navigate(`/listings/${created.id}`)
      }
    } catch (err) {
      setError(err)
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return <p className="hint">Yükleniyor...</p>
  }

  return (
    <div className="page">
      <h1>{isEdit ? 'İlanı Düzenle' : 'Yeni İlan'}</h1>
      <form className="card form-card" onSubmit={handleSubmit}>
        <ErrorBanner error={error} />
        <div className="form-grid">
          {!isEdit && (
            <label>
              Tip
              <select value={form.type} onChange={(e) => update('type', e.target.value)}>
                <option value="WASTE">Atık</option>
                <option value="RAW_MATERIAL">Hammadde talebi</option>
              </select>
            </label>
          )}
          <label className="span-2">
            Başlık
            <input value={form.title} onChange={(e) => update('title', e.target.value)} required />
          </label>
          <label>
            Kategori
            <input value={form.category} onChange={(e) => update('category', e.target.value)} required />
          </label>
          <label>
            Şehir
            <input value={form.city} onChange={(e) => update('city', e.target.value)} required />
          </label>
          <label>
            Miktar
            <input
              type="number"
              step="0.001"
              value={form.quantity}
              onChange={(e) => update('quantity', e.target.value)}
              required
            />
          </label>
          <label>
            Birim
            <input value={form.unit} onChange={(e) => update('unit', e.target.value)} required />
          </label>
          <label>
            Fiyat (opsiyonel)
            <input type="number" step="0.01" value={form.price} onChange={(e) => update('price', e.target.value)} />
          </label>
          <label>
            Belge linki (opsiyonel)
            <input value={form.specSheetUrl} onChange={(e) => update('specSheetUrl', e.target.value)} />
          </label>
          <label className="span-2">
            Açıklama
            <textarea rows={4} value={form.description} onChange={(e) => update('description', e.target.value)} />
          </label>
        </div>

        <h3>Ek Spesifikasyonlar</h3>
        <AttributesEditor value={form.attributes} onChange={(attributes) => update('attributes', attributes)} />

        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? 'Kaydediliyor...' : isEdit ? 'Güncelle' : 'İlanı Yayınla'}
        </button>
      </form>
    </div>
  )
}
