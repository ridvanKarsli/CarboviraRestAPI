import { useState } from 'react'

// Listing.attributes backend'de sabit kolonlar yerine key-value tutuluyor (nem oranı, saflık
// yüzdesi vs. malzemeye göre değişiyor) — burada da aynı mantıkla serbest bir satır listesi.
export default function AttributesEditor({ value, onChange }) {
  const [newKey, setNewKey] = useState('')
  const [newValue, setNewValue] = useState('')

  const entries = Object.entries(value || {})

  function updateEntry(key, nextValue) {
    onChange({ ...value, [key]: nextValue })
  }

  function removeEntry(key) {
    const next = { ...value }
    delete next[key]
    onChange(next)
  }

  function addEntry() {
    if (!newKey.trim()) {
      return
    }
    onChange({ ...value, [newKey.trim()]: newValue })
    setNewKey('')
    setNewValue('')
  }

  return (
    <div className="attributes-editor">
      {entries.map(([key, val]) => (
        <div className="attribute-row" key={key}>
          <span className="attribute-key">{key}</span>
          <input value={val} onChange={(e) => updateEntry(key, e.target.value)} />
          <button type="button" className="btn btn-ghost btn-small" onClick={() => removeEntry(key)}>
            Sil
          </button>
        </div>
      ))}
      <div className="attribute-row attribute-row-new">
        <input placeholder="alan adı (örn. nem oranı)" value={newKey} onChange={(e) => setNewKey(e.target.value)} />
        <input placeholder="değer (örn. %5)" value={newValue} onChange={(e) => setNewValue(e.target.value)} />
        <button type="button" className="btn btn-ghost btn-small" onClick={addEntry}>
          Ekle
        </button>
      </div>
    </div>
  )
}
