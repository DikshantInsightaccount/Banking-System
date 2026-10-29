export default function KycStatusBadge({ value }) {
  const v = (value || "").toString().trim().toUpperCase();

  const cls =
    v === "VERIFIED"
      ? "badge text-bg-success"     // ✅ GREEN
      : v === "REJECTED"
      ? "badge text-bg-danger"      // 🔴 RED
      : v === "PENDING"
      ? "badge text-bg-warning"     // 🟡 YELLOW
      : "badge text-bg-secondary";  // ⚪ fallback

  return <span className={cls}>{v || "UNKNOWN"}</span>;
}
``