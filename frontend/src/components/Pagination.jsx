export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) {
    return null
  }

  return (
    <div className="pagination">
      <button className="btn btn-ghost" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        Önceki
      </button>
      <span>
        Sayfa {page + 1} / {totalPages}
      </span>
      <button className="btn btn-ghost" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        Sonraki
      </button>
    </div>
  )
}
