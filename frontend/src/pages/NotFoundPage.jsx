import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="page not-found">
      <h1>Sayfa bulunamadı</h1>
      <Link to="/">Ana sayfaya dön</Link>
    </div>
  )
}
