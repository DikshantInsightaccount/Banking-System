export default function TransactionStatusBadge({ value }) {
  const v = (value || "").toString().toUpperCase();
  const cls =
    v === "SUCCESS"
      ? "badge text-bg-success"
      : v === "FAILED"
      ? "badge text-bg-danger"
      : "badge text-bg-secondary";
  return <span className={cls}>{v || "UNKNOWN"}</span>;
}