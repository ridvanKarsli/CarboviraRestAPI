import { Link } from 'react-router-dom'
import StatusBadge from './StatusBadge'

const TYPE_LABELS = {
  WASTE: 'Atık',
  RAW_MATERIAL: 'Hammadde Talebi',
}

export default function ListingCard({ listing }) {
  return (
    <Link to={`/listings/${listing.id}`} className="listing-card">
      <div className="listing-card-header">
        <span className="listing-type">{TYPE_LABELS[listing.type] ?? listing.type}</span>
        <StatusBadge status={listing.status} />
      </div>
      <h3>{listing.title}</h3>
      <p className="listing-meta">
        {listing.category} · {listing.quantity} {listing.unit} · {listing.city}
      </p>
      {listing.price != null && <p className="listing-price">{listing.price} ₺</p>}
      <p className="listing-company">{listing.companyName}</p>
    </Link>
  )
}
