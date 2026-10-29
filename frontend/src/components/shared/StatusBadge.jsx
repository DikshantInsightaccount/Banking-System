export default function StatusBadge({ value }) {
  const v = (value || "").toString().toUpperCase();
  const cls =
    v === "ACTIVE"
      ? "badge text-bg-success"
      : v === "INACTIVE"
      ? "badge text-bg-secondary"
      : "badge text-bg-dark";

  return <span className={cls}>{v || "UNKNOWN"}</span>;
}