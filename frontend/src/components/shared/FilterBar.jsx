export default function FilterBar({
  query,
  onQueryChange,
  placeholder = "Search...",
  right,
}) {
  return (
    <div className="d-flex gap-2 align-items-center mb-3">
      <input
        className="form-control"
        value={query}
        onChange={(e) => onQueryChange(e.target.value)}
        placeholder={placeholder}
      />
      {right ? <div className="d-flex gap-2">{right}</div> : null}
    </div>
  );
}