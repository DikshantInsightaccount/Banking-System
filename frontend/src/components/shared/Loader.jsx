export default function Loader({ label = "Loading..." }) {
  return (
    <div className="d-flex align-items-center gap-2 py-3">
      <div className="spinner-border spinner-border-sm" role="status" />
      <span className="text-muted">{label}</span>
    </div>
  );
}