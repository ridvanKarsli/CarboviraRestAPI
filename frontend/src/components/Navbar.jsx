import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand">
          Carbovira
        </Link>
        {user ? (
          <nav className="nav-links">
            <Link to="/">İlanlar</Link>
            <Link to="/listings/nearby">Yakınımdakiler</Link>
            <Link to="/listings/mine">İlanlarım</Link>
            <Link to="/conversations">Görüşmeler</Link>
            <Link to="/company">Firmam</Link>
            <Link to="/impact-report">Etki Raporu</Link>
            <span className="user-chip">
              {user.fullName} · {user.companyName ?? 'Platform Yönetimi'}
            </span>
            <button className="btn btn-ghost" onClick={handleLogout}>
              Çıkış
            </button>
          </nav>
        ) : (
          <nav className="nav-links">
            <Link to="/login">Giriş</Link>
            <Link to="/register" className="btn btn-primary btn-small">
              Kayıt Ol
            </Link>
          </nav>
        )}
      </div>
    </header>
  )
}
