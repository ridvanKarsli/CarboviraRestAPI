const LABELS = {
  ACTIVE: 'Aktif',
  PASSIVE: 'Pasif',
  MATCHED: 'Eşleşti',
  ARCHIVED: 'Arşivlendi',
}

export default function StatusBadge({ status }) {
  return <span className={`status-badge status-${status?.toLowerCase()}`}>{LABELS[status] ?? status}</span>
}
