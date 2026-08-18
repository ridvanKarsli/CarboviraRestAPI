import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import ErrorBanner from '../components/ErrorBanner'

const INITIAL_FORM = {
  companyName: '',
  taxNumber: '',
  sector: '',
  city: '',
  address: '',
  fullName: '',
  email: '',
  password: '',
}

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(INITIAL_FORM)
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await register(form)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="card auth-card auth-card-wide" onSubmit={handleSubmit}>
        <h1>Firma Kaydı</h1>
        <p className="hint">Kayıt, firmanızı ve ilk yönetici kullanıcısını birlikte oluşturur.</p>
        <ErrorBanner error={error} />
        <div className="form-grid">
          <label>
            Firma adı
            <input value={form.companyName} onChange={(e) => update('companyName', e.target.value)} required />
          </label>
          <label>
            Vergi numarası
            <input value={form.taxNumber} onChange={(e) => update('taxNumber', e.target.value)} required />
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
          <label>
            Ad soyad
            <input value={form.fullName} onChange={(e) => update('fullName', e.target.value)} required />
          </label>
          <label>
            E-posta
            <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required />
          </label>
          <label>
            Şifre
            <input
              type="password"
              value={form.password}
              onChange={(e) => update('password', e.target.value)}
              minLength={8}
              required
            />
          </label>
        </div>
        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? 'Kaydediliyor...' : 'Kayıt Ol'}
        </button>
        <p className="auth-switch">
          Zaten hesabın var mı? <Link to="/login">Giriş yap</Link>
        </p>
      </form>
    </div>
  )
}
