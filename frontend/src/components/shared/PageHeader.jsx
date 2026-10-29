export default function PageHeader({ title, subtitle, right }) {
  return (
    <div className="d-flex align-items-start justify-content-between mb-3">
      <div>
        <h5 className="mb-1">{title}</h5>
        {subtitle ? <div className="text-muted small">{subtitle}</div> : null}
      </div>
      {right ? <div>{right}</div> : null}
    </div>
  );
}