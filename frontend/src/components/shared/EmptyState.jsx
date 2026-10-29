export default function EmptyState({ title = "No data", subtitle = "Nothing to show." }) {
  return (
    <div className="border rounded bg-white p-4 text-center">
      <div className="fw-semibold">{title}</div>
      <div className="text-muted small">{subtitle}</div>
    </div>
  );
}