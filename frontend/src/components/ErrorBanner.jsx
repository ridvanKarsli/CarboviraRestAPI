export default function ErrorBanner({ error }) {
  if (!error) {
    return null
  }

  const violations = error.body?.violations
  return (
    <div className="error-banner">
      <p>{error.body?.message || error.message}</p>
      {violations?.length > 0 && (
        <ul>
          {violations.map((v) => (
            <li key={v.field}>
              {v.field}: {v.message}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
